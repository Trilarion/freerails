package freerails.savegames;

import freerails.model.game.GameCalendar;
import freerails.model.game.GameRules;
import freerails.model.game.GameSpeed;
import freerails.model.game.GameTime;
import freerails.model.terrain.*;
import freerails.model.train.WagonAndEngineTypesFactory;
import freerails.model.world.FullWorld;
import freerails.model.world.World;
import freerails.model.world.WorldItem;
import freerails.model.world.SharedKey;
import freerails.util.Vec2D;
import freerails.util.ui.ProgressMonitorModel;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public static World newMap(String filePath) {
        String mapName = filePath;

        mapName = mapName.toLowerCase();
        mapName = mapName.replace(' ', '_');

        FullWorld world = new FullWorld();

        WagonAndEngineTypesFactory.addTypesToWorld(world);

        addTerrainTileTypesList(world);

        URL track_xml_url = FullSaveGameManager.class.getResource("/freerails/data/track_tiles.xml");

        TrackTilesXmlHandlerImpl trackSetFactory = new TrackTilesXmlHandlerImpl(track_xml_url);

        trackSetFactory.addTrackRules(world);

        // Load the terrain map
        URL map_url = FullSaveGameManager.class.getResource("/freerails/data/" + mapName + ".png");

        // converts an image file into a map.
        // Implemented Terrain Randomisation to randomly position the terrain types for each tile on the map.
        final List<Integer> countryTypes = new ArrayList();
        final List<Integer> non_countryTypes = new ArrayList();


        // Setup progress monitor..
        ProgressMonitorModel.EMPTY.setValue(0);

        Image mapImage = (new ImageIcon(map_url)).getImage();
        Rectangle mapRect = new Rectangle(0, 0, mapImage.getWidth(null), mapImage.getHeight(null));
        BufferedImage mapBufferedImage = new BufferedImage(mapRect.width, mapRect.height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = mapBufferedImage.getGraphics();
        g.drawImage(mapImage, 0, 0, null);
        world.setupMap(new Vec2D(mapRect.width, mapRect.height));

        ProgressMonitorModel.EMPTY.nextStep(mapRect.width);

        Map<Integer, Integer> rgb2TerrainType = new HashMap<>();

        for (int i = 0; i < world.size(SharedKey.TerrainTypes); i++) {
            TerrainType terrainType = (TerrainType) world.get(SharedKey.TerrainTypes, i);
            rgb2TerrainType.put(terrainType.getRGB(), i);
        }

        TerrainType terrainTypeTile;

        for (int c = 0; c < world.size(SharedKey.TerrainTypes); c++) {
            terrainTypeTile = (TerrainType) world.get(SharedKey.TerrainTypes, c);

            if (terrainTypeTile.getCategory() == TerrainCategory.Country) {
                if ((!terrainTypeTile.getTerrainTypeName().equals("Clear"))) {
                    countryTypes.add(c);
                }
            }

            if (terrainTypeTile.getCategory() == TerrainCategory.Ocean || terrainTypeTile.getCategory() == TerrainCategory.River || terrainTypeTile.getCategory() == TerrainCategory.Hill) {
                non_countryTypes.add(c);
            }
        }

        TerrainRandomizer terrainRandomizer = new TerrainRandomizer(countryTypes, non_countryTypes);

        /*
         * create vector to keep track of terrain randomisation 'clumping'
         */
        List<TerrainAtLocation> locations = new ArrayList();

        for (int x = 0; x < mapRect.width; x++) {
            ProgressMonitorModel.EMPTY.setValue(x);

            for (int y = 0; y < mapRect.height; y++) {
                int rgb = mapBufferedImage.getRGB(x, y);
                FullTerrainTile tile;
                Integer type = rgb2TerrainType.get(rgb);

                if (null == type) {
                    throw new NullPointerException("There is no terrain type mapped to rgb value " + rgb + " at location " + x + ", " + y);
                }

                tile = FullTerrainTile.getInstance(terrainRandomizer.getNewType(type));
                Vec2D location = new Vec2D(x, y);
                if (countryTypes.contains(tile.getTerrainTypeID())) {
                    locations.add(new TerrainAtLocation(location, tile.getTerrainTypeID()));
                }

                world.setTile(location, tile);
            }
        }

        for (TerrainAtLocation terrainAtLocation : locations) {
            FullTerrainTile tile = FullTerrainTile.getInstance(terrainAtLocation.getType());

            Vec2D location = terrainAtLocation.getLocation();
            int val = 3;
            Vec2D v = new Vec2D(val, val);

            double prob = 0.75;

            if (world.boundsContain(Vec2D.subtract(location, v)) && world.boundsContain(Vec2D.add(location, v))) {
                for (int m = location.x - val; m < location.x + val; m++) {
                    for (int n = location.y - val; n < location.y + val; n++) {
                        if (Math.random() > prob) {
                            Vec2D p = new Vec2D(m, n);
                            if (!non_countryTypes.contains(((FullTerrainTile) world.getTile(p)).getTerrainTypeID())) {
                                world.setTile(p, tile);
                            }
                        }
                    }
                }
            }
        }

        // Load the city names
        URL cities_xml_url = FullSaveGameManager.class.getResource("/freerails/data/" + mapName + "_cities.xml");

        try {
            InputSource is = new InputSource(cities_xml_url.toString());

            DefaultHandler handler = new CityXmlParser(world);
            SAXParserFactory factory = SAXParserFactory.newInstance();

            try {
                SAXParser saxParser = factory.newSAXParser();
                saxParser.parse(is, handler);
            } catch (IOException | ParserConfigurationException ignored) {}
        } catch (SAXException ignored) {}

        // Randomly position the city tiles
        CityTilePositioner cityTilePositioner = new CityTilePositioner(world);
        cityTilePositioner.initCities();

        // Set the time..
        world.set(WorldItem.Calendar, new GameCalendar(1200, 1840));
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
            URL url = FullSaveGameManager.class.getResource("/freerails/data/cargo_and_terrain.xml");

            CargoAndTerrainXmlParser.parse(url, new CargoAndTerrainXmlHandlerImpl(world));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
