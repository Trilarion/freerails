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

package freerails.model.terrain;

import junit.framework.TestCase;

import java.util.List;

public class TerrainUtilsTest extends TestCase {

    public void testProtectedTerrainIsNeverReplaced() {
        assertEquals(8, TerrainUtils.randomizeTerrain(8, List.of(4, 7), List.of(8, 9)));
    }

    public void testUnprotectedTerrainUsesClearOrOneOfTheCandidates() {
        List<Integer> candidates = List.of(7, 8, 9);

        for (int i = 0; i < 100; i++) {
            int result = TerrainUtils.randomizeTerrain(6, candidates, List.of(10));
            assertTrue(result == 4 || candidates.contains(result));
        }
    }
}
