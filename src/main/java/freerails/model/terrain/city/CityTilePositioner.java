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

package freerails.model.terrain.city;

import freerails.model.terrain.Terrain;
import freerails.model.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Selects urban, industry, and resource terrain types for city tiles.
 */
final class CityTilePositioner {

    private final Random random;
    private final List<Terrain> urbanTerrainTypes = new ArrayList<>();
    private final List<Terrain> resourceTerrainTypes = new ArrayList<>();
    private final List<Terrain> industryTerrainTypes = new ArrayList<>();

    CityTilePositioner(World world, Random random) {
        this.random = random;

        for (Terrain terrainType : world.getTerrains()) {
            switch (terrainType.getCategory()) {
                case URBAN:
                    urbanTerrainTypes.add(terrainType);
                    break;
                case INDUSTRY:
                    industryTerrainTypes.add(terrainType);
                    break;
                case RESOURCE:
                    resourceTerrainTypes.add(terrainType);
                    break;
            }
        }
    }

    void addResourceTile(CityModel city) {
        int tileTypeNo = random.nextInt(resourceTerrainTypes.size());
        city.addTile(resourceTerrainTypes.get(tileTypeNo));
    }

    void addIndustryTile(CityModel city) {
        List<Terrain> availableIndustries = city.getIndustriesNotAtCity();
        if (!availableIndustries.isEmpty()) {
            int tileTypeNo = random.nextInt(availableIndustries.size());
            city.addTile(availableIndustries.get(tileTypeNo));
        }
    }

    void addUrbanTile(CityModel city) {
        int tileTypeNo = random.nextInt(urbanTerrainTypes.size());
        city.addTile(urbanTerrainTypes.get(tileTypeNo));
    }
}
