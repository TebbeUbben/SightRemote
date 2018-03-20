package sugar.free.sightremote.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "sightremote.db";
    private static final int DATABASE_VERSION = 5;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private Dao<BolusDelivered, Integer> bolusDeliveredDao;
    private Dao<BolusProgrammed, Integer> bolusProgrammedDao;
    private Dao<EndOfTBR, Integer> endOfTBRDao;
    private Dao<Offset, Integer> offsetDao;
    private Dao<PumpStatusChanged, Integer> pumpStatusChangedDao;
    private Dao<TimeChanged, Integer> timeChangedDao;
    private Dao<CannulaFilled, Integer> cannulaFilledDao;
    private Dao<DailyTotal, Integer> dailyTotalDao;
    private Dao<BatteryInserted, Integer> batteryInsertedDao;
    private Dao<CartridgeInserted, Integer> cartridgeInsertedDao;
    private Dao<TubeFilled, Integer> tubeFilledDao;
    private Dao<OccurenceOfAlert, Integer> occurenceOfAlertDao;

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTableIfNotExists(connectionSource, BolusDelivered.class);
            TableUtils.createTableIfNotExists(connectionSource, BolusProgrammed.class);
            TableUtils.createTableIfNotExists(connectionSource, EndOfTBR.class);
            TableUtils.createTableIfNotExists(connectionSource, Offset.class);
            TableUtils.createTableIfNotExists(connectionSource, PumpStatusChanged.class);
            TableUtils.createTableIfNotExists(connectionSource, TimeChanged.class);
            TableUtils.createTableIfNotExists(connectionSource, CannulaFilled.class);
            TableUtils.createTableIfNotExists(connectionSource, DailyTotal.class);
            TableUtils.createTableIfNotExists(connectionSource, CartridgeInserted.class);
            TableUtils.createTableIfNotExists(connectionSource, BatteryInserted.class);
            TableUtils.createTableIfNotExists(connectionSource, TubeFilled.class);
            TableUtils.createTableIfNotExists(connectionSource, OccurenceOfAlert.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try {
                TableUtils.dropTable(connectionSource, BolusDelivered.class, false);
                TableUtils.dropTable(connectionSource, BolusProgrammed.class, false);
                TableUtils.dropTable(connectionSource, CannulaFilled.class, false);
                TableUtils.createTable(connectionSource, BolusDelivered.class);
                TableUtils.createTable(connectionSource, BolusProgrammed.class);
                TableUtils.createTable(connectionSource, CannulaFilled.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 3) {
            try {
                TableUtils.createTableIfNotExists(connectionSource, DailyTotal.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 4) {
            try {
                TableUtils.createTableIfNotExists(connectionSource, CartridgeInserted.class);
                TableUtils.createTableIfNotExists(connectionSource, BatteryInserted.class);
                TableUtils.createTableIfNotExists(connectionSource, TubeFilled.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (oldVersion < 5) {
            try {
                TableUtils.createTableIfNotExists(connectionSource, OccurenceOfAlert.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Dao<BolusDelivered, Integer> getBolusDeliveredDao() {
        try {
            if (bolusDeliveredDao == null) bolusDeliveredDao = getDao(BolusDelivered.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bolusDeliveredDao;
    }

    public Dao<BolusProgrammed, Integer> getBolusProgrammedDao() {
        try {
            if (bolusProgrammedDao == null) bolusProgrammedDao = getDao(BolusProgrammed.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bolusProgrammedDao;
    }

    public Dao<EndOfTBR, Integer> getEndOfTBRDao() {
        try {
            if (endOfTBRDao == null) endOfTBRDao = getDao(EndOfTBR.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return endOfTBRDao;
    }

    public Dao<Offset, Integer> getOffsetDao() {
        try {
            if (offsetDao == null) offsetDao = getDao(Offset.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return offsetDao;
    }

    public Dao<PumpStatusChanged, Integer> getPumpStatusChangedDao() {
        try {
            if (pumpStatusChangedDao == null) pumpStatusChangedDao = getDao(PumpStatusChanged.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pumpStatusChangedDao;
    }

    public Dao<TimeChanged, Integer> getTimeChangedDao() {
        try {
            if (timeChangedDao == null) timeChangedDao = getDao(TimeChanged.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return timeChangedDao;
    }

    public Dao<CannulaFilled, Integer> getCannulaFilledDao() {
        try {
            if (cannulaFilledDao == null) cannulaFilledDao = getDao(CannulaFilled.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cannulaFilledDao;
    }

    public Dao<DailyTotal, Integer> getDailyTotalDao() {
        try {
            if (dailyTotalDao == null) dailyTotalDao = getDao(DailyTotal.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dailyTotalDao;
    }

    public Dao<BatteryInserted, Integer> getBatteryInsertedDao() {
        try {
            if (batteryInsertedDao == null) batteryInsertedDao = getDao(BatteryInserted.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return batteryInsertedDao;
    }

    public Dao<TubeFilled, Integer> getTubeFilledDao() {
        try {
            if (tubeFilledDao == null) tubeFilledDao = getDao(TubeFilled.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tubeFilledDao;
    }

    public Dao<CartridgeInserted, Integer> getCartridgeInsertedDao() {
        try {
            if (cartridgeInsertedDao == null) cartridgeInsertedDao = getDao(CartridgeInserted.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cartridgeInsertedDao;
    }

    public Dao<OccurenceOfAlert, Integer> getOccurenceOfAlertDao() {
        try {
            if (occurenceOfAlertDao == null) occurenceOfAlertDao = getDao(OccurenceOfAlert.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return occurenceOfAlertDao;
    }
}
