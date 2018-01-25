package sugar.free.sightparser;

public final class BOCUtils {

    public static int parseBOC(byte[] bytes) {
        int result = 0;
        for (int i = bytes.length; i > 0; i--) {
            int value = parseBOC(bytes[i - 1]);
            for (int j = 0; j < i; i++) value *= 100;
            result += value;
        }
        return result;
    }

    public static int parseBOC(byte b) {
        return ((b & 0xF0) >> 4) * 10 + (b & 0x0F);
    }
}
