/*
 * Created on 20-Mar-2003
 *
 */
package jfreerails.world.top;

import jfreerails.world.accounts.Receipt;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.Money;
import jfreerails.world.player.Player;
import junit.framework.TestCase;


/**    Junit test.
 * @author Luke
 *
 */
public class WorldImplTest extends TestCase {
    private final FreerailsSerializable fs = new FreerailsSerializable() {
        };

    public void testGet() {
        WorldImpl w = new WorldImpl();
        w.add(SKEY.TERRAIN_TYPES, fs);
        assertEquals(w.get(SKEY.TERRAIN_TYPES, 0), fs);
    }

    public void testConstructor() {
        World w = new WorldImpl();
        assertEquals("The width should be zero", 0, w.getMapWidth());
        assertEquals("The height should be zero", 0, w.getMapHeight());
    }

    /** Tests that changing the object returned by defensiveCopy() does not alter
     * the world object that was copied.
     */
    public void testDefensiveCopy() {
        World original;
        World copy;
        original = new WorldImpl();
        copy = original.defensiveCopy();
        assertNotSame("The copies should be different objects.", original, copy);
        assertEquals("The copies should be logically equal.", original, copy);

        copy.add(SKEY.TERRAIN_TYPES, fs);

        assertFalse(original.equals(copy));
        assertFalse(copy.equals(original));
        assertEquals(1, copy.size(SKEY.TERRAIN_TYPES));
        assertEquals(0, original.size(SKEY.TERRAIN_TYPES));
    }

    public void testEquals() {
        World original;
        World copy;
        original = new WorldImpl();
        copy = original.defensiveCopy();

        Player player = new Player("Name", null, 0);
        int index = copy.addPlayer(player);
        assertEquals(index, 0);
        assertFalse(copy.equals(original));
        original.addPlayer(player);
        assertEquals("The copies should be logically equal.", original, copy);

        Transaction t = new Receipt(new Money(100), Transaction.Category.MISC_INCOME);
        copy.addTransaction(t, player.getPrincipal());
        assertEquals(new Money(100),
            copy.getCurrentBalance(player.getPrincipal()));
        assertFalse(copy.equals(original));
    }
}