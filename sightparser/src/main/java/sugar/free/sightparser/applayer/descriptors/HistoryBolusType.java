package sugar.free.sightparser.applayer.descriptors;

public enum  HistoryBolusType {

    STANDARD((short) 0x1F00),
    EXTENDED((short) 0xE300),
    MULTIWAVE((short) 0xFC00);

    private static final long serialVersionUID = 1L;

    private short value;

    HistoryBolusType(short value) {
        this.value = value;
    }

    public static HistoryBolusType getBolusType(short value) {
        for (HistoryBolusType bolusType : values()) if (bolusType.value == value) return bolusType;
        return null;
    }

    public short getValue() {
        return value;
    }
}
