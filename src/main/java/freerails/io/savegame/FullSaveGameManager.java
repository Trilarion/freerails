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

/*
 *
 */
package freerails.io.savegame;

import freerails.server.SaveGamesManager;
import freerails.server.ServerGameModel;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Loads and saves complete server game models in the configured save-game directory.
 */
public class FullSaveGameManager implements SaveGamesManager {

    // TODO meaningful version string that actually changes if the savegame structure changes
    /** Format version stored with each save-game file. */
    public static final String VERSION = "CVS";
    private static final TaggedLogger logger = Logger.tag("FullSaveGameManager");

    /**
     * Returns the names of save-game files in the configured directory.
     *
     * @return the available save-game names, or {@code null} if listing fails
     */
    @Override
    public String[] getSaveGameNames() {
        // TODO default location for save games, anyway use different file open dialogs
        java.io.File dir = new File("./");
        FilenameFilter filter = new SavedGameFileFilter();
        return dir.list(filter);
    }

    /**
     * Serializes a game object to a compressed save-game file.
     *
     * @param path the path of the save-game file
     * @param serializable the object to serialize to the save-game file
     * @throws IOException if the operation cannot complete because of an input/output error
     */
    @Override
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
     * Loads and decompresses a server game model from a save-game file.
     *
     * @param path the path of the save-game file
     * @return the loaded server game model
     * @throws IOException if the operation cannot complete because of an input/output error
     */
    @Override
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

}