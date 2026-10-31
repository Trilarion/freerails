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
 * Applies the turn-based growth rules to cities with stations.
 */
public class CityGrowth {

    private final World world;
    private final Random random = new Random();
    private final CityTilePositioner tilePositioner;

    /**
     * Creates a city-growth service for the supplied world.
     *
     * @param world world whose cities may grow
     */
    public CityGrowth(World world) {
        this.world = world;
        tilePositioner = new CityTilePositioner(world, random);
    }

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
        for (City city : world.getCities()) {
            CityModel cityModel = new CityModel();
            cityModel.loadFromMap(world, city.getId());

            if (cityModel.getUrbanCityTiles().size() + cityModel.getIndustryCityTiles().size()
                    + cityModel.getResourceCityTiles().size() < 16 && cityModel.getStations() > 0) {
                switch (random.nextInt(10)) {
                    case 0:
                    case 1:
                        tilePositioner.addResourceTile(cityModel);
                        break;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        tilePositioner.addUrbanTile(cityModel);
                        break;
                    case 6:
                        tilePositioner.addIndustryTile(cityModel);
                        break;
                    default:
                        break;
                }
                cityModel.writeToMap(world);
            }
        }
    }
}
