package sugar.free.sightremote.receivers;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import sugar.free.sightparser.handling.SightServiceConnector;
import sugar.free.sightremote.SightRemote;

public class ACLReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String mac = ((BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE")).getAddress();
        SightServiceConnector serviceConnector = SightRemote.getInstance().getServiceConnector();
        if (serviceConnector.isConnectedToService()) serviceConnector.aclDisconnect(mac);
    }
}
