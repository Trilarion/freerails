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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A bunch of static methods.
 */
public class Utils {

    /**
     *
     * @param mutableList
     * @param <T>
     * @return
     */
    public static <T> List<T> immutateList(List<T> mutableList) {
        List<T> copiedList = new ArrayList<>(mutableList);
        return Collections.unmodifiableList(copiedList);
    }

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

    /**
     * @param values
     * @return
     */
    public static ImmutableList<Integer> integerImmutableListFromBoolean(boolean... values) {
        Integer[] integers = new Integer[values.length];
        for (int i = 0; i < values.length; i++) {
            integers[i] = values[i] ? 1 : 0;
        }
        return new ImmutableList<Integer>(integers);
    }

    /**
     * Returns the sum of the ints stored in the list.
     *
     * @return
     */
    public static int sumOfIntegerImmutableList(ImmutableList<Integer> list) {
        int sum = 0;
        for (int i = 0; i < list.size(); i++) {
            sum += list.get(i);
        }
        return sum;
    }

    /**
     * @return
     */
    public static <T extends Serializable> ImmutableList<T> removeLastOfImmutableList(ImmutableList<T> list) {
        T[] values = (T[]) new Serializable[list.size() - 1];
        for (int i = 0; i < list.size() - 1; i++) {
            values[i] = list.get(i);
        }
        return new ImmutableList<T>(values);
    }

    public static <T extends Serializable> ImmutableList<T> combineTwoImmutableLists(ImmutableList<T> listA, ImmutableList<T> listB) {
        T[] values = (T[]) new Serializable[listA.size() + listB.size()];
        for (int i = 0; i < listA.size(); i++) {
            values[i] = listA.get(i);
        }
        for (int i = 0; i < listB.size(); i++) {
            values[listA.size() + i] = listB.get(i);
        }
        return new ImmutableList<T>(values);
    }
}