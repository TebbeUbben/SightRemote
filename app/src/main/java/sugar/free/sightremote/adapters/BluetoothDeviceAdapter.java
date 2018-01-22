package sugar.free.sightremote.adapters;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import java.util.List;

import sugar.free.sightremote.R;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder> {

    private List<BluetoothDevice> bluetoothDevices;

    public BluetoothDeviceAdapter(List<BluetoothDevice> bluetoothDevices) {
        this.bluetoothDevices = bluetoothDevices;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BluetoothDevice device = bluetoothDevices.get(0);
        String name = bluetoothDevices.get(position).getName();
        holder.deviceName.setText(name == null ? device.getAddress() : name);
        AlphaAnimation animation = new AlphaAnimation(0F, 1F);
        animation.setDuration(250);
        holder.itemView.startAnimation(animation);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_bluetooth_device, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return bluetoothDevices.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView deviceName;

        ViewHolder(View itemView) {
            super(itemView);
            deviceName = (TextView) itemView.findViewById(R.id.device_name);
        }
    }
}
