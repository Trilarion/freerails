/*
 * Created on Jun 26, 2004
 */
package jfreerails.network;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import jfreerails.util.Utils;
import jfreerails.world.top.WorldImpl;

/**
 * Stores saved games in memory rather than on disk.
 * 
 * @author Luke
 * 
 */
public class SavedGamesManager4UnitTests implements SavedGamesManager {
	private String[] mapsAvailable = { "map1", "map2" };

	private final HashMap<String, Serializable> savedGames = new HashMap<String, Serializable>();

	public String[] getSaveGameNames() {
		Object[] keys = savedGames.keySet().toArray();

		String[] names = new String[keys.length];

		for (int i = 0; i < names.length; i++) {
			names[i] = (String) keys[i];
		}

		return names;
	}

	public String[] getNewMapNames() {
		return mapsAvailable.clone();
	}

	public void saveGame(Serializable w, String name) throws IOException {
		// Make a copy so that the saved version's state cannot be changed.
		Serializable copy = Utils.cloneBySerialisation(w);
		this.savedGames.put(name, copy);
	}

	public Serializable loadGame(String name) throws IOException {
		Serializable o = savedGames.get(name);

		return Utils.cloneBySerialisation(o);
	}

	public Serializable newMap(String name) throws IOException {
		return new WorldImpl(10, 10);
	}
}