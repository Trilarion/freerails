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

package freerails.server;

import java.util.List;

// TODO maybe shift to world

/**
 * Class to randomly select a terrain type for a terrain tile.
 *
 * Terrain Types are: 0) City (Urban) 1) Refinery (Industry) 2) Village
 * (Urban) 3) Factory (Industry) 4) Clear (Country) 5) Farm (Country) 6)
 * Desert (Country) 7) Ocean (Ocean) 8) Harbour (Ocean) 9) Stock-Yard
 * (Industry) 10) Food_Proc._Plant (Industry) 11) Cattle_Ranch
 * (Resource) 12) Grain_Elevator (Resource) 13) Oil_Well (Resource) 14)
 * Lumber_Mill (Resource) 15) Sugar_Plant. (Resource) 16) River (River)
 * 17) Landing (River) 18) Terminal (Special) 19) Jungle (Country) 20)
 * Hills (Hill) 21) Foothills (Hill) 22) Mountain (Hill)
 */
// TODO only used by MapFactory, should go there
public class TerrainRandomizer {

    private final List<Integer> terrainTypes;
    private final List<Integer> non_terrainTypes;


    /**
     * @param num
     * @param num2
     */

    public TerrainRandomizer(List<Integer> num, List<Integer> num2) {
        terrainTypes = num;
        non_terrainTypes = num2;
    }

    /**
     * @param type
     * @return
     */
    public int getNewType(int type) {
        double value;
        double divide = 1.0 / terrainTypes.size();

        // allow any terrain type to be drawn over except those listed in
        // non_terrainTypes
        if (!non_terrainTypes.contains(type)) {
            double CLEAR_PERCENTAGE = 0.98;
            if (Math.random() < CLEAR_PERCENTAGE) {
                // make the tile Clear
                return 4;
            }
            value = Math.random();

            /*
             * at the moment, this logic produces a balanced and even
             * distribution of the different country tiles (currently 3).
             * somehow it would be better to have the actual proportions of
             * Farms, Jungle and Desert etc vary. dunno how.
             */
            for (int i = 0; i < terrainTypes.size(); i++) {
                if ((value > (i * divide)) && (value <= ((i + 1) * divide))) {
                    return terrainTypes.get(i);
                }
            }
        }

        return type;
    }
}