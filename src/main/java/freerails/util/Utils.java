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
import java.util.*;

/**
 * A bunch of static methods.
 */
public final class Utils {

    private Utils() {
    }

    /**
     *
     * @param mutableList
     * @param <T>
     * @return
     */
    public static <T> List<T> immutableList(List<T> mutableList) {
        List<T> copiedList = new ArrayList<>(mutableList);
        return Collections.unmodifiableList(copiedList);
    }

    /**
     *
     * @param mutableArray
     * @param <T>
     * @return
     */
    public static <T> List<T> immutableList(T[] mutableArray) {
        List<T> copiedList = new ArrayList<>(Arrays.asList(mutableArray));
        return Collections.unmodifiableList(copiedList);
    }

    /**
     * @param a
     * @param b
     * @return
     */
    public static boolean equalsBySerialization(Serializable a, Serializable b) {

        byte[] bytesA = writeToByteArray(a);
        byte[] bytesB = writeToByteArray(b);
        if (bytesA.length != bytesB.length) return false;

        for (int i = 0; i < bytesA.length; i++) {
            if (bytesA[i] != bytesB[i]) return false;
        }

        return true;
    }

    /**
     * @param serializable
     * @return
     */
    public static Serializable cloneBySerialisation(Serializable serializable) {
        byte[] bytes = writeToByteArray(serializable);
        return readFromByteArray(bytes);
    }

    /**
     *
     * @param bytes
     * @return
     */
    private static Serializable readFromByteArray(byte[] bytes) {

        Serializable serializable;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            serializable = (Serializable) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }

        return serializable;
    }

    /**
     *
     * @param serializable
     * @return
     */
    private static byte[] writeToByteArray(Serializable serializable) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutput objectOutput = new ObjectOutputStream(byteArrayOutputStream);
            objectOutput.writeObject(serializable);
            objectOutput.flush();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return byteArrayOutputStream.toByteArray();
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
                if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
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
     */
    public static boolean equal(Object a, Object b) {
        if (null == a || null == b) {
            return null == a && null == b;
        }
        return a.equals(b);
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param reference an object reference
     * @param <T>
     * @return the non-null reference that was checked
     */
    public static <T> T verifyNotNull(T reference, String message) throws NullPointerException {
        if (null == reference) {
            throw new NullPointerException(message);
        }
        return reference;
    }

    /**
     * Convenience method.
     *
     * @param reference an object reference
     * @param <T>
     * @return the non-null reference that was checked
     */
    public static <T> T verifyNotNull(T reference) throws NullPointerException {
        return verifyNotNull(reference, null);
    }

    /**
     * Ensures that all elements of an iterable are not null as well as the iterable itself.
     *
     * @param iterable an iterable reference
     * @param <E> the element type of the iterable
     * @param <T> the iterable type
     * @return the iterable that was checked
     */
    public static <E, T extends Iterable<E>> T verifyNoneNull(T iterable) {
        verifyNotNull(iterable);
        for (E e: iterable) {
            verifyNotNull(e);
        }
        return iterable;
    }

    /**
     * Ensures that a list is unmodifiable (i.e. that nothing can be added).
     *
     * Unmodifiable lists can e.g. be created by Collections.UnmodifiableList().
     *
     * @param list a list
     * @param <T> element type of the list
     * @return the list that is verified to be unmodifiable
     */
    public static <T> List<T> verifyUnmodifiable(List<T> list) {
        try {
            list.addAll(Collections.emptyList());
        } catch (Exception e) {
            return list;
        }
        throw new IllegalArgumentException("List is modifiable.");
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
        return new ImmutableList<>(integers);
    }

    /**
     * Returns the sum of the ints stored in the list.
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
        return new ImmutableList<>(values);
    }

    public static <T extends Serializable> ImmutableList<T> combineTwoImmutableLists(ImmutableList<T> listA, ImmutableList<T> listB) {
        T[] values = (T[]) new Serializable[listA.size() + listB.size()];
        for (int i = 0; i < listA.size(); i++) {
            values[i] = listA.get(i);
        }
        for (int i = 0; i < listB.size(); i++) {
            values[listA.size() + i] = listB.get(i);
        }
        return new ImmutableList<>(values);
    }

    /**
     *
     * @param collection
     * @param <T>
     */
    public static <T> void removeLast(Collection<T> collection) {
        collection.remove(collection.size() - 1);
    }
}