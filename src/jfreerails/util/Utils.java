/*
 * Created on Jun 26, 2004
 */
package jfreerails.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


/**
 *  A bunch of static methods.
 *  @author Luke
 *
 */
public class Utils {
    public static Serializable cloneBySerialisation(Object m)
        throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(out);
        objectOut.writeObject(m);
        objectOut.flush();

        byte[] bytes = out.toByteArray();

        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ObjectInputStream objectIn = new ObjectInputStream(in);
        Serializable o;

        try {
            o = (Serializable)objectIn.readObject();
        } catch (ClassNotFoundException e) {
            //Should never happend.
            throw new IllegalStateException();
        }

        return o;
    }
}