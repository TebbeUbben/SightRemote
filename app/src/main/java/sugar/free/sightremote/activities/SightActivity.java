package sugar.free.sightremote.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import sugar.free.sightparser.handling.ServiceConnectionCallback;
import sugar.free.sightparser.handling.SightServiceConnector;
import sugar.free.sightparser.handling.StatusCallback;
import sugar.free.sightparser.pipeline.Status;
import sugar.free.sightremote.R;
import sugar.free.sightremote.activities.boluses.ExtendedBolusActivity;
import sugar.free.sightremote.activities.boluses.MultiwaveBolusActivity;
import sugar.free.sightremote.activities.boluses.StandardBolusActivity;
import sugar.free.sightremote.activities.history.BolusHistoryActivity;
import sugar.free.sightremote.activities.history.TBRHistoryActivity;
import sugar.free.sightremote.database.DatabaseHelper;

public abstract class SightActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private FrameLayout contentFrame;

    private SightServiceConnector sightServiceConnector;
    private Snackbar snackbar;
    private boolean autoOverlay = false;
    private boolean manualOverlay = false;

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(false);
        if (getSelectedNavItemID() != 0)
            navigationView.getMenu().findItem(getSelectedNavItemID()).setChecked(true);

        int id = item.getItemId();

        if (id == getSelectedNavItemID()) return false;
        else if (id == R.id.nav_status) {
            Intent intent = new Intent(this, StatusActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        else if (id == R.id.nav_standard_bolus) startActivity(StandardBolusActivity.class);
        else if (id == R.id.nav_extended_bolus) startActivity(ExtendedBolusActivity.class);
        else if (id == R.id.nav_multiwave_bolus) startActivity(MultiwaveBolusActivity.class);
        else if (id == R.id.nav_tbr) startActivity(TemporaryBasalRateActivity.class);
        else if (id == R.id.nav_br_profiles) startActivity(ChangeActiveBRProfileActivity.class);
        else if (id == R.id.nav_bolus_data) startActivity(BolusHistoryActivity.class);
        else if (id == R.id.nav_tbr_data) startActivity(TBRHistoryActivity.class);

        if (finishAfterNavigationClick()) finish();
        drawerLayout.closeDrawers();
        return true;
    }

    protected boolean finishAfterNavigationClick() {
         return true;
    }

    private void startActivity(Class<? extends Activity> activity) {
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getRootLayout());
        setupToolbar();

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        contentFrame = findViewById(R.id.content_frame);

        if (useNavigationDrawer() && drawerLayout != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_navigation, R.string.close_navigation);
            drawerLayout.addDrawerListener(actionBarDrawerToggle);
            navigationView.setNavigationItemSelectedListener(this);
            if (getSelectedNavItemID() != 0)
                navigationView.getMenu().findItem(getSelectedNavItemID()).setChecked(true);
        } else if (drawerLayout != null)
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        hideOverlay();

        sightServiceConnector = new SightServiceConnector(this);
        sightServiceConnector.setConnectionCallback(connectionCallback);
        sightServiceConnector.addStatusCallback(statusCallback);
    }

    protected int getSelectedNavItemID() {
        return 0;
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (useNavigationDrawer() && drawerLayout != null) actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (useNavigationDrawer() && drawerLayout != null)
            return actionBarDrawerToggle.onOptionsItemSelected(item);
        return false;
    }

    protected void connectedToService() {

    }

    protected void disconnectedFromService() {

    }

    protected void statusChanged(Status status) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        sightServiceConnector.disconnectFromService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        sightServiceConnector.connectToService();
    }

    private ServiceConnectionCallback connectionCallback = new ServiceConnectionCallback() {

        private Status latest;

        @Override
        public void onServiceConnected() {
            Status status = sightServiceConnector.getStatus();
            if (status != latest) {
                if (useOverlay()) updateOverlay(status);
                updateSnackbar(status);
                latest = status;
            }
            connectedToService();
        }

        @Override
        public void onServiceDisconnected() {
            disconnectedFromService();
        }
    };

    private void updateSnackbar(Status status) {
        if (!snackbarEnabled()) return;
        if (status == Status.CONNECTED) {
            dismissSnackbar();
        } else if (status == Status.CONNECTING) {
            showSnackbar(Snackbar.make(getRootView(), R.string.connecting, Snackbar.LENGTH_INDEFINITE));
        }
    }

    private void updateOverlay(Status status) {
        if (useOverlay()) {
            if (status == Status.CONNECTED) {
                autoOverlay = false;
                hideOverlay();
            } else {
                autoOverlay = true;
                showOverlay();
            }
        }
    }

    private void showOverlay() {
        runOnUiThread(() -> {
            View overlay = getOverlay();
            if (overlay != null) overlay.setVisibility(View.VISIBLE);
        });
    }

    private void hideOverlay() {
        runOnUiThread(() -> {
            if (manualOverlay || autoOverlay) return;
            View overlay = getOverlay();
            if (overlay != null) overlay.setVisibility(View.GONE);
        });
    }

    private StatusCallback statusCallback = status -> {
        updateSnackbar(status);
        updateOverlay(status);
        statusChanged(status);
    };

    public SightServiceConnector getServiceConnector() {
        return sightServiceConnector;
    }

    protected int getRootLayout() {
        return R.layout.activity_base;
    }

    protected View getRootView() {
        return contentFrame;
    }

    protected View getOverlay() {
        return findViewById(R.id.overlay);
    }

    protected boolean useOverlay() {
        return true;
    }

    protected boolean snackbarEnabled() {
        return true;
    }

    protected void showSnackbar(final Snackbar snackbar) {
        runOnUiThread(() -> {
            if (snackbar != null) snackbar.dismiss();
            SightActivity.this.snackbar = snackbar;
            snackbar.show();
        });
    }

    protected void dismissSnackbar() {
        runOnUiThread(() -> {
            if (snackbar != null) {
                snackbar.dismiss();
            }
        });
    }

    protected boolean useNavigationDrawer() {
        return true;
    }

    protected void setupToolbar() {
    }

    protected void setContent(@LayoutRes int id) {
        contentFrame.removeAllViews();
        contentFrame.addView(getLayoutInflater().inflate(id, null));
    }

    protected void showManualOverlay() {
        manualOverlay = true;
        showOverlay();
    }

    protected void hideManualOverlay() {
        manualOverlay = false;
        hideOverlay();
    }

    protected DatabaseHelper getDatabaseHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return databaseHelper;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }

    protected SharedPreferences getPreferences() {
        if (sharedPreferences == null)
            sharedPreferences = getSharedPreferences("sugar.free.sightremote.services.SIGHTREMOTE", MODE_PRIVATE);
        return sharedPreferences;
    }
}
