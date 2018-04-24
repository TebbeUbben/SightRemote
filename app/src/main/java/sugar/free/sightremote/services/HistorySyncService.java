package sugar.free.sightremote.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.spongycastle.util.Pack;
import sugar.free.sightparser.applayer.descriptors.HistoryReadingDirection;
import sugar.free.sightparser.applayer.descriptors.HistoryType;
import sugar.free.sightparser.applayer.descriptors.history_frames.*;
import sugar.free.sightparser.applayer.messages.history.OpenHistoryReadingSessionMessage;
import sugar.free.sightparser.applayer.messages.status.ReadDateTimeMessage;
import sugar.free.sightparser.applayer.messages.status_param.ReadStatusParamBlockMessage;
import sugar.free.sightparser.applayer.descriptors.status_param_blocks.SystemIdentificationBlock;
import sugar.free.sightparser.handling.HistoryBroadcast;
import sugar.free.sightparser.handling.ServiceConnectionCallback;
import sugar.free.sightparser.handling.SightServiceConnector;
import sugar.free.sightparser.handling.SingleMessageTaskRunner;
import sugar.free.sightparser.handling.StatusCallback;
import sugar.free.sightparser.handling.TaskRunner;
import sugar.free.sightparser.handling.taskrunners.ReadHistoryTaskRunner;
import sugar.free.sightparser.pipeline.Status;
import sugar.free.sightremote.database.BatteryInserted;
import sugar.free.sightremote.database.BolusDelivered;
import sugar.free.sightremote.database.BolusProgrammed;
import sugar.free.sightremote.database.CannulaFilled;
import sugar.free.sightremote.database.CartridgeInserted;
import sugar.free.sightremote.database.DailyTotal;
import sugar.free.sightremote.database.DatabaseHelper;
import sugar.free.sightremote.database.EndOfTBR;
import sugar.free.sightremote.database.OccurenceOfAlert;
import sugar.free.sightremote.database.Offset;
import sugar.free.sightremote.database.PumpStatusChanged;
import sugar.free.sightremote.database.TimeChanged;
import sugar.free.sightremote.database.TubeFilled;
import sugar.free.sightremote.utils.CrashlyticsUtil;
import sugar.free.sightremote.utils.HistoryResync;
import sugar.free.sightremote.utils.HistorySendIntent;

import static sugar.free.sightremote.utils.Preferences.*;

public class HistorySyncService extends Service implements StatusCallback, TaskRunner.ResultCallback, ServiceConnectionCallback {

