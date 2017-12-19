package freerails.network;

import freerails.util.Utils;
import freerails.world.top.WorldImpl;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Stores saved games in memory rather than on disk.
 *
 */
public class SavedGamesManager4UnitTests implements SavedGamesManager {
    private final String[] mapsAvailable = {"map1", "map2"};

    private final HashMap<String, Serializable> savedGames = new HashMap<>();

    /**
     *
     * @return
     */
    public String[] getSaveGameNames() {
        Object[] keys = savedGames.keySet().toArray();

        String[] names = new String[keys.length];

        for (int i = 0; i < names.length; i++) {
            names[i] = (String) keys[i];
        }

        return names;
    }

    /**
     *
     * @return
     */
    public String[] getNewMapNames() {
        return mapsAvailable.clone();
    }

    /**
     *
     * @param w
     * @param name
     * @throws IOException
     */
    public void saveGame(Serializable w, String name) throws IOException {
        // Make a copy so that the saved version's state cannot be changed.
        Serializable copy = Utils.cloneBySerialisation(w);
        this.savedGames.put(name, copy);
    }

    /**
     *
     * @param name
     * @return
     * @throws IOException
     */
    public Serializable loadGame(String name) throws IOException {
        Serializable o = savedGames.get(name);

        return Utils.cloneBySerialisation(o);
    }

    /**
     *
     * @param name
     * @return
     * @throws IOException
     */
    public Serializable newMap(String name) throws IOException {
        return new WorldImpl(10, 10);
    }
}