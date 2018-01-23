package sugar.free.sightremote.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import sugar.free.sightparser.handling.StatusCallback;
import sugar.free.sightparser.pipeline.Status;
import sugar.free.sightremote.R;
import sugar.free.sightremote.adapters.BluetoothDeviceAdapter;
import sugar.free.sightremote.adapters.PairingProgressAdapter;
import sugar.free.sightremote.utils.RecyclerItemClickListener;
import sugar.free.sightparser.handling.ServiceConnectionCallback;
import sugar.free.sightparser.handling.SightServiceConnector;


public class SetupActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSION_REQUEST_ID = 5123;

    private SightServiceConnector serviceConnector = new SightServiceConnector(this);
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private List<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    private BluetoothDeviceAdapter bluetoothDeviceAdapter = new BluetoothDeviceAdapter(bluetoothDevices);
    private PairingProgressAdapter pairingProgressAdapter = new PairingProgressAdapter();

    private boolean receiverRegistered = false;

    private ViewPager viewPager;
    private ScrollView licenseAgreement;
    private Button proceed;
    private LinearLayout pairing;
    private RecyclerView deviceList;
    private LinearLayout progress;
    private RecyclerView progressList;
    private LinearLayout setupComplete;
    private Button closeWizard;
    private CheckBox agree;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        viewPager = findViewById(R.id.view_pager);
        licenseAgreement = findViewById(R.id.license_agreement);
        proceed = findViewById(R.id.proceed);
        pairing = findViewById(R.id.pairing);
        deviceList = findViewById(R.id.device_list);
        progress = findViewById(R.id.progress);
        progressList = findViewById(R.id.progress_list);
        setupComplete = findViewById(R.id.setup_complete);
        closeWizard = findViewById(R.id.close_wizard);
        agree = findViewById(R.id.agree);

        proceed.setOnClickListener(this);
        closeWizard.setOnClickListener(this);

        viewPager.setAdapter(setupPagerAdapter);
        viewPager.setOffscreenPageLimit(4);

        deviceList.setLayoutManager(new LinearLayoutManager(this));
        deviceList.setAdapter(bluetoothDeviceAdapter);
        deviceList.addOnItemTouchListener(new RecyclerItemClickListener(this, deviceList, devicesClickListener));

        progressList.setLayoutManager(new LinearLayoutManager(this));
        progressList.setAdapter(pairingProgressAdapter);

        serviceConnector.addStatusCallback(statusCallback);
        serviceConnector.setConnectionCallback(connectionCallback);
    }

    @Override
    protected void onStart() {
        super.onStart();
        serviceConnector.connectToService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        serviceConnector.disconnectFromService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopBluetoothScan();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (viewPager.getCurrentItem() == 1) startBluetoothScan();
    }

    @Override
    public void onClick(View v) {
        if (v == proceed) {
            if (agree.isChecked()) {
                viewPager.setCurrentItem(1);
                startBluetoothScan();
            } else Toast.makeText(this, R.string.accept_terms, Toast.LENGTH_SHORT).show();
        } else if (v == closeWizard) {
            startActivity(new Intent(this, StatusActivity.class));
            finish();
        }
    }

    public void startBluetoothScan() {
        bluetoothDevices.clear();
        bluetoothDeviceAdapter.notifyDataSetChanged();
        if (checkLocationPermission()) {
            if (bluetoothAdapter != null) {
                bluetoothAdapter.enable();
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
                intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                registerReceiver(bluetoothBroadcastReceiver, intentFilter);
                receiverRegistered = true;
                bluetoothAdapter.startDiscovery();
            } else {
                Toast.makeText(this, R.string.bluetooth_not_found, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean checkLocationPermission() {
        boolean granted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!granted) ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_ID);
        return granted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_ID) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) startBluetoothScan();
            else finish();
        }
    }

    public void stopBluetoothScan() {
        if (receiverRegistered) {
            unregisterReceiver(bluetoothBroadcastReceiver);
            receiverRegistered = false;
        }
        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    private PagerAdapter setupPagerAdapter = new PagerAdapter() {
        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (position == 0) return licenseAgreement;
            else if (position == 1) return pairing;
            else if (position == 2) return progress;
            else if (position == 3) return setupComplete;
            return null;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
        }
    };

    private BroadcastReceiver bluetoothBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!bluetoothDevices.contains(bluetoothDevice)) {
                    bluetoothDevices.add((BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
                    bluetoothDeviceAdapter.notifyDataSetChanged();
                }
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) bluetoothAdapter.startDiscovery();
        }
    };

    private RecyclerItemClickListener.OnItemClickListener devicesClickListener = new RecyclerItemClickListener.OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            stopBluetoothScan();
            viewPager.setCurrentItem(2);
            serviceConnector.pair(bluetoothDevices.get(position).getAddress());
        }

        @Override
        public void onLongItemClick(View view, int position) {

        }
    };

    private StatusCallback statusCallback = new StatusCallback() {
        @Override
        public void onStatusChange(final Status status) {
            if (viewPager.getCurrentItem() != 2) return;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (status == Status.CONNECTING)
                        pairingProgressAdapter.setProgress(1);
                    else if  (status == Status.EXCHANGING_KEYS)
                        pairingProgressAdapter.setProgress(2);
                    else if (status == Status.WAITING_FOR_CODE_CONFIRMATION)
                        pairingProgressAdapter.setProgress(3);
                    else if (status == Status.APP_BINDING)
                        pairingProgressAdapter.setProgress(4);
                    else if (status == Status.CONNECTED)
                        viewPager.setCurrentItem(3);
                    else if (status == Status.CODE_REJECTED) {
                        Toast.makeText(SetupActivity.this, R.string.connection_declined, Toast.LENGTH_SHORT).show();
                        viewPager.setCurrentItem(1);
                        startBluetoothScan();
                    } else if (status == Status.DISCONNECTED) {
                        Toast.makeText(SetupActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                        viewPager.setCurrentItem(1);
                        startBluetoothScan();
                    }
                }
            });
        }
    };

    private ServiceConnectionCallback connectionCallback = new ServiceConnectionCallback() {
        @Override
        public void onServiceConnected() {
            statusCallback.onStatusChange(serviceConnector.getStatus());
        }
    };
}
