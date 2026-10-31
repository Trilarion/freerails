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

package freerails.model;

import freerails.model.operations.cargo.CargoConversion;
import freerails.model.operations.cargo.CargoProductionOrConsumption;
import freerails.model.game.Rules;
import freerails.model.simulation.Time;
import freerails.model.terrain.TerrainTile;
import freerails.model.operations.train.TrainTemplate;
import junit.framework.TestCase;

import java.util.List;

public class RecordCustomBehaviorTest extends TestCase {

    public void testRecordGeneratedEqualityUsesAllComponents() {
        Rules rules = new Rules(true, false);
        assertEquals(rules, new Rules(true, false));
        assertEquals(rules.hashCode(), new Rules(true, false).hashCode());
        assertFalse(rules.equals(new Rules(false, false)));

        CargoProductionOrConsumption production = new CargoProductionOrConsumption(2, 1.5);
        assertEquals(production, new CargoProductionOrConsumption(2, 1.5));
        assertFalse(production.equals(new CargoProductionOrConsumption(3, 1.5)));
        assertFalse(production.equals(new CargoProductionOrConsumption(2, 2.5)));

        CargoConversion conversion = new CargoConversion(2, 3, 1.5);
        assertEquals(conversion, new CargoConversion(2, 3, 1.5));
        assertFalse(conversion.equals(new CargoConversion(4, 3, 1.5)));
        assertFalse(conversion.equals(new CargoConversion(2, 4, 1.5)));
        assertFalse(conversion.equals(new CargoConversion(2, 3, 2.5)));
    }

    public void testCargoRatesNormalizeNegativeZero() {
        CargoProductionOrConsumption production = new CargoProductionOrConsumption(2, -0.0);
        assertEquals(0L, Double.doubleToRawLongBits(production.rate()));
        assertEquals(production, new CargoProductionOrConsumption(2, 0.0));

        CargoConversion conversion = new CargoConversion(2, 3, -0.0);
        assertEquals(0L, Double.doubleToRawLongBits(conversion.conversionRate()));
        assertEquals(conversion, new CargoConversion(2, 3, 0.0));
    }

    public void testDomainSpecificStringRepresentations() {
        assertEquals("42", new Time(42).toString());
        assertEquals("trackPiece=null and terrainType is 7", new TerrainTile(7).toString());
        assertEquals("engine type: 1, with 2 wagons", new TrainTemplate(1, List.of(2, 3)).toString());
    }
}
