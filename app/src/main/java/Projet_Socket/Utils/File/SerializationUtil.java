package Projet_Socket.Utils.File;

import org.jetbrains.annotations.NotNull;

import java.io.*;

/**
 * Utilitaire de sérialisation
 */
public class SerializationUtil {

    /**
     * Serializes a packet.
     *
     * @param obj the object/packet to serialize.
     * @return the serialized byte-array
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @NotNull
    public static byte[] serialize(Object obj) throws IOException {
        var out = new ByteArrayOutputStream();
        var os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    /**
     * Deserializes a packet.
     *
     * @param data the data
     * @return packet
     * @throws IOException            Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        var in = new ByteArrayInputStream(data);
        var is = new ObjectInputStream(in);
        return is.readObject();
    }

}
