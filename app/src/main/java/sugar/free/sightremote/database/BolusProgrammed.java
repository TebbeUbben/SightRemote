package sugar.free.sightremote.database;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import sugar.free.sightparser.applayer.descriptors.HistoryBolusType;

@DatabaseTable(tableName = "bolusProgrammed")
@Getter
@Setter
public class BolusProgrammed {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(index = true)
    private int eventNumber;

    @DatabaseField
    private String pump;

    @DatabaseField(dataType = DataType.DATE_LONG)
    private Date dateTime;


    @DatabaseField(dataType = DataType.ENUM_STRING)
    private HistoryBolusType bolusType;

    @DatabaseField
    private float immediateAmount;

    @DatabaseField
    private float extendedAmount;

    @DatabaseField
    private int duration;

    @DatabaseField(index = true)
    private int bolusId;

}
