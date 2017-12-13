/*
 * Created on 28-Mar-2003
 *
 */
package jfreerails.move;

import jfreerails.world.station.ProductionAtEngineShop;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.WagonAndEngineTypesFactory;


/**
 * Junit TestCase for ChangeProductionAtEngineShopMove.
 * @author Luke
 *
 */
public class ChangeProductionAtEngineShopMoveTest extends AbstractMoveTestCase {
    private ProductionAtEngineShop before;
    private ProductionAtEngineShop after;
    private int engineType;
    private int wagonType;
    private int[] wagons;

    protected void setUp() {
        super.setUp();
        getWorld().add(KEY.STATIONS, new StationModel(),
		testPlayer.getPrincipal());
        getWorld().add(KEY.STATIONS, new StationModel(),
		testPlayer.getPrincipal());
        getWorld().add(KEY.STATIONS, new StationModel(),
		testPlayer.getPrincipal());

        WagonAndEngineTypesFactory wetf = new WagonAndEngineTypesFactory();
        wetf.addTypesToWorld(getWorld());
        engineType = 0;
        wagonType = 0;
        wagons = new int[] {wagonType, wagonType};
        after = new ProductionAtEngineShop(engineType, wagons);
    }

    public void testMove() {
        before = null;

        ChangeProductionAtEngineShopMove m;

        //Should fail because current production at station 0 is null;
        m = new ChangeProductionAtEngineShopMove(after, before, 0,
		testPlayer.getPrincipal());
        assertTryMoveFails(m);
        assertDoMoveFails(m);

        //Should fail because station 6 does not exist.
        m = new ChangeProductionAtEngineShopMove(before, after, 6,
		testPlayer.getPrincipal());
        assertTryMoveFails(m);
        assertDoMoveFails(m);

        //Should go through
        m = new ChangeProductionAtEngineShopMove(before, after, 0,
		testPlayer.getPrincipal());
        assertTryMoveIsOk(m);
        assertDoMoveIsOk(m);
        assertTryUndoMoveIsOk(m);
        assertUndoMoveIsOk(m);

        //It should not be repeatable.
        assertOkButNotRepeatable(m);

        assertEqualsSurvivesSerialisation(m);
    }

    public void testProductionAtEngineShopEquals() {
        ProductionAtEngineShop a;
        ProductionAtEngineShop b;
        ProductionAtEngineShop c;
        ProductionAtEngineShop d;
        a = null;
        b = new ProductionAtEngineShop(engineType, wagons);
        c = new ProductionAtEngineShop(engineType, wagons);
        assertEquals(c, b);
        assertEquals(b, c);
    }
}
