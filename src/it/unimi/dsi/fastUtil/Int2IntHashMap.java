/*
 * fastUtil 1.3: Fast & compact specialized hash-based utility classes for Java
 *
 * Copyright (C) 2002 Sebastiano Vigna
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package it.unimi.dsi.fastUtil;

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;


/** A type-specific hash map with a very fast, small-footprint implementation.
 */
public final class Int2IntHashMap extends Int2IntAbstractMap
    implements Serializable, Cloneable, Hash, Int2IntMap {
    /** The array of keys. */
    private transient int[] m_key;

    /** The array of values. */
    private transient int[] m_value;

    /** The array of occupancy states. */
    private transient byte[] m_state;

    /** The acceptable load factor. */
    private final float m_f;

    /** Index into the prime list, giving the current table size. */
    private int m_p;

    /** Table size. Must be the p-th item of {@link Hash#primes}. */
    private transient int m_n;

    /** Number of entries in the map. */
    private int m_count;

    /** Cached set of entries and keys. */
    private transient volatile Set entries;

    /** Cached set of entries and keys. */
    private transient volatile Set keys;

    /** Cached collection of values. */
    private transient volatile Collection values;

    /**
     * The default return value for <code>get()</code>, <code>put()</code> and
     * <code>remove()</code>.
          */
    private int defRetValue = 0;

    /** Creates a new hash map.
     *
     * The actual table size is the least available prime greater than <code>n</code>/<code>f</code>.
     *
     * @param n the expected number of elements in the hash map.
     * @param f the load factor.
     * @see Hash#primes
     */
    public Int2IntHashMap(int n, float f) {
        if (f <= 0 || f > 1) {
            throw new IllegalArgumentException(
                "Load factor must be greater than 0 and smaller than or equal to 1");
        }

        if (n < 0) {
            throw new IllegalArgumentException(
                "Hash table size must be nonnegative");
        }

        int l = Arrays.binarySearch(primes, (int)(n / f) + 1);

        if (l < 0) {
            l = -l - 1;
        }

        this.m_f = f;
        this.m_n = primes[l];
        m_p = l;
        m_key = new int[this.m_n];
        m_value = new int[this.m_n];
        m_state = new byte[this.m_n];
    }

    /** Creates a new hash map with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor.
     *
     * @param n the expected number of elements in the hash map.
     */
    public Int2IntHashMap(int n) {
        this(n, DEFAULT_LOAD_FACTOR);
    }

    /** Creates a new hash map with {@link Hash#DEFAULT_INITIAL_SIZE} entries
     * and {@link Hash#DEFAULT_LOAD_FACTOR} as load factor.
     */
    public Int2IntHashMap() {
        this(DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR);
    }

    /** Creates a new hash map copying a given one.
     *
     * @param m a {@link Map} to be copied into the new hash map.
     * @param f the load factor.
     */
    public Int2IntHashMap(Map m, float f) {
        this(m.size(), f);
        putAll(m);
    }

    /** Creates a new hash map with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor copying a given one.
     *
     * @param m a {@link Map} to be copied into the new hash map.
     */
    public Int2IntHashMap(Map m) {
        this(m, DEFAULT_LOAD_FACTOR);
    }

    /*
     * The following private methods implements some basic building blocks used by
     * all accessors.  They are (and should be maintained) identical to those used in HashSet.drv.
     */

    /** Searches for a key, keeping track of a possible insertion point.
     *
     * The instance variables used by the search are to be passed as parameters to
     * increase access speed.
     *
     * @param k the key.
     * @param key the key array.
     * @param state the state array.
     * @return the index of the correct insertion point, if the key is not found; otherwise,
     * <var>-i</var>-1, where <var>i</var> is the index of the entry containing the key.
     */
    private int findInsertionPoint(final int k, final int[] key,
        final byte[] state) {
        // First of all, we make the key into a positive integer.
        final int k2i = (k) & 0x7FFFFFFF;

        // The primary hash, a.k.a. starting point.
        int h1 = k2i % m_n;
        final int s = h1;

        // The secondary hash.
        final int h2 = (k2i % (m_n - 2)) + 1;

        while (state[h1] == OCCUPIED && !((key[h1]) == (k))) {
            h1 = (h1 + h2) % m_n; // There's always a non-OCCUPIED entry.
        }

        if (state[h1] == FREE) {
            return h1;
        }

        if (state[h1] == OCCUPIED) {
            return -h1 - 1; // Necessarily, KEY_EQUAL(key[h1], k).
        }

        /* Tables without deletions will never use code beyond this point. */
        final int i = h1; // Remember first available bucket for later.

        /** See the comments in the documentation of the interface Hash. */
        while (state[h1] != FREE && !((key[h1]) == (k))) {
            if ((h1 = (h1 + h2) % m_n) == s) {
                return i;
            }
        }

        return state[h1] == OCCUPIED ? -h1 - 1 : i; // In the first case, necessarily, KEY_EQUAL(key[h1], k).
    }

    /** Searches for a key.
     *
     * The instance variables used by the search are to be passed as parameters to
     * increase access speed.
     *
     * @param k the key.
     * @param key the key array.
     * @param state the state array.
     * @return the index of the entry containing the key, or -1 if the key wasn't found.
     */
    private int findKey(final int k, final int[] key, final byte[] state) {
        // First of all, we make the key into a positive integer.
        final int k2i = (k) & 0x7FFFFFFF;

        // The primary hash, a.k.a. starting point.
        int h1 = k2i % m_n;
        final int s = h1;

        // The secondary hash.
        final int h2 = (k2i % (m_n - 2)) + 1;

        /** See the comments in the documentation of the interface Hash. */
        while (state[h1] != FREE && !((key[h1]) == (k))) {
            if ((h1 = (h1 + h2) % m_n) == s) {
                return -1;
            }
        }

        return state[h1] == OCCUPIED ? h1 : -1; // In the first case, necessarily, KEY_EQUAL(key[h1], k).
    }

    public Object put(final Object ok, final Object ov) {
        final int oldValue;
        final int v = (((Integer)(ov)).intValue());
        final int[] key = this.m_key;
        final int k = (((Integer)(ok)).intValue());
        final byte[] state = this.m_state;

        final int i = findInsertionPoint(k, key, state);

        if (i < 0) {
            oldValue = m_value[-i - 1];
            m_value[-i - 1] = v;

            return (new Integer(oldValue));
        }

        state[i] = OCCUPIED;
        key[i] = k;
        m_value[i] = v;

        if (++m_count >= m_n * m_f) {
            rehash(Math.min(m_p + 16, primes.length - 1)); // Table too filled, let's rehash
        }

        return null;
    }

    public int put(final int k, final int v) {
        final int oldValue;
        final int[] key = this.m_key;
        final byte[] state = this.m_state;

        final int i = findInsertionPoint(k, key, state);

        if (i < 0) {
            oldValue = m_value[-i - 1];
            m_value[-i - 1] = v;

            return oldValue;
        }

        state[i] = OCCUPIED;
        key[i] = k;
        m_value[i] = v;

        if (++m_count >= m_n * m_f) {
            rehash(Math.min(m_p + 16, primes.length - 1)); // Table too filled, let's rehash
        }

        return defRetValue;
    }

    public Object remove(final Object ok) {
        final int k = (((Integer)(ok)).intValue());
        final byte[] state = this.m_state;

        final int i = findKey(k, m_key, state);

        if (i < 0) {
            return null;
        }

        state[i] = REMOVED;

        m_count--;

        return (new Integer(m_value[i]));
    }

    public void setDefRetValue(final int rv) {
        defRetValue = rv;
    }

    public int getDefRetValue() {
        return defRetValue;
    }

    public boolean containsValue(final Object v) {
        return containsValue((((Integer)(v)).intValue()));
    }

    public boolean containsValue(final int v) {
        final int[] value = this.m_value;
        final byte[] state = this.m_state;

        int i = 0;
        int j = m_count;

        while (j-- != 0) {
            while (state[i] != OCCUPIED) {
                i++;
            }

            if (((value[i]) == (v))) {
                return true;
            }

            i++;
        }

        return false;
    }

    public void clear() {
        this.m_count = 0;
        Arrays.fill(m_state, FREE);
        // We null all object entries so that the garbage collector can do its work.
    }

    private static class Entry implements Int2IntMap.Entry {
        int key;
        int value;

        Entry(Object key, Object value) {
            this.key = (((Integer)(key)).intValue());
            this.value = (((Integer)(value)).intValue());
        }

        Entry(int key, int value) {
            this.key = key;
            this.value = value;
        }

        public Object getKey() {
            return (new Integer(key));
        }

        public int getIntKey() {
            return key;
        }

        public Object getValue() {
            return (new Integer(value));
        }

        public int getIntValue() {
            return value;
        }

        public int setValue(final int value) {
            final int oldValue = this.value;
            this.value = value;

            return oldValue;
        }

        public Object setValue(final Object value) {
            return (new Integer(setValue((((Integer)(value)).intValue()))));
        }

        public boolean equals(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }

            Entry e = (Entry)o;

            return ((key) == (e.key)) && ((value) == (e.value));
        }

        public int hashCode() {
            return (key) ^ (value);
        }

        public String toString() {
            return key + "=" + value;
        }
    }

    public Set entrySet() {
        if (entries == null) {
            entries = new AbstractSet() {
                        public Iterator iterator() {
                            return new Iterator() {
                                    int pos = 0;
                                    int last = -1;
                                    int c = m_count;

                                    {
                                        final byte[] state = Int2IntHashMap.this.m_state;
                                        final int n = Int2IntHashMap.this.m_n;

                                        if (c != 0) {
                                            while (pos < n &&
                                                    state[pos] != OCCUPIED) {
                                                pos++;
                                            }
                                        }
                                    }

                                    public boolean hasNext() {
                                        return c != 0 && pos < m_n;
                                    }

                                    public Object next() {
                                        Entry retVal;
                                        final byte[] state = Int2IntHashMap.this.m_state;
                                        final int n = Int2IntHashMap.this.m_n;

                                        if (!hasNext()) {
                                            throw new NoSuchElementException();
                                        }

                                        retVal = new Entry(m_key[last = pos],
                                                m_value[pos]);

                                        if (--c != 0) {
                                            do {
                                                pos++;
                                            } while (pos < n &&
                                                    state[pos] != OCCUPIED);
                                        }

                                        return retVal;
                                    }

                                    public void remove() {
                                        if (last == -1) {
                                            throw new IllegalStateException();
                                        }

                                        m_state[last] = REMOVED;

                                        m_count--;
                                    }
                                };
                        }

                        public boolean contains(Object o) {
                            if (!(o instanceof Map.Entry)) {
                                return false;
                            }

                            Map.Entry e = (Map.Entry)o;
                            Object v = get(e.getKey());

                            return ((v) == (e.getValue()));
                        }

                        public boolean remove(Object o) {
                            if (!(o instanceof Map.Entry)) {
                                return false;
                            }

                            Map.Entry e = (Map.Entry)o;
                            Object v = get(e.getKey());

                            if (((v) == (e.getValue()))) {
                                return remove(e.getKey());
                            }

                            return false;
                        }

                        public int size() {
                            return m_count;
                        }

                        public void clear() {
                            Int2IntHashMap.this.clear();
                        }
                    };
        }

        return entries;
    }

    public Set keySet() {
        if (keys == null) {
            keys = new IntAbstractSet() {
                        public Iterator iterator() {
                            return new IntIterator() {
                                    int pos = 0;
                                    int last = -1;
                                    int c = m_count;

                                    {
                                        final byte[] state = Int2IntHashMap.this.m_state;
                                        final int n = Int2IntHashMap.this.m_n;

                                        if (c != 0) {
                                            while (pos < n &&
                                                    state[pos] != OCCUPIED) {
                                                pos++;
                                            }
                                        }
                                    }

                                    public boolean hasNext() {
                                        return c != 0 && pos < m_n;
                                    }

                                    public int nextInt() {
                                        int retVal;
                                        final byte[] state = Int2IntHashMap.this.m_state;
                                        final int n = Int2IntHashMap.this.m_n;

                                        if (!hasNext()) {
                                            throw new NoSuchElementException();
                                        }

                                        retVal = m_key[last = pos];

                                        if (--c != 0) {
                                            do {
                                                pos++;
                                            } while (pos < n &&
                                                    state[pos] != OCCUPIED);
                                        }

                                        return retVal;
                                    }

                                    public Object next() {
                                        Object retVal;
                                        final byte[] state = Int2IntHashMap.this.m_state;
                                        final int n = Int2IntHashMap.this.m_n;

                                        if (!hasNext()) {
                                            throw new NoSuchElementException();
                                        }

                                        retVal = (new Integer(m_key[last = pos]));

                                        if (--c != 0) {
                                            do {
                                                pos++;
                                            } while (pos < n &&
                                                    state[pos] != OCCUPIED);
                                        }

                                        return retVal;
                                    }

                                    public void remove() {
                                        if (last == -1) {
                                            throw new IllegalStateException();
                                        }

                                        m_state[last] = REMOVED;

                                        m_count--;
                                    }
                                };
                        }

                        public int size() {
                            return m_count;
                        }

                        public boolean contains(int k) {
                            return containsKey(k);
                        }

                        public boolean remove(int k) {
                            int oldCount = m_count;
                            Int2IntHashMap.this.remove(k);

                            return m_count != oldCount;
                        }

                        public boolean contains(Object ok) {
                            return containsKey(ok);
                        }

                        public boolean remove(Object ok) {
                            int oldCount = m_count;
                            Int2IntHashMap.this.remove(ok);

                            return m_count != oldCount;
                        }

                        public void clear() {
                            Int2IntHashMap.this.clear();
                        }
                    };
        }

        return keys;
    }

    public Collection values() {
        if (values == null) {
            values = new IntAbstractCollection() {
                        public Iterator iterator() {
                            return new IntIterator() {
                                    int pos = 0;
                                    int last = -1;
                                    int c = m_count;

                                    {
                                        final byte[] state = Int2IntHashMap.this.m_state;
                                        final int n = Int2IntHashMap.this.m_n;

                                        if (c != 0) {
                                            while (pos < n &&
                                                    state[pos] != OCCUPIED) {
                                                pos++;
                                            }
                                        }
                                    }

                                    public boolean hasNext() {
                                        return c != 0 && pos < m_n;
                                    }

                                    public int nextInt() {
                                        int retVal;
                                        final byte[] state = Int2IntHashMap.this.m_state;
                                        final int n = Int2IntHashMap.this.m_n;

                                        if (!hasNext()) {
                                            throw new NoSuchElementException();
                                        }

                                        retVal = m_value[pos];

                                        if (--c != 0) {
                                            do {
                                                pos++;
                                            } while (pos < n &&
                                                    state[pos] != OCCUPIED);
                                        }

                                        return retVal;
                                    }

                                    public Object next() {
                                        Object retVal;
                                        final byte[] state = Int2IntHashMap.this.m_state;
                                        final int n = Int2IntHashMap.this.m_n;

                                        if (!hasNext()) {
                                            throw new NoSuchElementException();
                                        }

                                        retVal = (new Integer(m_value[pos]));

                                        if (--c != 0) {
                                            do {
                                                pos++;
                                            } while (pos < n &&
                                                    state[pos] != OCCUPIED);
                                        }

                                        return retVal;
                                    }

                                    public void remove() {
                                        throw new UnsupportedOperationException();
                                    }
                                };
                        }

                        public int size() {
                            return m_count;
                        }

                        public boolean contains(Object ok) {
                            return containsValue(ok);
                        }

                        public boolean contains(int v) {
                            return containsValue(v);
                        }

                        public void clear() {
                            Int2IntHashMap.this.clear();
                        }
                    };
        }

        return values;
    }

    /** Rehashes the map, keeping the same size.
     * This method should be called when the map underwent numerous deletions and insertions.
     * In this case, free entries become rare, and unsuccessful searches
     * require probing <em>all</em> entries. For reasonable load factors this method is linear in the number of entries.
     * You will need as much additional free memory as
     * that occupied by the table.
     * @return <code>true</code> if there was enough memory to rehash the map, <code>false</code> otherwise.
     */
    public boolean rehash() {
        try {
            rehash(m_p);
        } catch (OutOfMemoryError cantDoIt) {
            return false;
        }

        return true;
    }

    /** Resizes the map.
     * @param newP the new size as an index in {@link Hash#primes}.
     */
    private void rehash(final int newP) {
        int i = 0;
        int j = m_count;
        int k2i;
        int h1;
        int h2;

        int k;
        int v;

        final int newN = primes[newP];
        final int[] key = this.m_key;
        final int[] newKey = new int[newN];
        final int[] value = this.m_value;
        final int[] newValue = new int[newN];
        final byte[] state = this.m_state;
        final byte[] newState = new byte[newN];

        while (j-- != 0) {
            while (state[i] != OCCUPIED) {
                i++;
            }

            k = key[i];
            v = value[i];
            k2i = (k) & 0x7FFFFFFF;

            h1 = k2i % newN;
            h2 = (k2i % (newN - 2)) + 1;

            while (newState[h1] != FREE) {
                h1 = (h1 + h2) % newN;
            }

            newState[h1] = OCCUPIED;
            newKey[h1] = k;
            newValue[h1] = v;
            i++;
        }

        this.m_n = newN;
        this.m_p = newP;
        this.m_key = newKey;
        this.m_value = newValue;
        this.m_state = newState;
    }

    public boolean containsKey(int k) {
        return findKey(k, m_key, m_state) >= 0;
    }

    public int size() {
        return m_count;
    }

    public boolean isEmpty() {
        return m_count == 0;
    }

    public Object get(final Object ok) {
        final int i = findKey((((Integer)(ok)).intValue()), m_key, m_state);

        return i < 0 ? null : (new Integer(m_value[i]));
    }

    public int get(final int k) {
        final int i = findKey(k, m_key, m_state);

        return i < 0 ? defRetValue : m_value[i];
    }

    /** Removes the entry with the given key from the map.
     *
     * @param k the key.
     * @return the old value, or the default return value if no value was present for the given key.
     */
    public int remove(final int k) {
        final byte[] state = this.m_state;

        final int i = findKey(k, m_key, state);

        if (i < 0) {
            return defRetValue;
        }

        state[i] = REMOVED;

        m_count--;

        return m_value[i];
    }

    public boolean containsKey(final Object ok) {
        return findKey((((Integer)(ok)).intValue()), m_key, m_state) >= 0;
    }

    /** Returns a deep copy of the map.
     *  @return a deep copy of the map.
     */
    public Object clone() {
        Int2IntHashMap c;

        try {
            c = (Int2IntHashMap)super.clone();
        } catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }

        c.m_key = m_key.clone();
        c.m_value = m_value.clone();
        c.m_state = m_state.clone();

        return c;
    }

    /** Returns a hash code for this map.
     *
     * This method overrides the generic method provided by the superclass.
     * Since <code>equals()</code> is not overriden, it is important
     * that the value returned by this method is the same value as
     * the one returned by the overriden method.
     *
     * @return a hash code for this map.
     */
    public int hashCode() {
        int h = 0;
        int i = 0;
        int j = m_count;

        while (j-- != 0) {
            while (m_state[i] != OCCUPIED) {
                i++;
            }

            h += m_key[i];

            h += m_value[i];
            i++;
        }

        return h;
    }

    private void writeObject(java.io.ObjectOutputStream s)
        throws IOException {
        final int[] key = this.m_key;
        final int[] value = this.m_value;
        final byte[] state = this.m_state;
        int i = 0;
        int j = m_count;

        s.defaultWriteObject();

        while (j-- != 0) {
            while (state[i] != OCCUPIED) {
                i++;
            }

            s.writeInt(key[i]);
            s.writeInt(value[i]);
            i++;
        }
    }

    private void readObject(java.io.ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.m_n = primes[m_p];

        this.m_key = new int[m_n];
        this.m_value = new int[m_n];
        this.m_state = new byte[m_n];

        int count = this.m_count;
        this.m_count = 0;

        while (count-- != 0) {
            put(s.readInt(), s.readInt());
        }
    }

    private static void speedTest(int n, float f) {
        int i;
        Int2IntMap m;
        Map t;
        Random r = new Random();
        long v;
        long ct;

        for (int k = 0; k < 10; k++) {
            if (k > 0) {
                System.out.println();
            }

            t = new HashMap(n);
            m = new Int2IntHashMap(n, f);

            /* First of all, we measure the time that is necessary to produce the inputs. */
            v = System.currentTimeMillis();

            for (i = 0; i < n; i++) {
                new Integer(r.nextInt());

                new Integer(r.nextInt());
            }

            ct = System.currentTimeMillis() - v;

            v = System.currentTimeMillis();

            /* Then we add pairs to t. */
            for (i = 0; i < n; i++) {
                t.put(new Integer(r.nextInt()), new Integer(r.nextInt()));
            }

            System.out.println("Added " + n +
                " pairs in old HashMap (actual size: " + t.size() + ") in " +
                (System.currentTimeMillis() - v) / 1000.0 + "s" +
                " (actual time: " +
                (System.currentTimeMillis() - v - ct) / 1000.0 + "s)");

            v = System.currentTimeMillis();

            /* Then we check for pairs in t. */
            for (i = 0; i < n; i++) {
                t.get(new Integer(r.nextInt()));
            }

            System.out.println("Examined " + n + " pairs in old HashMap in " +
                (System.currentTimeMillis() - v) / 1000.0 + "s" +
                " (actual time: " +
                (System.currentTimeMillis() - v - ct) / 1000.0 + "s)");

            v = System.currentTimeMillis();

            /* Again, we measure the time that is necessary to produce the inputs. */
            v = System.currentTimeMillis();

            for (i = 0; i < n; i++) {
                r.nextInt();
            }

            ct = System.currentTimeMillis() - v;

            v = System.currentTimeMillis();

            /* Then we add pairs to m. */
            for (i = 0; i < n; i++) {
                m.put(new Integer(r.nextInt()), new Integer(r.nextInt()));
            }

            System.out.println("Added " + n +
                " pairs in new HashMap (actual size: " + m.size() + ") in " +
                (System.currentTimeMillis() - v) / 1000.0 + "s" +
                " (actual time: " +
                (System.currentTimeMillis() - v - ct) / 1000.0 + "s)");

            v = System.currentTimeMillis();

            /* Then we check for pairs in m. */
            for (i = 0; i < n; i++) {
                m.get(r.nextInt());
            }

            System.out.println("Examined " + n + " pairs in new HashMap in " +
                (System.currentTimeMillis() - v) / 1000.0 + "s" +
                " (actual time: " +
                (System.currentTimeMillis() - v - ct) / 1000.0 + "s)");

            /* Then we enumerate items in t. */
            v = System.currentTimeMillis();

            for (Iterator it = t.keySet().iterator(); it.hasNext();) {
                t.get(it.next());
            }

            System.out.println("Iterated on old HashMap in " +
                (System.currentTimeMillis() - v) / 1000.0 + "s");

            /* Then we enumerate items in m. */
            v = System.currentTimeMillis();

            for (IntIterator it = (IntIterator)m.keySet().iterator();
                    it.hasNext();) {
                m.get(it.nextInt());
            }

            System.out.println("Iterated on new HashMap in " +
                (System.currentTimeMillis() - v) / 1000.0 + "s");
        }
    }

    private static boolean valEquals(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    private static void regressionTest(int n, float f) {
        Int2IntHashMap m = new Int2IntHashMap(Hash.DEFAULT_INITIAL_SIZE, f);
        Map t = new HashMap();
        Random r = new Random();

        /* First of all, we fill t with random data. */
        for (int i = 0; i < n; i++) {
            t.put(new Integer(r.nextInt()), new Integer(r.nextInt()));
        }

        /* Now we add to m the same data */
        m.putAll(t);

        if (!m.equals(t)) {
            System.out.println("Error: !m.equals(t) after insertion.\n");
        }

        if (!t.equals(m)) {
            System.out.println("Error: !t.equals(m) after insertion.\n");
        }

        /* Now we check that m actually holds that data. */
        for (Iterator i = t.entrySet().iterator(); i.hasNext();) {
            java.util.Map.Entry e = (java.util.Map.Entry)i.next();

            if (!valEquals(e.getValue(), m.get(e.getKey()))) {
                System.out.println("Error: m and t differ on an entry (" + e +
                    ") after insertion (iterating on t).\n");
            }
        }

        /* Now we check that m actually holds that data, but iterating on m. */
        for (Iterator i = m.entrySet().iterator(); i.hasNext();) {
            Entry e = (Entry)i.next();

            if (!valEquals(e.getValue(), t.get(e.getKey()))) {
                System.out.println("Error: m and t differ on an entry (" + e +
                    ") after insertion (iterating on m).\n");
            }
        }

        /* Now we check that m actually holds the same keys. */
        for (Iterator i = t.keySet().iterator(); i.hasNext();) {
            Object o = i.next();

            if (!m.containsKey(o)) {
                System.out.println("Error: m and t differ on a key (" + o +
                    ") after insertion (iterating on t).\n");
                System.exit(-1);
            }

            if (!m.keySet().contains(o)) {
                System.out.println("Error: m and t differ on a key (" + o +
                    ", in keySet()) after insertion (iterating on t).\n");
                System.exit(-1);
            }
        }

        /* Now we check that m actually holds the same keys, but iterating on m. */
        for (Iterator i = m.keySet().iterator(); i.hasNext();) {
            Object o = i.next();

            if (!t.containsKey(o)) {
                System.out.println(
                    "Error: m and t differ on a key after insertion (iterating on m).\n");
                System.exit(-1);
            }

            if (!t.keySet().contains(o)) {
                System.out.println(
                    "Error: m and t differ on a key (in keySet()) after insertion (iterating on m).\n");
                System.exit(-1);
            }
        }

        /* Now we check that m actually hold the same values. */
        for (Iterator i = t.values().iterator(); i.hasNext();) {
            Object o = i.next();

            if (!m.containsValue(o)) {
                System.out.println(
                    "Error: m and t differ on a value after insertion (iterating on t).\n");
                System.exit(-1);
            }

            if (!m.values().contains(o)) {
                System.out.println(
                    "Error: m and t differ on a value (in values()) after insertion (iterating on t).\n");
                System.exit(-1);
            }
        }

        /* Now we check that m actually hold the same values, but iterating on m. */
        for (Iterator i = m.values().iterator(); i.hasNext();) {
            Object o = i.next();

            if (!t.containsValue(o)) {
                System.out.println(
                    "Error: m and t differ on a value after insertion (iterating on m).\n");
                System.exit(-1);
            }

            if (!t.values().contains(o)) {
                System.out.println(
                    "Error: m and t differ on a value (in values()) after insertion (iterating on m).\n");
                System.exit(-1);
            }
        }

        /* Now we check that inquiries about random data give the same answer in m and t. For
                m we use the polymorphic method. */
        for (int i = 0; i < n; i++) {
            int T = r.nextInt();

            if (m.containsKey((new Integer(T))) != t.containsKey(
                        (new Integer(T)))) {
                System.out.println(
                    "Error: divergence in keys between t and m (polymorphic method).\n");
                System.exit(-1);
            }

            if ((m.get(T) != 0) != ((t.get((new Integer(T))) == null
                    ? ((int)0) : (((Integer)(t.get((new Integer(T))))).intValue())) != 0) ||
                    t.get((new Integer(T))) != null &&
                    !m.get((new Integer(T))).equals(t.get((new Integer(T))))) {
                System.out.println(
                    "Error: divergence between t and m (polymorphic method).\n");
                System.exit(-1);
            }
        }

        /* Again, we check that inquiries about random data give the same answer in m and t, but
                for m we use the standard method. */
        for (int i = 0; i < n; i++) {
            int T = r.nextInt();

            if (!valEquals(m.get((new Integer(T))), t.get((new Integer(T))))) {
                System.out.println(
                    "Error: divergence between t and m (standard method).\n");
                System.exit(-1);
            }
        }

        /* Now we put and remove random data in m and t, checking that the result is the same. */
        for (int i = 0; i < 20 * n; i++) {
            int T = r.nextInt();
            int U = r.nextInt();

            if (!valEquals(m.put((new Integer(T)), (new Integer(U))),
                        t.put((new Integer(T)), (new Integer(U))))) {
                System.out.println(
                    "Error: divergence in put() between t and m.\n");
                System.exit(-1);
            }

            T = r.nextInt();

            if (!valEquals(m.remove((new Integer(T))),
                        t.remove((new Integer(T))))) {
                System.out.println(
                    "Error: divergence in remove() between t and m.\n");
                System.exit(-1);
            }
        }

        if (!m.equals(t)) {
            System.out.println("Error: !m.equals(t) after removal.\n");
        }

        if (!t.equals(m)) {
            System.out.println("Error: !t.equals(m) after removal.\n");
        }

        /* Now we check that m actually holds the same data. */
        for (Iterator i = t.entrySet().iterator(); i.hasNext();) {
            java.util.Map.Entry e = (java.util.Map.Entry)i.next();

            if (!valEquals(e.getValue(), m.get(e.getKey()))) {
                System.out.println("Error: m and t differ on an entry (" + e +
                    ") after removal (iterating on t).\n");
                System.exit(-1);
            }
        }

        /* Now we check that m actually holds that data, but iterating on m. */
        for (Iterator i = m.entrySet().iterator(); i.hasNext();) {
            Entry e = (Entry)i.next();

            if (!valEquals(e.getValue(), t.get(e.getKey()))) {
                System.out.println("Error: m and t differ on an entry (" + e +
                    ") after removal (iterating on m).\n");
                System.exit(-1);
            }
        }

        /* Now we check that m actually holds the same keys. */
        for (Iterator i = t.keySet().iterator(); i.hasNext();) {
            Object o = i.next();

            if (!m.containsKey(o)) {
                System.out.println("Error: m and t differ on a key (" + o +
                    ") after removal (iterating on t).\n");
                System.exit(-1);
            }

            if (!m.keySet().contains(o)) {
                System.out.println("Error: m and t differ on a key (" + o +
                    ", in keySet()) after removal (iterating on t).\n");
                System.exit(-1);
            }
        }

        /* Now we check that m actually holds the same keys, but iterating on m. */
        for (Iterator i = m.keySet().iterator(); i.hasNext();) {
            Object o = i.next();

            if (!t.containsKey(o)) {
                System.out.println(
                    "Error: m and t differ on a key after removal (iterating on m).\n");
                System.exit(-1);
            }

            if (!t.keySet().contains(o)) {
                System.out.println(
                    "Error: m and t differ on a key (in keySet()) after removal (iterating on m).\n");
                System.exit(-1);
            }
        }

        /* Now we check that m actually hold the same values. */
        for (Iterator i = t.values().iterator(); i.hasNext();) {
            Object o = i.next();

            if (!m.containsValue(o)) {
                System.out.println(
                    "Error: m and t differ on a value after removal (iterating on t).\n");
                System.exit(-1);
            }

            if (!m.values().contains(o)) {
                System.out.println(
                    "Error: m and t differ on a value (in values()) after removal (iterating on t).\n");
                System.exit(-1);
            }
        }

        /* Now we check that m actually hold the same values, but iterating on m. */
        for (Iterator i = m.values().iterator(); i.hasNext();) {
            Object o = i.next();

            if (!t.containsValue(o)) {
                System.out.println(
                    "Error: m and t differ on a value after removal (iterating on m).\n");
                System.exit(-1);
            }

            if (!t.values().contains(o)) {
                System.out.println(
                    "Error: m and t differ on a value (in values()) after removal (iterating on m).\n");
                System.exit(-1);
            }
        }

        int h = m.hashCode();

        /* Now we save and read m. */
        try {
            java.io.File ff = new java.io.File("regressionTest");
            java.io.OutputStream os = new java.io.FileOutputStream(ff);
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(os);

            oos.writeObject(m);
            oos.close();

            java.io.InputStream is = new java.io.FileInputStream(ff);
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(is);

            m = (Int2IntHashMap)ois.readObject();
            ois.close();
            ff.delete();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        if (m.hashCode() != h) {
            System.out.println("Error: hashCode() changed after save/read.\n");
        }

        /* Now we check that m actually holds that data. */
        for (Iterator i = t.keySet().iterator(); i.hasNext();) {
            Object o = i.next();

            if (!valEquals(m.get(o), t.get(o))) {
                System.out.println(
                    "Error: m and t differ on an entry after save/read.\n");
                System.exit(-1);
            }
        }

        /* Now we take out of m everything, and check that it is empty. */
        for (Iterator i = t.keySet().iterator(); i.hasNext();) {
            m.remove(i.next());
        }

        if (!m.isEmpty()) {
            System.out.println("Error: m is not empty (as it should be).\n");
            System.exit(-1);
        }

        m = new Int2IntHashMap(n, f);
        t.clear();

        int x;

        /* Now we torture-test the hash table. This part is implemented only for integers and longs. */
        int p = m.m_n;

        for (int i = 0; i < p; i++) {
            for (int j = 0; j < 20; j++) {
                m.put(i + (r.nextInt() % 10) * p, 1);
                m.remove(i + (r.nextInt() % 10) * p);
            }

            for (int j = -10; j < 10; j++) {
                m.remove(i + j * p);
            }
        }

        t.putAll(m);

        /* Now all table entries are REMOVED. */
        for (int i = 0; i < (p * f) / 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (!valEquals(m.put((new Integer(
                                    x = i + (r.nextInt() % 10) * p)),
                                (new Integer(1))),
                            t.put((new Integer(x)), (new Integer(1))))) {
                    System.out.println(
                        "Error: m and t differ on an entry during torture-test insertion.");
                }
            }
        }

        if (!m.equals(t)) {
            System.out.println(
                "Error: !m.equals(t) after torture-test insertion.\n");
        }

        if (!t.equals(m)) {
            System.out.println(
                "Error: !t.equals(m) after torture-test insertion.\n");
        }

        for (int i = 0; i < p / 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (!valEquals(m.remove(
                                (new Integer(x = i + (r.nextInt() % 10) * p))),
                            t.remove((new Integer(x))))) {
                    System.out.println(
                        "Error: m and t differ on an entry during torture-test removal.");
                }
            }
        }

        if (!m.equals(t)) {
            System.out.println(
                "Error: !m.equals(t) after torture-test removal.\n");
        }

        if (!t.equals(m)) {
            System.out.println(
                "Error: !t.equals(m) after torture-test removal.\n");
        }

        if (!m.equals(m.clone())) {
            System.out.println(
                "Error: !m.equals(m.clone()) after torture-test removal.\n");
        }

        if (!((Int2IntHashMap)m.clone()).equals(m)) {
            System.out.println(
                "Error: !m.clone().equals(m) after torture-test removal.\n");
        }

        m.rehash();

        if (!m.equals(t)) {
            System.out.println("Error: !m.equals(t) after rehash().\n");
        }

        if (!t.equals(m)) {
            System.out.println("Error: !t.equals(m) after rehash().\n");
        }

        System.out.println("Regression test OK.\n");
        System.exit(0);
    }

    public static void main(String[] args) {
        float f = Hash.DEFAULT_LOAD_FACTOR;
        int n = Integer.parseInt(args[1]);

        if (args.length > 2) {
            f = Float.parseFloat(args[2]);
        }

        if (args[0].equals("speedTest")) {
            speedTest(n, f);
        } else if (args[0].equals("regressionTest")) {
            regressionTest(n, f);
        }
    }
}

// Local Variables:
// mode: jde
// End:
