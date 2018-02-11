package sugar.free.sightparser.applayer.messages.status;

import lombok.Getter;
import sugar.free.sightparser.applayer.messages.AppLayerMessage;
import sugar.free.sightparser.applayer.descriptors.alerts.Alert;
import sugar.free.sightparser.applayer.descriptors.AlertStatus;
import sugar.free.sightparser.applayer.descriptors.Service;
import sugar.free.sightparser.applayer.descriptors.AlertCategory;
import sugar.free.sightparser.pipeline.ByteBuf;

@Getter
public class ActiveAlertMessage extends AppLayerMessage {

    private static final long serialVersionUID = 1L;

    private int alertID;
    private AlertCategory alertCategory;
    private AlertStatus alertStatus;
    private Alert alert;

    @Override
    public Service getService() {
        return Service.STATUS;
    }

    @Override
    public short getCommand() {
        return (short) 0xD903;
    }

    @Override
    protected void parse(ByteBuf byteBuf) throws Exception {
        alertID = byteBuf.readUInt16LE();
        alertCategory = AlertCategory.getAlertCategory(byteBuf.readShort());
        Class<? extends Alert> alertClass = Alert.ALERTS.get(byteBuf.readShort());
        alertStatus = AlertStatus.getAlertStaus(byteBuf.readShort());
        if (alertClass == null) return;
        alert = alertClass.newInstance();
        alert.parse(byteBuf);
    }

    @Override
    protected boolean inCRC() {
        return true;
    }
}
