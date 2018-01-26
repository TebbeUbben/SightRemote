package sugar.free.sightremote.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import sugar.free.sightparser.applayer.descriptors.HistoryReadingDirection;
import sugar.free.sightparser.applayer.descriptors.HistoryType;
import sugar.free.sightparser.applayer.history.HistoryFrame;
import sugar.free.sightparser.applayer.history.OpenHistoryReadingSessionMessage;
import sugar.free.sightparser.applayer.history.history_frames.BolusDeliveredFrame;
import sugar.free.sightparser.applayer.history.history_frames.BolusProgrammedFrame;
import sugar.free.sightparser.applayer.history.history_frames.EndOfTBRFrame;
import sugar.free.sightparser.applayer.history.history_frames.PumpStatusChangedFrame;
import sugar.free.sightparser.applayer.status_param.ReadStatusParamBlockMessage;
import sugar.free.sightparser.applayer.status_param.blocks.SystemIdentificationBlock;
import sugar.free.sightparser.handling.HistoryBroadcast;
import sugar.free.sightparser.handling.ServiceConnectionCallback;
import sugar.free.sightparser.handling.SightServiceConnector;
import sugar.free.sightparser.handling.SingleMessageTaskRunner;
import sugar.free.sightparser.handling.StatusCallback;
import sugar.free.sightparser.handling.TaskRunner;
import sugar.free.sightparser.handling.taskrunners.ReadHistoryTaskRunner;
import sugar.free.sightparser.pipeline.Status;
import sugar.free.sightremote.database.BolusDelivered;
import sugar.free.sightremote.database.BolusProgrammed;
import sugar.free.sightremote.database.DatabaseHelper;
import sugar.free.sightremote.database.EndOfTBR;
import sugar.free.sightremote.database.Offset;
import sugar.free.sightremote.database.PumpStatusChanged;

public class HistorySyncService extends Service implements StatusCallback, TaskRunner.ResultCallback, ServiceConnectionCallback {

    private DatabaseHelper databaseHelper = null;
    private SightServiceConnector connector;
    private PowerManager powerManager;
    private AlarmManager alarmManager;
    private PowerManager.WakeLock wakeLock;
    private String pumpSerialNumber;
    private int status;
    private List<HistoryFrame> historyFrames = new ArrayList<>();
    private boolean syncing;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        if (databaseHelper == null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }

