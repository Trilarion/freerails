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

package freerails.model.train;

import freerails.model.train.motion.TrainPositionOnMap;
import freerails.util.Segment;
import freerails.util.TestUtils;
import freerails.model.track.PathIterator;
import freerails.model.track.SimplePathIteratorImpl;
import freerails.util.Vec2D;
import junit.framework.TestCase;

import java.util.Arrays;

/**
 *
 */
public class TrainPositionOnMapTest extends TestCase {

    private final Vec2D p1 = new Vec2D(10, 11);
    private final Vec2D p2 = new Vec2D(10, 20);
    private final Vec2D p3 = new Vec2D(10, 30);

    private final Vec2D p4 = new Vec2D(11, 22);
    private final Vec2D p5 = new Vec2D(11, 33);

    private final Vec2D p6 = new Vec2D(20, 30);
    private final Vec2D p7 = new Vec2D(20, 22);

    private final Vec2D p8 = new Vec2D(22, 33);

    private final Vec2D p9 = new Vec2D(30, 33);
    private final Vec2D p10 = new Vec2D(30, 40);
    private final Vec2D p11 = new Vec2D(33, 44);

    private final Vec2D p12 = new Vec2D(40, 44);
    private final Vec2D p13 = new Vec2D(48, 49);

    private final Vec2D p14 = new Vec2D(50, 55);
    private final Vec2D p15 = new Vec2D(60, 66);

    /**
     *
     */
    public void testGetLength() {
        TrainPositionOnMap trainPositionOnMap = TrainPositionOnMap.createInstance(Arrays.asList(p1, p7, p9, p12));
        assertEquals(4, trainPositionOnMap.getLength());
    }

    /**
     *
     */
    public void testGetPoint() {
        TrainPositionOnMap trainPositionOnMap = TrainPositionOnMap.createInstance(Arrays.asList(p1, p7));

        assertEquals(trainPositionOnMap.getP(0), p1);
        assertEquals(trainPositionOnMap.getP(1), p7);
    }

    /**
     *
     */
    public void testPath() {
        TrainPositionOnMap trainPositionOnMap = TrainPositionOnMap.createInstance(Arrays.asList(p1, p7, p9, p12));

        PathIterator pathIterator = trainPositionOnMap.path();
        Segment segment = null;
        assertTrue(pathIterator.hasNext());

        segment = pathIterator.nextSegment();
        TestUtils.assertLineSegmentEquals(p1, p7,segment);
        assertTrue(pathIterator.hasNext());

        segment = pathIterator.nextSegment();
        TestUtils.assertLineSegmentEquals(p7, p9, segment);
        assertTrue(pathIterator.hasNext());

        segment = pathIterator.nextSegment();
        TestUtils.assertLineSegmentEquals(p9, p12, segment);
        assertTrue(!pathIterator.hasNext());
    }

    /**
     *
     */
    public void testReversePath() {
        TrainPositionOnMap trainPositionOnMap = TrainPositionOnMap.createInstance(Arrays.asList(p12, p9, p7, p1));

        PathIterator pathIterator = trainPositionOnMap.reversePath();
        Segment segment = null;
        assertTrue(pathIterator.hasNext());

        segment = pathIterator.nextSegment();
        TestUtils.assertLineSegmentEquals(p1, p7,segment);
        assertTrue(pathIterator.hasNext());

        segment = pathIterator.nextSegment();
        TestUtils.assertLineSegmentEquals(p7, p9,segment);
        assertTrue(pathIterator.hasNext());

        segment = pathIterator.nextSegment();
        TestUtils.assertLineSegmentEquals(p9, p12,segment);
        assertTrue(!pathIterator.hasNext());
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
        a = TrainPositionOnMap.createInstance(Arrays.asList(p1, p7));
        b = TrainPositionOnMap.createInstance(Arrays.asList(p7, p9));
        c = TrainPositionOnMap.createInstance(Arrays.asList(p1, p9));

        d = b.addToHead(a);
        assertEquals(d, c);

        f = TrainPositionOnMap.createInstance(Arrays.asList(p12, p14));
        g = TrainPositionOnMap.createInstance(Arrays.asList(p1, p9, p12));

        i = TrainPositionOnMap.createInstance(Arrays.asList(p1, p9, p14));
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
        a = TrainPositionOnMap.createInstance(Arrays.asList(p1, p7));
        b = TrainPositionOnMap.createInstance(Arrays.asList(p7, p9));
        c = TrainPositionOnMap.createInstance(Arrays.asList(p9, p12));

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
        a = TrainPositionOnMap.createInstance(Arrays.asList(p1, p7));
        b = TrainPositionOnMap.createInstance(Arrays.asList(p7, p9));
        c = TrainPositionOnMap.createInstance(Arrays.asList(p1, p9));

        d = a.addToTail(b);
        assertEquals(d, c);

        f = TrainPositionOnMap.createInstance(Arrays.asList(p12, p14));
        g = TrainPositionOnMap.createInstance(Arrays.asList(p1, p9, p12));
        i = TrainPositionOnMap.createInstance(Arrays.asList(p1, p9, p14));
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
        a = TrainPositionOnMap.createInstance(Arrays.asList(p1, p7));
        b = TrainPositionOnMap.createInstance(Arrays.asList(p7, p9));
        c = TrainPositionOnMap.createInstance(Arrays.asList(p9, p12));

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
        a = TrainPositionOnMap.createInstance(Arrays.asList(p1, p7, p12, p14));
        b = TrainPositionOnMap.createInstance(Arrays.asList(p1, p7, p9));
        c = TrainPositionOnMap.createInstance(Arrays.asList(p1, p12, p14));

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
        a = TrainPositionOnMap.createInstance(Arrays.asList(p1, p7, p12, p14, p15));
        c = TrainPositionOnMap.createInstance(Arrays.asList(p13, p14, p15));
        e = TrainPositionOnMap.createInstance(Arrays.asList(p1, p7, p12, p13));

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
        a = TrainPositionOnMap.createInstance(Arrays.asList(p1, p7, p12, p14));
        b = TrainPositionOnMap.createInstance(Arrays.asList(p1, p7, p9));
        c = TrainPositionOnMap.createInstance(Arrays.asList(p9, p12, p14));

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
        a = TrainPositionOnMap.createInstance(Arrays.asList(p1, p7));
        b = TrainPositionOnMap.createInstance(Arrays.asList(p1, p7));
        c = TrainPositionOnMap.createInstance(Arrays.asList(p9, p12));

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
        PathIterator path = new SimplePathIteratorImpl(Arrays.asList(p12, p9, p7, p1));
        TrainPositionOnMap a = TrainPositionOnMap
                .createInSameDirectionAsPath(path);

        assertEquals(a.getLength(), 4);

        assertEquals(a.getP(0), p12);
        assertEquals(a.getP(1), p9);
        assertEquals(a.getP(2), p7);
        assertEquals(a.getP(3), p1);
    }

    /**
     *
     */
    public void testCreateInOppositeDirectionToPath() {
        PathIterator path = new SimplePathIteratorImpl(Arrays.asList(p12, p9, p7, p1));
        TrainPositionOnMap a = TrainPositionOnMap.createInSameDirectionAsPath(path).reverse();

        assertEquals(a.getLength(), 4);

        assertEquals(a.getP(3), p12);
        assertEquals(a.getP(2), p9);
        assertEquals(a.getP(1), p7);
        assertEquals(a.getP(0), p1);
    }
}