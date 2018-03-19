package sugar.free.sightremote.activities.history;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;

import lombok.AccessLevel;
import lombok.Getter;
import sugar.free.sightparser.handling.HistoryBroadcast;
import sugar.free.sightremote.R;
import sugar.free.sightremote.activities.SightActivity;

public abstract class HistoryActivity extends SightActivity implements SwipeRefreshLayout.OnRefreshListener {

    @Getter(value = AccessLevel.PROTECTED)
    private RecyclerView list;
    private SwipeRefreshLayout refreshLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_history);

        list = findViewById(R.id.list);
        refreshLayout = findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent));
    }

    @Override
    protected void connectedToService() {
        getServiceConnector().connect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(broadcastReceiver, new IntentFilter(HistoryBroadcast.ACTION_SYNC_FINISHED));
        sendBroadcast(new Intent(HistoryBroadcast.ACTION_START_SYNC));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            runOnUiThread(() -> {
                showData();
                refreshLayout.setRefreshing(false);
            });
        }
    };

    protected abstract void showData();

    @Override
    protected boolean useOverlay() {
        return false;
    }

    @Override
    protected boolean snackbarEnabled() {
        return false;
    }

    @Override
    public void onRefresh() {
        sendBroadcast(new Intent(HistoryBroadcast.ACTION_START_SYNC));
    }
}
