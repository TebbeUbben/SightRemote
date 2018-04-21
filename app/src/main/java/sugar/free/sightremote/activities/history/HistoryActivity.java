package sugar.free.sightremote.activities.history;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import io.fabric.sdk.android.services.concurrency.AsyncTask;
import lombok.AccessLevel;
import lombok.Getter;
import sugar.free.sightparser.handling.HistoryBroadcast;
import sugar.free.sightremote.R;
import sugar.free.sightremote.activities.SightActivity;
import sugar.free.sightremote.adapters.history.HistoryAdapter;

import java.sql.SQLException;
import java.util.List;

public abstract class HistoryActivity extends SightActivity implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView list;
    private SwipeRefreshLayout refreshLayout;
    private HistoryAdapter adapter = getAdapter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent(R.layout.activity_history);

        list = findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
        refreshLayout = findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.colorAccent));

        new LoadDataTask().execute();
    }

    @Override
    protected void connectedToService() {
        getServiceConnector().connect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(broadcastReceiver, new IntentFilter(HistoryBroadcast.ACTION_SYNC_FINISHED));
        requestHistorySync();
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
                new LoadDataTask().doInBackground();
                refreshLayout.setRefreshing(false);
            });
        }
    };

    protected abstract HistoryAdapter getAdapter();

    protected abstract List loadData() throws SQLException;

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
        requestHistorySync();
    }

    private class LoadDataTask extends AsyncTask<Void, Void, List> {
        @Override
        protected void onPreExecute() {
            showLoadingIndicator();
        }

        @Override
        protected List doInBackground(Void... voids) {
            try {
                return loadData();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List result) {
            adapter.setHistoryEntries(result);
            adapter.notifyDataSetChanged();
            hideLoadingIndicator();
        }
    }
}