    private DatabaseHelper databaseHelper = null;
    private HistoryResync historyResync = null;
    private SightServiceConnector connector;
    private PowerManager powerManager;
    private AlarmManager alarmManager;
    private SharedPreferences activityPreferences;
    private PendingIntent pendingIntent;
    private long dateTimeOffset;
    private PowerManager.WakeLock wakeLock;
    private String pumpSerialNumber;
    private boolean syncing;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public DatabaseHelper getDatabaseHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return databaseHelper;
    }

    @Override
    public void onCreate() {
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        pendingIntent = PendingIntent.getService(this, 0, new Intent(this, HistorySyncService.class), 0);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "HistorySyncService");
        connector = new SightServiceConnector(this);
        connector.addStatusCallback(this);
        connector.setConnectionCallback(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getStringExtra("action");
        if (HistoryBroadcast.ACTION_START_SYNC.equals(action)) {
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent = null;
            }
            if (syncing) HistorySendIntent.sendStillSyncing(this, getAppsWithHistoryPermission());
            else startSync();
        } else if (HistoryBroadcast.ACTION_START_RESYNC.equals(action)) {
            if (historyResync == null)
                historyResync = new HistoryResync(getApplicationContext(), getDatabaseHelper());
            historyResync.doResync(getAppsWithHistoryPermission());
        }
        if (pendingIntent == null && getBooleanPref(PREF_BOOLEAN_BACKGROUND_SYNC_ENABLED)) {
            pendingIntent = PendingIntent.getService(this, 0, new Intent(this, HistorySyncService.class), 0);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, AlarmManager.INTERVAL_FIFTEEN_MINUTES, AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                    pendingIntent);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        pendingIntent.cancel();
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }

    private List<String> getAppsWithHistoryPermission() {
        List<String> packagesWithPermission = new ArrayList<>();

        PackageManager packageManager = getPackageManager();
        List<PackageInfo> packages = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS);

        for (PackageInfo packageInfo : packages) {
            if (packageInfo.requestedPermissions == null) continue;
            for (String permission : packageInfo.requestedPermissions) {
                if (permission.equals("sugar.free.sightremote.HISTORY_BROADCASTS")) {
                    packagesWithPermission.add(packageInfo.packageName);
                }
            }
        }

        return packagesWithPermission;
    }

    @Override
    public void onStatusChange(Status status, long statusTime, long waitTime) {
        if (status == Status.CONNECTED) {
            connector.connect();
            ReadStatusParamBlockMessage readMessage = new ReadStatusParamBlockMessage();
            readMessage.setStatusBlockId(SystemIdentificationBlock.ID);
            new SingleMessageTaskRunner(connector, readMessage).fetch(this);
        } else if (status == Status.DISCONNECTED) {
            connector.disconnect();
            connector.disconnectFromService();
            syncing = false;
            HistorySendIntent.sendSyncFinished(this, getAppsWithHistoryPermission());
            if (wakeLock.isHeld()) wakeLock.release();
        }
    }

    @Override
    public void onResult(Object result) {
        if (result instanceof ReadStatusParamBlockMessage) {
            pumpSerialNumber = ((SystemIdentificationBlock) ((ReadStatusParamBlockMessage) result).getStatusBlock()).getSerialNumber();
            new SingleMessageTaskRunner(connector, new ReadDateTimeMessage()).fetch(this);
        } else if (result instanceof ReadHistoryTaskRunner.HistoryResult) {
            ReadHistoryTaskRunner.HistoryResult historyResult = (ReadHistoryTaskRunner.HistoryResult) result;
            List<HistoryFrame> historyFrames = historyResult.getHistoryFrames();
            if (historyResult.getLatestEventNumber() > 0)
                Offset.setOffset(getDatabaseHelper(), pumpSerialNumber, HistoryType.ALL, historyResult.getLatestEventNumber());
            connector.disconnect();
            connector.disconnectFromService();
            processHistoryFrames(historyFrames);
            syncing = false;
            HistorySendIntent.sendSyncFinished(this, getAppsWithHistoryPermission());
            if (wakeLock.isHeld()) wakeLock.release();
        } else if (result instanceof ReadDateTimeMessage) {
            ReadDateTimeMessage dateTimeMessage = (ReadDateTimeMessage) result;
            Date pumpDate = parseDateTime(dateTimeMessage.getYear(), dateTimeMessage.getMonth(), dateTimeMessage.getDay(), dateTimeMessage.getHour(), dateTimeMessage.getMinute(), dateTimeMessage.getSecond());
            dateTimeOffset = new Date().getTime() - pumpDate.getTime();
            new ReadHistoryTaskRunner(connector, createOpenMessage(HistoryType.ALL),
                    Offset.getOffset(getDatabaseHelper(), pumpSerialNumber, HistoryType.ALL) == -1 ? 20 : Integer.MAX_VALUE).fetch(this);
        }
    }

    private void processHistoryFrames(List<HistoryFrame> historyFrames) {
        List<String> packages = getAppsWithHistoryPermission();

        List<BolusDelivered> bolusDeliveredEntries = new ArrayList<>();
        List<BolusProgrammed> bolusProgrammedEntries = new ArrayList<>();
        List<EndOfTBR> endOfTBREntries = new ArrayList<>();
        List<PumpStatusChanged> pumpStatusChangedEntries = new ArrayList<>();
        List<CannulaFilled> cannulaFilledEntries = new ArrayList<>();
        List<TimeChanged> timeChangedEntries = new ArrayList<>();
        List<DailyTotal> dailyTotalEntries = new ArrayList<>();
        List<TubeFilled> tubeFilledEntries = new ArrayList<>();
        List<CartridgeInserted> cartridgeInsertedEntries = new ArrayList<>();
        List<BatteryInserted> batteryInsertedEntries = new ArrayList<>();
        List<OccurenceOfAlert> occurenceOfAlertEntries = new ArrayList<>();
        for (HistoryFrame historyFrame : historyFrames) {
            Log.d("HistorySyncService", "Received " + historyFrame.getClass().getSimpleName());
            if (historyFrame instanceof BolusDeliveredFrame)
                bolusDeliveredEntries.add(processFrame((BolusDeliveredFrame) historyFrame));
            else if (historyFrame instanceof BolusProgrammedFrame)
                bolusProgrammedEntries.add(processFrame((BolusProgrammedFrame) historyFrame));
            else if (historyFrame instanceof EndOfTBRFrame)
                endOfTBREntries.add(processFrame((EndOfTBRFrame) historyFrame));
            else if (historyFrame instanceof PumpStatusChangedFrame)
                pumpStatusChangedEntries.add(processFrame((PumpStatusChangedFrame) historyFrame));
            else if (historyFrame instanceof TimeChangedFrame)
                timeChangedEntries.add(processFrame((TimeChangedFrame) historyFrame));
            else if (historyFrame instanceof CannulaFilledFrame)
                cannulaFilledEntries.add(processCannulaFilledFrame((CannulaFilledFrame) historyFrame));
            else if (historyFrame instanceof DailyTotalFrame)
                dailyTotalEntries.add(processFrame((DailyTotalFrame) historyFrame));
            else if (historyFrame instanceof TubeFilledFrame)
                tubeFilledEntries.add(processFrame((TubeFilledFrame) historyFrame));
            else if (historyFrame instanceof CartridgeInsertedFrame)
                cartridgeInsertedEntries.add(processFrame((CartridgeInsertedFrame) historyFrame));
            else if (historyFrame instanceof BatteryInsertedFrame)
                batteryInsertedEntries.add(processFrame((BatteryInsertedFrame) historyFrame));
            else if (historyFrame instanceof OccurenceOfErrorFrame)
                occurenceOfAlertEntries.add(processFrame((OccurenceOfErrorFrame) historyFrame));
            else if (historyFrame instanceof OccurenceOfMaintenanceFrame)
                occurenceOfAlertEntries.add(processFrame((OccurenceOfMaintenanceFrame) historyFrame));
            else if (historyFrame instanceof OccurenceOfWarningFrame)
                occurenceOfAlertEntries.add(processFrame((OccurenceOfWarningFrame) historyFrame));
        }
        try {
            for (BolusDelivered bolusDelivered : bolusDeliveredEntries) {
                if (getDatabaseHelper().getBolusDeliveredDao().queryBuilder().where()
                        .eq("eventNumber", bolusDelivered.getEventNumber()).and().eq("pump", pumpSerialNumber).countOf() > 0)
                    continue;
                getDatabaseHelper().getBolusDeliveredDao().create(bolusDelivered);
                HistorySendIntent.send(getApplicationContext(), bolusDelivered, false, packages);
            }
            for (BolusProgrammed bolusProgrammed : bolusProgrammedEntries) {
                if (getDatabaseHelper().getBolusProgrammedDao().queryBuilder().where()
                        .eq("eventNumber", bolusProgrammed.getEventNumber()).and().eq("pump", pumpSerialNumber).countOf() > 0)
                    continue;
                getDatabaseHelper().getBolusProgrammedDao().create(bolusProgrammed);
                HistorySendIntent.send(getApplicationContext(), bolusProgrammed, false, packages);
            }
            for (EndOfTBR endOfTBR : endOfTBREntries) {
                if (getDatabaseHelper().getEndOfTBRDao().queryBuilder().where()
                        .eq("eventNumber", endOfTBR.getEventNumber()).and().eq("pump", pumpSerialNumber).countOf() > 0)
                    continue;
                getDatabaseHelper().getEndOfTBRDao().create(endOfTBR);
                HistorySendIntent.send(getApplicationContext(), endOfTBR, false, packages);
            }
            for (PumpStatusChanged pumpStatusChanged : pumpStatusChangedEntries) {
                if (getDatabaseHelper().getPumpStatusChangedDao().queryBuilder().where()
                        .eq("eventNumber", pumpStatusChanged.getEventNumber()).and().eq("pump", pumpSerialNumber).countOf() > 0)
                    continue;
                getDatabaseHelper().getPumpStatusChangedDao().create(pumpStatusChanged);
                PumpStatusChanged oldStatus = getDatabaseHelper().getPumpStatusChangedDao().queryBuilder()
                        .orderBy("dateTime", false)
                        .where()
                        .lt("dateTime", pumpStatusChanged.getDateTime())
                        .queryForFirst();
                if (oldStatus != null)
                    HistorySendIntent.send(getApplicationContext(), pumpStatusChanged, oldStatus.getDateTime(), false, packages);
                else HistorySendIntent.send(getApplicationContext(), pumpStatusChanged, false, packages);
            }
            for (TimeChanged timeChanged : timeChangedEntries) {
                if (getDatabaseHelper().getTimeChangedDao().queryBuilder().where()
                        .eq("eventNumber", timeChanged.getEventNumber()).and().eq("pump", pumpSerialNumber).countOf() > 0)
                    continue;
                getDatabaseHelper().getTimeChangedDao().create(timeChanged);
                HistorySendIntent.send(getApplicationContext(), timeChanged, false, packages);
            }
            for (CannulaFilled cannulaFilled : cannulaFilledEntries) {
                if (getDatabaseHelper().getCannulaFilledDao().queryBuilder().where()
                        .eq("eventNumber", cannulaFilled.getEventNumber()).and().eq("pump", pumpSerialNumber).countOf() > 0)
                    continue;
                getDatabaseHelper().getCannulaFilledDao().create(cannulaFilled);
                HistorySendIntent.send(getApplicationContext(), cannulaFilled, false, packages);
            }
            for (DailyTotal dailyTotal : dailyTotalEntries) {
                if (getDatabaseHelper().getDailyTotalDao().queryBuilder().where()
                        .eq("eventNumber", dailyTotal.getEventNumber()).and().eq("pump", pumpSerialNumber).countOf() > 0)
                    continue;
                getDatabaseHelper().getDailyTotalDao().create(dailyTotal);
                HistorySendIntent.send(getApplicationContext(), dailyTotal, false, packages);
            }
            for (TubeFilled tubeFilled : tubeFilledEntries) {
                if (getDatabaseHelper().getTubeFilledDao().queryBuilder().where()
                        .eq("eventNumber", tubeFilled.getEventNumber()).and().eq("pump", pumpSerialNumber).countOf() > 0)
                    continue;
                getDatabaseHelper().getTubeFilledDao().create(tubeFilled);
                HistorySendIntent.send(getApplicationContext(), tubeFilled, false, packages);
            }
            for (CartridgeInserted cartridgeInserted : cartridgeInsertedEntries) {
                if (getDatabaseHelper().getCartridgeInsertedDao().queryBuilder().where()
                        .eq("eventNumber", cartridgeInserted.getEventNumber()).and().eq("pump", pumpSerialNumber).countOf() > 0)
                    continue;
                getDatabaseHelper().getCartridgeInsertedDao().create(cartridgeInserted);
                HistorySendIntent.send(getApplicationContext(), cartridgeInserted, false, packages);
            }
            for (BatteryInserted batteryInserted : batteryInsertedEntries) {
                if (getDatabaseHelper().getBatteryInsertedDao().queryBuilder().where()
                        .eq("eventNumber", batteryInserted.getEventNumber()).and().eq("pump", pumpSerialNumber).countOf() > 0)
                    continue;
                getDatabaseHelper().getBatteryInsertedDao().create(batteryInserted);
                HistorySendIntent.send(getApplicationContext(), batteryInserted, false, packages);
            }
            for (OccurenceOfAlert occurenceOfAlert : occurenceOfAlertEntries) {
                if (getDatabaseHelper().getOccurenceOfAlertDao().queryBuilder().where()
                        .eq("eventNumber", occurenceOfAlert.getEventNumber()).and().eq("pump", pumpSerialNumber).countOf() > 0)
                    continue;
                getDatabaseHelper().getOccurenceOfAlertDao().create(occurenceOfAlert);
                HistorySendIntent.send(getApplicationContext(), occurenceOfAlert, false, packages);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private EndOfTBR processFrame(EndOfTBRFrame frame) {
        EndOfTBR endOfTBR = new EndOfTBR();
        endOfTBR.setDuration(frame.getDuration());
        endOfTBR.setAmount(frame.getAmount());
        endOfTBR.setEventNumber(frame.getEventNumber());
        endOfTBR.setPump(pumpSerialNumber);

        int eventTimeSeconds = frame.getEventHour() * 60 * 60 + frame.getEventMinute() * 60 + frame.getEventSecond();
        int startTimeSeconds = frame.getStartHour() * 60 * 60 + frame.getStartMinute() * 60 + frame.getStartSecond();
        boolean startedOnDayBefore = startTimeSeconds >= eventTimeSeconds;

        Date eventTime = parseDateTimeAddOffset(frame.getEventYear(), frame.getEventMonth(), frame.getEventDay(),
                frame.getEventHour(), frame.getEventMinute(), frame.getEventSecond());
        Date startTime = parseDateTimeAddOffset(frame.getEventYear(), frame.getEventMonth(), frame.getEventDay() - (startedOnDayBefore ? 1 : 0),
                frame.getStartHour(), frame.getStartMinute(), frame.getStartSecond());
        endOfTBR.setDateTime(eventTime);
        endOfTBR.setStartTime(startTime);
        return endOfTBR;
    }

    private PumpStatusChanged processFrame(PumpStatusChangedFrame frame) {
        PumpStatusChanged pumpStatusChanged = new PumpStatusChanged();
        pumpStatusChanged.setOldValue(frame.getOldValue());
        pumpStatusChanged.setNewValue(frame.getNewValue());
        pumpStatusChanged.setEventNumber(frame.getEventNumber());
        pumpStatusChanged.setPump(pumpSerialNumber);

        Date eventTime = parseDateTimeAddOffset(frame.getEventYear(), frame.getEventMonth(), frame.getEventDay(),
                frame.getEventHour(), frame.getEventMinute(), frame.getEventSecond());
        pumpStatusChanged.setDateTime(eventTime);
        return pumpStatusChanged;
    }

    private BolusDelivered processFrame(BolusDeliveredFrame frame) {
        BolusDelivered bolusDelivered = new BolusDelivered();
        bolusDelivered.setBolusId(frame.getBolusId());
        bolusDelivered.setBolusType(frame.getBolusType());
        bolusDelivered.setDuration(frame.getDuration());
        bolusDelivered.setEventNumber(frame.getEventNumber());
        bolusDelivered.setExtendedAmount(frame.getExtendedAmount());
        bolusDelivered.setImmediateAmount(frame.getImmediateAmount());
        bolusDelivered.setPump(pumpSerialNumber);

        int eventTimeSeconds = frame.getEventHour() * 60 * 60 + frame.getEventMinute() * 60 + frame.getEventSecond();
        int startTimeSeconds = frame.getStartHour() * 60 * 60 + frame.getStartMinute() * 60 + frame.getStartSecond();
        boolean startedOnDayBefore = startTimeSeconds >= eventTimeSeconds;

        Date eventTime = parseDateTimeAddOffset(frame.getEventYear(), frame.getEventMonth(), frame.getEventDay(),
                frame.getEventHour(), frame.getEventMinute(), frame.getEventSecond());
        Date startTime = parseDateTimeAddOffset(frame.getEventYear(), frame.getEventMonth(), frame.getEventDay() - (startedOnDayBefore ? 1 : 0),
                frame.getStartHour(), frame.getStartMinute(), frame.getStartSecond());
        bolusDelivered.setDateTime(eventTime);
        bolusDelivered.setStartTime(startTime);
        return bolusDelivered;
    }

    private BolusProgrammed processFrame(BolusProgrammedFrame frame) {
        BolusProgrammed bolusProgrammed = new BolusProgrammed();
        bolusProgrammed.setBolusId(frame.getBolusId());
        bolusProgrammed.setBolusType(frame.getBolusType());
        bolusProgrammed.setDuration(frame.getDuration());
        bolusProgrammed.setEventNumber(frame.getEventNumber());
        bolusProgrammed.setExtendedAmount(frame.getExtendedAmount());
        bolusProgrammed.setImmediateAmount(frame.getImmediateAmount());
        bolusProgrammed.setPump(pumpSerialNumber);

        Date eventTime = parseDateTimeAddOffset(frame.getEventYear(), frame.getEventMonth(),
                frame.getEventDay(), frame.getEventHour(), frame.getEventMinute(), frame.getEventSecond());
        bolusProgrammed.setDateTime(eventTime);
        return bolusProgrammed;
    }

    private TimeChanged processFrame(TimeChangedFrame frame) {
        TimeChanged timeChanged = new TimeChanged();
        timeChanged.setEventNumber(frame.getEventNumber());
        timeChanged.setPump(pumpSerialNumber);

        Date eventTime = parseDateTimeAddOffset(frame.getEventYear(), frame.getEventMonth(),
                frame.getEventDay(), frame.getEventHour(), frame.getEventMinute(), frame.getEventSecond());
        timeChanged.setDateTime(eventTime);

        Date beforeTime = parseDateTimeAddOffset(frame.getBeforeYear(), frame.getBeforeMonth(),
                frame.getBeforeDay(), frame.getBeforeHour(), frame.getBeforeMinute(), frame.getBeforeSecond());
        timeChanged.setTimeBefore(beforeTime);

        return timeChanged;
    }

    private CannulaFilled processCannulaFilledFrame(CannulaFilledFrame frame) {
        CannulaFilled cannulaFilled = new CannulaFilled();
        cannulaFilled.setEventNumber(frame.getEventNumber());
        cannulaFilled.setPump(pumpSerialNumber);
        cannulaFilled.setAmount(frame.getAmount());

        Date eventTime = parseDateTimeAddOffset(frame.getEventYear(), frame.getEventMonth(),
                frame.getEventDay(), frame.getEventHour(), frame.getEventMinute(), frame.getEventSecond());
        cannulaFilled.setDateTime(eventTime);

        return cannulaFilled;
    }

    private TubeFilled processFrame(TubeFilledFrame frame) {
        TubeFilled tubeFilled = new TubeFilled();
        tubeFilled.setEventNumber(frame.getEventNumber());
        tubeFilled.setPump(pumpSerialNumber);
        tubeFilled.setAmount(frame.getAmount());

        Date eventTime = parseDateTimeAddOffset(frame.getEventYear(), frame.getEventMonth(),
                frame.getEventDay(), frame.getEventHour(), frame.getEventMinute(), frame.getEventSecond());
        tubeFilled.setDateTime(eventTime);

        return tubeFilled;
    }

    private CartridgeInserted processFrame(CartridgeInsertedFrame frame) {
        CartridgeInserted cartridgeInserted = new CartridgeInserted();
        cartridgeInserted.setEventNumber(frame.getEventNumber());
        cartridgeInserted.setPump(pumpSerialNumber);
        cartridgeInserted.setAmount(frame.getAmount());

        Date eventTime = parseDateTimeAddOffset(frame.getEventYear(), frame.getEventMonth(),
                frame.getEventDay(), frame.getEventHour(), frame.getEventMinute(), frame.getEventSecond());
        cartridgeInserted.setDateTime(eventTime);

        return cartridgeInserted;
    }

    private BatteryInserted processFrame(BatteryInsertedFrame frame) {
        BatteryInserted batteryInserted = new BatteryInserted();
        batteryInserted.setEventNumber(frame.getEventNumber());
        batteryInserted.setPump(pumpSerialNumber);

        Date eventTime = parseDateTimeAddOffset(frame.getEventYear(), frame.getEventMonth(),
                frame.getEventDay(), frame.getEventHour(), frame.getEventMinute(), frame.getEventSecond());
        batteryInserted.setDateTime(eventTime);

        return batteryInserted;
    }

    private DailyTotal processFrame(DailyTotalFrame frame) {
        DailyTotal dailyTotal = new DailyTotal();
        dailyTotal.setEventNumber(frame.getEventNumber());
        dailyTotal.setBasalTotal(frame.getBasalTotal());
        dailyTotal.setBolusTotal(frame.getBolusTotal());

        Date eventTime = parseDateTimeAddOffset(frame.getEventYear(), frame.getEventMonth(),
                frame.getEventDay(), frame.getEventHour(), frame.getEventMinute(), frame.getEventSecond());
        dailyTotal.setDateTime(eventTime);

        Date totalDate = parseDateTime(frame.getTotalYear(), frame.getTotalMonth(),
                frame.getTotalDay(), 0, 0, 0);
        dailyTotal.setTotalDate(totalDate);

        return dailyTotal;
    }

    private OccurenceOfAlert processFrame(OccurenceOfAlertFrame frame) {
        OccurenceOfAlert occurenceOfAlert = new OccurenceOfAlert();
        occurenceOfAlert.setEventNumber(frame.getEventNumber());
        occurenceOfAlert.setAlertType(frame.getAlertType().getSimpleName());
        occurenceOfAlert.setAlertId(frame.getAlertId());

        Date eventTime = parseDateTimeAddOffset(frame.getEventYear(), frame.getEventMonth(),
                frame.getEventDay(), frame.getEventHour(), frame.getEventMinute(), frame.getEventSecond());
        occurenceOfAlert.setDateTime(eventTime);

        return occurenceOfAlert;
    }

    private OpenHistoryReadingSessionMessage createOpenMessage(HistoryType historyType) {
        OpenHistoryReadingSessionMessage openMessage = new OpenHistoryReadingSessionMessage();
        openMessage.setHistoryType(historyType);
        long offset = Offset.getOffset(getDatabaseHelper(), pumpSerialNumber, historyType);
        if (offset != -1) {
            openMessage.setOffset(offset + 1);
            openMessage.setReadingDirection(HistoryReadingDirection.FORWARD);
        } else {
            openMessage.setOffset(0xFFFFFFFF);
            openMessage.setReadingDirection(HistoryReadingDirection.BACKWARD);
        }
        return openMessage;
    }

    private Date parseDateTime(int year, int month, int day, int hour, int minute, int second) {
        Calendar calendar = new GregorianCalendar(TimeZone.getDefault(), Locale.getDefault());
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Date parseDateTimeAddOffset(int year, int month, int day, int hour, int minute, int second) {
        Date date = parseDateTime(year, month, day, hour, minute, second);
        date = new Date(date.getTime() + dateTimeOffset);
        return date;
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
        connector.disconnect();
        connector.disconnectFromService();
        syncing = false;
        HistorySendIntent.sendSyncFinished(this, getAppsWithHistoryPermission());
        if (wakeLock.isHeld()) wakeLock.release();
    }

    private void startSync() {
        if (!wakeLock.isHeld()) wakeLock.acquire(60000);
        syncing = true;
        connector.connectToService();
        HistorySendIntent.sendSyncStarted(this, getAppsWithHistoryPermission());
    }

    @Override
    public void onServiceConnected() {
        if (!connector.isUseable()) connector.disconnectFromService();
        else {
            connector.connect();
            if (connector.getStatus() == Status.CONNECTED) {
                onStatusChange(Status.CONNECTED, 0, 0);
            }
        }
    }

    @Override
    public void onServiceDisconnected() {
        syncing = false;
        if (wakeLock.isHeld()) wakeLock.release();
    }
}
