package sugar.free.sightremote.adapters.history;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import sugar.free.sightparser.applayer.descriptors.alerts.Error10RewindError;
import sugar.free.sightparser.applayer.descriptors.alerts.Error13LanguageError;
import sugar.free.sightparser.applayer.descriptors.alerts.Error6MechanicalError;
import sugar.free.sightparser.applayer.descriptors.alerts.Error7ElectronicError;
import sugar.free.sightparser.applayer.descriptors.alerts.Maintenance20CartridgeNotInserted;
import sugar.free.sightparser.applayer.descriptors.alerts.Maintenance21CartridgeEmpty;
import sugar.free.sightparser.applayer.descriptors.alerts.Maintenance22BatteryEmpty;
import sugar.free.sightparser.applayer.descriptors.alerts.Maintenance23AutomaticOff;
import sugar.free.sightparser.applayer.descriptors.alerts.Maintenance24Occlusion;
import sugar.free.sightparser.applayer.descriptors.alerts.Maintenance25LoantimeOver;
import sugar.free.sightparser.applayer.descriptors.alerts.Maintenance26CartridgeChangeNotCompleted;
import sugar.free.sightparser.applayer.descriptors.alerts.Maintenance27DataDownloadFailed;
import sugar.free.sightparser.applayer.descriptors.alerts.Maintenance28PauseModeTimeout;
import sugar.free.sightparser.applayer.descriptors.alerts.Maintenance29BatteryTypeNotSet;
import sugar.free.sightparser.applayer.descriptors.alerts.Maintenance30CartridgeTypeNotSet;
import sugar.free.sightparser.applayer.descriptors.alerts.Warning31CartridgeLow;
import sugar.free.sightparser.applayer.descriptors.alerts.Warning32BatteryLow;
import sugar.free.sightparser.applayer.descriptors.alerts.Warning33InvalidDateTime;
import sugar.free.sightparser.applayer.descriptors.alerts.Warning36TBRCancelled;
import sugar.free.sightparser.applayer.descriptors.alerts.Warning38BolusCancelled;
import sugar.free.sightparser.applayer.descriptors.alerts.Warning39LoantimeWarning;
import sugar.free.sightremote.R;
import sugar.free.sightremote.SightRemote;
import sugar.free.sightremote.database.OccurenceOfAlert;
import sugar.free.sightremote.utils.HTMLUtil;

