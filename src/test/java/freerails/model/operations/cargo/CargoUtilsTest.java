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

package freerails.model.operations.cargo;

import freerails.model.ModelConstants;
import freerails.model.simulation.Time;
import freerails.model.world.World;
import freerails.util.IntPoint2D;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/** Tests cargo utility calculations. */
public class CargoUtilsTest extends TestCase {

    /**
     * Tests car loads are grouped by category and partial loads are omitted.
     */
    public void testCalculateCarLoadsGroupsCompleteLoadsByCategory() {
        World base = WorldGenerator.minimalWorld();
        SortedSet<Cargo> cargos = new TreeSet<>();
        cargos.add(new Cargo(7, "Mail", CargoCategory.MAIL, 1));
        cargos.add(new Cargo(90, "Coal", CargoCategory.BULK_FREIGHT, 2));
        World world = new World.Builder()
                .setRules(base.getRules())
                .setMapSize(new IntPoint2D(2, 2))
                .setCargos(cargos)
                .build();

        CargoBatchBundle bundle = new CargoBatchBundle();
        bundle.addCargo(new CargoBatch(7, IntPoint2D.ZERO, new Time(0), 0),
                ModelConstants.UNITS_OF_CARGO_PER_WAGON - 1);
        bundle.addCargo(new CargoBatch(90, IntPoint2D.ZERO, new Time(0), 0),
                ModelConstants.UNITS_OF_CARGO_PER_WAGON - 1);
        bundle.addCargo(new CargoBatch(90, new IntPoint2D(1, 0), new Time(1), 0),
                1);

        Map<CargoCategory, List<Integer>> carLoads = CargoUtils.calculateCarLoads(world, bundle);

        assertEquals(List.of(), carLoads.get(CargoCategory.MAIL));
        assertEquals(List.of(90), carLoads.get(CargoCategory.BULK_FREIGHT));
        for (CargoCategory category : CargoCategory.values()) {
            if (category != CargoCategory.MAIL && category != CargoCategory.BULK_FREIGHT) {
                assertEquals(List.of(), carLoads.get(category));
            }
        }
    }
}
