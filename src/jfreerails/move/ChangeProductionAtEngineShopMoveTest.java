/*
 * Created on 28-Mar-2003
 *
 */
package jfreerails.move;

import jfreerails.world.station.ProductionAtEngineShop;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.MapFixtureFactory;
import jfreerails.world.top.WagonAndEngineTypesFactory;


/**
 * Junit TestCase for ChangeProductionAtEngineShopMove.
 * @author Luke
 *
 */
public class ChangeProductionAtEngineShopMoveTest extends AbstractMoveTestCase {
    private ProductionAtEngineShop[] before;
    private ProductionAtEngineShop[] after;
    private int engineType;
    private int wagonType;
    private int[] wagons;

    protected void setUp() {
        super.setUp();
        getWorld().add(KEY.STATIONS, new StationModel(),
            MapFixtureFactory.TEST_PRINCIPAL);
        getWorld().add(KEY.STATIONS, new StationModel(),
            MapFixtureFactory.TEST_PRINCIPAL);
        getWorld().add(KEY.STATIONS, new StationModel(),
            MapFixtureFactory.TEST_PRINCIPAL);

        WagonAndEngineTypesFactory wetf = new WagonAndEngineTypesFactory();
        wetf.addTypesToWorld(getWorld());
        engineType = 0;
        wagonType = 0;
        wagons = new int[] {wagonType, wagonType};
        after = new ProductionAtEngineShop[] {
                new ProductionAtEngineShop(engineType, wagons)
            };
    }

    public void testMove() {
        before = new ProductionAtEngineShop[0];

        ChangeProductionAtEngineShopMove m;

        //Should fail because current production at station 0 is null;
        m = new ChangeProductionAtEngineShopMove(after, before, 0,
                MapFixtureFactory.TEST_PRINCIPAL);
        assertTryMoveFails(m);
        assertDoMoveFails(m);

        //Should fail because station 6 does not exist.
        m = new ChangeProductionAtEngineShopMove(before, after, 6,
                MapFixtureFactory.TEST_PRINCIPAL);
        assertTryMoveFails(m);
        assertDoMoveFails(m);

        //Should go through
        m = new ChangeProductionAtEngineShopMove(before, after, 0,
                MapFixtureFactory.TEST_PRINCIPAL);
        assertTryMoveIsOk(m);
        assertDoMoveIsOk(m);
        assertTryUndoMoveIsOk(m);
        assertUndoMoveIsOk(m);

        //It should not be repeatable.
        assertOkButNotRepeatable(m);

        assertEqualsSurvivesSerialisation(m);
    }

    public void testProductionAtEngineShopEquals() {
        ProductionAtEngineShop b;
        ProductionAtEngineShop c;
        b = new ProductionAtEngineShop(engineType, wagons);
        c = new ProductionAtEngineShop(engineType, wagons);
        assertEquals(c, b);
        assertEquals(b, c);
    }
}