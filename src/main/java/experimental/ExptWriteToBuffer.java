/*
 * Created on 21-Apr-2003
 *
 */
package experimental;

import org.apache.log4j.Logger;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Experiment to try out reading and writing to a buffer to test serialization
 * code.
 *
 * @author Luke
 */
public class ExptWriteToBuffer {
    private static final Logger logger = Logger
            .getLogger(ExptWriteToBuffer.class.getName());

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            Point p = new Point(10, 10);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream objectOut = new ObjectOutputStream(out);
            objectOut.writeObject(p);
            objectOut.flush();

            byte[] bytes = out.toByteArray();

            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            ObjectInputStream objectIn = new ObjectInputStream(in);
            Object o = objectIn.readObject();
            Point p2 = (Point) o;

            if (p.equals(p2)) {
                logger.info("The two objects are equal!");
            } else {
                logger.info("The two objects are not equal!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}