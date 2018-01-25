package sugar.free.sightparser.applayer.descriptors;

public enum PumpStatus {

    STARTED((short) 0xE300),
    STOPPED((short) 0x1F00),
    PAUSED((short) 0xFC00);

    PumpStatus(short value) {
        this.value = value;
    }

    private static final long serialVersionUID = 1L;

    private short value;

    public short getValue() {
        return value;
    }

    public static PumpStatus getPumpStatus(short value) {
        for (PumpStatus pumpStatus : values()) if (pumpStatus.getValue() == value) return pumpStatus;
        return null;
    }
}
