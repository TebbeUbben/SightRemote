package sugar.free.sightremote.utils;

import android.content.Context;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import sugar.free.sightremote.database.BolusDelivered;
import sugar.free.sightremote.database.DatabaseHelper;
import sugar.free.sightremote.database.EndOfTBR;

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

            android.util.Log.d(TAG, "Resending TBR list " + records.size());
            for (EndOfTBR endOfTBR : records) {
                HistorySendIntent.sendEndOfTBR(context, endOfTBR, true);
            }
        } catch (SQLException e) {
            android.util.Log.e(TAG, "SQL ERROR: " + e);
        }

        try {
            final List<BolusDelivered> records = databaseHelper.getBolusDeliveredDao().queryBuilder()
                    .orderBy("dateTime", true)
                    .where().ge("dateTime", yesterday).query();

            android.util.Log.d("HistoryResync", "Resending Bolus list " + records.size());
            for (BolusDelivered bolusDelivered : records) {
                HistorySendIntent.sendBolusDelivered(context, bolusDelivered, true);
            }
        } catch (SQLException e) {
            android.util.Log.e(TAG, "SQL ERROR: " + e);
        }

    }
}
