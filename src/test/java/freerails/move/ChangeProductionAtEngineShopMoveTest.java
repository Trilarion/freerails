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

/*
 *
 */
package freerails.move;

import freerails.util.ImList;
import freerails.world.KEY;
import freerails.world.WagonAndEngineTypesFactory;
import freerails.world.station.TrainBlueprint;
import freerails.world.station.Station;
import freerails.world.top.MapFixtureFactory;

/**
 * Junit TestCase for ChangeProductionAtEngineShopMove.
 */
public class ChangeProductionAtEngineShopMoveTest extends AbstractMoveTestCase {

    private ImList<TrainBlueprint> after;

    private int engineType;

    private int[] wagons;

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getWorld().add(MapFixtureFactory.TEST_PRINCIPAL, KEY.STATIONS,
                new Station());
        getWorld().add(MapFixtureFactory.TEST_PRINCIPAL, KEY.STATIONS,
                new Station());
        getWorld().add(MapFixtureFactory.TEST_PRINCIPAL, KEY.STATIONS,
                new Station());

        WagonAndEngineTypesFactory wetf = new WagonAndEngineTypesFactory();
        wetf.addTypesToWorld(getWorld());
        engineType = 0;
        int wagonType = 0;
        wagons = new int[]{wagonType, wagonType};
        after = new ImList<>(new TrainBlueprint(engineType, wagons));
    }

    /**
     *
     */
    @Override
    public void testMove() {
        ImList<TrainBlueprint> before = new ImList<>();

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

    /**
     *
     */
    public void testProductionAtEngineShopEquals() {
        TrainBlueprint b;
        TrainBlueprint c;
        b = new TrainBlueprint(engineType, wagons);
        c = new TrainBlueprint(engineType, wagons);
        assertEquals(c, b);
        assertEquals(b, c);
    }
}