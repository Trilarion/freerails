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

import java.io.Serializable;
import java.util.*;

/**
 * Immutable implementation of the List interface. Implements all non-mutating list
 * operations and permits all elements, including null.
 *
 * All unsupported (mutating) methods throw an UnsupportedOperationException.
 *
 * Based on java.util.ArrayList without mutable parts.
 */
public final class ImmutableList<E> implements List<E>, RandomAccess, Serializable {

    private static final long serialVersionUID = 7783795429456000765L;
    private static final String MUTATION_ERROR_MESSAGE = "Tried to mutate an immutable list.";
    /**
     * The underlying array.
     */
    private final E[] values;

    /**
     * Constructs an immutable list containing the elements.
     * @param items
     */
    @SafeVarargs
    public ImmutableList(E... items) {
        values = items.clone();
    }

    /**
     * Constructs an immutable list containing the elements of the specified
     * collection, in the order they are returned by the collection's iterator.
     *
     * @param c the collection whose elements are to be placed in this immutable list
     */
    public ImmutableList(Collection<? extends E> c) {
        values = c.toArray((E[])new Object[c.size()]);
    }

    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list
     */
    @Override
    public int size() {
        return values.length;
    }

    /**
     * Returns {@code true} if this list contains no elements.
     *
     * @return {@code true} if this list contains no elements
     */
    @Override
    public boolean isEmpty() {
        return values.length == 0;
    }

    /**
     * Returns {@code true} if this list contains the specified element.
     *
     * @param o element whose presence in this list is to be tested
     * @return {@code true} if this list contains the specified element
     */
    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    /**
     * Returns the index of the first occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     */
    @Override
    public int indexOf(Object o) {
        if (o == null) {
            for (int i = 0; i < values.length; i++)
                if (values[i]==null)
                    return i;
        } else {
            for (int i = 0; i < values.length; i++)
                if (o.equals(values[i]))
                    return i;
        }
        return -1;
    }

    /**
     * Returns the index of the last occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     */
    @Override
    public int lastIndexOf(Object o) {
        if (o == null) {
            for (int i = values.length-1; i >= 0; i--)
                if (values[i]==null)
                    return i;
        } else {
            for (int i = values.length-1; i >= 0; i--)
                if (o.equals(values[i]))
                    return i;
        }
        return -1;
    }

    /**
     * Returns an array containing all of the elements in this list
     * in proper sequence (from first to last element).
     *
     * No references to it are maintained by this list. (In other words,
     * this method must allocate a new array).
     *
     * @return an array containing all of the elements in this list in
     *         proper sequence
     */
    @Override
    public Object[] toArray() {
        return values.clone();
    }

    /**
     *
     * @param a
     * @param <T>
     * @return
     */
    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length < values.length) {
            // Make a new array of a's runtime type, but my contents:
            return (T[]) Arrays.copyOf(values, values.length, a.getClass());
        }
        System.arraycopy(values, 0, a, 0, values.length);
        return a;
    }

    /**
     * Returns the element at the specified position in this list.
     *
     * @param  index index of the element to return
     * @return the element at the specified position in this list
     */
    public E get(int index) {
        return values[index];
    }

    /**
     * Replacing an element at a specified position in this immutable list
     * is not allowed.
     *
     * Calling this method will throw an UnsupportedOperationException
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException(MUTATION_ERROR_MESSAGE);
    }

    /**
     * Adding an element to this immutable list is not allowed.
     *
     * Calling this method will throw an UnsupportedOperationException
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException(MUTATION_ERROR_MESSAGE);
    }

    /**
     * Adding an element at a specified position to this immutable list
     * is not allowed.
     *
     * Calling this method will throw an UnsupportedOperationException
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException(MUTATION_ERROR_MESSAGE);
    }

    /**
     *
     * @param index
     * @return
     */
    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException(MUTATION_ERROR_MESSAGE);
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException(MUTATION_ERROR_MESSAGE);
    }

    /**
     *
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException(MUTATION_ERROR_MESSAGE);
    }

    /**
     *
     * @param c
     * @return
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException(MUTATION_ERROR_MESSAGE);
    }

    /**
     *
     * @param index
     * @param c
     * @return
     */
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException(MUTATION_ERROR_MESSAGE);
    }

    /**
     *
     * @param c
     * @return
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException(MUTATION_ERROR_MESSAGE);
    }

    /**
     *
     * @param c
     * @return
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException(MUTATION_ERROR_MESSAGE);
    }

    /**
     *
     * Similar to containsAll() in java.util.AbstractCollection.
     *
     * @param c
     * @return
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object e : c)
            if (!contains(e))
                return false;
        return true;
    }

    /**
     * Read only ListIterator without any checks. Works because list is immutable.
     * @param index
     * @return
     */
    @Override
    public ListIterator<E> listIterator(int index) {
        return new ListIterator<E>() {

            private int idx = index;

            @Override
            public boolean hasNext() {
                return idx < values.length;
            }

            @Override
            public E next() {
                return values[idx++];
            }

            @Override
            public boolean hasPrevious() {
                return idx > 0;
            }

            @Override
            public E previous() {
                return values[idx--];
            }

            @Override
            public int nextIndex() {
                if (hasNext()) {
                    return idx++;
                }
                return values.length;
            }

            @Override
            public int previousIndex() {
                if (hasPrevious()) {
                    return idx--;
                }
                return -1;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException(MUTATION_ERROR_MESSAGE);
            }

            @Override
            public void set(E e) {
                throw new UnsupportedOperationException(MUTATION_ERROR_MESSAGE);
            }

            @Override
            public void add(E e) {
                throw new UnsupportedOperationException(MUTATION_ERROR_MESSAGE);
            }
        };
    }

    /**
     *
     * @return
     */
    @Override
    public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    /**
     * Read-only iterator, no checks. Works here, because the list is immutable.
     *
     * @return
     */
    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {

            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < values.length;
            }

            @Override
            public E next() {
                return values[index++];
            }
        };
    }

    /**
     *
     * @param fromIndex
     * @param toIndex
     * @return
     */
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return null;
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ImmutableList)) return false;
        final ImmutableList other = (ImmutableList) obj;
        return Arrays.equals(values, other.values);
    }

    /**
     * Hashcode is based on the contents of this list.
     *
     * @return
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }
}
