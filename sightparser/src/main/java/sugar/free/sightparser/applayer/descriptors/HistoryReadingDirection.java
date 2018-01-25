package sugar.free.sightparser.applayer.descriptors;

import lombok.Getter;

public enum  HistoryReadingDirection {

    FORWARD((short) 0x1F00),
    BACKWARD((short) 0xE300);

    private static final long serialVersionUID = 1L;

    @Getter
    private short value;

    HistoryReadingDirection(short value) {
        this.value = value;
    }
}
