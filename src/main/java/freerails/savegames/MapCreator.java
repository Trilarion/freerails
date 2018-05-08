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
import freerails.model.world.SharedKey;
import freerails.util.Vec2D;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.List;

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
        SortedSet<TerrainType2> terrainTypes = GsonManager.loadTerrainTypes(file);

        World.Builder builder = new World.Builder().setEngines(engines).setCities(cities).setCargos(cargos).setTerrainTypes(terrainTypes);
        World world = builder.build();

        addTerrainTileTypesList(world);

        URL track_xml_url = MapCreator.class.getResource("/freerails/data/track_tiles.xml");

        TrackTilesXmlHandlerImpl trackSetFactory = new TrackTilesXmlHandlerImpl(track_xml_url);

        trackSetFactory.addTrackRules(world);

        // Load the terrain map
        URL map_url = MapCreator.class.getResource("/freerails/data/" + mapName + ".png");

        // converts an image file into a map.
        // Implemented Terrain Randomisation to randomly position the terrain types for each tile on the map.


        Image mapImage = (new ImageIcon(map_url)).getImage();
        Rectangle mapRect = new Rectangle(0, 0, mapImage.getWidth(null), mapImage.getHeight(null));
        BufferedImage mapBufferedImage = new BufferedImage(mapRect.width, mapRect.height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = mapBufferedImage.getGraphics();
        g.drawImage(mapImage, 0, 0, null);
        world.setupMap(new Vec2D(mapRect.width, mapRect.height));

        Map<Integer, Integer> rgb2TerrainType = new HashMap<>();

        for (int i = 0; i < world.size(SharedKey.TerrainTypes); i++) {
            TerrainType terrainType = (TerrainType) world.get(SharedKey.TerrainTypes, i);
            rgb2TerrainType.put(terrainType.getRGB(), i);
        }

        // TODO what is the purpose of the following section
        TerrainType terrainTypeTile;
        final List<Integer> countryTypes = new ArrayList();
        final List<Integer> non_countryTypes = new ArrayList();

        for (int c = 0; c < world.size(SharedKey.TerrainTypes); c++) {
            terrainTypeTile = (TerrainType) world.get(SharedKey.TerrainTypes, c);

            if (terrainTypeTile.getCategory() == TerrainCategory.COUNTRY) {
                if ((!terrainTypeTile.getTerrainTypeName().equals("Clear"))) {
                    countryTypes.add(c);
                }
            }

            if (terrainTypeTile.getCategory() == TerrainCategory.OCEAN || terrainTypeTile.getCategory() == TerrainCategory.RIVER || terrainTypeTile.getCategory() == TerrainCategory.HILL) {
                non_countryTypes.add(c);
            }
        }

        TerrainRandomizer terrainRandomizer = new TerrainRandomizer(countryTypes, non_countryTypes);

        /*
         * create vector to keep track of terrain randomisation 'clumping'
         */
        List<TerrainAtLocation> locations = new ArrayList();

        for (int x = 0; x < mapRect.width; x++) {
            for (int y = 0; y < mapRect.height; y++) {
                int rgb = mapBufferedImage.getRGB(x, y);
                TerrainTile tile;
                Integer type = rgb2TerrainType.get(rgb);

                if (null == type) {
                    throw new NullPointerException("There is no terrain type mapped to rgb value " + rgb + " at location " + x + ", " + y);
                }

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

    /**
     * Adds cargo and terrain types defined in an XML file to a World
     *
     * @param world
     */
    public static void addTerrainTileTypesList(World world) {
        try {
            URL url = MapCreator.class.getResource("/freerails/data/cargo_and_terrain.xml");

            CargoAndTerrainXmlParser.parse(url, new CargoAndTerrainXmlHandlerImpl(world));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        /*
        // convert cargotypes from xml to json
        Map<CargoCategory, CargoCategory> conversion = new HashMap<>();
        conversion.put(CargoCategory.MAIL, CargoCategory.MAIL);
        conversion.put(CargoCategory.PASSENGER, CargoCategory.PASSENGER);
        conversion.put(CargoCategory.FAST_FREIGHT, CargoCategory.FAST_FREIGHT);
        conversion.put(CargoCategory.BULK_FREIGHT, CargoCategory.BULK_FREIGHT);
        conversion.put(CargoCategory.SLOW_FREIGHT, CargoCategory.SLOW_FREIGHT);

        File file = new File("cargo_types.json");
        SortedSet<CargoType2> cargoTypes = new TreeSet<>();
        for (int i = 0; i < world.getCargoTypes().size(); i++) {
            CargoType a = (CargoType) world.get(SharedKey.CargoTypes, i);
            CargoType2 b = new CargoType2(i, a.getName(), conversion.get(a.getCategory()), a.getUnitWeight());
            cargoTypes.add(b);
        }
        try {
            GsonManager.save(file, cargoTypes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        */

        // convert terraintypes from xml to json
        File file = new File("terrain_types.json");
        //File f2 = new File("terrain_colors.json");
        SortedSet<TerrainType2> terrainTypes = new TreeSet<>();
        //Map<Integer, ARGBColor> rgbMap = new HashMap<>();
        for (int i = 0; i < world.size(SharedKey.TerrainTypes); i++) {
            TerrainType a = (TerrainType) world.get(SharedKey.TerrainTypes, i);
            TerrainType2 b = new TerrainType2(i, a.getTerrainTypeName(), a.getCategory());
            terrainTypes.add(b);
            //rgbMap.put(i, new ARGBColor(a.getRGB()));
        }
        try {
            GsonManager.save(file, terrainTypes);
            // GsonManager.save(f2, rgbMap);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
}
