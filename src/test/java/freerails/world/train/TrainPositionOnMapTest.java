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

package freerails.world.train;

import freerails.util.LineSegment;
import freerails.world.track.PathIterator;
import freerails.world.track.SimplePathIteratorImpl;
import junit.framework.TestCase;

/**
 * Junit test.
 */
public class TrainPositionOnMapTest extends TestCase {

    /**
     * @param arg0
     */
    public TrainPositionOnMapTest(String arg0) {
        super(arg0);
    }

    /**
     *
     */
    public void testGetLength() {
        TrainPositionOnMap a;
        a = TrainPositionOnMap.createInstance(new Integer[]{10, 20, 30, 40},
                new Integer[]{11, 22, 33, 44});
        assertEquals(4, a.getLength());
    }

    /**
     *
     */
    public void testGetPoint() {
        TrainPositionOnMap a;
        a = TrainPositionOnMap.createInstance(new Integer[]{10, 20}, new Integer[]{
                11, 22});

        assertEquals(a.getX(0), 10);
        assertEquals(a.getY(0), 11);

        assertEquals(a.getX(1), 20);
        assertEquals(a.getY(1), 22);
    }

    /**
     *
     */
    public void testPath() {
        TrainPositionOnMap a;
        a = TrainPositionOnMap.createInstance(new Integer[]{10, 20, 30, 40},
                new Integer[]{11, 22, 33, 44});

        PathIterator path = a.path();
        LineSegment line = new LineSegment();
        assertTrue(path.hasNext());
        path.nextSegment(line);
        assertEquals(line, new LineSegment(10, 11, 20, 22));
        assertTrue(path.hasNext());
        path.nextSegment(line);
        assertEquals(line, new LineSegment(20, 22, 30, 33));
        assertTrue(path.hasNext());
        path.nextSegment(line);
        assertEquals(line, new LineSegment(30, 33, 40, 44));
        assertTrue(!path.hasNext());
    }

    /**
     *
     */
    public void testReversePath() {
        TrainPositionOnMap a;
        a = TrainPositionOnMap.createInstance(new Integer[]{40, 30, 20, 10},
                new Integer[]{44, 33, 22, 11});

        PathIterator path = a.reversePath();
        LineSegment line = new LineSegment();
        assertTrue(path.hasNext());
        path.nextSegment(line);
        assertEquals(line, new LineSegment(10, 11, 20, 22));
        assertTrue(path.hasNext());
        path.nextSegment(line);
        assertEquals(line, new LineSegment(20, 22, 30, 33));
        assertTrue(path.hasNext());
        path.nextSegment(line);
        assertEquals(line, new LineSegment(30, 33, 40, 44));
        assertTrue(!path.hasNext());
    }

    /**
     * Test for TrainPosition createInstance(int[], int[])
     */
    public void testCreateInstanceIArrayIArray() {
        try {
            TrainPositionOnMap.createInstance(new Integer[]{40, 30, 20, 10},
                    new Integer[]{44, 33, 22, 11});
        } catch (Exception e) {
            assertTrue(false);
        }

        try {
            TrainPositionOnMap.createInstance(new Integer[]{40, 30, 20},
                    new Integer[]{44, 33, 22, 11});
            assertTrue(false);
        } catch (Exception e) {
        }
    }

