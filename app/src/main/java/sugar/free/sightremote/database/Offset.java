package sugar.free.sightremote.database;

import android.util.Log;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import sugar.free.sightparser.applayer.descriptors.HistoryType;

@DatabaseTable(tableName = "offsets")
@Getter
@Setter
public class Offset {

    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private String pump;

    @DatabaseField(dataType = DataType.ENUM_STRING)
    private HistoryType historyType;

    @DatabaseField
    private long offset;

    public static long getOffset(DatabaseHelper helper, String pump, HistoryType historyType) {
        try {
            List<Offset> result = helper.getOffsetDao().queryBuilder()
                    .where().eq("historyType", historyType)
                    .and().eq("pump", pump).query();
            if (result.size() > 0) return result.get(0).getOffset();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    };

    public static void setOffset(DatabaseHelper helper, String pump, HistoryType historyType, long offset) {
        try {
            List<Offset> result = helper.getOffsetDao().queryBuilder()
                    .where().eq("historyType", historyType)
                    .and().eq("pump", pump).query();
            if (result.size() > 0) {
                Offset updateOffset = result.get(0);
                updateOffset.setOffset(offset);
                helper.getOffsetDao().update(updateOffset);
            } else {
                Offset createOffset = new Offset();
                createOffset.setOffset(offset);
                createOffset.setPump(pump);
                createOffset.setHistoryType(historyType);
                helper.getOffsetDao().create(createOffset);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
