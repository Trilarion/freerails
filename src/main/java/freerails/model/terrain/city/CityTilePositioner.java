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

// TODO does two things, initial position of tiles and growing of cities
// TODO what is random used for? Seed of random generator?, convert to static code maybe?
/**
 * Initializes city layouts and grows cities by changing the supplied world
 * directly. Changes made after play begins must be propagated to clients by
 * the caller.
 */
public class CityTilePositioner {

    private final Random random = new Random();
    private final List<Terrain> urbanTerrainTypes = new ArrayList<>();
    private final List<Terrain> resourceTerrainTypes = new ArrayList<>();
    private final List<Terrain> industryTerrainTypes = new ArrayList<>();
    private final World world;

    /**
     * @param world world whose city tiles will be initialized and grown
     */
    public CityTilePositioner(World world) {
        this.world = world;

        // get the different types of Urban/Industry/Resource terrain
        for (Terrain terrainType: world.getTerrains()) {
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

    /** Randomly populates each city's surrounding area with initial tiles. */
    public void initCities() {
        for (City city: world.getCities()) {
            CityModel cityModel = new CityModel();
            cityModel.loadFromMap(world, city.getId());

            final int urbanTiles = 2 + random.nextInt(3);

            for (int i = 0; i < urbanTiles; i++) {
                addUrbanTile(cityModel);
            }

            final int industryTiles = random.nextInt(3);

            for (int i = 0; i < industryTiles; i++) {
                addIndustryTile(cityModel);
            }

            final int resourceTiles = random.nextInt(3);

            for (int i = 0; i < resourceTiles; i++) {
                addResourceTile(cityModel);
            }

            cityModel.writeToMap(world);
        }
    }

    /**
     * Adds one randomly selected resource terrain type to an available city tile.
     *
     * @param city city model to update
     */
    private void addResourceTile(CityModel city) {
        int tileTypeNo = random.nextInt(resourceTerrainTypes.size());
        Terrain type = resourceTerrainTypes.get(tileTypeNo);
        city.addTile(type);
    }

    /**
     * Adds a not-yet-present industry type when one is available.
     *
     * @param city city model to update
     */
    private void addIndustryTile(CityModel city) {
        int size = city.getIndustriesNotAtCity().size();

        if (size > 0) {
            int tileTypeNo = random.nextInt(size);
            Terrain type = city.getIndustriesNotAtCity().get(tileTypeNo);
            city.addTile(type);
        }
    }

    /**
     * Adds one randomly selected urban terrain type to an available city tile.
     *
     * @param city city model to update
     */
    private void addUrbanTile(CityModel city) {
        int tileTypeNo = random.nextInt(urbanTerrainTypes.size());
        Terrain type = urbanTerrainTypes.get(tileTypeNo);
        city.addTile(type);
    }

    // TODO make that static and move somewhere else
    /**
     * Grows cities that have at least one station and fewer than 16 developed
     * tiles, using the configured random growth chances.
     */
    public void growCities() {
        /*
         * At some stage this will be refined to take into account how much
         * cargo has been picked up and delivered and what city tiles are
         * already present.
         */
        for (City city: world.getCities()) {
            CityModel cityModel = new CityModel();
            cityModel.loadFromMap(world, city.getId());

            // Only increase cities with stations and less than 16 tiles
            if (cityModel.getUrbanCityTiles().size() + cityModel.getIndustryCityTiles().size() + cityModel.getResourceCityTiles().size() < 16 && cityModel.getStations() > 0) {
                switch (random.nextInt(10)) {
                    case 0:
                    case 1:
                        addResourceTile(cityModel); // 20% chance
                        break;

                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        addUrbanTile(cityModel); // 40% chance

                        break;

                    case 6:
                        addIndustryTile(cityModel); // 10% chance

                        break;

                    default:
                        // Do nothing. 30% chance
                        break;
                }
                cityModel.writeToMap(world);
            }
        }
    }
}