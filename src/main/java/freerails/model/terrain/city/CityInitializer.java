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

import freerails.model.world.World;

import java.util.Random;

/**
 * Populates each city with its initial urban, industry, and resource tiles.
 */
public class CityInitializer {

    private final World world;
    private final Random random = new Random();
    private final CityTilePositioner tilePositioner;

    /**
     * @param world world whose cities will be initialized
     */
    public CityInitializer(World world) {
        this.world = world;
        tilePositioner = new CityTilePositioner(world, random);
    }

    /** Randomly populates each city's surrounding area with initial tiles. */
    public void initializeCities() {
        for (City city : world.getCities()) {
            CityModel cityModel = new CityModel();
            cityModel.loadFromMap(world, city.getId());

            int urbanTiles = 2 + random.nextInt(3);
            for (int i = 0; i < urbanTiles; i++) {
                tilePositioner.addUrbanTile(cityModel);
            }

            int industryTiles = random.nextInt(3);
            for (int i = 0; i < industryTiles; i++) {
                tilePositioner.addIndustryTile(cityModel);
            }

            int resourceTiles = random.nextInt(3);
            for (int i = 0; i < resourceTiles; i++) {
                tilePositioner.addResourceTile(cityModel);
            }

            cityModel.writeToMap(world);
        }
    }
}
