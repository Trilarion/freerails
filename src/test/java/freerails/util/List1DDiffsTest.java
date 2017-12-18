/*
 * Created on 24-Jul-2005
 *
 */
package freerails.util;

import freerails.world.player.Player;
import junit.framework.TestCase;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author jkeller1
 */
public class List1DDiffsTest extends TestCase {

    private List1D<Object> list;
    private List1DDiff<Object> diffs;
    private SortedMap<ListKey, Object> map;

    /**
     *
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        list = new List1DImpl<>();
        map = new TreeMap<>();
        diffs = new List1DDiff<>(map, list, test.test);
    }

    /**
     *
     */
    public void testChangingValues() {

        list.add(String.valueOf(1));
        assertEquals(diffs.get(0), String.valueOf(1));
        assertEquals(diffs.size(), list.size());

        diffs.set(String.valueOf(2), 0);
        assertEquals(diffs.get(0), String.valueOf(2));
        assertEquals(1, map.size());
        diffs.set(String.valueOf(1), 0);
        assertEquals(0, map.size());

    }

    /**
     *
     */
    public void testAdd() {
        Player player0 = new Player("player0", 0);

        Player player1 = new Player("player1", 1);

        int i = diffs.add(player0);
        assertEquals(0, i);
        assertEquals(1, diffs.size());
        assertEquals(player0, diffs.get(0));
        i = diffs.add(player1);
        assertEquals(1, i);
        assertEquals(2, diffs.size());
        assertEquals(player1, diffs.get(1));
    }

    /**
     *
     */
    public void testAddAndRemove() {
        list.add(String.valueOf(1));
        assertEquals(String.valueOf(1), diffs.get(0));
        int i = diffs.add(String.valueOf(2));
        assertEquals(1, i);
        assertEquals(String.valueOf(1), diffs.get(0));
        assertEquals(diffs.get(1), String.valueOf(2));
        assertEquals(2, diffs.size());
        assertEquals(2, map.size());

        Object removed = diffs.removeLast();
        assertEquals(String.valueOf(2), removed);
        assertEquals(1, diffs.size());
        assertEquals(0, map.size());

        removed = diffs.removeLast();
        assertEquals(String.valueOf(1), removed);
        assertEquals(0, diffs.size());
        assertEquals(1, map.size());
    }

    /**
     *
     */
    public void testAddAndRemove2() {
        list.add(String.valueOf(1));
        list.add(String.valueOf(1));
        list.add(String.valueOf(1));
        diffs.removeLast();
        diffs.removeLast();
        assertEquals(1, diffs.size());
        assertEquals(1, map.size());

        diffs.add(String.valueOf(2));
        diffs.add(String.valueOf(2));
        diffs.add(String.valueOf(2));
        diffs.add(String.valueOf(2));

        assertEquals(5, diffs.size());

        assertEquals("4 elements + end=5", 5, map.size());

        assertEquals(String.valueOf(1), diffs.get(0));
        assertEquals(String.valueOf(2), diffs.get(1));
        assertEquals(String.valueOf(2), diffs.get(2));
        assertEquals(String.valueOf(2), diffs.get(3));
        assertEquals(String.valueOf(2), diffs.get(3));

        diffs.set(String.valueOf(3), 2);

        assertEquals(5, diffs.size());

        assertEquals(5, map.size());

        assertEquals(String.valueOf(3), diffs.get(2));

        diffs.set(String.valueOf(4), 4);

        assertEquals(String.valueOf(4), diffs.get(4));

        diffs.removeLast();

        diffs.removeLast();

        diffs.removeLast();

        diffs.removeLast();

        assertEquals(1, diffs.size());

        assertEquals("fork=1", 1, map.size());

    }

    /**
     *
     */
    public void testSortedMap() {
        ListKey elementKey1 = new ListKey(ListKey.Type.Element, test.test, 0);
        ListKey elementKey2 = new ListKey(ListKey.Type.Element, test.test, 1);
        ListKey elementKey3 = new ListKey(ListKey.Type.Element, test.test, 0);
        map.put(elementKey1, String.valueOf(1));
        assertFalse(map.containsKey(elementKey2));
        assertTrue(map.containsKey(elementKey1));
        assertTrue(map.containsKey(elementKey3));
    }

    enum test {
        test
    }

}
