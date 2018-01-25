package sugar.free.sightparser.applayer.descriptors;

public enum ActiveBolusType {

    STANDARD((short) 0xE300),
    EXTENDED((short) 0xFC00),
    MULTIWAVE((short) 0x2503);

    private static final long serialVersionUID = 1L;

    private short value;

    ActiveBolusType(short value) {
        this.value = value;
    }

    public static ActiveBolusType getBolusType(short value) {
        for (ActiveBolusType bolusType : values()) if (bolusType.value == value) return bolusType;
        return null;
    }

    public short getValue() {
        return value;
    }
}
