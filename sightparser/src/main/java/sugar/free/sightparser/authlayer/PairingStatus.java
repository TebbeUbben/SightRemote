package sugar.free.sightparser.authlayer;

public enum PairingStatus {

    CONFIRMED((short) 0x3B2E),
    REJECTED((short) 0xAA1E),
    PENDING((short) 0x9306);

    private short value;

    PairingStatus(short value) {
        this.value = value;
    }

    public short getValue() {
        return value;
    }

    public static PairingStatus getByValue(short value) {
        for (PairingStatus pairingStatus : values())
            if (value == pairingStatus.getValue())
                return pairingStatus;
        return null;
    }
}
