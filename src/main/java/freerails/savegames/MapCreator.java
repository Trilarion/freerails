package freerails.savegames;

import freerails.io.GsonManager;
import freerails.model.cargo.Cargo;
import freerails.model.game.GameCalendar;
import freerails.model.game.GameRules;
import freerails.model.game.GameSpeed;
import freerails.model.game.GameTime;
import freerails.model.terrain.*;
import freerails.model.train.Engine;
import freerails.model.world.World;
import freerails.model.world.WorldItem;
import freerails.util.Array2D;
import freerails.util.Vec2D;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 *
 */
public class MapCreator {

    private MapCreator() {}

    // TODO This would be better implemented in a config file, or better still dynamically determined by scanning the directory.
    public static String[] getAvailableMapNames() {
        return new String[]{"South America", "Small South America"};
    }

    /**
     * Note, the map name is converted to lower case and any spaces are replaced
     * with underscores.
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public static World newMap(String filePath) throws URISyntaxException, IOException {
        String mapName = filePath;

        mapName = mapName.toLowerCase();
        mapName = mapName.replace(' ', '_');

        // load engines
        URL url = MapCreator.class.getResource("/freerails/data/scenario/engines.json");
        File file = new File(url.toURI());
        SortedSet<Engine> engines = GsonManager.loadEngines(file);

        // load cities
        url = MapCreator.class.getResource("/freerails/data/scenario/" + mapName + "_cities.json");
        file = new File(url.toURI());
        SortedSet<City> cities = GsonManager.loadCities(file);

        // load cargo types
        url = MapCreator.class.getResource("/freerails/data/scenario/cargo_types.json");
        file = new File(url.toURI());
        SortedSet<Cargo> cargos = GsonManager.loadCargoTypes(file);

        // load terrain types
        url = MapCreator.class.getResource("/freerails/data/scenario/terrain_types.json");
        file = new File(url.toURI());
        SortedSet<Terrain> terrainTypes = GsonManager.loadTerrainTypes(file);

        World.Builder builder = new World.Builder().setEngines(engines).setCities(cities).setCargos(cargos).setTerrainTypes(terrainTypes);
        World world = builder.build();

        URL track_xml_url = MapCreator.class.getResource("/freerails/data/track_tiles.xml");

        TrackTilesXmlHandlerImpl trackSetFactory = new TrackTilesXmlHandlerImpl(track_xml_url);

        trackSetFactory.addTrackRules(world);

        /*
        // Load the terrain map
        URL map_url = MapCreator.class.getResource("/freerails/data/" + mapName + ".png");

        // converts an image file into a map.
        // Implemented Terrain Randomisation to randomly position the terrain types for each tile on the map.

        Image mapImage = (new ImageIcon(map_url)).getImage();
        Rectangle mapRect = new Rectangle(0, 0, mapImage.getWidth(null), mapImage.getHeight(null));
        BufferedImage mapBufferedImage = new BufferedImage(mapRect.width, mapRect.height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = mapBufferedImage.getGraphics();
        g.drawImage(mapImage, 0, 0, null);

        Map<Integer, Integer> rgb2TerrainType = new HashMap<>();

        for (int i = 0; i < world.getTerrains().size(); i++) {
            TerrainType terrainType = (TerrainType) world.get(SharedKey.TrackRules, i);
            rgb2TerrainType.put(terrainType.getRGB(), i);
        }
        */


        // Load the terrain map

        /*
        // special code to store terrain type from map in json
        final int width = mapImage.getWidth(null);
        final int height = mapImage.getHeight(null);
        Array2D map = new Array2D(width, height);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int rgb = mapBufferedImage.getRGB(j, i);
                map.set(j, i, rgb2TerrainType.get(rgb));
            }
        }
        file = new File(mapName + "_map.json");
        GsonManager.saveCompact(file, map);
        */

        url = MapCreator.class.getResource("/freerails/data/scenario/" + mapName + "_map.json");
        file = new File(url.toURI());
        Array2D map = GsonManager.loadArray2D(file);

        world.setupMap(map.getSize());

        // TODO what is the purpose of the following section (randomization)
        final List<Integer> countryTypes = new ArrayList();
        final List<Integer> non_countryTypes = new ArrayList();

        for (Terrain terrainType: world.getTerrains()) {

            if (terrainType.getCategory() == TerrainCategory.COUNTRY) {
                if ((!terrainType.getName().equals("Clear"))) {
                    countryTypes.add(terrainType.getId());
                }
            }

            if (terrainType.getCategory() == TerrainCategory.OCEAN || terrainType.getCategory() == TerrainCategory.RIVER || terrainType.getCategory() == TerrainCategory.HILL) {
                non_countryTypes.add(terrainType.getId());
            }
        }

        TerrainRandomizer terrainRandomizer = new TerrainRandomizer(countryTypes, non_countryTypes);

        /*
         * create vector to keep track of terrain randomisation 'clumping'
         */
        List<TerrainAtLocation> locations = new ArrayList();

        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                TerrainTile tile;
                Integer type = map.get(x, y);

                tile = new TerrainTile(terrainRandomizer.getNewType(type));
                Vec2D location = new Vec2D(x, y);
                if (countryTypes.contains(tile.getTerrainTypeId())) {
                    locations.add(new TerrainAtLocation(location, tile.getTerrainTypeId()));
                }

                world.setTile(location, tile);
            }
        }

        for (TerrainAtLocation terrainAtLocation : locations) {
            TerrainTile tile = new TerrainTile(terrainAtLocation.getType());

            Vec2D location = terrainAtLocation.getLocation();
            int val = 3;
            Vec2D v = new Vec2D(val, val);

            double prob = 0.75;

            if (world.boundsContain(Vec2D.subtract(location, v)) && world.boundsContain(Vec2D.add(location, v))) {
                for (int m = location.x - val; m < location.x + val; m++) {
                    for (int n = location.y - val; n < location.y + val; n++) {
                        if (Math.random() > prob) {
                            Vec2D p = new Vec2D(m, n);
                            if (!non_countryTypes.contains(world.getTile(p).getTerrainTypeId())) {
                                world.setTile(p, tile);
                            }
                        }
                    }
                }
            }
        }

        // Randomly position the city tiles
        CityTilePositioner cityTilePositioner = new CityTilePositioner(world);
        cityTilePositioner.initCities();

        // Set the time..
        world.set(WorldItem.Calendar, new GameCalendar(1200, 1840));
        // TODO is this necessary, should be this by default
        world.setTime(new GameTime(0));
        world.set(WorldItem.GameSpeed, new GameSpeed(10));
        world.set(WorldItem.GameRules, GameRules.DEFAULT_RULES);

        /*
         * Note, money used to get added to player finances here, now it is done
         * when players are added. See AddPlayerMove
         */
        return world;
    }

}
