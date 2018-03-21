package sugar.free.sightremote.activities.history;

import sugar.free.sightremote.R;
import sugar.free.sightremote.adapters.history.HistoryAdapter;
import sugar.free.sightremote.adapters.history.TDDAdapter;

import java.sql.SQLException;
import java.util.List;

public class TDDHistoryActivity extends HistoryActivity {

    @Override
    protected List loadData() throws SQLException {
        return getDatabaseHelper().getDailyTotalDao()
                .queryBuilder().orderBy("dateTime", false).query();
    }

    @Override
    protected HistoryAdapter getAdapter() {
        return new TDDAdapter();
    }

    @Override
    protected int getSelectedNavItemID() {
        return R.id.nav_tdd_data;
    }
}
