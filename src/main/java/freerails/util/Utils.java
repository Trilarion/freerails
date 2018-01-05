/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.util;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.StringTokenizer;

/**
 * A bunch of static methods.
 */
public strictfp class Utils {

    /**
     * @param a
     * @param b
     * @return
     */
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

    /**
     * @param m
     * @return
     */
    public static Serializable cloneBySerialisation(Serializable m) {
        try {
            byte[] bytes = write2ByteArray(m);

            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            ObjectInputStream objectIn = new ObjectInputStream(in);
            Serializable o;

            o = (Serializable) objectIn.readObject();
            return o;
        } catch (ClassNotFoundException | IOException e) {
            // Should never happen.
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
            throw new IllegalStateException();
        }

        return out.toByteArray();
    }

    /**
     * @param str
     * @return
     */
    public static String capitalizeEveryWord(String str) {
        StringBuilder result = new StringBuilder();
        StringTokenizer tok = new StringTokenizer(str);

        while (tok.hasMoreTokens()) {
            String token = tok.nextToken().toLowerCase();
            result.append(Character.toUpperCase(token.charAt(0))).append(token.substring(1)).append(' ');
        }
        return result.toString().trim();
    }

    /**
     * @param o
     * @return
     */
    public static String findConstantFieldName(Object o) {
        Field[] fields = o.getClass().getFields();
        for (Field field : fields) {
            int modifiers = field.getModifiers();

            try {
                if (Modifier.isStatic(modifiers)
                        && Modifier.isPublic(modifiers)) {
                    Object o2 = field.get(null);
                    if (o2.equals(o)) {
                        return field.getName();
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
     * @param a
     * @param b
     * @param c
     * @return
     * @throws IllegalArgumentException if {@code a == 0}
     * @throws IllegalArgumentException if {@code (b * b - 4 * a * c) < 0}
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

    /**
     * Returns true if the objects are equal or both null, otherwise returns
     * false. Does not throw null pointer exceptions when either of the objects
     * is null.
     *
     * @param a
     * @param b
     * @return
     */
    public static boolean equal(Object a, Object b) {
        if (null == a || null == b) {
            return null == a && null == b;
        }
        return a.equals(b);
    }
}