package sugar.free.sightparser.applayer.descriptors;

import lombok.Getter;

public enum MessagePriority {

    HIGHEST(2),
    HIGHER(1),
    NORMAL(0),
    LOW(-1),
    LOWEST(-2);

    @Getter
    private int value;

    MessagePriority(int value) {
        this.value = value;
    }
}
