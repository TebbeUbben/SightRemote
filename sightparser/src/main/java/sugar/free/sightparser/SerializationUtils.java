package sugar.free.sightparser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public final class SerializationUtils {

    private SerializationUtils() {
    }

    public static byte[] serialize(Serializable serializable) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutput out;
        byte[] bytes = null;
        try {
            out = new ObjectOutputStream(baos);
            out.writeObject(serializable);
            out.flush();
            bytes = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                baos.close();
            } catch (IOException e) {
            }
        }
        return bytes;
    }

    public static Serializable deserialize(byte[] bytes) {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInput in;
        Serializable serializable = null;
        try {
            in = new ObjectInputStream(bais);
            serializable = (Serializable) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                bais.close();
            } catch (IOException e) {
            }
        }
        return serializable;
    }

}
