/*
 * Created on 11-Aug-2003
 *
 */
package jfreerails.server;

import java.util.Arrays;
import jfreerails.move.AddTransactionMove;
import jfreerails.world.accounts.AddItemTransaction;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.Money;
import jfreerails.world.top.ItemsTransactionAggregator;
import jfreerails.world.top.MapFixtureFactory;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;
import junit.framework.TestCase;


/**
 * @author Luke Lindsay
 *
 */
public class TrackMaintenanceMoveGeneratorTest extends TestCase {
    private World w;

    protected void setUp() throws Exception {
        w = new WorldImpl(20, 20);
        w.addPlayer(MapFixtureFactory.TEST_PLAYER);
        MapFixtureFactory.generateTrackRuleList(w);
    }

    public void testGenerateMove() {
        addTrack(0, 50);

        /* The maintenace cost of track type 0 is 10 (see MapFixtureFactory), so
        * the cost of maintaining 50 units is 500. */
        AddTransactionMove m = TrackMaintenanceMoveGenerator.generateMove(w,
                MapFixtureFactory.TEST_PRINCIPAL, Transaction.TRACK_MAINTENANCE);
        Transaction t = m.getTransaction();
        Money expected = new Money(-500);
        Money actual = t.getValue();
        assertEquals(expected, actual);
    }

    public void testCalulateNumberOfEachTrackType() {
        int[] actual;
        int[] expected;
        actual = calNumOfEachTrackType();

        /*
        actual = ItemsTransactionAggregator.calulateNumberOfEachTrackType(w,
                MapFixtureFactory.TEST_PRINCIPAL, 0);
               */
        expected = new int[] {0, 0, 0}; //No track has been built yet.
        assertTrue(Arrays.equals(expected, actual));

        addTrack(0, 10);

        actual = calNumOfEachTrackType();
        expected = new int[] {10, 0, 0};
        assertTrue(Arrays.equals(expected, actual));

        addTrack(2, 20);

        actual = calNumOfEachTrackType();
        expected = new int[] {10, 0, 20};
        assertTrue(Arrays.equals(expected, actual));
    }

    private int[] calNumOfEachTrackType() {
        int[] actual;
        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(w,
                MapFixtureFactory.TEST_PRINCIPAL);
        aggregator.setStartYear(0);
        actual = new int[3];
        aggregator.setType(0);
        actual[0] = aggregator.calulateQuantity();
        aggregator.setType(1);
        actual[1] = aggregator.calulateQuantity();
        aggregator.setType(2);
        actual[2] = aggregator.calulateQuantity();

        return actual;
    }

    /** Utility method to add the specifed number of units of the specified track type. */
    private void addTrack(int trackType, int quantity) {
        AddItemTransaction t = new AddItemTransaction(AddItemTransaction.TRACK,
                trackType, quantity, new Money(trackType));
        w.addTransaction(t, MapFixtureFactory.TEST_PRINCIPAL);
    }
}