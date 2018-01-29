package sugar.free.sightparser.applayer.descriptors;

import lombok.Getter;

public enum RestrictionLevel {

    PUMP((short) 0x1F00),
    DEVICE_LEVEL_1A((short) 0xE300),
    DEVICE_LEVEL_1B((short) 0xFC00),
    DEVICE_LEVEL_1C((short) 0x2503),
    CALYPSO((short) 0xC603),
    DEVICE_LEVEL_2A((short) 0xD903),
    DEVICE_LEVEL_2B((short) 0x4A05),
    DEVICE_LEVEL_2C((short) 0x5505),
    DEVICE_LEVEL_2D((short) 0xA905),
    CURIE_PWD((short) 0xB605),
    DEVICE_LEVEL_3A((short) 0x6F06),
    DEVICE_LEVEL_3B((short) 0x7006),
    DEVICE_LEVEL_3C((short) 0x8C06),
    CURIE_HCP((short) 0x2618),
    DEVICE_LEVEL_4A((short) 0x3918),
    DEVICE_LEVEL_4B((short) 0xC518),
    DEVICE_LEVEL_4C((short) 0xDA18),
    DEVICE_LEVEL_4D((short) 0x031B),
    DEVICE_LEVEL_4E((short) 0x1C1B),
    DEVICE_LEVEL_4F((short) 0xE01B),
    DEVICE_LEVEL_4G((short) 0xFF1B),
    DEVICE_LEVEL_4H((short) 0x6C1D),
    FACTORY((short) 0x731D);

    private static final long serialVersionUID = 1L;

    @Getter
    private short value;

    RestrictionLevel(short value) {
        this.value = value;
    }

    public static RestrictionLevel getByValue(short value) {
        for (RestrictionLevel restrictionLevel : values())
            if (restrictionLevel.getValue() == value) return restrictionLevel;
        return null;
    }
}
