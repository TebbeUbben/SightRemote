package sugar.free.sightremote.activities.history;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;

import java.sql.SQLException;
import java.util.List;

import sugar.free.sightremote.R;
import sugar.free.sightremote.adapters.TDDAdapter;
import sugar.free.sightremote.database.BolusDelivered;
import sugar.free.sightremote.database.DailyTotal;

public class TDDHistoryActivity extends HistoryActivity {

    private TDDAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getList().setLayoutManager(new LinearLayoutManager(this));
        getList().setAdapter(adapter = new TDDAdapter());

        showData();
    }


    @Override
    protected void showData() {
        try {
            List<DailyTotal> dailyTotals = getDatabaseHelper().getDailyTotalDao()
                    .queryBuilder().orderBy("dateTime", false).query();
            adapter.setDailyTotals(dailyTotals);
            adapter.notifyDataSetChanged();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected int getSelectedNavItemID() {
        return R.id.nav_tdd_data;
    }
}
