package jfreerails.controller.pathfinder;

import java.util.NoSuchElementException;
import junit.framework.TestCase;


/**
 * 27-Nov-2002
 * @author Luke Lindsay
 *
 */
public class SimpleAStarPathFinderTest extends TestCase {
    Map map;
    SimpleAStarPathFinder pathFinder;

    /**
     * Constructor for SimpleAStarPathFinderTest.
     * @param arg0
     */
    public SimpleAStarPathFinderTest(String arg0) {
        super(arg0);
    }

    protected void setUp() {
        this.map = new Map();
        pathFinder = new SimpleAStarPathFinder();
    }

    public void testFindpath() {
        setUp();

        int i = pathFinder.findpath(0, new int[] {1}, map);
        assertEquals(1, i);

        i = pathFinder.findpath(0, new int[] {5}, map);
        assertEquals(1, i);

        i = pathFinder.findpath(0, new int[] {4}, map);
        assertEquals(1, i);

        i = pathFinder.findpath(5, new int[] {7}, map);
        assertEquals(6, i);

        i = pathFinder.findpath(4, new int[] {1}, map);
        assertEquals(2, i);

        i = pathFinder.findpath(5, new int[] {0, 7}, map);
        assertEquals(6, i);

        i = pathFinder.findpath(5, new int[] {4}, map);
        assertEquals(2, i);

        i = pathFinder.findpath(4, new int[] {4}, map);
        assertEquals(SimpleAStarPathFinder.PATH_NOT_FOUND, i);
    }

    public void testExplorer() {
        setUp();
        assertEquals(0, map.getPosition());
        assertTrue(map.hasNextEdge());
        map.nextEdge();
        assertTrue(!map.hasNextEdge());
        assertEquals(1, map.getVertexConnectedByEdge());
        assertEquals(11, map.getEdgeLength());
        map.moveForward();
        assertEquals(1, map.getPosition());
        assertTrue(map.hasNextEdge());
        map.nextEdge();
        assertEquals(0, map.getVertexConnectedByEdge());

        //now try jumping to a different position.
        map.setPosition(2);
        assertEquals(2, map.getPosition());
        assertTrue(map.hasNextEdge());
        map.nextEdge();
        assertEquals(5, map.getVertexConnectedByEdge());
    }
}

class Node {
    int[] edges;
    int[] distances;

    Node(int[] e, int[] d) {
        if (e.length != d.length) {
            throw new IllegalArgumentException("e.length=" + e.length +
                ", e.length=" + e.length);
        }

        edges = e;
        distances = d;
    }
}

class Map implements GraphExplorer {
    //draw the graph on a piece of paper to see it.
    Node[] nodes = new Node[] {
            new Node(new int[] {1}, new int[] {11}), //0
            new Node(new int[] {0, 5}, new int[] {11, 4}), //1
            new Node(new int[] {5, 3, 4}, new int[] {5, 10, 12}), //2
            new Node(new int[] {2}, new int[] {10}), //3	
            new Node(new int[] {5, 2}, new int[] {18, 12}), //4
            new Node(new int[] {1, 6, 4, 2}, new int[] {4, 3, 18, 5}), //5
            new Node(new int[] {5, 7}, new int[] {3, 4}), //6
            new Node(new int[] {6}, new int[] {4}), //7
        };
    int position = 0;
    int branch = -1;

    public void setPosition(int i) {
        this.position = i;
        this.branch = -1;
    }

    public int getPosition() {
        return this.position;
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

    public int getEdgeLength() {
        return nodes[position].distances[branch];
    }

    public boolean hasNextEdge() {
        if (nodes[position].edges.length > (branch + 1)) {
            return true;
        } else {
            return false;
        }
    }

    public void moveForward() {
        this.setPosition(this.getVertexConnectedByEdge());
    }

    public boolean isAtStation() {
        return false;
    }
}