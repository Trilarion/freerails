package jfreerails.controller;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.zip.GZIPInputStream;
import jfreerails.world.common.FreerailsSerializable;


/**
 * Abstract root class from which client-server commands are subclassed.
 * @author rob
 */
public abstract class ServerCommand implements FreerailsSerializable {
    public static final String FREERAILS_SAV = "freerails.sav";
    public static final String VERSION = "CVS";

    public static boolean isSaveGameAvailable() {
        try {
            FileInputStream in = new FileInputStream(ServerCommand.FREERAILS_SAV);
            GZIPInputStream zipin = new GZIPInputStream(in);
            ObjectInputStream objectIn = new ObjectInputStream(zipin);
            String version_string = (String)objectIn.readObject();

            if (!ServerCommand.VERSION.equals(version_string)) {
                throw new Exception(version_string);
            }

            in.close();

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}