package sugar.free.sightremote.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import sugar.free.sightparser.handling.ServiceConnectionCallback;
import sugar.free.sightparser.handling.SightServiceConnector;
import sugar.free.sightremote.R;

public class LauncherActivity extends AppCompatActivity implements ServiceConnectionCallback {

    private static final String AUTHORIZE_POLL_EXTRA = "authorize_poll";

    private SightServiceConnector connector;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        final Bundle bundle = getIntent().getExtras();
        if ((bundle != null) && (!bundle.getString(AUTHORIZE_POLL_EXTRA, "").equals(""))) {
            startActivity(new Intent(this, AuthorizeActivity.class).putExtra(AUTHORIZE_POLL_EXTRA, bundle.getString(AUTHORIZE_POLL_EXTRA)));
            finish();
        } else {
            connector = new SightServiceConnector(this);
            connector.setConnectionCallback(this);
            connector.connectToService();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connector != null && connector.isConnectedToService()) connector.disconnectFromService();
    }

    @Override
    public void onServiceConnected() {
        if (connector.isUseable()) startActivity(new Intent(this, StatusActivity.class));
        else startActivity(new Intent(this, SetupActivity.class));
        finish();
    }

    @Override
    public void onServiceDisconnected() {

    }
}
