package sugar.free.sightparser.applayer.status;

public enum  BolusType {

    INSTANT((short) 0xE300),
    EXTENDED((short) 0xFC00),
    MULTIWAVE((short) 0x2503);

    private static final long serialVersionUID = 1L;

    private short value;

    BolusType(short value) {
        this.value = value;
    }

    public static BolusType getBolusType(short value) {
        for (BolusType bolusType : values()) if (bolusType.value == value) return bolusType;
        return null;
    }

    public short getValue() {
        return value;
    }
}
