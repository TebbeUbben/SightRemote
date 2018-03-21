package sugar.free.sightremote.activities.history;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.sql.SQLException;
import java.util.List;

import sugar.free.sightremote.R;
import sugar.free.sightremote.adapters.history.HistoryAdapter;
import sugar.free.sightremote.adapters.history.TBRAdapter;
import sugar.free.sightremote.database.EndOfTBR;

public class TBRHistoryActivity extends HistoryActivity {

    @Override
    protected List loadData() throws SQLException {
        return getDatabaseHelper().getEndOfTBRDao()
                .queryBuilder().orderBy("dateTime", false).query();
    }

    @Override
    public HistoryAdapter getAdapter() {
        return new TBRAdapter();
    }

    @Override
    protected int getSelectedNavItemID() {
        return R.id.nav_tbr_data;
    }
}
