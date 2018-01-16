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
package freerails.server;

import freerails.client.ProgressMonitorModel;
import freerails.controller.ServerControlInterface;
import freerails.network.NewGameMessageToServer;
import freerails.network.SaveGamesManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 */
public class SaveGameManagerImpl implements SaveGamesManager {

    private static final Logger logger = Logger.getLogger(SaveGameManagerImpl.class.getName());

    /**
     * @return
     */
    public String[] getSaveGameNames() {
        java.io.File dir = new File("./");
        FilenameFilter filter = new SavedGameFileFilter();
        return dir.list(filter);
    }

    /**
     * @return
     */
    public String[] getNewMapNames() {
        return NewGameMessageToServer.getMapNames();
    }

    /**
     * @param w
     * @param s
     * @throws IOException
     */
    public void saveGame(Serializable w, String s) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("Saving game..  " + s);

        FileOutputStream out = new FileOutputStream(s);
        GZIPOutputStream zipout = new GZIPOutputStream(out);

        ObjectOutput objectOut = new ObjectOutputStream(zipout);

        objectOut.writeObject(ServerControlInterface.VERSION);
        objectOut.writeObject(w);

        objectOut.flush();
        objectOut.close();
        out.close();

        long finishTime = System.currentTimeMillis();
        long deltaTime = finishTime - startTime;
        logger.info("done, " + deltaTime + "ms");
    }

    /**
     * @param name
     * @return
     * @throws IOException
     */
    public Serializable loadGame(String name) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("Loading game..  " + name);

        FileInputStream in = new FileInputStream(name);
        GZIPInputStream zipin = new GZIPInputStream(in);
        ObjectInputStream objectIn = new ObjectInputStream(zipin);
        String version_string;

        try {
            version_string = (String) objectIn.readObject();

            if (!ServerControlInterface.VERSION.equals(version_string)) {
                throw new IOException(version_string);
            }

            Serializable game = (Serializable) objectIn.readObject();

            /*
              load player private data
             */

            // for (int i = 0; i < world.getNumberOfPlayers(); i++) {
            // Player player = world.getPlayer(i);
            // player.loadSession(objectIn);
            // }
            long finishTime = System.currentTimeMillis();
            long deltaTime = finishTime - startTime;
            logger.info("done, " + deltaTime + "ms");

            return game;
        } catch (ClassNotFoundException | InvalidClassException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * @param name
     * @return
     * @throws IOException
     */
    public Serializable newMap(String name) {
        return OldWorldImpl.createWorldFromMapFile(name, ProgressMonitorModel.EMPTY);
    }
}