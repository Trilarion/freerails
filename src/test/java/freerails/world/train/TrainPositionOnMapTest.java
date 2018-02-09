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
import freerails.util.TestUtils;
import freerails.world.track.PathIterator;
import freerails.world.track.SimplePathIteratorImpl;
import junit.framework.Test;
import junit.framework.TestCase;

/**
 *
 */
public class TrainPositionOnMapTest extends TestCase {

    /**
     *
     */
    public void testGetLength() {
        TrainPositionOnMap trainPositionOnMap = TrainPositionOnMap.createInstance(new Integer[]{10, 20, 30, 40}, new Integer[]{11, 22, 33, 44});
        assertEquals(4, trainPositionOnMap.getLength());
    }

    /**
     *
     */
    public void testGetPoint() {
        TrainPositionOnMap trainPositionOnMap = TrainPositionOnMap.createInstance(new Integer[]{10, 20}, new Integer[]{11, 22});

        assertEquals(trainPositionOnMap.getX(0), 10);
        assertEquals(trainPositionOnMap.getY(0), 11);

        assertEquals(trainPositionOnMap.getX(1), 20);
        assertEquals(trainPositionOnMap.getY(1), 22);
    }

    /**
     *
     */
    public void testPath() {
        TrainPositionOnMap trainPositionOnMap = TrainPositionOnMap.createInstance(new Integer[]{10, 20, 30, 40}, new Integer[]{11, 22, 33, 44});

        PathIterator pathIterator = trainPositionOnMap.path();
        LineSegment segment = new LineSegment();
        assertTrue(pathIterator.hasNext());

        pathIterator.nextSegment(segment);
        TestUtils.assertLineSegmentEquals(10,11,20,22,segment);
        assertTrue(pathIterator.hasNext());

        pathIterator.nextSegment(segment);
        TestUtils.assertLineSegmentEquals(20,22,30,33, segment);
        assertTrue(pathIterator.hasNext());

        pathIterator.nextSegment(segment);
        TestUtils.assertLineSegmentEquals(30,33,40,44,segment);
        assertTrue(!pathIterator.hasNext());
    }

    /**
     *
     */
    public void testReversePath() {
        TrainPositionOnMap trainPositionOnMap = TrainPositionOnMap.createInstance(new Integer[]{40, 30, 20, 10}, new Integer[]{44, 33, 22, 11});

        PathIterator pathIterator = trainPositionOnMap.reversePath();
        LineSegment segment = new LineSegment();
        assertTrue(pathIterator.hasNext());

        pathIterator.nextSegment(segment);
        TestUtils.assertLineSegmentEquals(10,11,20,22,segment);
        assertTrue(pathIterator.hasNext());

        pathIterator.nextSegment(segment);
        TestUtils.assertLineSegmentEquals(20,22,30,33,segment);
        assertTrue(pathIterator.hasNext());

        pathIterator.nextSegment(segment);
        TestUtils.assertLineSegmentEquals(30,33,40,44,segment);
        assertTrue(!pathIterator.hasNext());
    }

    /**
     * Test for TrainPosition createInstance(int[], int[])
     */
    public void testCreateInstanceIArrayIArray() {
        TrainPositionOnMap.createInstance(new Integer[]{40, 30, 20, 10}, new Integer[]{44, 33, 22, 11});
        TestUtils.assertThrows(() -> TrainPositionOnMap.createInstance(new Integer[]{40, 30, 20}, new Integer[]{44, 33, 22, 11}));
    }

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
        a = TrainPositionOnMap.createInstance(new Integer[]{10, 20}, new Integer[]{11, 22});
        b = TrainPositionOnMap.createInstance(new Integer[]{20, 30}, new Integer[]{22, 33});
        c = TrainPositionOnMap.createInstance(new Integer[]{10, 30}, new Integer[]{11, 33});

        d = b.addToHead(a);
        assertEquals(d, c);

