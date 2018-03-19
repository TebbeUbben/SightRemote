package sugar.free.sightremote.database;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@DatabaseTable(tableName = "tubeFilled")
@Getter
@Setter
public class TubeFilled {

    @DatabaseField(generatedId = true)
    private long id;

    @DatabaseField
    private long eventNumber;

    @DatabaseField
    private String pump;

    @DatabaseField(dataType = DataType.DATE_LONG)
    private Date dateTime;


    @DatabaseField
    private double amount;
}
