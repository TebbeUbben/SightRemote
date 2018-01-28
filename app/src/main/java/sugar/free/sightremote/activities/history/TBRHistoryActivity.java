package sugar.free.sightremote.activities.history;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;

import java.sql.SQLException;
import java.util.List;

import sugar.free.sightremote.R;
import sugar.free.sightremote.adapters.BolusAdapter;
import sugar.free.sightremote.adapters.TBRAdapter;
import sugar.free.sightremote.database.BolusDelivered;
import sugar.free.sightremote.database.EndOfTBR;

public class TBRHistoryActivity extends HistoryActivity {

    private TBRAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getBolusList().setLayoutManager(new LinearLayoutManager(this));
        getBolusList().setAdapter(adapter = new TBRAdapter());

        showData();
    }


    @Override
    protected void showData() {
        try {
            List<EndOfTBR> endOfTBRs = getDatabaseHelper().getEndOfTBRDao()
                    .queryBuilder().orderBy("dateTime", false).query();
            adapter.setEndOfTBRs(endOfTBRs);
            adapter.notifyDataSetChanged();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected int getSelectedNavItemID() {
        return R.id.nav_tbr_data;
    }
}