    /*
     * public void testAdd() { TrainPosition a, b, c, d, e, f, g, h , i, j;
     * a=TrainPosition.createInstance(new int[] {10,20}, new int[]{11,22});
     * b=TrainPosition.createInstance(new int[] {20, 30}, new int[]{22,33});
     * c=TrainPosition.createInstance(new int[] {10, 30}, new int[]{11, 33});
     *
     * d=TrainPosition.add(a, b); assertEquals(d, c); e=TrainPosition.add(b, a);
     * assertEquals(e, c);
     *
     *
     * f = TrainPosition.createInstance( new int[] { 40, 50 }, new int[] { 44,
     * 55 }); g = TrainPosition.createInstance( new int[] { 10, 30, 40 }, new
     * int[] { 11, 33, 44 });
     *
     * i = TrainPosition.createInstance( new int[] { 10, 30, 50 }, new int[] {
     * 11, 33, 55 }); j = TrainPosition.add(f, g); assertEquals(i, j);
     *
     *  }
     */
    /*
     * public void testRemove() { TrainPosition a, b, c, d, e, f, g, h , i, j ,
     * k; a=TrainPosition.createInstance(new int[] {10,20 ,40 , 50, 60}, new
     * int[]{11,22, 44, 55 , 66}); b=TrainPosition.createInstance(new int[] {10,
     * 20, 30}, new int[]{11,22,33}); c=TrainPosition.createInstance(new int[]
     * {48, 50, 60}, new int[]{49,55, 66});
     *
     * d=TrainPosition.createInstance(new int[] {30, 40 , 50, 60}, new int[]{33,
     * 44, 55, 66}); e=TrainPosition.createInstance(new int[] {10, 20, 40, 48},
     * new int[]{11, 22, 44, 49});
     *
     * f=TrainPosition.remove(a, b); assertEquals(f, d);
     *
     * g=TrainPosition.remove(a, c); assertEquals(g, e);
     *
     * h = TrainPosition.createInstance( new int[] { 10, 30, 50 }, new int[] {
     * 11, 33, 55 });
     *
     * i = TrainPosition.createInstance( new int[] { 10, 20 }, new int[] { 11,
     * 22 });
     *
     * j = TrainPosition.createInstance( new int[] { 20, 30, 50 }, new int[] {
     * 22, 33, 55 });
     *
     * k = TrainPosition.remove(h, i);
     *
     * assertEquals(k, j); }
     *
     */
    /*
     * public void testCanBeAdded() { TrainPosition a, b, c, d;
     * a=TrainPosition.createInstance(new int[] {10,20}, new int[]{11,22});
     * b=TrainPosition.createInstance(new int[] {20, 30}, new int[]{22,33});
     * c=TrainPosition.createInstance(new int[] {30, 40}, new int[]{33,44});
     *
     * assertTrue(TrainPosition.canBeAdded(a, b));
     * assertTrue(TrainPosition.canBeAdded(b, a));
     * assertTrue(TrainPosition.canBeAdded(b, c));
     * assertTrue(!TrainPosition.canBeAdded(c, b));
     *
     * assertTrue(!TrainPosition.canBeAdded(a, c));
     * assertTrue(!TrainPosition.canBeAdded(c, a));
     *
     * //Test that we cannot add a position to itself
     * assertTrue(!TrainPosition.canBeAdded(a, a));
     * assertTrue(!TrainPosition.canBeAdded(b, b));
     * assertTrue(!TrainPosition.canBeAdded(c, c)); }
     */
    /*
     * public void testCanBeRemoved() { TrainPosition a, b, c, d;
     * a=TrainPosition.createInstance(new int[] {10,20 ,40 , 50}, new
     * int[]{11,22, 44, 55}); b=TrainPosition.createInstance(new int[] {10, 20,
     * 30}, new int[]{11,22,33}); c=TrainPosition.createInstance(new int[] {30,
     * 40, 50}, new int[]{33,44,55});
     *
     * assertTrue(TrainPosition.canBeRemoved(a, b));
     *
     * assertTrue(!TrainPosition.canBeRemoved(b, a));
     *
     * assertTrue(TrainPosition.canBeRemoved(a, c));
     *
     * assertTrue(!TrainPosition.canBeRemoved(c, a));
     *
     * //Test that we cannot remove a position from itself
     * assertTrue(!TrainPosition.canBeRemoved(a, a));
     * assertTrue(!TrainPosition.canBeRemoved(b, b));
     * assertTrue(!TrainPosition.canBeRemoved(c, c)); }
     */

    /**
     *
     */

