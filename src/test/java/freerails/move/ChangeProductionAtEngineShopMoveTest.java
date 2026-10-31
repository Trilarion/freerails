/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

import freerails.model.building.EngineShop;
import freerails.model.operations.train.TrainTemplate;
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
     * Initializes the fixture before each test.
     * @throws Exception if the set up operation fails.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getWorld().addEngineShop(WorldGenerator.TEST_PLAYER, new EngineShop(0, Vec2D.ZERO));

        engineType = 0;
        int wagonType = 0;
        after = Arrays.asList(new TrainTemplate(engineType, Arrays.asList(wagonType, wagonType)));
    }

    /**
     * Tests move behavior.
     */
    public void testMove() {
        ChangeProductionAtEngineShopMove m;

        // Should fail because engine shop 6 does not exist.
        m = new ChangeProductionAtEngineShopMove(after, 6, WorldGenerator.TEST_PLAYER);
        assertMoveNotApplicable(m);
        assertMoveApplyFails(m);

        // Should go through
        m = new ChangeProductionAtEngineShopMove(after, 0, WorldGenerator.TEST_PLAYER);

        // It should be repeatable.
        assertOkAndRepeatable(m);

        assertSurvivesSerialisation(m);
    }

    /**
     * Tests production at engine shop equals behavior.
     */
    public void testProductionAtEngineShopEquals() {
        TrainTemplate b = new TrainTemplate(engineType, wagons);
        TrainTemplate c = new TrainTemplate(engineType, wagons);
        assertEquals(c, b);
        assertEquals(b, c);
    }
}