package freerails.server;

import freerails.world.terrain.TerrainType;
import freerails.world.top.SKEY;
import freerails.world.top.World;

import java.util.ArrayList;
import java.util.Random;

/**
 * This class initialises cities and controls their growth. It makes changes to
 * directly to the world object, so if the game has already started, use
 * WorldDifferences and MapDiffMove to pass changes to the clients.
 *
 * @author Luke
 */
public class CityTilePositioner {
    final Random random = new Random();

    final ArrayList<TerrainType> urbanTerrainTypes = new ArrayList<>();

    final ArrayList<TerrainType> industryTerrainTypes = new ArrayList<>();

    final ArrayList<TerrainType> resourceTerrainTypes = new ArrayList<>();

    final World w;

    /**
     *
     * @param w
     */
    public CityTilePositioner(World w) {
        this.w = w;

        // get the different types of Urban/Industry/Resource terrain
        for (int i = 0; i < w.size(SKEY.TERRAIN_TYPES); i++) {
            TerrainType type = (TerrainType) w.get(SKEY.TERRAIN_TYPES, i);
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
        final int numCities = w.size(SKEY.CITIES);
        CityEconomicModel[] cities = new CityEconomicModel[numCities];

        for (int cityId = 0; cityId < numCities; cityId++) {
            CityEconomicModel city = new CityEconomicModel();
            city.loadFromMap(w, cityId);

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

            city.write2map(w);
            cities[cityId] = city;
        }
    }

    private void addResourceTile(CityEconomicModel city) {
        int tileTypeNo = random.nextInt(resourceTerrainTypes.size());
        TerrainType type = resourceTerrainTypes.get(tileTypeNo);
        city.addTile(type);
    }

    private void addIndustryTile(CityEconomicModel city) {
        int size = city.industriesNotAtCity.size();

        if (size > 0) {
            int tileTypeNo = random.nextInt(size);
            TerrainType type = city.industriesNotAtCity.get(tileTypeNo);
            city.addTile(type);
        }
    }

    private void addUrbanTile(CityEconomicModel city) {
        int tileTypeNo = random.nextInt(urbanTerrainTypes.size());
        TerrainType type = urbanTerrainTypes.get(tileTypeNo);
        city.addTile(type);
    }

    void growCities() {
        final int numCities = w.size(SKEY.CITIES);

        /*
         * At some stage this will be refined to take into account how much
         * cargo has been picked up and delivered and what city tiles are
         * already present.
         */
        for (int cityId = 0; cityId < numCities; cityId++) {
            CityEconomicModel city = new CityEconomicModel();
            city.loadFromMap(w, cityId);

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

                city.write2map(w);
            }
        }
    }
}