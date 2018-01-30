package sugar.free.sightremote.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public class LauncherActivity extends AppCompatActivity {

    private static final String AUTHORIZE_POLL_EXTRA = "authorize_poll";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle bundle = getIntent().getExtras();
        if ((bundle != null) && (!bundle.getString(AUTHORIZE_POLL_EXTRA, "").equals(""))) {
            startActivity(new Intent(this, AuthorizeActivity.class).putExtra(AUTHORIZE_POLL_EXTRA, bundle.getString(AUTHORIZE_POLL_EXTRA)));
        } else {
            if (getSharedPreferences("sugar.free.sightremote.services.SIGHTSERVICE", MODE_PRIVATE).contains("DEVICEMAC"))
                startActivity(new Intent(this, StatusActivity.class));
            else startActivity(new Intent(this, SetupActivity.class));
        }
        finish();
    }
}
