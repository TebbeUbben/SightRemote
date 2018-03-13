package sugar.free.sightremote.database;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Tebbe Ubben on 13.03.2018.
 */

@DatabaseTable(tableName = "dailyTotal")
@Getter
@Setter
public class DailyTotal {

    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private long eventNumber;

    @DatabaseField
    private String pump;

    @DatabaseField(dataType = DataType.DATE_LONG)
    private Date dateTime;

    @DatabaseField(dataType = DataType.DATE_LONG)
    private Date totalDate;

    @DatabaseField
    private double bolusTotal;

    @DatabaseField
    private double basalTotal;
}