    public void testAddToHead() {
        TrainPositionOnMap a;
        TrainPositionOnMap b;
        TrainPositionOnMap c;
        TrainPositionOnMap d;
        TrainPositionOnMap f;
        TrainPositionOnMap g;
        TrainPositionOnMap i;
        TrainPositionOnMap j;
        a = TrainPositionOnMap.createInstance(new Integer[]{10, 20}, new Integer[]{
                11, 22});
        b = TrainPositionOnMap.createInstance(new Integer[]{20, 30}, new Integer[]{
                22, 33});
        c = TrainPositionOnMap.createInstance(new Integer[]{10, 30}, new Integer[]{
                11, 33});

        d = b.addToHead(a);
        assertEquals(d, c);

        f = TrainPositionOnMap.createInstance(new Integer[]{40, 50}, new Integer[]{
                44, 55});
        g = TrainPositionOnMap.createInstance(new Integer[]{10, 30, 40},
                new Integer[]{11, 33, 44});

        i = TrainPositionOnMap.createInstance(new Integer[]{10, 30, 50},
                new Integer[]{11, 33, 55});
        j = f.addToHead(g);
        assertEquals(i, j);
    }

    /**
     *
     */
    public void testCanAddToHead() {
        TrainPositionOnMap a;
        TrainPositionOnMap b;
        TrainPositionOnMap c;
        a = TrainPositionOnMap.createInstance(new Integer[]{10, 20}, new Integer[]{
                11, 22});
        b = TrainPositionOnMap.createInstance(new Integer[]{20, 30}, new Integer[]{
                22, 33});
        c = TrainPositionOnMap.createInstance(new Integer[]{30, 40}, new Integer[]{
                33, 44});

        assertTrue(b.canAddToHead(a));
        assertTrue(!a.canAddToHead(b));

        assertTrue(c.canAddToHead(b));
        assertTrue(!b.canAddToHead(c));

        assertTrue(!c.canAddToHead(a));
        assertTrue(!a.canAddToHead(c));
    }

    /**
     *
     */
    public void testAddToTail() {
        TrainPositionOnMap a;
        TrainPositionOnMap b;
        TrainPositionOnMap c;
        TrainPositionOnMap d;
        TrainPositionOnMap f;
        TrainPositionOnMap g;
        TrainPositionOnMap i;
        TrainPositionOnMap j;
        a = TrainPositionOnMap.createInstance(new Integer[]{10, 20}, new Integer[]{
                11, 22});
        b = TrainPositionOnMap.createInstance(new Integer[]{20, 30}, new Integer[]{
                22, 33});
        c = TrainPositionOnMap.createInstance(new Integer[]{10, 30}, new Integer[]{
                11, 33});

        d = a.addToTail(b);
        assertEquals(d, c);

        f = TrainPositionOnMap.createInstance(new Integer[]{40, 50}, new Integer[]{
                44, 55});
        g = TrainPositionOnMap.createInstance(new Integer[]{10, 30, 40},
                new Integer[]{11, 33, 44});

        i = TrainPositionOnMap.createInstance(new Integer[]{10, 30, 50},
                new Integer[]{11, 33, 55});
        j = g.addToTail(f);
        assertEquals(i, j);
    }

    /**
     *
     */
    public void testCanAddToTail() {
        TrainPositionOnMap a;
        TrainPositionOnMap b;
        TrainPositionOnMap c;
        a = TrainPositionOnMap.createInstance(new Integer[]{10, 20}, new Integer[]{
                11, 22});
        b = TrainPositionOnMap.createInstance(new Integer[]{20, 30}, new Integer[]{
                22, 33});
        c = TrainPositionOnMap.createInstance(new Integer[]{30, 40}, new Integer[]{
                33, 44});

        assertTrue(!b.canAddToTail(a));
        assertTrue(a.canAddToTail(b));

        assertTrue(!c.canAddToTail(b));
        assertTrue(b.canAddToTail(c));

        assertTrue(!c.canAddToTail(a));
        assertTrue(!a.canAddToTail(c));
    }

    /**
     *
     */
    public void testCanRemoveFromHead() {
        TrainPositionOnMap a;
        TrainPositionOnMap b;
        TrainPositionOnMap c;
        a = TrainPositionOnMap.createInstance(new Integer[]{10, 20, 40, 50},
                new Integer[]{11, 22, 44, 55});
        b = TrainPositionOnMap.createInstance(new Integer[]{10, 20, 30},
                new Integer[]{11, 22, 33});
        c = TrainPositionOnMap.createInstance(new Integer[]{30, 40, 50},
                new Integer[]{33, 44, 55});

        assertTrue(!b.canRemoveFromHead(a));
        assertTrue(a.canRemoveFromHead(b));

        assertTrue(!c.canRemoveFromHead(b));
        assertTrue(!b.canRemoveFromHead(c));

        assertTrue(!c.canRemoveFromHead(a));
        assertTrue(!a.canRemoveFromHead(c));
    }

