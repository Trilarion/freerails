/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
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

package freerails.model.train;

import freerails.model.ModelConstants;
import freerails.model.cargo.Cargo;
import freerails.model.cargo.CargoBatch;
import freerails.model.cargo.CargoBatchBundle;
import freerails.model.cargo.CargoCategory;
import freerails.model.world.World;
import freerails.util.Vec2D;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class TrainUtilsTest extends TestCase {

    public void testSpaceAvailableUsesCargoIndicesNotStableIds() {
        World base = WorldGenerator.minimalWorld();
        SortedSet<Cargo> cargos = new TreeSet<>();
        cargos.add(new Cargo(7, "Mail", CargoCategory.MAIL, 1));
        cargos.add(new Cargo(90, "Coal", CargoCategory.BULK_FREIGHT, 2));
        World world = new World.Builder()
                .setRules(base.getRules())
                .setMapSize(new Vec2D(2, 2))
                .setCargos(cargos)
                .build();

        CargoBatchBundle cargo = new CargoBatchBundle();
        cargo.addCargo(new CargoBatch(90, Vec2D.ZERO, 0, 0), 10);
        List<Integer> space = TrainUtils.spaceAvailable2(world, cargo, List.of(90));

        assertEquals(0, space.get(world.getCargoIndex(7)).intValue());
        assertEquals(ModelConstants.UNITS_OF_CARGO_PER_WAGON - 10,
                space.get(world.getCargoIndex(90)).intValue());
    }
}
