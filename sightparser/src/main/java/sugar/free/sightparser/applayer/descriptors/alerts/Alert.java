package sugar.free.sightparser.applayer.descriptors.alerts;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import sugar.free.sightparser.pipeline.ByteBuf;

public abstract class Alert implements Serializable {

    public static final Map<Short, Class<? extends Alert>> ALERTS = new HashMap<>();

    static {
        ALERTS.put((short) 0x1F00, Reminder1DeliverBolus.class);
        ALERTS.put((short) 0xE300, Reminder2MissedBolus.class);
        ALERTS.put((short) 0xFC00, Reminder3AlarmClock.class);
        ALERTS.put((short) 0x2503, Reminder4ChangeInfusionSet.class);
        ALERTS.put((short) 0x3A03, Reminder7TBRCompleted.class);

        ALERTS.put((short) 0xC603, Warning31CartridgeLow.class);
        ALERTS.put((short) 0xD903, Warning32BatteryLow.class);
        ALERTS.put((short) 0x4A05, Warning33InvalidDateTime.class);
        ALERTS.put((short) 0x5505, Warning34EndOfWarranty.class);
        ALERTS.put((short) 0xA905, Warning36TBRCancelled.class);
        ALERTS.put((short) 0xB605, Warning38BolusCancelled.class);
        ALERTS.put((short) 0x6F06, Warning39LoantimeWarning.class);

        ALERTS.put((short) 0x7006, Maintenance20CartridgeNotInserted.class);
        ALERTS.put((short) 0x8C06, Maintenance21CartridgeEmpty.class);
        ALERTS.put((short) 0x9306, Maintenance22BatteryEmpty.class);
        ALERTS.put((short) 0x2618, Maintenance23AutomaticOff.class);
        ALERTS.put((short) 0x3918, Maintenance24Occlusion.class);
        ALERTS.put((short) 0xC518, Maintenance25LoantimeOver.class);
        ALERTS.put((short) 0xDA18, Maintenance26CartridgeChangeNotCompleted.class);
        ALERTS.put((short) 0x031B, Maintenance27DataDownloadFailed.class);
        ALERTS.put((short) 0x1C1B, Maintenance28PauseModeTimeout.class);
        ALERTS.put((short) 0xE01B, Maintenance29BatteryTypeNotSet.class);
        ALERTS.put((short) 0xFF1B, Maintenance30CartridgeTypeNotSet.class);

        ALERTS.put((short) 0x6C1D, Error6MechanicalError.class);
        ALERTS.put((short) 0x731D, Error10RewindError.class);
        ALERTS.put((short) 0x3A03, Error13LanguageError.class);
    }

    public void parse(ByteBuf byteBuf) {
    }

}