    public DatabaseHelper getDatabaseHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return databaseHelper;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "HistorySyncService");
        connector = new SightServiceConnector(this);
        connector.addStatusCallback(this);
        connector.setConnectionCallback(this);
        getApplicationContext().registerReceiver(broadcastReceiver, new IntentFilter(HistoryBroadcast.ACTION_START_SYNC));
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, AlarmManager.INTERVAL_FIFTEEN_MINUTES, AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                PendingIntent.getBroadcast(this, 0, new Intent(HistoryBroadcast.ACTION_START_SYNC), 0));
        return START_STICKY;
    }

    @Override
    public void onStatusChange(Status status) {
        if (status == Status.CONNECTED) {
            connector.connect();
            ReadStatusParamBlockMessage readMessage = new ReadStatusParamBlockMessage();
            readMessage.setStatusBlockId(SystemIdentificationBlock.ID);
            new SingleMessageTaskRunner(connector, readMessage).fetch(this);
        } else if (status == Status.DISCONNECTED) {
            connector.disconnect();
            connector.disconnectFromService();
        }
    }

    @Override
    public void onResult(Object result) {
        if (result instanceof ReadStatusParamBlockMessage) {
            pumpSerialNumber = ((SystemIdentificationBlock) ((ReadStatusParamBlockMessage) result).getStatusBlock()).getSerialNumber();
            status = 1;
            new ReadHistoryTaskRunner(connector, createOpenMessage(HistoryType.TBR)).fetch(this);
        } else if (result instanceof List && status == 1) {
            List<HistoryFrame> entries = (List<HistoryFrame>) result;
            Offset.setOffset(getDatabaseHelper(), pumpSerialNumber, HistoryType.TBR, entries.get(entries.size() - 1).getEventNumber());
            historyFrames.addAll(entries);
            status = 2;
            new ReadHistoryTaskRunner(connector, createOpenMessage(HistoryType.THERAPY)).fetch(this);
        } else if (result instanceof List && status == 2) {
            List<HistoryFrame> entries = (List<HistoryFrame>) result;
            Offset.setOffset(getDatabaseHelper(), pumpSerialNumber, HistoryType.THERAPY, entries.get(entries.size() - 1).getEventNumber());
            historyFrames.addAll(entries);
            status = 0;
            connector.disconnectFromService();
            processHistoryFrames();
            historyFrames = new ArrayList<>();
        }
    }

    private void processHistoryFrames() {
        List<BolusDelivered> bolusDeliveredEntries = new ArrayList<>();
        List<BolusProgrammed> bolusProgrammedEntries = new ArrayList<>();
        List<EndOfTBR> endOfTBREntries = new ArrayList<>();
        List<PumpStatusChanged> pumpStatusChangedEntries = new ArrayList<>();
        for (HistoryFrame historyFrame : historyFrames) {
            if (historyFrame instanceof BolusDeliveredFrame)
                bolusDeliveredEntries.add(processBolusDeliveredFrame((BolusDeliveredFrame) historyFrame));
            else if (historyFrame instanceof BolusProgrammedFrame)
                bolusProgrammedEntries.add(processBolusProgrammedFrame((BolusProgrammedFrame) historyFrame));
            else if (historyFrame instanceof EndOfTBRFrame)
                endOfTBREntries.add(processEndOfTBRFrame((EndOfTBRFrame) historyFrame));
            else if (historyFrame instanceof PumpStatusChangedFrame)
                pumpStatusChangedEntries.add(processPumpStatusChangedFrame((PumpStatusChangedFrame) historyFrame));
        }
        try {
            for (BolusProgrammed bolusProgrammed : bolusProgrammedEntries) {
                getDatabaseHelper().getBolusProgrammedDao().create(bolusProgrammed);
                Intent intent = new Intent();
                intent.setAction(HistoryBroadcast.ACTION_BOLUS_PROGRAMMED);
                intent.putExtra(HistoryBroadcast.EXTRA_BOLUS_ID, bolusProgrammed.getBolusId());
                intent.putExtra(HistoryBroadcast.EXTRA_BOLUS_TYPE, bolusProgrammed.getBolusType().toString());
                intent.putExtra(HistoryBroadcast.EXTRA_DURATION, bolusProgrammed.getDuration());
                intent.putExtra(HistoryBroadcast.EXTRA_EVENT_NUMBER, bolusProgrammed.getEventNumber());
                intent.putExtra(HistoryBroadcast.EXTRA_EXTENDED_AMOUNT, bolusProgrammed.getExtendedAmount());
                intent.putExtra(HistoryBroadcast.EXTRA_IMMEDIATE_AMOUNT, bolusProgrammed.getImmediateAmount());
                intent.putExtra(HistoryBroadcast.EXTRA_PUMP_SERIAL_NUMBER, bolusProgrammed.getPump());
                intent.putExtra(HistoryBroadcast.EXTRA_EVENT_TIME, bolusProgrammed.getDateTime());
                sendBroadcast(intent);
            }
            for (BolusDelivered bolusDelivered : bolusDeliveredEntries) {
                getDatabaseHelper().getBolusDeliveredDao().create(bolusDelivered);
                Intent intent = new Intent();
                intent.setAction(HistoryBroadcast.ACTION_BOLUS_DELIVERED);
                intent.putExtra(HistoryBroadcast.EXTRA_BOLUS_ID, bolusDelivered.getBolusId());
                intent.putExtra(HistoryBroadcast.EXTRA_BOLUS_TYPE, bolusDelivered.getBolusType().toString());
                intent.putExtra(HistoryBroadcast.EXTRA_DURATION, bolusDelivered.getDuration());
                intent.putExtra(HistoryBroadcast.EXTRA_EVENT_NUMBER, bolusDelivered.getEventNumber());
                intent.putExtra(HistoryBroadcast.EXTRA_EXTENDED_AMOUNT, bolusDelivered.getExtendedAmount());
                intent.putExtra(HistoryBroadcast.EXTRA_IMMEDIATE_AMOUNT, bolusDelivered.getImmediateAmount());
                intent.putExtra(HistoryBroadcast.EXTRA_PUMP_SERIAL_NUMBER, bolusDelivered.getPump());
                intent.putExtra(HistoryBroadcast.EXTRA_EVENT_TIME, bolusDelivered.getDateTime());
                intent.putExtra(HistoryBroadcast.EXTRA_START_TIME, bolusDelivered.getStartTime());
                sendBroadcast(intent);
            }
            for (EndOfTBR endOfTBR : endOfTBREntries) {
                getDatabaseHelper().getEndOfTBRDao().create(endOfTBR);
                Intent intent = new Intent();
                intent.setAction(HistoryBroadcast.ACTION_END_OF_TBR);
                intent.putExtra(HistoryBroadcast.EXTRA_DURATION, endOfTBR.getDuration());
                intent.putExtra(HistoryBroadcast.EXTRA_TBR_AMOUNT, endOfTBR.getAmount());
                intent.putExtra(HistoryBroadcast.EXTRA_EVENT_NUMBER, endOfTBR.getEventNumber());
                intent.putExtra(HistoryBroadcast.EXTRA_PUMP_SERIAL_NUMBER, endOfTBR.getPump());
                intent.putExtra(HistoryBroadcast.EXTRA_EVENT_TIME, endOfTBR.getDateTime());
                intent.putExtra(HistoryBroadcast.EXTRA_START_TIME, endOfTBR.getStartTime());
                sendBroadcast(intent);
            }
            for (PumpStatusChanged pumpStatusChanged : pumpStatusChangedEntries) {
                getDatabaseHelper().getPumpStatusChangedDao().create(pumpStatusChanged);
                Intent intent = new Intent();
                intent.setAction(HistoryBroadcast.ACTION_END_OF_TBR);
                intent.putExtra(HistoryBroadcast.EXTRA_OLD_STATUS, pumpStatusChanged.getOldValue().toString());
                intent.putExtra(HistoryBroadcast.EXTRA_NEW_STATUS, pumpStatusChanged.getNewValue().toString());
                intent.putExtra(HistoryBroadcast.EXTRA_EVENT_NUMBER, pumpStatusChanged.getEventNumber());
                intent.putExtra(HistoryBroadcast.EXTRA_PUMP_SERIAL_NUMBER, pumpStatusChanged.getPump());
                intent.putExtra(HistoryBroadcast.EXTRA_EVENT_TIME, pumpStatusChanged.getDateTime());
                sendBroadcast(intent);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private EndOfTBR processEndOfTBRFrame(EndOfTBRFrame frame) {
        EndOfTBR endOfTBR = new EndOfTBR();
        endOfTBR.setDuration(frame.getDuration());
        endOfTBR.setAmount(frame.getAmount());
        endOfTBR.setEventNumber(frame.getEventNumber());
        endOfTBR.setPump(pumpSerialNumber);

        int eventTimeSeconds = frame.getEventHour() * 60 * 60 + frame.getEventMinute()  * 60 + frame.getEventSecond();
        int startTimeSeconds = frame.getStartHour() * 60 * 60 + frame.getStartMinute()  * 60 + frame.getStartSecond();
        boolean startedOnDayBefore = startTimeSeconds >= eventTimeSeconds;

        Date eventTime = parseDateTime(frame.getEventYear(), frame.getEventMonth(), frame.getEventDay(),
                frame.getEventHour(), frame.getEventMinute(), frame.getEventSecond());
        Date startTime = parseDateTime(frame.getEventYear(), frame.getEventMonth(), frame.getEventDay() - (startedOnDayBefore ? 1 : 0),
                frame.getStartHour(), frame.getStartMinute(), frame.getStartSecond());
        endOfTBR.setDateTime(eventTime);
        endOfTBR.setStartTime(startTime);
        return endOfTBR;
    }

    private PumpStatusChanged processPumpStatusChangedFrame(PumpStatusChangedFrame frame) {
        PumpStatusChanged pumpStatusChanged = new PumpStatusChanged();
        pumpStatusChanged.setOldValue(frame.getOldValue());
        pumpStatusChanged.setNewValue(frame.getNewValue());
        pumpStatusChanged.setEventNumber(frame.getEventNumber());
        pumpStatusChanged.setPump(pumpSerialNumber);

        Date eventTime = parseDateTime(frame.getEventYear(), frame.getEventMonth(), frame.getEventDay(),
                frame.getEventHour(), frame.getEventMinute(), frame.getEventSecond());
        pumpStatusChanged.setDateTime(eventTime);
        return pumpStatusChanged;
    }

    private BolusDelivered processBolusDeliveredFrame(BolusDeliveredFrame frame) {
        BolusDelivered bolusDelivered = new BolusDelivered();
        bolusDelivered.setBolusId(frame.getBolusId());
        bolusDelivered.setBolusType(frame.getBolusType());
        bolusDelivered.setDuration(frame.getDuration());
        bolusDelivered.setEventNumber(frame.getEventNumber());
        bolusDelivered.setExtendedAmount(frame.getExtendedAmount());
        bolusDelivered.setImmediateAmount(frame.getImmediateAmount());
        bolusDelivered.setPump(pumpSerialNumber);

        int eventTimeSeconds = frame.getEventHour() * 60 * 60 + frame.getEventMinute()  * 60 + frame.getEventSecond();
        int startTimeSeconds = frame.getStartHour() * 60 * 60 + frame.getStartMinute()  * 60 + frame.getStartSecond();
        boolean startedOnDayBefore = startTimeSeconds >= eventTimeSeconds;

        Date eventTime = parseDateTime(frame.getEventYear(), frame.getEventMonth(), frame.getEventDay(),
                frame.getEventHour(), frame.getEventMinute(), frame.getEventSecond());
        Date startTime = parseDateTime(frame.getEventYear(), frame.getEventMonth(), frame.getEventDay() - (startedOnDayBefore ? 1 : 0),
                frame.getStartHour(), frame.getStartMinute(), frame.getStartSecond());
        bolusDelivered.setDateTime(eventTime);
        bolusDelivered.setStartTime(startTime);
        return bolusDelivered;
    }

    private BolusProgrammed processBolusProgrammedFrame(BolusProgrammedFrame frame) {
        BolusProgrammed bolusProgrammed = new BolusProgrammed();
        bolusProgrammed.setBolusId(frame.getBolusId());
        bolusProgrammed.setBolusType(frame.getBolusType());
        bolusProgrammed.setDuration(frame.getDuration());
        bolusProgrammed.setEventNumber(frame.getEventNumber());
        bolusProgrammed.setExtendedAmount(frame.getExtendedAmount());
        bolusProgrammed.setImmediateAmount(frame.getImmediateAmount());
        bolusProgrammed.setPump(pumpSerialNumber);

        Date eventTime = parseDateTime(frame.getEventYear(), frame.getEventMonth(), frame.getEventDay(), frame.getEventHour(), frame.getEventMinute(), frame.getEventSecond());
        bolusProgrammed.setDateTime(eventTime);
        return bolusProgrammed;
    }

    private OpenHistoryReadingSessionMessage createOpenMessage(HistoryType historyType) {
        OpenHistoryReadingSessionMessage openMessage = new OpenHistoryReadingSessionMessage();
        openMessage.setHistoryType(historyType);
        openMessage.setOffset(Offset.getOffset(getDatabaseHelper(), pumpSerialNumber, historyType) + 1);
        openMessage.setReadingDirection(HistoryReadingDirection.FORWARD);
        return openMessage;
    }

    private Date parseDateTime(int year, int month, int day, int hour, int minute, int second) {
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        return calendar.getTime();
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
        connector.disconnect();
        connector.disconnectFromService();
    }

    private void startSync() {
        wakeLock.acquire();
        syncing = true;
        connector.connectToService();
        if (syncing) sendBroadcast(new Intent(HistoryBroadcast.ACTION_SYNC_STARTED));
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(HistoryBroadcast.ACTION_START_SYNC)) {
                if (syncing) sendBroadcast(new Intent(HistoryBroadcast.ACTION_STILL_SYNCING));
                else startSync();
            }
        }
    };

    @Override
    public void onServiceConnected() {
        connector.connect();
        if (connector.getStatus() == Status.CONNECTED) {
            onStatusChange(Status.CONNECTED);
        }
    }

    @Override
    public void onServiceDisconnected() {
        syncing = false;
        sendBroadcast(new Intent(HistoryBroadcast.ACTION_SYNC_FINISHED));
        wakeLock.release();
    }
}
