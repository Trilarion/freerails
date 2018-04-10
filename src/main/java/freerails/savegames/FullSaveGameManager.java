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

import freerails.server.ServerGameModel;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 */
public class FullSaveGameManager implements SaveGamesManager {

    // TODO meaningful version string that actually changes if the savegame structure changes
    public static final String VERSION = "CVS";
    private static final Logger logger = Logger.getLogger(FullSaveGameManager.class.getName());

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

}