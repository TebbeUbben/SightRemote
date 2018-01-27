package sugar.free.sightparser.applayer.descriptors;

import lombok.Getter;

public enum HistoryType {

    TBR((short) 0x2503),
    BOLUS((short) 0x3A03),
    ALL((short) 0x1F00);

    private static final long serialVersionUID = 1L;

    @Getter
    private short value;

    HistoryType(short value) {
        this.value = value;
    }
}
