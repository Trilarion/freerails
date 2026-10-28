/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * General-purpose collection, serialization, validation, and comparison
 * helpers.
 */
public final class Utils {

    /**
     * Prevents instantiation of this utility class.
     */
    private Utils() {
    }

    /**
     * Copies a list and returns an unmodifiable view of the copy.
     *
     * @param mutableList list whose elements are copied
     * @param <T> element type
     * @return an unmodifiable list containing the elements in iteration order
     */
    public static <T> List<T> immutableList(List<T> mutableList) {
        List<T> copiedList = new ArrayList<>(mutableList);
        return Collections.unmodifiableList(copiedList);
    }

    /**
     * Copies an array into an unmodifiable list.
     *
     * @param mutableArray array whose elements are copied
     * @param <T> element type
     * @return an unmodifiable list containing the array elements in order
     */
    public static <T> List<T> immutableList(T[] mutableArray) {
        List<T> copiedList = new ArrayList<>(Arrays.asList(mutableArray));
        return Collections.unmodifiableList(copiedList);
    }

    /**
     * Compares two serializable objects by comparing their serialized byte
     * representations.
     *
     * @param a first object to serialize
     * @param b second object to serialize
     * @return {@code true} if the serialized byte arrays are identical
     * @throws IllegalStateException if either object cannot be serialized
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
     * Creates a deep copy of an object by serializing and deserializing it.
     *
     * @param serializable object to copy
     * @return the deserialized copy
     * @throws IllegalStateException if serialization or deserialization fails
     */
    public static Serializable cloneBySerialisation(Serializable serializable) {
        byte[] bytes = writeToByteArray(serializable);
        return readFromByteArray(bytes);
    }

    /**
     * Deserializes an object from a byte array.
     *
     * @param bytes serialized object data
     * @return the deserialized object
     * @throws IllegalStateException if the data cannot be deserialized
     */
    private static Serializable readFromByteArray(byte[] bytes) {
        try (ObjectInputStream objectInputStream =
                     new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (Serializable) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Serializes an object into a byte array.
     *
     * @param serializable object to serialize
     * @return its serialized representation
     * @throws IllegalStateException if the object cannot be serialized
     */
    private static byte[] writeToByteArray(Serializable serializable) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutput = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutput.writeObject(serializable);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Finds the name of the first public static field whose value equals the
     * supplied object.
     *
     * @param o object to match against public static fields of its class
     * @return the matching field name, or {@code null} if there is no match
     * @throws NullPointerException if {@code o} is null
     * @throws IllegalStateException if a matching field cannot be accessed
     */
    public static String findConstantFieldName(Object o) {
        Field[] fields = o.getClass().getFields();
        for (Field field : fields) {
            int modifiers = field.getModifiers();

            try {
                if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
                    Object value = field.get(null);
                    if (Objects.equals(value, o)) {
                        return field.getName();
                    }
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Could not read public field " + field.getName(), e);
            }
        }

        return null;
    }

    /**
     * Compares two objects, treating two {@code null} references as equal.
     *
     * @param a first object
     * @param b second object
     * @return {@code true} if both are null or {@link Object#equals(Object)}
     * reports them equal
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
     * @param message message to include in the exception if the reference is null
     * @param <T> referenced value type
     * @return the non-null reference that was checked
     * @throws NullPointerException if {@code reference} is null
     */
    public static <T> T verifyNotNull(T reference, String message) throws NullPointerException {
        if (null == reference) {
            throw new NullPointerException(message);
        }
        return reference;
    }

    /**
     * Checks that an object reference is not null.
     *
     * @param reference an object reference
     * @param <T> referenced value type
     * @return the non-null reference that was checked
     * @throws NullPointerException if {@code reference} is null
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
     * @throws NullPointerException if the iterable or any of its elements is null
     */
    public static <E, T extends Iterable<E>> T verifyNoneNull(T iterable) {
        verifyNotNull(iterable);
        for (E e: iterable) {
            verifyNotNull(e);
        }
        return iterable;
    }

    /**
     * Returns an unmodifiable defensive copy of a list.
     *
     * @param list a list
     * @param <T> element type of the list
     * @return an unmodifiable copy containing the same elements
     */
    public static <T> List<T> verifyUnmodifiable(List<T> list) {
        return List.copyOf(list);
    }

    /**
     * Converts boolean values to integers, using {@code 1} for true and
     * {@code 0} for false.
     *
     * @param values boolean values to convert
     * @return a mutable list containing one integer for each input value
     */
    public static List<Integer> integerListFromBoolean(boolean... values) {
        List<Integer> integers = new ArrayList<>(values.length);
        for (boolean value : values) {
            integers.add(value ? 1 : 0);
        }
        return integers;
    }

    /**
     * Returns the sum of the integers in a list.
     *
     * @param list integers to sum
     * @return the sum of the list's values
     * @throws NullPointerException if the list or one of its elements is null
     */
    public static int sumOfIntegerList(List<Integer> list) {
        int sum = 0;
        for (Integer value : list) {
            sum += value;
        }
        return sum;
    }

    /**
     * Returns a copy of a list with its final element removed.
     *
     * @param list list to copy
     * @param <T> element type
     * @return a new list containing all but the last element
     * @throws IndexOutOfBoundsException if {@code list} is empty
     */
    public static <T> List<T> removeLastOfList(List<T> list) {
        List<T> values = new ArrayList<>(list);
        if (values.isEmpty()) {
            throw new IndexOutOfBoundsException("List is empty");
        }
        values.remove(values.size() - 1);
        return values;
    }

    /**
     * Returns a new list containing the elements of two lists in sequence.
     *
     * @param listA elements to place first
     * @param listB elements to place after {@code listA}
     * @param <T> element type
     * @return a mutable list containing both input lists' elements
     */
    public static <T> List<T> combineTwoLists(List<T> listA, List<T> listB) {
        List<T> values = new ArrayList<>(listA.size() + listB.size());
        values.addAll(listA);
        values.addAll(listB);
        return values;
    }

    /**
     * Removes the last element from a list.
     *
     * @param list list from which to remove the last element
     * @param <T> element type
     * @throws IndexOutOfBoundsException if the list is empty
     * @throws UnsupportedOperationException if the list does not support removal
     */
    public static <T> void removeLast(List<T> list) {
        if (list.isEmpty()) {
            throw new IndexOutOfBoundsException("List is empty");
        }
        list.remove(list.size() - 1);
    }

    /**
     * Creates a mutable map associating each key with the same value.
     *
     * @param keys keys to include in the map
     * @param value value to associate with every key
     * @param <K> key type
     * @param <V> value type
     * @return a new map initialized with the supplied keys
     * @throws NullPointerException if {@code keys} is null
     */
    public static <K, V> Map<K, V> initializedMap(Collection<K> keys, V value) {
        verifyNotNull(keys);
        Map<K, V> map = new HashMap<>();
        for (K key: keys) {
            map.put(key, value);
        }
        return map;
    }

    /**
     * Tests whether a collection contains an object with the given identifier.
     *
     * @param id identifier to find
     * @param c collection to search
     * @param <E> identifiable element type
     * @return {@code true} if an element has the specified identifier
     * @throws NullPointerException if the collection is null
     */
    public static <E extends Identifiable> boolean containsId(final int id, @NotNull final Collection<E> c) {
        for (E e: c) {
            if (e.getId() == id) {
                return true;
            }
        }
        return false;
    }
}