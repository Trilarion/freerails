/*
 * Created on Sep 11, 2004
 *
 */
package jfreerails.server;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import jfreerails.network.NewGameServerCommand;
import jfreerails.network.SavedGamesManager;
import jfreerails.network.ServerControlInterface;
import jfreerails.util.FreerailsProgressMonitor;


/**
 * @author Luke
 *
 */
public class SavedGamesManagerImpl implements SavedGamesManager {
    private static final Logger logger = Logger.getLogger(SavedGamesManagerImpl.class.getName());

    public String[] getSaveGameNames() {
        return new String[0];
    }

    public String[] getNewMapNames() {
        return NewGameServerCommand.getMapNames();
    }

    public void saveGame(Serializable w, String s) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("Saving game..  " + s);

        FileOutputStream out = new FileOutputStream(s);
        GZIPOutputStream zipout = new GZIPOutputStream(out);

        ObjectOutputStream objectOut = new ObjectOutputStream(zipout);

        objectOut.writeObject(ServerControlInterface.VERSION);
        objectOut.writeObject(w);

        objectOut.flush();
        objectOut.close();
        out.close();

        long finishTime = System.currentTimeMillis();
        long deltaTime = finishTime - startTime;
        logger.info("done, " + deltaTime + "ms");
    }

    public Serializable loadGame(String name) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("Loading game..  " + name);

        FileInputStream in = new FileInputStream(name);
        GZIPInputStream zipin = new GZIPInputStream(in);
        ObjectInputStream objectIn = new ObjectInputStream(zipin);
        String version_string;

        try {
            version_string = (String)objectIn.readObject();

            if (!ServerControlInterface.VERSION.equals(version_string)) {
                throw new IOException(version_string);
            }

            Serializable game = (Serializable)objectIn.readObject();

            /**
             * load player private data
             */

            //            for (int i = 0; i < world.getNumberOfPlayers(); i++) {
            //                Player player = world.getPlayer(i);
            //                player.loadSession(objectIn);
            //            }
            long finishTime = System.currentTimeMillis();
            long deltaTime = finishTime - startTime;
            logger.info("done, " + deltaTime + "ms");

            return game;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    public Serializable newMap(String name) throws IOException {
        return OldWorldImpl.createWorldFromMapFile(name,
            FreerailsProgressMonitor.NULL_INSTANCE);
    }
}