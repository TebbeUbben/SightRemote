package sugar.free.sightremote.activities.history;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import sugar.free.sightremote.R;
import sugar.free.sightremote.adapters.SystemAdapter;
import sugar.free.sightremote.database.BatteryInserted;
import sugar.free.sightremote.database.CannulaFilled;
import sugar.free.sightremote.database.CartridgeInserted;
import sugar.free.sightremote.database.DailyTotal;
import sugar.free.sightremote.database.PumpStatusChanged;
import sugar.free.sightremote.database.TubeFilled;

public class SystemHistoryActivity extends HistoryActivity {

    private SystemAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getList().setLayoutManager(new LinearLayoutManager(this));
        getList().setAdapter(adapter = new SystemAdapter());

        showData();
    }


    @Override
    protected void showData() {
        try {
            List<Object> objects = new ArrayList<>();
            objects.addAll(getDatabaseHelper().getPumpStatusChangedDao().queryForAll());
            objects.addAll(getDatabaseHelper().getBatteryInsertedDao().queryForAll());
            objects.addAll(getDatabaseHelper().getCartridgeInsertedDao().queryForAll());
            objects.addAll(getDatabaseHelper().getTubeFilledDao().queryForAll());
            objects.addAll(getDatabaseHelper().getCannulaFilledDao().queryForAll());
            Collections.sort(objects, (o1, o2) -> getDate(o2).compareTo(getDate(o1)));
            adapter.setObjects(objects);
            adapter.notifyDataSetChanged();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected int getSelectedNavItemID() {
        return R.id.nav_system_data;
    }

    private static Date getDate(Object object) {
        if (object instanceof PumpStatusChanged) return ((PumpStatusChanged) object).getDateTime();
        else if (object instanceof BatteryInserted) return ((BatteryInserted) object).getDateTime();
        else if (object instanceof CartridgeInserted) return ((CartridgeInserted) object).getDateTime();
        else if (object instanceof TubeFilled) return ((TubeFilled) object).getDateTime();
        else if (object instanceof CannulaFilled) return ((CannulaFilled) object).getDateTime();
        return null;
    }
}
