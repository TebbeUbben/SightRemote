package sugar.free.sightremote.activities.history;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;

import java.sql.SQLException;
import java.util.List;

import sugar.free.sightremote.R;
import sugar.free.sightremote.adapters.BolusAdapter;
import sugar.free.sightremote.database.BolusDelivered;

public class BolusHistoryActivity extends HistoryActivity {

    private BolusAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getList().setLayoutManager(new LinearLayoutManager(this));
        getList().setAdapter(adapter = new BolusAdapter());

        showData();
    }


    @Override
    protected void showData() {
        try {
            List<BolusDelivered> bolusesDelivered = getDatabaseHelper().getBolusDeliveredDao()
                    .queryBuilder().orderBy("dateTime", false).query();
            adapter.setBolusesDelivered(bolusesDelivered);
            adapter.notifyDataSetChanged();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected int getSelectedNavItemID() {
        return R.id.nav_bolus_data;
    }
}
