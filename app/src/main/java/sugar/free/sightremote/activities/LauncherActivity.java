package sugar.free.sightremote.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSharedPreferences("sugar.free.sightremote.services.SIGHTSERVICE", MODE_PRIVATE).contains("DEVICEMAC"))
            startActivity(new Intent(this, StatusActivity.class));
        else startActivity(new Intent(this, SetupActivity.class));
        finish();
    }
}
