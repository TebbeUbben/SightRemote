package sugar.free.sightparser.applayer.descriptors;

import lombok.Getter;

public enum HistoryType {

    BOLUS((short) 0xE300),
    TBR((short) 0x2503);

    private static final long serialVersionUID = 1L;

    @Getter
    private short value;

    HistoryType(short value) {
        this.value = value;
    }
}
