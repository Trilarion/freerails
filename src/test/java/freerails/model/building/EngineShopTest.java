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

package freerails.model.building;

import freerails.model.operations.train.TrainTemplate;
import freerails.util.Utils;
import freerails.util.Vec2D;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests engine shop behavior.
 */
public class EngineShopTest extends TestCase {

    /**
     * Tests production queue is copied and unmodifiable behavior.
     */
    public void testProductionQueueIsCopiedAndUnmodifiable() {
        List<TrainTemplate> templates = new ArrayList<>(List.of(new TrainTemplate(4, List.of(7))));
        EngineShop shop = new EngineShop(2, Vec2D.ZERO);
        shop.setProduction(templates);
        templates.clear();

        assertEquals(List.of(new TrainTemplate(4, List.of(7))), shop.getProduction());
        try {
            shop.getProduction().clear();
            fail("Expected the production queue to be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            // The queue is exposed as a read-only view.
        }
    }

    /**
     * Tests engine shop survives serialization behavior.
     */
    public void testEngineShopSurvivesSerialization() {
        EngineShop shop = new EngineShop(2, new Vec2D(3, 4));
        shop.setProduction(List.of(new TrainTemplate(4, List.of(7, 8))));

        EngineShop copy = (EngineShop) Utils.cloneBySerialisation(shop);

        assertEquals(shop.getId(), copy.getId());
        assertEquals(shop.getLocation(), copy.getLocation());
        assertEquals(shop.getProduction(), copy.getProduction());
    }
}
