package sugar.free.sightparser.pipeline;

import java.io.UnsupportedEncodingException;

@SuppressWarnings({"WeakerAccess", "unused"})
public class ByteBuf {

    private byte[] bytes;
    private int size = 0;

    public ByteBuf(int length) {
        bytes = new byte[length];
    }

    public int length() {
        return bytes.length;
    }

    public int size() {
        return size;
    }

    public void putByte(byte b) {
        bytes[size] = b;
        size++;
    }

    public void putBytes(byte[] b) {
        putBytes(b, b.length);
    }

    public void putBytes(byte b, int count) {
        for (int i = 0; i < count; i++) putByte(b);
    }

    public void putBytesLE(byte[] b) {
        putBytesLE(b, b.length);
    }

    public void putBytes(byte[] b, int length) {
        System.arraycopy(b, 0, bytes, size, length);
        size += length;
    }

    public void putBytesLE(byte[] b, int length) {
        for (int i = length - 1; i >= 0; i--) putByte(b[i]);
    }

    public void putShort(short s) {
        putByte((byte) (s >> 8));
        putByte((byte) s);
    }

    public void putShortLE(short s) {
        putByte((byte) s);
        putByte((byte) (s >> 8));

    }

    public void putInt(int i) {
        putByte((byte) (i >> 24));
        putByte((byte) (i >> 16));
        putByte((byte) (i >> 8));
        putByte((byte) i);
    }

    public void putIntLE(int i) {
        putByte((byte) i);
        putByte((byte) (i >> 8));
        putByte((byte) (i >> 16));
        putByte((byte) (i >> 24));
    }

    public void putFloat(float f) {
        putInt(Float.floatToIntBits(f));
    }

    public void putFloatLE(float f) {
        putIntLE(Float.floatToIntBits(f));
    }

    public void putDouble(double d) {
        putLong(Double.doubleToLongBits(d));
    }

    public void putDoubleLE(double d) {
        putLongLE(Double.doubleToLongBits(d));
    }

    public void putLong(long l) {
        putByte((byte) (l >> 56));
        putByte((byte) (l >> 48));
        putByte((byte) (l >> 40));
        putByte((byte) (l >> 32));
        putByte((byte) (l >> 24));
        putByte((byte) (l >> 16));
        putByte((byte) (l >> 8));
        putByte((byte) l);
    }

    public void putLongLE(long l) {
        putByte((byte) l);
        putByte((byte) (l >> 8));
        putByte((byte) (l >> 16));
        putByte((byte) (l >> 24));
        putByte((byte) (l >> 32));
        putByte((byte) (l >> 40));
        putByte((byte) (l >> 48));
        putByte((byte) (l >> 56));
    }

    public byte getByte(int position) {
        return bytes[position];
    }

    public byte getByte() {
        return getByte(0);
    }

    public byte readByte() {
        byte b = getByte(0);
        shift(1);
        return b;
    }

    public byte[] getBytes(int position, int length) {
        byte[] copy = new byte[length];
        System.arraycopy(bytes, position, copy, 0, length);
        return copy;
    }

    public byte[] getBytesLE(int position, int length) {
        byte[] copy = new byte[length];
        for (int i = 0; i < length; i++) {
            copy[i] = bytes[length - 1 - i + position];
        }
        return copy;
    }

    public byte[] getBytes(int length) {
        return getBytes(0, length);
    }

    public byte[] getBytesLE(int length) {
        return getBytesLE(0, length);
    }

    public byte[] readBytes(int length) {
        byte[] bytes = getBytes(0, length);
        shift(length);
        return bytes;
    }

    public byte[] readBytesLE(int length) {
        byte[] bytes = getBytesLE(0, length);
        shift(length);
        return bytes;
    }

    public short getShort(int position) {
        return (short) (bytes[position++]  << 8 |
                bytes[position] & 0xFF);
    }

    public short getShortLE(int position) {
        return (short) (bytes[position++] & 0xFF |
                bytes[position]  << 8);
    }

    public short getShort() {
        return getShort(0);
    }

    public short getShortLE() {
        return getShortLE(0);
    }

    public short readShort() {
        short s = getShort(0);
        shift(2);
        return s;
    }

    public short readShortLE() {
        short s = getShortLE(0);
        shift(2);
        return s;
    }

    public int getInt(int position) {
        return bytes[position++] << 24 |
                (bytes[position++] & 0xFF) << 16 |
                (bytes[position++] & 0xFF) << 8 |
                bytes[position] & 0xFF;
    }

