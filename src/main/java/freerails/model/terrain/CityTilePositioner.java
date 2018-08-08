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

package freerails.model.terrain;

import freerails.model.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Initialises cities and controls their growth. It makes changes to
 * directly to the world object, so if the game has already started, use
 * WorldDifferences and MapDiffMove to pass changes to the clients.
 */
// TODO what is random used for? Seed of random generator?, convert to static code maybe?
public class CityTilePositioner {

    private final Random random = new Random();
    private final List<Terrain> urbanTerrainTypes = new ArrayList<>();
    private final List<Terrain> resourceTerrainTypes = new ArrayList<>();
    private final List<Terrain> industryTerrainTypes = new ArrayList<>();
    private final World world;

    /**
     * @param world
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

    private void addResourceTile(CityModel city) {
        int tileTypeNo = random.nextInt(resourceTerrainTypes.size());
        Terrain type = resourceTerrainTypes.get(tileTypeNo);
        city.addTile(type);
    }

    private void addIndustryTile(CityModel city) {
        int size = city.industriesNotAtCity.size();

        if (size > 0) {
            int tileTypeNo = random.nextInt(size);
            Terrain type = city.industriesNotAtCity.get(tileTypeNo);
            city.addTile(type);
        }
    }

    private void addUrbanTile(CityModel city) {
        int tileTypeNo = random.nextInt(urbanTerrainTypes.size());
        Terrain type = urbanTerrainTypes.get(tileTypeNo);
        city.addTile(type);
    }

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
            if (cityModel.size() < 16 && cityModel.stations > 0) {
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