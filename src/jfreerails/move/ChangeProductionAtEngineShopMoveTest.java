/*
 * Created on 28-Mar-2003
 *
 */
package jfreerails.move;

import jfreerails.world.common.ImList;
import jfreerails.world.station.PlannedTrain;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.MapFixtureFactory;
import jfreerails.world.top.WagonAndEngineTypesFactory;

/**
 * Junit TestCase for ChangeProductionAtEngineShopMove.
 * 
 * @author Luke
 * 
 */
public class ChangeProductionAtEngineShopMoveTest extends AbstractMoveTestCase {
	private ImList<PlannedTrain> before;

	private ImList<PlannedTrain> after;

	private int engineType;

	private int wagonType;

	private int[] wagons;

	protected void setUp() throws Exception {
		super.setUp();
		getWorld().add(MapFixtureFactory.TEST_PRINCIPAL, KEY.STATIONS,
				new StationModel());
		getWorld().add(MapFixtureFactory.TEST_PRINCIPAL, KEY.STATIONS,
				new StationModel());
		getWorld().add(MapFixtureFactory.TEST_PRINCIPAL, KEY.STATIONS,
				new StationModel());

		WagonAndEngineTypesFactory wetf = new WagonAndEngineTypesFactory();
		wetf.addTypesToWorld(getWorld());
		engineType = 0;
		wagonType = 0;
		wagons = new int[] { wagonType, wagonType };
		after = new ImList<PlannedTrain>(new PlannedTrain(
				engineType, wagons));
	}

	public void testMove() {
		before = new ImList<PlannedTrain>();

		ChangeProductionAtEngineShopMove m;

		// Should fail because current production at station 0 is null;
		m = new ChangeProductionAtEngineShopMove(after, before, 0,
				MapFixtureFactory.TEST_PRINCIPAL);
		assertTryMoveFails(m);
		assertDoMoveFails(m);

		// Should fail because station 6 does not exist.
		m = new ChangeProductionAtEngineShopMove(before, after, 6,
				MapFixtureFactory.TEST_PRINCIPAL);
		assertTryMoveFails(m);
		assertDoMoveFails(m);

		// Should go through
		m = new ChangeProductionAtEngineShopMove(before, after, 0,
				MapFixtureFactory.TEST_PRINCIPAL);
		assertTryMoveIsOk(m);
		assertDoMoveIsOk(m);
		assertTryUndoMoveIsOk(m);
		assertUndoMoveIsOk(m);

		// It should not be repeatable.
		assertOkButNotRepeatable(m);

		assertSurvivesSerialisation(m);
	}

	public void testProductionAtEngineShopEquals() {
		PlannedTrain b;
		PlannedTrain c;
		b = new PlannedTrain(engineType, wagons);
		c = new PlannedTrain(engineType, wagons);
		assertEquals(c, b);
		assertEquals(b, c);
	}
}