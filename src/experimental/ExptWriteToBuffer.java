/*
 * Created on 21-Apr-2003
 *
 */
package experimental;

import java.io.*;
import java.awt.Point;


/**
 * Experiment to try out reading and writing to a buffer to
 * test serialisaton code.
 * @author Luke
 *
 */
public class ExptWriteToBuffer {
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
            Point p2 = (Point)o;

            if (p.equals(p2)) {
                System.out.println("The two objects are equal!");
            } else {
                System.out.println("The two objects are not equal!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}