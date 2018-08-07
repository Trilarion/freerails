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

import freerails.model.station.*;

import freerails.model.world.PlayerKey;
import freerails.model.MapFixtureFactory;
import freerails.util.Vec2D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Junit TestCase for ChangeProductionAtEngineShopMove.
 */
public class ChangeProductionAtEngineShopMoveTest extends AbstractMoveTestCase {

    private List<TrainBlueprint> after;
    private int engineType;
    private Integer[] wagons;

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Station station = new Station(0, Vec2D.ZERO, "no name", 0, 0);
        getWorld().addStation(MapFixtureFactory.TEST_PLAYER, station);
        //getWorld().addStation(MapFixtureFactory.TEST_PLAYER, station);
        //getWorld().addStation(MapFixtureFactory.TEST_PLAYER, station);

        engineType = 0;
        int wagonType = 0;
        wagons = new Integer[]{wagonType, wagonType};
        after = Arrays.asList(new TrainBlueprint(engineType, wagons));
    }

    /**
     *
     */
    public void testMove() {
        List<TrainBlueprint> before = new ArrayList<>();

        ChangeProductionAtEngineShopMove m;

        // Should fail because current production at station 0 is null;
        m = new ChangeProductionAtEngineShopMove(after, before, 0, MapFixtureFactory.TEST_PLAYER);
        assertTryMoveFails(m);
        assertDoMoveFails(m);

        // Should fail because station 6 does not exist.
        m = new ChangeProductionAtEngineShopMove(before, after, 6, MapFixtureFactory.TEST_PLAYER);
        assertTryMoveFails(m);
        assertDoMoveFails(m);

        // Should go through
        m = new ChangeProductionAtEngineShopMove(before, after, 0, MapFixtureFactory.TEST_PLAYER);
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