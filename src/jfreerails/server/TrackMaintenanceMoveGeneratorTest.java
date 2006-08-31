/*
 * Created on 11-Aug-2003
 *
 */
package jfreerails.server;

import java.util.Arrays;

import jfreerails.world.accounts.AddItemTransaction;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.Money;
import jfreerails.world.top.ItemsTransactionAggregator;
import jfreerails.world.top.MapFixtureFactory;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;
import junit.framework.TestCase;

/**
 * JUnit test for TrackMaintenanceMoveGenerator.
 * 
 * @author Luke Lindsay
 * 
 */
public class TrackMaintenanceMoveGeneratorTest extends TestCase {
	private World w;

	@Override
	protected void setUp() throws Exception {
		w = new WorldImpl(20, 20);
		w.addPlayer(MapFixtureFactory.TEST_PLAYER);
		MapFixtureFactory.generateTrackRuleList(w);
	}

	public void testCalulateNumberOfEachTrackType() {
		int[] actual;
		int[] expected;
		actual = calNumOfEachTrackType();

		/*
		 * actual = ItemsTransactionAggregator.calulateNumberOfEachTrackType(w,
		 * MapFixtureFactory.TEST_PRINCIPAL, 0);
		 */
		expected = new int[] { 0, 0, 0 }; // No track has been built yet.
		assertTrue(Arrays.equals(expected, actual));

		addTrack(0, 10);

		actual = calNumOfEachTrackType();
		expected = new int[] { 10, 0, 0 };
		assertTrue(Arrays.equals(expected, actual));

		addTrack(2, 20);

		actual = calNumOfEachTrackType();
		expected = new int[] { 10, 0, 20 };
		assertTrue(Arrays.equals(expected, actual));
	}

	private int[] calNumOfEachTrackType() {
		int[] actual;
		ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(
				w, MapFixtureFactory.TEST_PRINCIPAL);
		actual = new int[3];
		aggregator.setType(0);
		actual[0] = aggregator.calculateQuantity();
		aggregator.setType(1);
		actual[1] = aggregator.calculateQuantity();
		aggregator.setType(2);
		actual[2] = aggregator.calculateQuantity();

		return actual;
	}

	/**
	 * Utility method to add the specifed number of units of the specified track
	 * type.
	 */
	private void addTrack(int trackType, int quantity) {
		AddItemTransaction t = new AddItemTransaction(
				Transaction.Category.TRACK, trackType, quantity, new Money(
						trackType));
		w.addTransaction(MapFixtureFactory.TEST_PRINCIPAL, t);
	}
}