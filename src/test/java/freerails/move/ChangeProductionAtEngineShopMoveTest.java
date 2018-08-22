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

import freerails.model.cargo.CargoBatchBundle;
import freerails.model.station.*;

import freerails.model.train.TrainTemplate;
import freerails.util.Vec2D;
import freerails.util.WorldGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Junit TestCase for ChangeProductionAtEngineShopMove.
 */
public class ChangeProductionAtEngineShopMoveTest extends AbstractMoveTestCase {

    private List<TrainTemplate> after;
    private int engineType;
    private List<Integer> wagons = new ArrayList<>();

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Station station = new Station(0, Vec2D.ZERO, "no name", 0, new CargoBatchBundle());
        getWorld().addStation(WorldGenerator.TEST_PLAYER, station);
        //getWorld().addStation(MapFixtureFactory.TEST_PLAYER, station);
        //getWorld().addStation(MapFixtureFactory.TEST_PLAYER, station);

        engineType = 0;
        int wagonType = 0;
        after = Arrays.asList(new TrainTemplate(engineType, Arrays.asList(wagonType, wagonType)));
    }

    /**
     *
     */
    public void testMove() {
        List<TrainTemplate> before = new ArrayList<>();

        ChangeProductionAtEngineShopMove m;

        // Should fail because current production at station 0 is null;
        m = new ChangeProductionAtEngineShopMove(after, before, 0, WorldGenerator.TEST_PLAYER);
        assertMoveNotApplicable(m);
        assertMoveApplyFails(m);

        // Should fail because station 6 does not exist.
        m = new ChangeProductionAtEngineShopMove(before, after, 6, WorldGenerator.TEST_PLAYER);
        assertMoveNotApplicable(m);
        assertMoveApplyFails(m);

        // Should go through
        m = new ChangeProductionAtEngineShopMove(before, after, 0, WorldGenerator.TEST_PLAYER);

        // It should not be repeatable.
        assertOkButNotRepeatable(m);

        assertSurvivesSerialisation(m);
    }

    /**
     *
     */
    public void testProductionAtEngineShopEquals() {
        TrainTemplate b = new TrainTemplate(engineType, wagons);
        TrainTemplate c = new TrainTemplate(engineType, wagons);
        assertEquals(c, b);
        assertEquals(b, c);
    }
}