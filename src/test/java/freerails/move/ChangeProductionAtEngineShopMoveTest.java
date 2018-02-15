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

import freerails.util.ImmutableList;
import freerails.model.world.PlayerKey;
import freerails.model.train.WagonAndEngineTypesFactory;
import freerails.model.station.TrainBlueprint;
import freerails.model.station.Station;
import freerails.model.MapFixtureFactory;

/**
 * Junit TestCase for ChangeProductionAtEngineShopMove.
 */
public class ChangeProductionAtEngineShopMoveTest extends AbstractMoveTestCase {

    private ImmutableList<TrainBlueprint> after;
    private int engineType;
    private Integer[] wagons;

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getWorld().add(MapFixtureFactory.TEST_PRINCIPAL, PlayerKey.Stations, new Station());
        getWorld().add(MapFixtureFactory.TEST_PRINCIPAL, PlayerKey.Stations, new Station());
        getWorld().add(MapFixtureFactory.TEST_PRINCIPAL, PlayerKey.Stations, new Station());

        WagonAndEngineTypesFactory wetf = new WagonAndEngineTypesFactory();
        WagonAndEngineTypesFactory.addTypesToWorld(getWorld());
        engineType = 0;
        int wagonType = 0;
        wagons = new Integer[]{wagonType, wagonType};
        after = new ImmutableList<>(new TrainBlueprint(engineType, wagons));
    }

    /**
     *
     */
    public void testMove() {
        ImmutableList<TrainBlueprint> before = new ImmutableList<>();

        ChangeProductionAtEngineShopMove m;

        // Should fail because current production at station 0 is null;
        m = new ChangeProductionAtEngineShopMove(after, before, 0, MapFixtureFactory.TEST_PRINCIPAL);
        assertTryMoveFails(m);
        assertDoMoveFails(m);

        // Should fail because station 6 does not exist.
        m = new ChangeProductionAtEngineShopMove(before, after, 6, MapFixtureFactory.TEST_PRINCIPAL);
        assertTryMoveFails(m);
        assertDoMoveFails(m);

        // Should go through
        m = new ChangeProductionAtEngineShopMove(before, after, 0, MapFixtureFactory.TEST_PRINCIPAL);
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