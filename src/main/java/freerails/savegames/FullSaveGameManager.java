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

/*
 *
 */
package freerails.savegames;

import freerails.server.CityTilePositioner;
import freerails.server.ServerGameModel;
import freerails.util.Vector2D;
import freerails.util.ui.ProgressMonitorModel;
import freerails.model.SKEY;
import freerails.model.terrain.*;
import freerails.model.world.FullWorld;
import freerails.model.ITEM;
import freerails.model.world.World;
import freerails.model.game.GameCalendar;
import freerails.model.game.GameRules;
import freerails.model.game.GameSpeed;
import freerails.model.game.GameTime;
import freerails.model.train.WagonAndEngineTypesFactory;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 */
public class FullSaveGameManager implements SaveGamesManager {

    // TODO meaning ful version string that actually changes if the savegame structure changes
    public static final String VERSION = "CVS";
    private static final Logger logger = Logger.getLogger(FullSaveGameManager.class.getName());

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

    /**
     * converts an image file into a map.
     *
     * Implemented Terrain Randomisation to randomly position the terrain types for each tile on the map.
     *
     * @param map_url
     * @param world
     * @param pm
     */
    public static void setupMap(URL map_url, FullWorld world, ProgressMonitorModel pm) {
        final List<Integer> countryTypes = new ArrayList();
        final List<Integer> non_countryTypes = new ArrayList();


        // Setup progress monitor..
        pm.setValue(0);

        Image mapImage = (new ImageIcon(map_url)).getImage();
        Rectangle mapRect = new Rectangle(0, 0, mapImage.getWidth(null), mapImage.getHeight(null));
        BufferedImage mapBufferedImage = new BufferedImage(mapRect.width, mapRect.height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = mapBufferedImage.getGraphics();
        g.drawImage(mapImage, 0, 0, null);
        world.setupMap(mapRect.width, mapRect.height);

        pm.nextStep(mapRect.width);

        Map<Integer, Integer> rgb2TerrainType = new HashMap<>();

        for (int i = 0; i < world.size(SKEY.TERRAIN_TYPES); i++) {
            TerrainType terrainType = (TerrainType) world.get(SKEY.TERRAIN_TYPES, i);
            rgb2TerrainType.put(terrainType.getRGB(), i);
        }

        TerrainType terrainTypeTile;

        for (int c = 0; c < world.size(SKEY.TERRAIN_TYPES); c++) {
            terrainTypeTile = (TerrainType) world.get(SKEY.TERRAIN_TYPES, c);

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
            pm.setValue(x);

            for (int y = 0; y < mapRect.height; y++) {
                int rgb = mapBufferedImage.getRGB(x, y);
                FullTerrainTile tile;
                Integer type = rgb2TerrainType.get(rgb);

                if (null == type) {
                    throw new NullPointerException("There is no terrain type mapped to rgb value " + rgb + " at location " + x + ", " + y);
                }

                tile = FullTerrainTile.getInstance(terrainRandomizer.getNewType(type));
                Vector2D location = new Vector2D(x, y);
                if (countryTypes.contains(tile.getTerrainTypeID())) {
                    locations.add(new TerrainAtLocation(location, tile.getTerrainTypeID()));
                }

                world.setTile(location, tile);
            }
        }

        for (TerrainAtLocation terrainAtLocation : locations) {
            FullTerrainTile tile = FullTerrainTile.getInstance(terrainAtLocation.getType());

            Vector2D location = terrainAtLocation.getLocation();
            int val = 3;
            Vector2D v = new Vector2D(val, val);

            double prob = 0.75;

            if (world.boundsContain(Vector2D.subtract(location, v)) && world.boundsContain(Vector2D.add(location, v))) {
                for (int m = location.x - val; m < location.x + val; m++) {
                    for (int n = location.y - val; n < location.y + val; n++) {
                        if (Math.random() > prob) {
                            Vector2D p = new Vector2D(m, n);
                            if (!non_countryTypes.contains(((FullTerrainTile) world.getTile(p)).getTerrainTypeID())) {
                                world.setTile(p, tile);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @return
     */
    public String[] getSaveGameNames() {
        // TODO default location for save games, anyway use different file open dialogs
        java.io.File dir = new File("./");
        FilenameFilter filter = new SavedGameFileFilter();
        return dir.list(filter);
    }

    /**
     * @return
     */
    public String[] getNewMapNames() {
        return getMapNames();
    }

    // TODO This would be better implemented in a config file, or better still dynamically determined by scanning the directory.

    public static String[] getMapNames() {
        return new String[]{"South America", "Small South America"};
    }
    /**
     * @param path
     * @param serializable
     * @throws IOException
     */
    public void saveGame(String path, Serializable serializable) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("Saving game..  " + path);

        FileOutputStream fileOutputStream = new FileOutputStream(path);
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream);
        ObjectOutput objectOutputStream = new ObjectOutputStream(gzipOutputStream);

        objectOutputStream.writeObject(VERSION);
        objectOutputStream.writeObject(serializable);

        objectOutputStream.flush();
        objectOutputStream.close();
        fileOutputStream.close();

        long finishTime = System.currentTimeMillis();
        logger.info("done, " + (finishTime - startTime));
    }

    // check that load game really works
    /**
     * @param path
     * @return
     * @throws IOException
     */
    public ServerGameModel loadGame(String path) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("Loading game..  " + path);

        FileInputStream fileInputStream = new FileInputStream(path);
        GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
        ObjectInputStream objectInputStream = new ObjectInputStream(gzipInputStream);

        try {
            String version = (String) objectInputStream.readObject();

            if (!VERSION.equals(version)) {
                throw new IOException("Unknown save game version: " + version);
            }

            ServerGameModel serverGameModel = (ServerGameModel) objectInputStream.readObject();

            long finishTime = System.currentTimeMillis();
            logger.info("done, " + (finishTime - startTime));

            return serverGameModel;
        } catch (ClassNotFoundException | InvalidClassException e) {
            throw new IOException(e);
        }
    }

    /**
     * Note, the map name is converted to lower case and any spaces are replaced
     * with underscores.
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public World newMap(String filePath) {
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
        setupMap(map_url, world, ProgressMonitorModel.EMPTY);

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
        world.set(ITEM.CALENDAR, new GameCalendar(1200, 1840));
        world.setTime(new GameTime(0));
        world.set(ITEM.GAME_SPEED, new GameSpeed(10));
        world.set(ITEM.GAME_RULES, GameRules.DEFAULT_RULES);

        /*
         * Note, money used to get added to player finances here, now it is done
         * when players are added. See AddPlayerMove
         */
        return world;
    }

}