    /**
     *
     */
    public void testRemoveFromTail() {
        TrainPositionOnMap a;
        TrainPositionOnMap c;
        TrainPositionOnMap e;
        TrainPositionOnMap f;
        a = TrainPositionOnMap.createInstance(new Integer[]{10, 20, 40, 50, 60},
                new Integer[]{11, 22, 44, 55, 66});

        c = TrainPositionOnMap.createInstance(new Integer[]{48, 50, 60},
                new Integer[]{49, 55, 66});

        e = TrainPositionOnMap.createInstance(new Integer[]{10, 20, 40, 48},
                new Integer[]{11, 22, 44, 49});

        f = a.removeFromTail(c);
        assertEquals(e, f);
    }

    /**
     *
     */
    public void testCanRemoveFromTail() {
        TrainPositionOnMap a;
        TrainPositionOnMap b;
        TrainPositionOnMap c;
        a = TrainPositionOnMap.createInstance(new Integer[]{10, 20, 40, 50},
                new Integer[]{11, 22, 44, 55});
        b = TrainPositionOnMap.createInstance(new Integer[]{10, 20, 30},
                new Integer[]{11, 22, 33});
        c = TrainPositionOnMap.createInstance(new Integer[]{30, 40, 50},
                new Integer[]{33, 44, 55});

        assertTrue(!b.canRemoveFromTail(a));
        assertTrue(!a.canRemoveFromTail(b));

        assertTrue(!c.canRemoveFromTail(b));
        assertTrue(!b.canRemoveFromTail(c));

        assertTrue(!c.canRemoveFromTail(a));
        assertTrue(a.canRemoveFromTail(c));
    }

    /**
     *
     */
    public void testEquals() {
        TrainPositionOnMap a;
        TrainPositionOnMap b;
        TrainPositionOnMap c;
        a = TrainPositionOnMap.createInstance(new Integer[]{10, 20}, new Integer[]{
                11, 22});
        b = TrainPositionOnMap.createInstance(new Integer[]{10, 20}, new Integer[]{
                11, 22});
        c = TrainPositionOnMap.createInstance(new Integer[]{30, 40}, new Integer[]{
                33, 44});

        assertTrue(a != null);
        assertTrue(!a.equals(new Object()));
        //noinspection EqualsWithItself
        assertTrue(a.equals(a));
        assertTrue(a.equals(b));
        assertTrue(!a.equals(c));
    }

    /**
     * Test for TrainPosition createInstance(PathIterator)
     */
    public void testCreateInstanceFreerailsPathIterator() {
        PathIterator path = new SimplePathIteratorImpl(new Integer[]{40,
                30, 20, 10}, new Integer[]{44, 33, 22, 11});
        TrainPositionOnMap a = TrainPositionOnMap
                .createInSameDirectionAsPath(path);

        assertEquals(a.getLength(), 4);

        assertEquals(a.getX(0), 40);
        assertEquals(a.getY(0), 44);

        assertEquals(a.getX(1), 30);
        assertEquals(a.getY(1), 33);

        assertEquals(a.getX(2), 20);
        assertEquals(a.getY(2), 22);

        assertEquals(a.getX(3), 10);
        assertEquals(a.getY(3), 11);
    }

    /**
     *
     */
    public void testCreateInOppositeDirectionToPath() {
        PathIterator path = new SimplePathIteratorImpl(new Integer[]{40,
                30, 20, 10}, new Integer[]{44, 33, 22, 11});
        TrainPositionOnMap a = TrainPositionOnMap.createInSameDirectionAsPath(
                path).reverse();

        assertEquals(a.getLength(), 4);

        assertEquals(a.getX(3), 40);
        assertEquals(a.getY(3), 44);

        assertEquals(a.getX(2), 30);
        assertEquals(a.getY(2), 33);

        assertEquals(a.getX(1), 20);
        assertEquals(a.getY(1), 22);

        assertEquals(a.getX(0), 10);
        assertEquals(a.getY(0), 11);
    }
}