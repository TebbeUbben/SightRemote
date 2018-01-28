package sugar.free.sightremote.database;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import sugar.free.sightparser.applayer.descriptors.PumpStatus;

@DatabaseTable(tableName = "pumpStatusChanged")
@Getter
@Setter
public class PumpStatusChanged {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(index = true)
    private int eventNumber;

    @DatabaseField
    private String pump;

    @DatabaseField(dataType = DataType.DATE_LONG)
    private Date dateTime;

    @DatabaseField(dataType = DataType.ENUM_STRING)
    private PumpStatus oldValue;

    @DatabaseField(dataType = DataType.ENUM_STRING)
    private PumpStatus newValue;
}
