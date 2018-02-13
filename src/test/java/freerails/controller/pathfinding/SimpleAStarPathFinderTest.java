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

package freerails.controller.pathfinding;

import freerails.controller.explorer.GraphExplorer;
import junit.framework.TestCase;

import java.util.NoSuchElementException;

/**
 * Test for SimpleAStarPathFinder.
 */
public class SimpleAStarPathFinderTest extends TestCase {

    private Map map;
    private SimpleAStarPathFinder pathFinder;

    /**
     *
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.map = new Map();
        pathFinder = new SimpleAStarPathFinder();
    }

    /**
     *
     */
    public void testFindpath() {
        int i = pathFinder.findstep(0, new int[]{1}, map);
        assertEquals(1, i);

        i = pathFinder.findstep(0, new int[]{5}, map);
        assertEquals(1, i);

        i = pathFinder.findstep(0, new int[]{4}, map);
        assertEquals(1, i);

        i = pathFinder.findstep(5, new int[]{7}, map);
        assertEquals(6, i);

        i = pathFinder.findstep(4, new int[]{1}, map);
        assertEquals(2, i);

        i = pathFinder.findstep(5, new int[]{0, 7}, map);
        assertEquals(6, i);

        i = pathFinder.findstep(5, new int[]{4}, map);
        assertEquals(2, i);

        i = pathFinder.findstep(4, new int[]{4}, map);
        assertEquals(PathFinderStatus.PATH_NOT_FOUND.id, i);

        i = pathFinder.findstep(2, new int[]{1}, map);
        assertEquals(1, i);
    }

    /**
     *
     */
    public void testExplorer() {
        assertEquals(0, map.getPosition());
        assertTrue(map.hasNextEdge());
        map.nextEdge();
        assertTrue(!map.hasNextEdge());
        assertEquals(1, map.getVertexConnectedByEdge());
        assertEquals(11, map.getEdgeCost());
        map.moveForward();
        assertEquals(1, map.getPosition());
        assertTrue(map.hasNextEdge());
        map.nextEdge();
        assertEquals(0, map.getVertexConnectedByEdge());

        // now try jumping to a different position.
        map.setPosition(2);
        assertEquals(2, map.getPosition());
        assertTrue(map.hasNextEdge());
        map.nextEdge();
        assertEquals(5, map.getVertexConnectedByEdge());
    }

    private static class Map implements GraphExplorer {
        // Look at SimpleAStarPathFinderTest.svg to see it
        private final Node[] nodes = new Node[]{
                new Node(new int[]{1}, new int[]{11}), // 0
                new Node(new int[]{0, 5, 2}, new int[]{11, 4, 8}), // 1 //
                // try
                // {11,4,4}
                new Node(new int[]{5, 3, 4, 1}, new int[]{5, 10, 12, 8}), // 2
                // //try{5,10,12,4}
                new Node(new int[]{2}, new int[]{10}), // 3
                new Node(new int[]{5, 2}, new int[]{18, 12}), // 4
                new Node(new int[]{1, 6, 4, 2}, new int[]{4, 3, 18, 5}), // 5
                new Node(new int[]{5, 7}, new int[]{3, 4}), // 6
                new Node(new int[]{6}, new int[]{4}), // 7
        };

        private int position = 0;

        private int branch = -1;

        public int getPosition() {
            return this.position;
        }

        public void setPosition(int vertex) {
            this.position = vertex;
            this.branch = -1;
        }

        public void nextEdge() {
            if (hasNextEdge()) {
                branch++;
            } else {
                throw new NoSuchElementException();
            }
        }

        public int getVertexConnectedByEdge() {
            return nodes[position].edges[branch];
        }

        public int getEdgeCost() {
            return nodes[position].distances[branch];
        }

        public boolean hasNextEdge() {
            return nodes[position].edges.length > (branch + 1);
        }

        public void moveForward() {
            this.setPosition(this.getVertexConnectedByEdge());
        }
    }

    private static class Node {

        int[] edges;
        int[] distances;

        Node(int[] e, int[] d) {
            if (e.length != d.length) {
                throw new IllegalArgumentException("e.length=" + e.length  + ", e.length=" + e.length);
            }

            edges = e;
            distances = d;
        }
    }
}