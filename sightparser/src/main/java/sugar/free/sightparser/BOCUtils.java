package sugar.free.sightparser;

public final class BOCUtils {

    public static int parseBOC(byte b) {
        return ((b & 0xF0) >> 4) * 10 + (b & 0x0F);
    }
}
