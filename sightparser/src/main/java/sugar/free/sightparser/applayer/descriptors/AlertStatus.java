package sugar.free.sightparser.applayer.descriptors;

public enum AlertStatus {

    ACTIVE((short) 0x1F00),
    MUTED((short) 0xE300);

    private static final long serialVersionUID = 1L;

    private short value;

    AlertStatus(short value) {
        this.value = value;
    }

    public static AlertStatus getAlertStaus(short value) {
        for (AlertStatus alertStatus : values()) if (alertStatus.value == value) return alertStatus;
        return null;
    }

    public short getValue() {
        return value;
    }
}
