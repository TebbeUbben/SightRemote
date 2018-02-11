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

    public void putUInt16LE(int i) {
        putByte((byte) (i & 0xFF));
        putByte((byte) ((i >> 8) & 0xFF));
    }

    public void putUInt32LE(long l) {
        putByte((byte) (l & 0xFF));
        putByte((byte) ((l >> 8) & 0xFF));
        putByte((byte) ((l >> 16) & 0xFF));
        putByte((byte) ((l >> 24) & 0xFF));
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

    public short getShort() {
        return getShort(0);
    }

    public short readShort() {
        short s = getShort();
        shift(2);
        return s;
    }

    public int getUInt16LE(int position) {
        return (bytes[position++] & 0xFF |
                (bytes[position] & 0xFF)  << 8);
    }

    public int getUInt16LE() {
        return getUInt16LE(0);
    }

    public int readUInt16LE() {
        int i = getUInt16LE();
        shift(2);
        return i;
    }

    public long getUInt32LE(int position) {
        return ((long) bytes[position++] & 0xFF) |
                ((long) bytes[position++] & 0xFF) << 8 |
                ((long) bytes[position++] & 0xFF) << 16 |
                ((long) bytes[position] & 0xFF) << 24;
    }

    public long getUInt32LE() {
        return getUInt32LE(0);
    }

    public long readUInt32LE() {
        long l = getUInt32LE();
        shift(2);
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

    public String getUTF16LE(int position, int length) {
        try {
            return new String(getBytes(position, length), "UTF-16LE").replace(Character.toString((char) 0), "");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getUTF16LE(int length) {
        return getUTF16LE(0, length);
    }

    public String readUTF16LE(int length) {
        String s = getUTF16LE(length);
        shift(length);
        return s;
    }

    public void putUTF16LE(String string, int length) {
        try {
            byte[] bytes = string.getBytes("UTF-16LE");
            putBytes(bytes);
            putBytes((byte) 0x00, length - bytes.length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void putUTF16LE(String string) {
        putUTF16LE(string, 0);
    }


    public String getASCII(int position, int length) {
        try {
            return new String(getBytes(position, length), "US-ASCII").replace(Character.toString((char) 0), "");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getASCII(int length) {
        return getASCII(0, length);
    }

    public String readASCII(int length) {
        String s = getASCII(length);
        shift(length);
        return s;
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