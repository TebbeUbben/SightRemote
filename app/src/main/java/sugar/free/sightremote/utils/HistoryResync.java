package sugar.free.sightremote.utils;

import android.content.Context;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import sugar.free.sightremote.database.BatteryInserted;
import sugar.free.sightremote.database.BolusDelivered;
import sugar.free.sightremote.database.BolusProgrammed;
import sugar.free.sightremote.database.CannulaFilled;
import sugar.free.sightremote.database.CartridgeInserted;
import sugar.free.sightremote.database.DailyTotal;
import sugar.free.sightremote.database.DatabaseHelper;
import sugar.free.sightremote.database.EndOfTBR;
import sugar.free.sightremote.database.OccurenceOfAlert;
import sugar.free.sightremote.database.PumpStatusChanged;
import sugar.free.sightremote.database.TimeChanged;
import sugar.free.sightremote.database.TubeFilled;

/**
 * Created by jamorham on 28/01/2018.
 *
 * Send the last 24 hours records from our database store
 */

public class HistoryResync {

    private static final String TAG = "HistoryResync";
    private DatabaseHelper databaseHelper;
    private Context context;

    public HistoryResync(Context context, DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
        this.context = context;
    }

    public void doResync() {
        resyncFromDatabase();
    }

    private void resyncFromDatabase() {

        final Date yesterday = new Date(System.currentTimeMillis() - 86400000);

        try {
            final List<EndOfTBR> records = databaseHelper.getEndOfTBRDao().queryBuilder()
                    .orderBy("dateTime", true)
                    .where().ge("dateTime", yesterday).query();

            android.util.Log.d(TAG, "Resending EndOfTBR list " + records.size());
            for (EndOfTBR endOfTBR : records) {
                HistorySendIntent.send(context, endOfTBR, true);
            }
        } catch (SQLException e) {
            android.util.Log.e(TAG, "SQL ERROR: " + e);
        }

        try {
            final List<BolusDelivered> records = databaseHelper.getBolusDeliveredDao().queryBuilder()
                    .orderBy("dateTime", true)
                    .where().ge("dateTime", yesterday).query();

            android.util.Log.d("HistoryResync", "Resending BolusDelivered list " + records.size());
            for (BolusDelivered bolusDelivered : records) {
                HistorySendIntent.send(context, bolusDelivered, true);
            }
        } catch (SQLException e) {
            android.util.Log.e(TAG, "SQL ERROR: " + e);
        }

        try {
            final List<BolusProgrammed> records = databaseHelper.getBolusProgrammedDao().queryBuilder()
                    .orderBy("dateTime", true)
                    .where().ge("dateTime", yesterday).query();

            android.util.Log.d("HistoryResync", "Resending BolusProgrammed list " + records.size());
            for (BolusProgrammed bolusProgrammed : records) {
                HistorySendIntent.send(context, bolusProgrammed, true);
            }
        } catch (SQLException e) {
            android.util.Log.e(TAG, "SQL ERROR: " + e);
        }

        try {
            final List<PumpStatusChanged> records = databaseHelper.getPumpStatusChangedDao().queryBuilder()
                    .orderBy("dateTime", true)
                    .where().ge("dateTime", yesterday).query();

            android.util.Log.d("HistoryResync", "Resending PumpStatusChanged list " + records.size());
            for (PumpStatusChanged pumpStatusChanged : records) {
                PumpStatusChanged oldStatus = databaseHelper.getPumpStatusChangedDao().queryBuilder()
                        .orderBy("dateTime", false)
                        .where()
                        .lt("dateTime", pumpStatusChanged.getDateTime())
                        .queryForFirst();
                if (oldStatus != null)
                    HistorySendIntent.send(context, pumpStatusChanged, oldStatus.getDateTime(), true);
                else HistorySendIntent.send(context, pumpStatusChanged, true);
            }
        } catch (SQLException e) {
            android.util.Log.e(TAG, "SQL ERROR: " + e);
        }

        try {
            final List<TimeChanged> records = databaseHelper.getTimeChangedDao().queryBuilder()
                    .orderBy("dateTime", true)
                    .where().ge("dateTime", yesterday).query();

            android.util.Log.d("HistoryResync", "Resending TimeChanged list " + records.size());
            for (TimeChanged timeChanged : records) {
                HistorySendIntent.send(context, timeChanged, true);
            }
        } catch (SQLException e) {
            android.util.Log.e(TAG, "SQL ERROR: " + e);
        }

        try {
            final List<CannulaFilled> records = databaseHelper.getCannulaFilledDao().queryBuilder()
                    .orderBy("dateTime", true)
                    .where().ge("dateTime", yesterday).query();

            android.util.Log.d("HistoryResync", "Resending CannulaFilled list " + records.size());
            for (CannulaFilled cannulaFilled : records) {
                HistorySendIntent.send(context, cannulaFilled, true);
            }
        } catch (SQLException e) {
            android.util.Log.e(TAG, "SQL ERROR: " + e);
        }

        try {
            final List<DailyTotal> records = databaseHelper.getDailyTotalDao().queryBuilder()
                    .orderBy("dateTime", true)
                    .where().ge("dateTime", yesterday).query();

            android.util.Log.d("HistoryResync", "Resending DailyTotal list " + records.size());
            for (DailyTotal dailyTotal : records) {
                HistorySendIntent.send(context, dailyTotal, true);
            }
        } catch (SQLException e) {
            android.util.Log.e(TAG, "SQL ERROR: " + e);
        }

        try {
            final List<TubeFilled> records = databaseHelper.getTubeFilledDao().queryBuilder()
                    .orderBy("dateTime", true)
                    .where().ge("dateTime", yesterday).query();

            android.util.Log.d("HistoryResync", "Resending TubeFilled list " + records.size());
            for (TubeFilled tubeFilled : records) {
                HistorySendIntent.send(context, tubeFilled, true);
            }
        } catch (SQLException e) {
            android.util.Log.e(TAG, "SQL ERROR: " + e);
        }

        try {
            final List<CartridgeInserted> records = databaseHelper.getCartridgeInsertedDao().queryBuilder()
                    .orderBy("dateTime", true)
                    .where().ge("dateTime", yesterday).query();

            android.util.Log.d("HistoryResync", "Resending CartridgeInserted list " + records.size());
            for (CartridgeInserted cartridgeInserted : records) {
                HistorySendIntent.send(context, cartridgeInserted, true);
            }
        } catch (SQLException e) {
            android.util.Log.e(TAG, "SQL ERROR: " + e);
        }

        try {
            final List<BatteryInserted> records = databaseHelper.getBatteryInsertedDao().queryBuilder()
                    .orderBy("dateTime", true)
                    .where().ge("dateTime", yesterday).query();

            android.util.Log.d("HistoryResync", "Resending BatteryInserted list " + records.size());
            for (BatteryInserted batteryInserted : records) {
                HistorySendIntent.send(context, batteryInserted, true);
            }
        } catch (SQLException e) {
            android.util.Log.e(TAG, "SQL ERROR: " + e);
        }

        try {
            final List<OccurenceOfAlert> records = databaseHelper.getOccurenceOfAlertDao().queryBuilder()
                    .orderBy("dateTime", true)
                    .where().ge("dateTime", yesterday).query();

            android.util.Log.d("HistoryResync", "Resending OccurenceOfAlert list " + records.size());
            for (OccurenceOfAlert occurenceOfAlert : records) {
                HistorySendIntent.send(context, occurenceOfAlert, true);
            }
        } catch (SQLException e) {
            android.util.Log.e(TAG, "SQL ERROR: " + e);
        }
    }
}
