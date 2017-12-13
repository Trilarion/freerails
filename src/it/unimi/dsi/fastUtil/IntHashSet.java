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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;


/**  A type-specific hash set with with a very fast, small-footprint implementation.
 *
 *
 */
public final class IntHashSet extends IntAbstractSet implements Serializable,
    Cloneable, Hash, IntSet {
    /** The array of keys. */
    private transient int[] key;

    /** The array of occupancy states. */
    private transient byte[] state;

    /** The acceptable load factor. */
    private final float f;

    /** Index into the prime list, giving the current table size. */
    private int p;

    /** Table size. Must be the p-th item of {@link Hash#primes}. */
    private transient int n;

    /** Number of entries in the set. */
    private int count;

    /** Creates a new hash set.
     *
     * The actual table size is the least available prime greater than <code>n</code>/<code>f</code>.
     *
     * @param n the expected number of elements in the hash set.
     * @param f the load factor.
     * @see Hash#primes
     */
    public IntHashSet(int n, float f) {
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

        //System.err.println("Allocation dimension: " + primes[l]);
        this.f = f;
        this.n = primes[l];
        p = l;
        key = new int[this.n];
        state = new byte[this.n];
    }

    /** Creates a new hash set with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor.
     *
     * @param n the expected number of elements in the hash set.
     */
    public IntHashSet(int n) {
        this(n, DEFAULT_LOAD_FACTOR);
    }

    /** Creates a new hash set with {@link Hash#DEFAULT_INITIAL_SIZE} elements
     * and {@link Hash#DEFAULT_LOAD_FACTOR} as load factor.
     */
    public IntHashSet() {
        this(DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR);
    }

    /** Creates a new hash set copying a given collection.
     *
     * @param c a {@link Collection} to be copied into the new hash set.
     * @param f the load factor.
     */
    public IntHashSet(Collection c, float f) {
        this(c.size(), f);
        addAll(c);
    }

    /** Creates a new hash set  with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor
     * copying a given collection.
     *
     * @param c a {@link Collection} to be copied into the new hash set.
     */
    public IntHashSet(Collection c) {
        this(c, DEFAULT_LOAD_FACTOR);
    }

    /** Creates a new set copying the elements of an array and
     *
     * @param a an array to be copied into the new hash set.
     * @param f the load factor.
     */
    public IntHashSet(int[] a, float f) {
        this(a.length, f);

        int i = a.length;

        while (i-- != 0) {
            add(a[i]);
        }
    }

    /** Creates a new hash set with {@link Hash#DEFAULT_LOAD_FACTOR} as load factor
     * copying the elements of an array.
     *
     * @param a an array to be copied into the new hash set.
     */
    public IntHashSet(int[] a) {
        this(a, DEFAULT_LOAD_FACTOR);
    }

    /*
     * The following private methods implements some basic building blocks used by
     * all accessors. They are (and should be maintained) identical to those used in HashMap.drv.
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
        final int k2i = ((int)(k)) & 0x7FFFFFFF;

        // The primary hash, a.k.a. starting point.
        int h1 = k2i % n;
        final int s = h1;

        // The secondary hash.
        final int h2 = (k2i % (n - 2)) + 1;

        while (state[h1] == OCCUPIED && !((key[h1]) == (k))) {
            h1 = (h1 + h2) % n; // There's always a non-OCCUPIED entry.
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
            if ((h1 = (h1 + h2) % n) == s) {
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
        final int k2i = ((int)(k)) & 0x7FFFFFFF;

        // The primary hash, a.k.a. starting point.
        int h1 = k2i % n;
        final int s = h1;

        // The secondary hash.
        final int h2 = (k2i % (n - 2)) + 1;

        /** See the comments in the documentation of the interface Hash. */
        while (state[h1] != FREE && !((key[h1]) == (k))) {
            if ((h1 = (h1 + h2) % n) == s) {
                return -1;
            }
        }

        return state[h1] == OCCUPIED ? h1 : -1; // In the first case, necessarily, KEY_EQUAL(key[h1], k).
    }

    public boolean add(final Object ok) {
        final int[] key = this.key;
        final int k = (((Integer)(ok)).intValue());
        final byte[] state = this.state;

        final int i = findInsertionPoint(k, key, state);

        if (i < 0) {
            return false;
        }

        state[i] = OCCUPIED;
        key[i] = k;

        if (++count >= n * f) {
            rehash(Math.min(p + 16, primes.length - 1)); // Table too filled, let's rehash
        }

        return true;
    }

    public boolean add(final int k) {
        final int[] key = this.key;
        final byte[] state = this.state;

        final int i = findInsertionPoint(k, key, state);

        if (i < 0) {
            return false;
        }

        state[i] = OCCUPIED;
        key[i] = k;

        if (++count >= n * f) {
            rehash(Math.min(p + 16, primes.length - 1)); // Table too filled, let's rehash
        }

        return true;
    }

    public boolean remove(final Object ok) {
        final byte[] state = this.state;

        final int i = findKey((((Integer)(ok)).intValue()), key, state);

        if (i < 0) {
            return false;
        }

        state[i] = REMOVED;

        count--;

        return true;
    }

    public void clear() {
        this.count = 0;
        Arrays.fill(state, FREE);
    }

    /* We override the method in the type-specific {@link AbstractCollection}
            with a faster version without iterators. */
    public int[] toIntArray(final int[] a) {
        final int[] key = this.key;
        final int[] result;
        final byte[] state = this.state;
        int i;
        int j;
        int pos = 0;

        if (a == null || a.length < count) {
            result = new int[count];
        } else {
            result = a;
        }

        i = count;
        j = 0;

        while (i-- != 0) {
            while (state[pos] != OCCUPIED) {
                pos++;
            }

            result[j++] = key[pos++];
        }

        return result;
    }

    /** Returns an iterator on this set.
     * The iterator can be safely cast to a type-specific iterator.
     * @return a type-specific iterator.
     */
    public Iterator iterator() {
        return new IntIterator() {
                int pos = 0;
                int last = -1;
                int c = count;

                {
                    final byte[] state = IntHashSet.this.state;
                    final int n = IntHashSet.this.n;

                    if (c != 0) {
                        while (pos < n && state[pos] != OCCUPIED) {
                            pos++;
                        }
                    }
                }

                public boolean hasNext() {
                    return c != 0 && pos < n;
                }

                public int nextInt() {
                    int retVal;
                    final byte[] state = IntHashSet.this.state;
                    final int n = IntHashSet.this.n;

                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }

                    retVal = key[last = pos];

                    if (--c != 0) {
                        do {
                            pos++;
                        } while (pos < n && state[pos] != OCCUPIED);
                    }

                    return retVal;
                }

                public Object next() {
                    Object retVal;
                    final byte[] state = IntHashSet.this.state;
                    final int n = IntHashSet.this.n;

                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }

                    retVal = (new Integer(key[last = pos]));

                    if (--c != 0) {
                        do {
                            pos++;
                        } while (pos < n && state[pos] != OCCUPIED);
                    }

                    return retVal;
                }

                public void remove() {
                    if (last == -1) {
                        throw new IllegalStateException();
                    }

                    state[last] = REMOVED;

                    count--;
                }
            };
    }

    /** Rehashes the set, keeping the same size.
     * This method should be called when the set underwent numerous deletions and insertions.
     * In this case, free entries become rare, and unsuccessful searches
     * require probing <em>all</em> entries.  For reasonable load factors this method is linear in the number of entries.
     * You will need as much additional free memory as
     * that occupied by the table.
     * @return <code>true</code> if there was enough memory to rehash the set, <code>false</code> otherwise.
     */
    public boolean rehash() {
        try {
            rehash(p);
        } catch (OutOfMemoryError cantDoIt) {
            return false;
        }

        return true;
    }

    /** Resizes the set.
     * @param newP the new size as an index in {@link Hash#primes}.
     */
    private void rehash(final int newP) {
        int i = 0;
        int j = count;
        int k2i;
        int h1;
        int h2;

        //System.err.println("Rehashing to size " +  primes[newP] + "...");
        int k;

        final int newN = primes[newP];
        final int[] key = this.key;
        final int[] newKey = new int[newN];
        final byte[] state = this.state;
        final byte[] newState = new byte[newN];

        while (j-- != 0) {
            while (state[i] != OCCUPIED) {
                i++;
            }

            k = key[i];
            k2i = ((int)(k)) & 0x7FFFFFFF;

            h1 = k2i % newN;
            h2 = (k2i % (newN - 2)) + 1;

            while (newState[h1] != FREE) {
                h1 = (h1 + h2) % newN;
            }

            newState[h1] = OCCUPIED;
            newKey[h1] = k;
            i++;
        }

        this.n = newN;
        this.p = newP;
        this.key = newKey;
        this.state = newState;
    }

    public int size() {
        return count;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public boolean contains(final Object ok) {
        return findKey((((Integer)(ok)).intValue()), key, state) >= 0;
    }

    public boolean contains(final int k) {
        return findKey(k, key, state) >= 0;
    }

    public boolean remove(final int k) {
        final int i = findKey(k, key, state);

        if (i < 0) {
            return false;
        }

        state[i] = REMOVED;

        count--;

        return true;
    }

    /** Returns a deep copy of the set.
     *  @return a deep copy of the set.
     */
    public Object clone() {
        IntHashSet c;

        try {
            c = (IntHashSet)super.clone();
        } catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }

        c.key = (int[])key.clone();
        c.state = (byte[])state.clone();

        return c;
    }

    /** Returns a hash code for this set.
     *
     * This method overrides the generic method provided by the superclass.
     * Since <code>equals()</code> is not overriden, it is important
     * that the value returned by this method is the same value as
     * the one returned by the overriden method.
     *
     * @return a hash code for this set.
     */
    public int hashCode() {
        int h = 0;
        int i = 0;
        int j = count;

        while (j-- != 0) {
            while (state[i] != OCCUPIED) {
                i++;
            }

            h += ((int)(key[i]));
            i++;
        }

        return h;
    }

    private void writeObject(java.io.ObjectOutputStream s)
        throws IOException {
        final int[] key = this.key;
        final byte[] state = this.state;
        int i = 0;
        int j = count;

        s.defaultWriteObject();

        while (j-- != 0) {
            while (state[i] != OCCUPIED) {
                i++;
            }

            s.writeInt(key[i]);
            i++;
        }
    }

    private void readObject(java.io.ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.n = primes[p];

        this.key = new int[n];
        this.state = new byte[n];

        int count = this.count;
        this.count = 0;

        while (count-- != 0) {
            add(s.readInt());
        }
    }

    private static void speedTest(int n, float f) {
        int i;
        IntSet m;
        Set t;
        Random r = new Random();
        Object o;
        long v;
        long fm;
        long ct;

        for (int k = 0; k < 10; k++) {
            if (k > 0) {
                System.out.println();
            }

            t = new HashSet(n);
            m = new IntHashSet(n, f);

            /* First of all, we measure the time that is necessary to produce the inputs. */
            v = System.currentTimeMillis();

            for (i = 0; i < n; i++) {
                new Integer(r.nextInt());
            }

            ct = System.currentTimeMillis() - v;

            v = System.currentTimeMillis();

            /* Then we add elements to t. */
            for (i = 0; i < n; i++) {
                t.add(new Integer(r.nextInt()));
            }

            System.out.println("Added " + n +
                " elements in old HashSet (actual size: " + t.size() + ") in " +
                (System.currentTimeMillis() - v) / 1000.0 + "s" +
                " (actual time: " +
                (System.currentTimeMillis() - v - ct) / 1000.0 + "s)");

            v = System.currentTimeMillis();

            /* Then we check for elements in t. */
            for (i = 0; i < n; i++) {
                t.contains(new Integer(r.nextInt()));
            }

            System.out.println("Examined " + n +
                " elements in old HashSet in " +
                (System.currentTimeMillis() - v) / 1000.0 + "s" +
                " (actual time: " +
                (System.currentTimeMillis() - v - ct) / 1000.0 + "s)");

            /* Again, we measure the time that is necessary to produce the inputs. */
            v = System.currentTimeMillis();

            for (i = 0; i < n; i++) {
                r.nextInt();
            }

            ct = System.currentTimeMillis() - v;

            v = System.currentTimeMillis();

            /* Then we add elements to m. */
            for (i = 0; i < n; i++) {
                m.add(new Integer(r.nextInt()));
            }

            System.out.println("Added " + n +
                " elements in new HashSet (actual size: " + m.size() + ") in " +
                (System.currentTimeMillis() - v) / 1000.0 + "s" +
                " (actual time: " +
                (System.currentTimeMillis() - v - ct) / 1000.0 + "s)");

            v = System.currentTimeMillis();

            /* Then we check for elements in m. */
            for (i = 0; i < n; i++) {
                m.contains(r.nextInt());
            }

            System.out.println("Examined " + n +
                " elements in new HashSet in " +
                (System.currentTimeMillis() - v) / 1000.0 + "s" +
                " (actual time: " +
                (System.currentTimeMillis() - v - ct) / 1000.0 + "s)");

            /* Then we enumerate items in t. */
            v = System.currentTimeMillis();

            for (Iterator it = t.iterator(); it.hasNext();) {
                it.next();
            }

            System.out.println("Iterated on old HashSet in " +
                (System.currentTimeMillis() - v) / 1000.0 + "s");

            /* Then we enumerate items in m. */
            v = System.currentTimeMillis();

            for (IntIterator it = (IntIterator)m.iterator(); it.hasNext();) {
                it.nextInt();
            }

            System.out.println("Iterated on new HashSet in " +
                (System.currentTimeMillis() - v) / 1000.0 + "s");
        }
    }

    private static void regressionTest(int n, float f) {
        IntHashSet m = new IntHashSet(Hash.DEFAULT_INITIAL_SIZE, f);
        Set t = new HashSet();
        Random r = new Random();

        /* First of all, we fill t with random data. */
        for (int i = 0; i < n; i++) {
            t.add(new Integer(r.nextInt()));
        }

        /* Now we add to m the same data */
        m.addAll(t);

        if (!m.equals(t)) {
            System.out.println("Error: !m.equals(t) after insertion.\n");
        }

        if (!t.equals(m)) {
            System.out.println("Error: !t.equals(m) after insertion.\n");
        }

        /* Now we check that m actually holds that data. */
        for (Iterator i = t.iterator(); i.hasNext();) {
            Object e = i.next();

            if (!m.contains(e)) {
                System.out.println("Error: m and t differ on a key (" + e +
                    ") after insertion (iterating on t).\n");
                System.exit(-1);
            }
        }

        /* Now we check that m actually holds that data, but iterating on m. */
        for (Iterator i = m.iterator(); i.hasNext();) {
            Object e = i.next();

            if (!t.contains(e)) {
                System.out.println("Error: m and t differ on a key (" + e +
                    ") after insertion (iterating on m).\n");
                System.exit(-1);
            }
        }

        /* Now we check that inquiries about random data give the same answer in m and t. For
                m we use the polymorphic method. */
        for (int i = 0; i < n; i++) {
            int T = r.nextInt();

            if (m.contains(T) != t.contains((new Integer(T)))) {
                System.out.println(
                    "Error: divergence in keys between t and m (polymorphic method).\n");
                System.exit(-1);
            }
        }

        /* Again, we check that inquiries about random data give the same answer in m and t, but
                for m we use the standard method. */
        for (int i = 0; i < n; i++) {
            int T = r.nextInt();

            if (m.contains((new Integer(T))) != t.contains((new Integer(T)))) {
                System.out.println(
                    "Error: divergence between t and m (standard method).\n");
                System.exit(-1);
            }
        }

        /* Now we put and remove random data in m and t, checking that the result is the same. */
        for (int i = 0; i < 20 * n; i++) {
            int T = r.nextInt();

            if (m.add((new Integer(T))) != t.add((new Integer(T)))) {
                System.out.println(
                    "Error: divergence in add() between t and m.\n");
                System.exit(-1);
            }

            T = r.nextInt();

            if (m.remove((new Integer(T))) != t.remove((new Integer(T)))) {
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

        /* Now we check that m actually holds that data. */
        for (Iterator i = t.iterator(); i.hasNext();) {
            Object e = i.next();

            if (!m.contains(e)) {
                System.out.println("Error: m and t differ on a key (" + e +
                    ") after removal (iterating on t).\n");
                System.exit(-1);
            }
        }

        /* Now we check that m actually holds that data, but iterating on m. */
        for (Iterator i = m.iterator(); i.hasNext();) {
            Object e = i.next();

            if (!t.contains(e)) {
                System.out.println("Error: m and t differ on a key (" + e +
                    ") after removal (iterating on m).\n");
                System.exit(-1);
            }
        }

        /* Now we make m into an array, make it again a set and check it is OK. */
        int[] a = m.toIntArray();

        if (!new IntHashSet(a).equals(m)) {
            System.out.println(
                "Error: toArray() output (or array-based constructor) is not OK.\n");
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

            m = (IntHashSet)ois.readObject();
            ois.close();
            ff.delete();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        if (m.hashCode() != h) {
            System.out.println("Error: hashCode() changed after save/read.\n");
        }

        /* Now we check that m actually holds that data, but iterating on m. */
        for (Iterator i = m.iterator(); i.hasNext();) {
            Object e = i.next();

            if (!t.contains(e)) {
                System.out.println("Error: m and t differ on a key (" + e +
                    ") after save/read.\n");
                System.exit(-1);
            }
        }

        /* Now we take out of m everything, and check that it is empty. */
        for (Iterator i = m.iterator(); i.hasNext();) {
            i.next();
            i.remove();
        }

        if (!m.isEmpty()) {
            System.out.println("Error: m is not empty (as it should be).\n");
            System.exit(-1);
        }

        m = new IntHashSet(n, f);
        t.clear();

        int x;

        /* Now we torture-test the hash table. This part is implemented only for integers and longs. */
        int p = m.n;

        for (int i = 0; i < p; i++) {
            for (int j = 0; j < 20; j++) {
                m.add(i + (r.nextInt() % 10) * p);
                m.remove(i + (r.nextInt() % 10) * p);
            }

            for (int j = -10; j < 10; j++) {
                m.remove(i + j * p);
            }
        }

        t.addAll(m);

        /* Now all table entries are REMOVED. */
        int k = 0;

        for (int i = 0; i < (p * f) / 10; i++) {
            for (int j = 0; j < 10; j++) {
                k++;

                if (m.add(x = i + (r.nextInt() % 10) * p) != t.add((new Integer(
                                x)))) {
                    System.out.println(
                        "Error: m and t differ on a key during torture-test insertion.");
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

        for (int i = 0; i < (p * f) / 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (m.remove(x = i + (r.nextInt() % 10) * p) != t.remove(
                            (new Integer(x)))) {
                    System.out.println(
                        "Error: m and t differ on a key during torture-test removal.");
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

        if (!((IntHashSet)m.clone()).equals(m)) {
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