public class AlertAdapter extends HistoryAdapter<AlertAdapter.ViewHolder, OccurenceOfAlert> {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = 0;
        switch (viewType) {
            case 0:
                layout = R.layout.adapter_error;
                break;
            case 1:
                layout = R.layout.adapter_maintenance;
                break;
            case 2:
                layout = R.layout.adapter_warning;
                break;
        }
        View itemView = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new AlertAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, OccurenceOfAlert entry, int position) {
        holder.alert.setText(getAlertDescription(entry));
        holder.dateTime.setText(new SimpleDateFormat(holder.dateTime.getResources().getString(R.string.history_date_time_formatter)).format(entry.getDateTime()));
    }

    private Spanned getAlertDescription(OccurenceOfAlert occurenceOfAlert) {
        String alertType = occurenceOfAlert.getAlertType();
        if (alertType.equals(Error6MechanicalError.class.getSimpleName()))
            return HTMLUtil.getHTML(R.string.alert, getString(R.string.alert_e6_code), getString(R.string.alert_e6_title));
        else if (alertType.equals(Error7ElectronicError.class.getSimpleName()))
            return HTMLUtil.getHTML(R.string.alert, getString(R.string.alert_e7_code), getString(R.string.alert_e7_title));
        else if (alertType.equals(Error10RewindError.class.getSimpleName()))
            return HTMLUtil.getHTML(R.string.alert, getString(R.string.alert_e10_code), getString(R.string.alert_e10_title));
        else if (alertType.equals(Error13LanguageError.class.getSimpleName()))
            return HTMLUtil.getHTML(R.string.alert, getString(R.string.alert_e13_code), getString(R.string.alert_e13_title));
        else if (alertType.equals(Maintenance20CartridgeNotInserted.class.getSimpleName()))
            return HTMLUtil.getHTML(R.string.alert, getString(R.string.alert_m20_code), getString(R.string.alert_m20_title));
        else if (alertType.equals(Maintenance21CartridgeEmpty.class.getSimpleName()))
            return HTMLUtil.getHTML(R.string.alert, getString(R.string.alert_m21_code), getString(R.string.alert_m21_title));
        else if (alertType.equals(Maintenance22BatteryEmpty.class.getSimpleName()))
            return HTMLUtil.getHTML(R.string.alert, getString(R.string.alert_m22_code), getString(R.string.alert_m22_title));
        else if (alertType.equals(Maintenance23AutomaticOff.class.getSimpleName()))
            return HTMLUtil.getHTML(R.string.alert, getString(R.string.alert_m23_code), getString(R.string.alert_m23_title));
        else if (alertType.equals(Maintenance24Occlusion.class.getSimpleName()))
            return HTMLUtil.getHTML(R.string.alert, getString(R.string.alert_m24_code), getString(R.string.alert_m24_title));
        else if (alertType.equals(Maintenance25LoantimeOver.class.getSimpleName()))
            return HTMLUtil.getHTML(R.string.alert, getString(R.string.alert_m25_code), getString(R.string.alert_m25_title));
        else if (alertType.equals(Maintenance26CartridgeChangeNotCompleted.class.getSimpleName()))
            return HTMLUtil.getHTML(R.string.alert, getString(R.string.alert_m26_code), getString(R.string.alert_m26_title));
        else if (alertType.equals(Maintenance27DataDownloadFailed.class.getSimpleName()))
            return HTMLUtil.getHTML(R.string.alert, getString(R.string.alert_m27_code), getString(R.string.alert_m27_title));
        else if (alertType.equals(Maintenance28PauseModeTimeout.class.getSimpleName()))
            return HTMLUtil.getHTML(R.string.alert, getString(R.string.alert_m28_code), getString(R.string.alert_m28_title));
        else if (alertType.equals(Maintenance29BatteryTypeNotSet.class.getSimpleName()))
            return HTMLUtil.getHTML(R.string.alert, getString(R.string.alert_m29_code), getString(R.string.alert_m29_title));
        else if (alertType.equals(Maintenance30CartridgeTypeNotSet.class.getSimpleName()))
            return HTMLUtil.getHTML(R.string.alert, getString(R.string.alert_m30_code), getString(R.string.alert_m30_title));
        else if (alertType.equals(Warning31CartridgeLow.class.getSimpleName()))
            return HTMLUtil.getHTML(R.string.alert, getString(R.string.alert_w31_code), getString(R.string.alert_w31_title));
        else if (alertType.equals(Warning32BatteryLow.class.getSimpleName()))
            return HTMLUtil.getHTML(R.string.alert, getString(R.string.alert_w32_code), getString(R.string.alert_w32_title));
        else if (alertType.equals(Warning33InvalidDateTime.class.getSimpleName()))
            return HTMLUtil.getHTML(R.string.alert, getString(R.string.alert_w33_code), getString(R.string.alert_w33_title));
        else if (alertType.equals(Warning36TBRCancelled.class.getSimpleName()))
            return HTMLUtil.getHTML(R.string.alert, getString(R.string.alert_w36_code), getString(R.string.alert_w36_title));
        else if (alertType.equals(Warning38BolusCancelled.class.getSimpleName()))
            return HTMLUtil.getHTML(R.string.alert, getString(R.string.alert_w38_code), getString(R.string.alert_w38_title));
        else if (alertType.equals(Warning39LoantimeWarning.class.getSimpleName()))
            return HTMLUtil.getHTML(R.string.alert, getString(R.string.alert_w39_code), getString(R.string.alert_w39_title));
        return null;
    }

    private String getString(int string) {
        return SightRemote.getInstance().getString(string);
    }

    @Override
    public int getItemViewType(OccurenceOfAlert entry, int position) {
        if (entry.getAlertType().startsWith("Error")) return 0;
        else if (entry.getAlertType().startsWith("Maintenance")) return 1;
        else if (entry.getAlertType().startsWith("Warning")) return 2;
        return super.getItemViewType(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView dateTime;
        private TextView alert;

        public ViewHolder(View itemView) {
            super(itemView);
            dateTime = itemView.findViewById(R.id.date_time);
            alert = itemView.findViewById(R.id.alert);
        }
    }

}
