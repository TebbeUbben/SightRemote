package sugar.free.sightremote.activities.history;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;

import java.sql.SQLException;
import java.util.List;

import sugar.free.sightremote.R;
import sugar.free.sightremote.adapters.AlertAdapter;
import sugar.free.sightremote.adapters.BolusAdapter;
import sugar.free.sightremote.database.BolusDelivered;
import sugar.free.sightremote.database.OccurenceOfAlert;

public class EventHistoryActivity extends HistoryActivity {

    private AlertAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getList().setLayoutManager(new LinearLayoutManager(this));
        getList().setAdapter(adapter = new AlertAdapter());

        showData();
    }


    @Override
    protected void showData() {
        try {
            List<OccurenceOfAlert> occurencesOfAlerts = getDatabaseHelper().getOccurenceOfAlertDao()
                    .queryBuilder().orderBy("dateTime", false).query();
            adapter.setOccurencesOfAlerts(occurencesOfAlerts);
            adapter.notifyDataSetChanged();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected int getSelectedNavItemID() {
        return R.id.nav_event_data;
    }
}
