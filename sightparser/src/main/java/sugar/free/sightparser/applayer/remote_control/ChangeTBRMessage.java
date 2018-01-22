package sugar.free.sightparser.applayer.remote_control;

import sugar.free.sightparser.applayer.Service;

public class ChangeTBRMessage extends SetTBRMessage {

    @Override
    public Service getService() {
        return Service.REMOTE_CONTROL;
    }

    @Override
    public short getCommand() {
        return 0x53A4;
    }
}