        f = TrainPositionOnMap.createInstance(new Integer[]{40, 50}, new Integer[]{44, 55});
        g = TrainPositionOnMap.createInstance(new Integer[]{10, 30, 40}, new Integer[]{11, 33, 44});

        i = TrainPositionOnMap.createInstance(new Integer[]{10, 30, 50},  new Integer[]{11, 33, 55});
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
        a = TrainPositionOnMap.createInstance(new Integer[]{10, 20}, new Integer[]{11, 22});
        b = TrainPositionOnMap.createInstance(new Integer[]{20, 30}, new Integer[]{22, 33});
        c = TrainPositionOnMap.createInstance(new Integer[]{30, 40}, new Integer[]{33, 44});

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
        a = TrainPositionOnMap.createInstance(new Integer[]{10, 20}, new Integer[]{11, 22});
        b = TrainPositionOnMap.createInstance(new Integer[]{20, 30}, new Integer[]{22, 33});
        c = TrainPositionOnMap.createInstance(new Integer[]{10, 30}, new Integer[]{11, 33});

        d = a.addToTail(b);
        assertEquals(d, c);

        f = TrainPositionOnMap.createInstance(new Integer[]{40, 50}, new Integer[]{ 44, 55});
        g = TrainPositionOnMap.createInstance(new Integer[]{10, 30, 40}, new Integer[]{11, 33, 44});
        i = TrainPositionOnMap.createInstance(new Integer[]{10, 30, 50}, new Integer[]{11, 33, 55});
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
        a = TrainPositionOnMap.createInstance(new Integer[]{10, 20}, new Integer[]{11, 22});
        b = TrainPositionOnMap.createInstance(new Integer[]{20, 30}, new Integer[]{22, 33});
        c = TrainPositionOnMap.createInstance(new Integer[]{30, 40}, new Integer[]{33, 44});

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
        a = TrainPositionOnMap.createInstance(new Integer[]{10, 20, 40, 50}, new Integer[]{11, 22, 44, 55});
        b = TrainPositionOnMap.createInstance(new Integer[]{10, 20, 30}, new Integer[]{11, 22, 33});
        c = TrainPositionOnMap.createInstance(new Integer[]{30, 40, 50}, new Integer[]{33, 44, 55});

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
        a = TrainPositionOnMap.createInstance(new Integer[]{10, 20, 40, 50, 60}, new Integer[]{11, 22, 44, 55, 66});
        c = TrainPositionOnMap.createInstance(new Integer[]{48, 50, 60}, new Integer[]{49, 55, 66});
        e = TrainPositionOnMap.createInstance(new Integer[]{10, 20, 40, 48}, new Integer[]{11, 22, 44, 49});

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
        a = TrainPositionOnMap.createInstance(new Integer[]{10, 20, 40, 50}, new Integer[]{11, 22, 44, 55});
        b = TrainPositionOnMap.createInstance(new Integer[]{10, 20, 30}, new Integer[]{11, 22, 33});
        c = TrainPositionOnMap.createInstance(new Integer[]{30, 40, 50}, new Integer[]{33, 44, 55});

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
        a = TrainPositionOnMap.createInstance(new Integer[]{10, 20}, new Integer[]{11, 22});
        b = TrainPositionOnMap.createInstance(new Integer[]{10, 20}, new Integer[]{11, 22});
        c = TrainPositionOnMap.createInstance(new Integer[]{30, 40}, new Integer[]{33, 44});

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
        PathIterator path = new SimplePathIteratorImpl(new Integer[]{40, 30, 20, 10}, new Integer[]{44, 33, 22, 11});
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
        PathIterator path = new SimplePathIteratorImpl(new Integer[]{40, 30, 20, 10}, new Integer[]{44, 33, 22, 11});
        TrainPositionOnMap a = TrainPositionOnMap.createInSameDirectionAsPath(path).reverse();

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