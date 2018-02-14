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

import freerails.server.*;
import freerails.server.gamemodel.ServerGameModel;
import freerails.server.parser.CargoAndTerrainHandlerImpl;
import freerails.server.parser.CargoAndTerrainParser;
import freerails.server.parser.TrackTilesHandlerImpl;
import freerails.world.world.FullWorld;
import freerails.world.ITEM;
import freerails.world.world.World;
import freerails.world.game.GameCalendar;
import freerails.world.game.GameRules;
import freerails.world.game.GameSpeed;
import freerails.world.game.GameTime;
import freerails.world.train.WagonAndEngineTypesFactory;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.URL;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 */
public class FullSaveGameManager implements SaveGamesManager {

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

            CargoAndTerrainParser.parse(url, new CargoAndTerrainHandlerImpl(world));
        } catch (Exception e) {
            throw new IllegalStateException(e);
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
     * @param filePath
     * @param w
     * @throws IOException
     */
    public void saveGame(String filePath, Serializable w) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("Saving game..  " + filePath);

        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream);
        ObjectOutput objectOutputStream = new ObjectOutputStream(gzipOutputStream);

        objectOutputStream.writeObject(VERSION);
        objectOutputStream.writeObject(w);

        objectOutputStream.flush();
        objectOutputStream.close();
        fileOutputStream.close();

        long finishTime = System.currentTimeMillis();
        logger.info("done, " + (finishTime - startTime));
    }

    /**
     * @param filePath
     * @return
     * @throws IOException
     */
    public ServerGameModel loadGame(String filePath) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("Loading game..  " + filePath);

        FileInputStream fileInputStream = new FileInputStream(filePath);
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
            throw new IOException(e.getMessage());
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

        TrackTilesHandlerImpl trackSetFactory = new TrackTilesHandlerImpl(track_xml_url);

        trackSetFactory.addTrackRules(world);

        // Load the terrain map
        URL map_url = FullSaveGameManager.class.getResource("/freerails/data/" + mapName + ".png");
        MapFactory.setupMap(map_url, world, ProgressMonitorModel.EMPTY);

        // Load the city names
        URL cities_xml_url = FullSaveGameManager.class.getResource("/freerails/data/" + mapName + "_cities.xml");

        try {
            InputSource is = new InputSource(cities_xml_url.toString());

            DefaultHandler handler = new CitySAXParser(world);
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