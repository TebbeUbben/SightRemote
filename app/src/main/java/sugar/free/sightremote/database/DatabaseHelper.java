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
    private static final int DATABASE_VERSION = 1;

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

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, BolusDelivered.class);
            TableUtils.createTable(connectionSource, BolusProgrammed.class);
            TableUtils.createTable(connectionSource, EndOfTBR.class);
            TableUtils.createTable(connectionSource, Offset.class);
            TableUtils.createTable(connectionSource, PumpStatusChanged.class);
            TableUtils.createTable(connectionSource, TimeChanged.class);
            TableUtils.createTable(connectionSource, CannulaFilled.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {

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
}
