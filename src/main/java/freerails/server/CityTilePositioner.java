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

import freerails.world.SKEY;
import freerails.world.World;
import freerails.world.terrain.TerrainType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Initialises cities and controls their growth. It makes changes to
 * directly to the world object, so if the game has already started, use
 * WorldDifferences and MapDiffMove to pass changes to the clients.
 */
// TODO what is random used for? Seed of random generator?
public class CityTilePositioner {

    final Random random = new Random();
    final List<TerrainType> urbanTerrainTypes = new ArrayList<>();
    final List<TerrainType> industryTerrainTypes = new ArrayList<>();
    final List<TerrainType> resourceTerrainTypes = new ArrayList<>();

    final World world;

    /**
     * @param world
     */
    public CityTilePositioner(World world) {
        this.world = world;

        // get the different types of Urban/Industry/Resource terrain
        for (int i = 0; i < world.size(SKEY.TERRAIN_TYPES); i++) {
            TerrainType type = (TerrainType) world.get(SKEY.TERRAIN_TYPES, i);
            switch (type.getCategory().ordinal()) {
                case 0:
                    urbanTerrainTypes.add(type);
                    break;
                case 6:
                    industryTerrainTypes.add(type);
                    break;
                case 7:
                    resourceTerrainTypes.add(type);
                    break;
            }
        }
    }

    void initCities() {
        final int numCities = world.size(SKEY.CITIES);
        CityModel[] cities = new CityModel[numCities];

        for (int cityId = 0; cityId < numCities; cityId++) {
            CityModel city = new CityModel();
            city.loadFromMap(world, cityId);

            final int urbanTiles = 2 + random.nextInt(3);

            for (int i = 0; i < urbanTiles; i++) {
                addUrbanTile(city);
            }

            final int industryTiles = random.nextInt(3);

            for (int i = 0; i < industryTiles; i++) {
                addIndustryTile(city);
            }

            final int resourceTiles = random.nextInt(3);

            for (int i = 0; i < resourceTiles; i++) {
                addResourceTile(city);
            }

            city.writeToMap(world);
            cities[cityId] = city;
        }
    }

    private void addResourceTile(CityModel city) {
        int tileTypeNo = random.nextInt(resourceTerrainTypes.size());
        TerrainType type = resourceTerrainTypes.get(tileTypeNo);
        city.addTile(type);
    }

    private void addIndustryTile(CityModel city) {
        int size = city.industriesNotAtCity.size();

        if (size > 0) {
            int tileTypeNo = random.nextInt(size);
            TerrainType type = city.industriesNotAtCity.get(tileTypeNo);
            city.addTile(type);
        }
    }

    private void addUrbanTile(CityModel city) {
        int tileTypeNo = random.nextInt(urbanTerrainTypes.size());
        TerrainType type = urbanTerrainTypes.get(tileTypeNo);
        city.addTile(type);
    }

    void growCities() {
        final int numCities = world.size(SKEY.CITIES);

        /*
         * At some stage this will be refined to take into account how much
         * cargo has been picked up and delivered and what city tiles are
         * already present.
         */
        for (int cityId = 0; cityId < numCities; cityId++) {
            CityModel city = new CityModel();
            city.loadFromMap(world, cityId);

            // Only increase cities with stations and less than 16 tiles
            if (city.size() < 16 && city.stations > 0) {
                switch (random.nextInt(10)) {
                    case 0:
                    case 1:
                        addResourceTile(city); // 20% chance

                        break;

                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        addUrbanTile(city); // 40% chance

                        break;

                    case 6:
                        addIndustryTile(city); // 10% chance

                        break;

                    default:
                        // do nothing, 30% chance
                        break;
                }
                city.writeToMap(world);
            }
        }
    }
}