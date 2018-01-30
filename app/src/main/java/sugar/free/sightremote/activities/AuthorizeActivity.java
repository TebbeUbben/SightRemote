package sugar.free.sightremote.activities;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import sugar.free.sightremote.R;

/**
 * Created by jamorham on 30/01/2018.
 */

public class AuthorizeActivity extends SightActivity {

    private static final String AUTHORIZE_POLL_EXTRA = "authorize_poll";
    private String packageName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorize);

        final Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            packageName = bundle.getString(AUTHORIZE_POLL_EXTRA, "");
            if (packageName.length() > 0) {
                ((TextView) findViewById(R.id.packageText)).setText(packageName);
                try {
                    Drawable icon = getPackageManager().getApplicationIcon(packageName);
                    ((ImageView) findViewById(R.id.packageIcon)).setImageDrawable(icon);
                    ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(packageName, 0);
                    CharSequence applicationLabel = getPackageManager().getApplicationLabel(applicationInfo);
                    ((TextView) findViewById(R.id.packageName)).setText(applicationLabel);
                } catch (PackageManager.NameNotFoundException e) {
                    // hmmm
                }
            } else {
                finish();
            }
        }
    }

    public void onDenyClick(View v) {
        getServiceConnector().setAuthorized(packageName, false);
        finish();
    }

    public void onAllowClick(View v) {
        getServiceConnector().setAuthorized(packageName, true);
        finish();
    }

}

