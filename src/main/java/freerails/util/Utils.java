/*
 * Created on Jun 26, 2004
 */
package freerails.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.StringTokenizer;

/**
 * A bunch of static methods.
 * 
 * @author Luke
 * 
 */
strictfp public class Utils {

    public static boolean equalsBySerialization(Serializable a, Serializable b) {

        byte[] bytesA = write2ByteArray(a);
        byte[] bytesB = write2ByteArray(b);
        if (bytesA.length != bytesB.length)
            return false;

        for (int i = 0; i < bytesA.length; i++) {
            if (bytesA[i] != bytesB[i])
                return false;
        }

        return true;

    }

    /** Used when debugging. */
    public static void write(Serializable m, String fileName) {
        try {
            File f = new File(fileName);
            OutputStream out = new FileOutputStream(f);
            ObjectOutputStream objectOut = new ObjectOutputStream(out);
            objectOut.writeObject(m);
            objectOut.flush();
            objectOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Serializable cloneBySerialisation(Serializable m) {
        try {
            byte[] bytes = write2ByteArray(m);

            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            ObjectInputStream objectIn = new ObjectInputStream(in);
            Serializable o;

            o = (Serializable) objectIn.readObject();
            return o;
        } catch (ClassNotFoundException e) {
            // Should never happen.
            throw new IllegalStateException();
        } catch (IOException e) {
            // Should never happen.
            e.printStackTrace();
            throw new IllegalStateException();
        }

    }

    private static byte[] write2ByteArray(Serializable m) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOut = new ObjectOutputStream(out);
            objectOut.writeObject(m);
            objectOut.flush();
        } catch (IOException e) {
            // Should never happen.
            e.printStackTrace();
            throw new IllegalStateException();
        }

        byte[] bytes = out.toByteArray();
        return bytes;
    }

    public static String capitalizeEveryWord(String str) {
        StringBuffer result = new StringBuffer();
        StringTokenizer tok = new StringTokenizer(str);

        while (tok.hasMoreTokens()) {
            String token = tok.nextToken().toLowerCase();
            result.append(Character.toUpperCase(token.charAt(0))
                    + token.substring(1) + " ");
        }
        return result.toString().trim();
    }

    public static String findConstantFieldName(Object o) {
        Field[] fields = o.getClass().getFields();
        for (int i = 0; i < fields.length; i++) {
            int modifiers = fields[i].getModifiers();

            try {
                if (Modifier.isStatic(modifiers)
                        && Modifier.isPublic(modifiers)) {
                    Object o2 = fields[i].get(null);
                    if (o2.equals(o)) {
                        return fields[i].getName();
                    }
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException();
            }
        }

        return null;
    }

    /**
     * Returns the largest solution of the quadratic equation ax<sup><font
     * size="-1">2</font></sup> + bx + c = 0.
     * 
     * @throws IllegalArgumentException
     *             if <code>a == 0</code>
     * @throws IllegalArgumentException
     *             if <code>(b * b - 4 * a * c) < 0</code>
     */
    public static double solveQuadratic(double a, double b, double c)
            throws IllegalArgumentException {
        if (a == 0) {
            throw new IllegalArgumentException("a == 0");
        }
        double disc = b * b - 4 * a * c;
        if (disc < 0)
            throw new IllegalArgumentException("(b * b - 4 * a * c) < 0");
        return (-b + StrictMath.sqrt(disc)) / (2 * a);

    }

    public static int hypotenuse(int a, int b) {
        double d = Math.hypot(a, b);
        return (int) Math.round(d);
    }

    /**
     * Returns true if the objects are equal or both null, otherwise returns
     * false. Does not throw null pointer exceptions when either of the objects
     * is null.
     */
    public static boolean equal(Object a, Object b) {
        if (null == a || null == b) {
            return null == a && null == b;
        }
        return a.equals(b);
    }
}