package sugar.free.sightremote.database;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import sugar.free.sightparser.applayer.descriptors.HistoryBolusType;

@DatabaseTable(tableName = "timeChanged")
@Getter
@Setter
public class TimeChanged {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(index = true)
    private int eventNumber;

    @DatabaseField
    private String pump;

    @DatabaseField(dataType = DataType.DATE_LONG)
    private Date dateTime;

    @DatabaseField(dataType = DataType.DATE_LONG)
    private Date timeBefore;

}
