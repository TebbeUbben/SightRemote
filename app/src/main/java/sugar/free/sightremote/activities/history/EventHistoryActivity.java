package sugar.free.sightremote.activities.history;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;

import java.sql.SQLException;
import java.util.List;

import sugar.free.sightremote.R;
import sugar.free.sightremote.adapters.history.AlertAdapter;
import sugar.free.sightremote.database.OccurenceOfAlert;

public class EventHistoryActivity extends HistoryActivity {

    @Override
    protected List loadData() throws SQLException {
        return getDatabaseHelper().getOccurenceOfAlertDao()
                .queryBuilder().orderBy("dateTime", false).query();
    }

    @Override
    public AlertAdapter getAdapter() {
        return new AlertAdapter();
    }

    @Override
    protected int getSelectedNavItemID() {
        return R.id.nav_event_data;
    }
}
