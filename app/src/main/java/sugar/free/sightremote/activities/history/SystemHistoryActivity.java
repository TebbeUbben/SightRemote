package sugar.free.sightremote.activities.history;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import sugar.free.sightremote.R;
import sugar.free.sightremote.adapters.history.SystemAdapter;
import sugar.free.sightremote.database.BatteryInserted;
import sugar.free.sightremote.database.CannulaFilled;
import sugar.free.sightremote.database.CartridgeInserted;
import sugar.free.sightremote.database.PumpStatusChanged;
import sugar.free.sightremote.database.TubeFilled;

public class SystemHistoryActivity extends HistoryActivity {

    @Override
    protected List loadData() throws SQLException {
        List<Object> objects = new ArrayList<>();
        objects.addAll(getDatabaseHelper().getPumpStatusChangedDao().queryForAll());
        objects.addAll(getDatabaseHelper().getBatteryInsertedDao().queryForAll());
        objects.addAll(getDatabaseHelper().getCartridgeInsertedDao().queryForAll());
        objects.addAll(getDatabaseHelper().getTubeFilledDao().queryForAll());
        objects.addAll(getDatabaseHelper().getCannulaFilledDao().queryForAll());
        Collections.sort(objects, (o1, o2) -> getDate(o2).compareTo(getDate(o1)));
        return objects;
    }

    @Override
    public SystemAdapter getAdapter() {
        return new SystemAdapter();
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
