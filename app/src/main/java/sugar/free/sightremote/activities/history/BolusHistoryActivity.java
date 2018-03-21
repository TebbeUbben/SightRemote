package sugar.free.sightremote.activities.history;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;

import java.sql.SQLException;
import java.util.List;

import sugar.free.sightremote.R;
import sugar.free.sightremote.adapters.history.BolusAdapter;
import sugar.free.sightremote.database.BolusDelivered;

public class BolusHistoryActivity extends HistoryActivity {

    @Override
    protected List loadData() throws SQLException {
        return getDatabaseHelper().getBolusDeliveredDao()
                .queryBuilder().orderBy("dateTime", false).query();
    }

    @Override
    public BolusAdapter getAdapter() {
        return new BolusAdapter();
    }

    @Override
    protected int getSelectedNavItemID() {
        return R.id.nav_bolus_data;
    }
}