    public int getIntLE(int position) {
        return bytes[position++] & 0xFF |
                (bytes[position++] & 0xFF) << 8  |
                (bytes[position++] & 0xFF) << 16 |
                bytes[position] << 24;
    }

    public int getInt() {
        return getInt(0);
    }

    public int readInt() {
        int i = getInt(0);
        shift(4);
        return i;
    }

    public int readIntLE() {
        int i = getIntLE(0);
        shift(4);
        return i;
    }

    public float getFloat(int position) {
        return Float.intBitsToFloat(getInt(position));
    }

    public float getFloatLE(int position) {
        return Float.intBitsToFloat(getIntLE(position));
    }

    public float getFloat() {
        return getFloat(0);
    }

    public float getFloatLE() {
        return getFloatLE(0);
    }

    public float readFloat() {
        float f = getFloat(0);
        shift(4);
        return f;
    }

    public float readFloatLE() {
        float f = getFloatLE(0);
        shift(4);
        return f;
    }

    public double getDouble(int position) {
        return Double.longBitsToDouble(getLong(position));
    }

    public double getDouble() {
        return getDouble(0);
    }

    public double readDouble() {
        double d = getDouble(0);
        shift(8);
        return d;
    }

    public double getDoubleLE(int position) {
        return Double.longBitsToDouble(getLongLE(position));
    }

    public double getDoubleLE() {
        return getDoubleLE(0);
    }

    public double readDoubleLE() {
        double d = getDoubleLE(0);
        shift(8);
        return d;
    }

    public long getLong(int position) {
        return ((long) bytes[position++]) << 56 |
                ((long) (bytes[position++] & 0xFF)) << 48 |
                ((long) (bytes[position++] & 0xFF)) << 40 |
                ((long) (bytes[position++] & 0xFF)) << 32 |
                ((long) (bytes[position++] & 0xFF)) << 24 |
                ((long) (bytes[position++] & 0xFF)) << 16 |
                ((long) (bytes[position++] & 0xFF)) << 8 |
                (long) bytes[position] & 0xFF;
    }

    public long getLongLE(int position) {
        return (long) bytes[position++] & 0xFF |
                ((long) (bytes[position++] & 0xFF)) << 8 |
                ((long) (bytes[position++] & 0xFF)) << 16 |
                ((long) (bytes[position++] & 0xFF)) << 24 |
                ((long) (bytes[position++] & 0xFF)) << 32 |
                ((long) (bytes[position++] & 0xFF)) << 40 |
                ((long) (bytes[position++] & 0xFF)) << 48 |
                ((long) bytes[position]) << 56;
    }

    public long getLong() {
        return getLong(0);
    }

    public long getLongLE() {
        return getLongLE(0);
    }

    public long readLong() {
        long l = getLong(0);
        shift(8);
        return l;
    }

    public long readLongLE() {
        long l = getLongLE(0);
        shift(8);
        return l;
    }

    public byte[] getBytes() {
        byte[] copy = new byte[size];
        System.arraycopy(bytes, 0, copy, 0, size);
        return copy;
    }

    public byte[] getBytesLE() {
        byte[] copy = new byte[size];
        for (int i = 0; i < size; i++) copy[i] = bytes[size - 1 - i];
        return copy;
    }

    public byte[] readBytes() {
        byte[] bytes = getBytes();
        shift(bytes.length);
        return bytes;
    }

    public void shift(int offset) {
        System.arraycopy(bytes, offset, bytes, 0, bytes.length - offset);
        size -= offset;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            if (i != 0) stringBuilder.append(" ");
            stringBuilder.append(String.format("%02X ", bytes[i]));
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    public String readUTF16LE(int length) {
        try {
            String string = new String(readBytes(length), "UTF-16LE");
            shift(length);
            return string;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getUTF16LE(int position, int length) {
        try {
            return new String(getBytes(position, length), "UTF-16LE");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getUTF16LE(int length) {
        return getUTF16LE(0, length);
    }

    public String readASCII(int length) {
        try {
            String string = new String(readBytes(length), "US-ASCII").replace(Character.toString((char) 0), "");
            shift(length);
            return string;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void putBoolean(boolean b) {
        putShort((short) (b ? 0x4B00 : 0xB400));
    }

    public boolean getBoolean(int position) {
        return getShort(position) == 0x4B00;
    }

    public boolean getBoolean() {
        return getBoolean(0);
    }

    public boolean readBoolean() {
        boolean b = getBoolean();
        shift(2);
        return b;
    }
}
