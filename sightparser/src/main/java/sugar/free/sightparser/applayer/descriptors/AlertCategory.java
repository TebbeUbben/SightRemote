package sugar.free.sightparser.applayer.descriptors;

public enum AlertCategory {

    NO_ALERT((short) 0x1F00),
    REMINDER((short) 0xE300),
    MAINTENANCE((short) 0xFC00),
    WARNING((short) 0x2503),
    ERROR((short) 0x3A03);

    private static final long serialVersionUID = 1L;

    private short value;

    AlertCategory(short value) {
        this.value = value;
    }

    public static AlertCategory getAlertCategory(short value) {
        for (AlertCategory alertCategory : values()) if (alertCategory.value == value) return alertCategory;
        return null;
    }

    public short getValue() {
        return value;
    }
}
