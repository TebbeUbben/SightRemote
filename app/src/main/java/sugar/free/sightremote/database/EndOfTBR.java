package sugar.free.sightremote.database;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@DatabaseTable(tableName = "endOfTBR")
@Getter
@Setter
public class EndOfTBR {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(index = true)
    private int eventNumber;

    @DatabaseField
    private String pump;

    @DatabaseField(dataType = DataType.DATE_LONG)
    private Date dateTime;


    @DatabaseField(dataType = DataType.DATE_LONG)
    private Date startTime;

    @DatabaseField
    private int amount;

    @DatabaseField(index = true)
    private int duration;

}
