package sugar.free.sightparser.applayer.descriptors.history_frames;

import lombok.Getter;
import sugar.free.sightparser.applayer.descriptors.alerts.Alert;
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
import sugar.free.sightparser.applayer.descriptors.alerts.Warning34EndOfWarranty;
import sugar.free.sightparser.applayer.descriptors.alerts.Warning36TBRCancelled;
import sugar.free.sightparser.applayer.descriptors.alerts.Warning38BolusCancelled;
import sugar.free.sightparser.applayer.descriptors.alerts.Warning39LoantimeWarning;
import sugar.free.sightparser.pipeline.ByteBuf;

@Getter
public abstract class OccurenceOfAlertFrame extends HistoryFrame {

    private static final long serialVersionUID = 1L;

    private Class<? extends Alert> alertType;
    private int alertId;

    @Override
    public void parse(ByteBuf byteBuf) {
        int alertId = byteBuf.readUInt16LE();
        switch (alertId) {
            case 6:
                alertType = Error6MechanicalError.class;
                break;
            case 7:
                alertType = Error7ElectronicError.class;
                break;
            case 10:
                alertType = Error10RewindError.class;
                break;
            case 13:
                alertType = Error13LanguageError.class;
                break;
            case 20:
                alertType = Maintenance20CartridgeNotInserted.class;
                break;
            case 21:
                alertType = Maintenance21CartridgeEmpty.class;
                break;
            case 22:
                alertType = Maintenance22BatteryEmpty.class;
                break;
            case 23:
                alertType = Maintenance23AutomaticOff.class;
                break;
            case 24:
                alertType = Maintenance24Occlusion.class;
                break;
            case 25:
                alertType = Maintenance25LoantimeOver.class;
                break;
            case 26:
                alertType = Maintenance26CartridgeChangeNotCompleted.class;
                break;
            case 27:
                alertType = Maintenance27DataDownloadFailed.class;
                break;
            case 28:
                alertType = Maintenance28PauseModeTimeout.class;
                break;
            case 29:
                alertType = Maintenance29BatteryTypeNotSet.class;
                break;
            case 30:
                alertType = Maintenance30CartridgeTypeNotSet.class;
                break;
            case 31:
                alertType = Warning31CartridgeLow.class;
                break;
            case 32:
                alertType = Warning32BatteryLow.class;
                break;
            case 33:
                alertType = Warning33InvalidDateTime.class;
                break;
            case 34:
                alertType = Warning34EndOfWarranty.class;
                break;
            case 36:
                alertType = Warning36TBRCancelled.class;
                break;
            case 38:
                alertType = Warning38BolusCancelled.class;
                break;
            case 39:
                alertType = Warning39LoantimeWarning.class;
                break;
        }
        alertId = byteBuf.readUInt16LE();
    }
}
