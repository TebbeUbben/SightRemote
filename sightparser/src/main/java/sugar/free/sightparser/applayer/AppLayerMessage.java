package sugar.free.sightparser.applayer;

import android.annotation.SuppressLint;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import sugar.free.sightparser.Errors;
import sugar.free.sightparser.Message;
import sugar.free.sightparser.applayer.configuration.ReadConfigurationBlockMessage;
import sugar.free.sightparser.applayer.connection.ActivateServiceMessage;
import sugar.free.sightparser.applayer.connection.BindMessage;
import sugar.free.sightparser.applayer.connection.ConnectMessage;
import sugar.free.sightparser.applayer.connection.DisconnectMessage;
import sugar.free.sightparser.applayer.connection.ServiceChallengeMessage;
import sugar.free.sightparser.applayer.remote_control.AvailableBolusesMessage;
import sugar.free.sightparser.applayer.remote_control.BolusMessage;
import sugar.free.sightparser.applayer.remote_control.CancelBolusMessage;
import sugar.free.sightparser.applayer.remote_control.CancelTBRMessage;
import sugar.free.sightparser.applayer.remote_control.ChangeTBRMessage;
import sugar.free.sightparser.applayer.remote_control.SetPumpStatusMessage;
import sugar.free.sightparser.applayer.remote_control.SetTBRMessage;
import sugar.free.sightparser.applayer.status.ActiveBolusesMessage;
import sugar.free.sightparser.applayer.status.BatteryAmountMessage;
import sugar.free.sightparser.applayer.status.CartridgeAmountMessage;
import sugar.free.sightparser.applayer.status.CurrentBasalMessage;
import sugar.free.sightparser.applayer.status.CurrentTBRMessage;
import sugar.free.sightparser.applayer.status.DateTimeMesssage;
import sugar.free.sightparser.applayer.status.FirmwareVersionMessage;
import sugar.free.sightparser.applayer.status.PumpStatusMessage;
import sugar.free.sightparser.applayer.status.WarrantyTimerMessage;
import sugar.free.sightparser.crypto.Cryptograph;
import sugar.free.sightparser.error.InvalidAppCRCError;
import sugar.free.sightparser.error.InvalidAppVersionError;
import sugar.free.sightparser.error.SightError;
import sugar.free.sightparser.error.UnknownAppErrorCodeError;
import sugar.free.sightparser.error.UnknownAppMessageError;
import sugar.free.sightparser.error.UnknownServiceError;
import sugar.free.sightparser.pipeline.ByteBuf;

public abstract class AppLayerMessage extends Message implements Serializable {

    private static final byte VERSION = 0x20;
    @SuppressLint("UseSparseArrays")
    private static Map<Byte, Map<Short, Class<? extends AppLayerMessage>>> MESSAGES = new HashMap<>();

    static {
        Map<Short, Class<? extends AppLayerMessage>> connectionMessages = new HashMap<>();
        connectionMessages.put((short) 0xCDF3, BindMessage.class);
        connectionMessages.put((short) 0x0BF0, ConnectMessage.class);
        connectionMessages.put((short) 0x14F0, DisconnectMessage.class);
        connectionMessages.put((short) 0xD2F3, ServiceChallengeMessage.class);
        connectionMessages.put((short) 0xF7F0, ActivateServiceMessage.class);
        MESSAGES.put(Service.CONNECTION.getServiceID(), connectionMessages);

        Map<Short, Class<? extends AppLayerMessage>> statusMessages = new HashMap<>();
        statusMessages.put((short) 0xFC00, PumpStatusMessage.class);
        statusMessages.put((short) 0xA905, CurrentBasalMessage.class);
        statusMessages.put((short) 0x3A03, CartridgeAmountMessage.class);
        statusMessages.put((short) 0x2503, BatteryAmountMessage.class);
        statusMessages.put((short) 0xB605, CurrentTBRMessage.class);
        statusMessages.put((short) 0x6F06, ActiveBolusesMessage.class);
        statusMessages.put((short) 0xD82E, FirmwareVersionMessage.class);
        statusMessages.put((short) 0x4A05, WarrantyTimerMessage.class);
        statusMessages.put((short) 0xE300, DateTimeMesssage.class);
        MESSAGES.put(Service.STATUS.getServiceID(), statusMessages);

        Map<Short, Class<? extends AppLayerMessage>> insulinControlMessages = new HashMap<>();
        insulinControlMessages.put((short) 0x031B, BolusMessage.class);
        insulinControlMessages.put((short) 0xE01B, CancelBolusMessage.class);
        insulinControlMessages.put((short) 0x3918, CancelTBRMessage.class);
        insulinControlMessages.put((short) 0xC518, SetTBRMessage.class);
        insulinControlMessages.put((short) 0x53A4, ChangeTBRMessage.class);
        insulinControlMessages.put((short) 0xDA18, AvailableBolusesMessage.class);
        insulinControlMessages.put((short) 0x2618, SetPumpStatusMessage.class);
        MESSAGES.put(Service.REMOTE_CONTROL.getServiceID(), insulinControlMessages);

        Map<Short, Class<? extends AppLayerMessage>> configurationMessages = new HashMap<>();
        configurationMessages.put((short) 0x561E, ReadConfigurationBlockMessage.class);
        MESSAGES.put(Service.CONFIGURATION.getServiceID(), configurationMessages);
    }

    protected byte[] getData() throws Exception {
        return new byte[0];
    }

    public abstract Service getService();

    public abstract short getCommand();

    protected void parse(ByteBuf byteBuf) throws Exception {
    }

    protected boolean inCRC() {
        return false;
    }

    protected boolean outCRC() {
        return false;
    }

    public byte[] serialize() throws Exception {
        byte[] data = getData();
        ByteBuf byteBuf = new ByteBuf(4 + data.length + (outCRC() ? 2 : 0));
        byteBuf.putByte(VERSION);
        byteBuf.putByte(getService().getServiceID());
        byteBuf.putShort(getCommand());
        byteBuf.putBytes(data);
        if (outCRC()) byteBuf.putShortLE((short) Cryptograph.calculateCRC(data));
        return byteBuf.getBytes();
    }

    public static AppLayerMessage deserialize(ByteBuf byteBuf) throws Exception {
        byte version = byteBuf.readByte();
        byte service = byteBuf.readByte();
        short command = byteBuf.readShort();
        short error = byteBuf.readShort();
        byte[] data = byteBuf.readBytes();
        if (version != VERSION) throw new InvalidAppVersionError(version, VERSION);
        if (!MESSAGES.containsKey(service)) throw new UnknownServiceError(service);
        Class<? extends AppLayerMessage> clazz = MESSAGES.get(service).get(command);
        if (clazz == null) throw new UnknownAppMessageError(service, command);
        if (error != 0x0000) {
            Class<? extends SightError> errorClass = Errors.ERRORS.get(error);
            if (errorClass != null) throw errorClass.newInstance();
            else throw new UnknownAppErrorCodeError(clazz, error);
        }
        AppLayerMessage message = clazz.newInstance();
        ByteBuf dataBuf = new ByteBuf(data.length);
        dataBuf.putBytes(data);
        if (message.inCRC()) {
            short crc = dataBuf.getShortLE(data.length - 2);
            byte[] bytes = dataBuf.getBytes(data.length - 2);
            short calculatedCRC = (short) Cryptograph.calculateCRC(bytes);
            if (crc != calculatedCRC) throw new InvalidAppCRCError(crc, calculatedCRC);
            dataBuf = new ByteBuf(bytes.length);
            dataBuf.putBytes(bytes);
        }
        message.parse(dataBuf);
        return message;
    }

}
