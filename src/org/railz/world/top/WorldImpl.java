/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.railz.world.top;

import java.awt.Point;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.railz.config.LogManager;
import org.railz.world.common.FreerailsSerializable;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.player.Player;
import org.railz.world.track.FreerailsTile;

/**
 * Implements storage for the world data. TODO At the moment all world data is
 * uniformly viewable by all parties. At some point in the future, instead of a
 * single WorldImpl accessible to all parties, different "Views" will be made
 * available for each party (server, and each connected client). Each client
 * will receive different moves according to their view. In this way, no client
 * will be able to view privileged information about other clients.
 */
public class WorldImpl implements World {

	/**
     * 
     */
	private static final long serialVersionUID = -2957312391788218266L;
	private static final String CLASS_NAME = WorldImpl.class.getName();
	private static final Logger LOGGER = LogManager.getLogger(CLASS_NAME);

	private static final boolean debug = (System
			.getProperty("org.railz.world.top.WorldImpl.debug") != null);

	/**
	 * An array of ArrayList indexed by keyNumber. If the key is shared, then
	 * the ArrayList consists of instances of the class corresponding to the KEY
	 * type. Otherwise, the ArrayList is indexed by Player index, and contains
	 * instances of ArrayList which themselves contain instances of the class
	 * corresponding to the KEY type.
	 */
	private final ArrayList[] lists = new ArrayList[KEY.getNumberOfKeys()];
	private final FreerailsSerializable[] items = new FreerailsSerializable[ITEM
			.getNumberOfKeys()];
	private FreerailsTile[][] map;

	public WorldImpl() {
		this.setupMap(0, 0);
		this.setupLists();
	}

	public WorldImpl(int mapWidth, int mapHeight) {
		this.setupMap(mapWidth, mapHeight);
		this.setupLists();
	}

	private WorldImpl(WorldImpl wi, FreerailsPrincipal viewer) {
		setupLists();
		int pi = wi.getPlayerIndex(viewer);
		for (int i = 0; i < lists.length; i++) {
			ArrayList al = wi.lists[i];
			KEY k = KEY.getKey(i);
			for (int j = 0; j < al.size(); j++) {
				if (k.isPrivate && pi != j) {
					lists[i].add(null);
				} else {
					lists[i].add(al.get(j));
				}
			}
		}
		for (int i = 0; i < wi.items.length; i++)
			items[i] = wi.items[i];
		map = wi.map;
	}

	synchronized ReadOnlyWorld getReadOnlyView(FreerailsPrincipal viewer) {
		return new WorldImpl(this, viewer);
	}

	public void setupMap(int mapWidth, int mapHeight) {
		map = new FreerailsTile[mapWidth][mapHeight];
	}

	public void setupLists() {
		for (int i = 0; i < lists.length; i++) {
			lists[i] = new ArrayList();
		}
	}

	@Override
	public FreerailsSerializable get(KEY key, int index) {
		return get(key, index, Player.NOBODY);
	}

	@Override
	public FreerailsSerializable get(KEY key, int index, FreerailsPrincipal p) {
		final String METHOD_NAME = "get";

		if (key.getKeyNumber() == 13) {
			String blah = null;
		}
		if (key.shared) {
			ArrayList list = lists[key.getKeyNumber()];
			return (FreerailsSerializable) list.get(index);
		}
		FreerailsSerializable frs = null;

		int keyNumber = key.getKeyNumber();
		int playerIndex = getPlayerIndex(p);

		if (keyNumber < lists.length) {
			ArrayList var = lists[keyNumber];

			if (playerIndex < var.size()) {
				ArrayList al = (ArrayList) var.get(playerIndex);
				if (index < al.size()) {
					frs = (FreerailsSerializable) al.get(index);
				} else {
					LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME,
							"OutOfBounds Attempt! Inside arraylist size = "
									+ al.size() + " while index is + " + index);
				}

			} else {
				LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME,
						"OutOfBounds Attempt! Middle arraylist list size = "
								+ var.size() + " while playerIndex is + "
								+ playerIndex);
			}

		} else {
			LOGGER.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME,
					"OutOfBounds Attempt! Outside list size = " + lists.length
							+ " while key number is + " + keyNumber);
		}

		return frs;
	}

	@Override
	public void set(KEY key, int index, FreerailsSerializable element) {
		set(key, index, element, Player.NOBODY);
	}

	@Override
	public void set(KEY key, int index, FreerailsSerializable element,
			FreerailsPrincipal p) {
		if (debug) {
			System.err.println("Setting " + element + " of type " + key
					+ " at index " + index + " for " + p);
		}

		if (key.shared) {
			lists[key.getKeyNumber()].set(index, element);

			return;
		}

		((ArrayList) lists[key.getKeyNumber()].get(getPlayerIndex(p))).set(
				index, element);
	}

	@Override
	public int add(KEY key, FreerailsSerializable element) {
		return add(key, element, Player.NOBODY);
	}

	@Override
	public int add(KEY key, FreerailsSerializable element, FreerailsPrincipal p) {
		if (debug) {
			System.err
					.println("Adding " + element + " to " + key + " for " + p);
		}

		if (key == KEY.PLAYERS) {
			return addPlayer((Player) element, p);
		}

		if (key.shared) {
			lists[key.getKeyNumber()].add(element);

			return size(key) - 1;
		}

		((ArrayList) lists[key.getKeyNumber()].get(getPlayerIndex(p)))
				.add(element);

		return size(key, p) - 1;
	}

	@Override
	public int size(KEY key) {
		return size(key, Player.NOBODY);
	}

	@Override
	public int size(KEY key, FreerailsPrincipal p) {
		if (key.shared) {
			return lists[key.getKeyNumber()].size();
		}

		return ((ArrayList) lists[key.getKeyNumber()].get(getPlayerIndex(p)))
				.size();
	}

	@Override
	public int getMapWidth() {
		return map.length;
	}

	@Override
	public int getMapHeight() {
		if (map.length == 0) {
			// When the map size is 0*0 we get a
			// java.lang.ArrayIndexOutOfBoundsException: 0
			// if we don't have check above.
			return 0;
		} else {
			return map[0].length;
		}
	}

	@Override
	public void setTile(int x, int y, FreerailsTile element) {
		map[x][y] = element;
	}

	@Override
	public FreerailsTile getTile(int x, int y) {
		final String METHOD_NAME = "getTile";
		if (x < map.length) {
			// FreerailsTile [] horizontal = map [x];
			if (y < map[x].length) {
				return map[x][y];
			} else {
				LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
						"y var is out of bounds for x=" + x + " and y=" + y);
				return null;
			}
		} else {
			LOGGER.logp(Level.WARNING, CLASS_NAME, METHOD_NAME,
					"x var is out of bounds for x=" + x + " and y=" + y);
			return null;
		}

	}

	@Override
	public FreerailsTile getTile(Point p) {
		return map[p.x][p.y];
	}

	@Override
	public boolean boundsContain(int x, int y) {
		if (x >= 0 && x < map.length && y >= 0 && y < map[0].length) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean boundsContain(KEY k, int index, FreerailsPrincipal p) {
		if (index >= 0 && index < this.size(k, p)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean boundsContain(KEY k, int index) {
		return boundsContain(k, index, Player.NOBODY);
	}

	@Override
	public FreerailsSerializable removeLast(KEY key) {
		return removeLast(key, Player.NOBODY);
	}

	@Override
	public FreerailsSerializable removeLast(KEY key, FreerailsPrincipal p) {
		if (debug) {
			System.err.println("Removing last " + key + " for " + p);
		}

		int size;

		if (key.shared) {
			size = lists[key.getKeyNumber()].size();
		} else {
			size = ((ArrayList) lists[key.getKeyNumber()]
					.get(getPlayerIndex(p))).size();
		}

		int index = size - 1;

		if (key.shared) {
			return (FreerailsSerializable) lists[key.getKeyNumber()]
					.remove(index);
		}

		return (FreerailsSerializable) ((ArrayList) lists[key.getKeyNumber()]
				.get(getPlayerIndex(p))).remove(index);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof WorldImpl) {
			WorldImpl test = (WorldImpl) o;

			if (lists.length != test.lists.length) {
				return false;
			} else {
				for (int i = 0; i < lists.length; i++) {
					if (!lists[i].equals(test.lists[i])) {
						return false;
					}
				}
			}

			if ((this.getMapWidth() != test.getMapWidth())
					|| (this.getMapHeight() != test.getMapHeight())) {
				return false;
			} else {
				for (int x = 0; x < this.getMapWidth(); x++) {
					for (int y = 0; y < this.getMapHeight(); y++) {
						if (!getTile(x, y).equals(test.getTile(x, y))) {
							return false;
						}
					}
				}
			}

			if (this.items.length != test.items.length) {
				return false;
			} else {
				for (int i = 0; i < this.items.length; i++) {
					// Some of the elements in the items array might be null, so
					// we check for this before
					// calling equals to avoid NullPointerExceptions.
					if (!(null == items[i] ? null == test.items[i] : items[i]
							.equals(test.items[i]))) {
						return false;
					}
				}
			}

			// phew!
			return true;
		} else {
			return false;
		}
	}

	@Override
	public FreerailsSerializable get(ITEM item) {
		return get(item, Player.NOBODY);
	}

	@Override
	public FreerailsSerializable get(ITEM item, FreerailsPrincipal p) {
		return items[item.getKeyNumber()];
	}

	@Override
	public void set(ITEM item, FreerailsSerializable element) {
		set(item, element, Player.NOBODY);
	}

	@Override
	public void set(ITEM item, FreerailsSerializable element,
			FreerailsPrincipal p) {
		items[item.getKeyNumber()] = element;
	}

	/**
	 * @param player
	 *            Player to add
	 * @param p
	 *            principal who is adding
	 * @return index of the player
	 */
	private int addPlayer(Player player, FreerailsPrincipal p) {
		if (p.equals(Player.NOBODY)) {
			// Player Nobody attempted to add a player
			return -1;
		}

		lists[KEY.PLAYERS.getKeyNumber()].add(player);

		int index = size(KEY.PLAYERS) - 1;

		for (int i = 0; i < KEY.getNumberOfKeys(); i++) {
			KEY key = KEY.getKey(i);

			if (key.shared != true) {
				while (lists[i].size() <= index) {
					lists[i].add(new ArrayList());
				}
			}
		}

		return index;
	}

	private static final int playerKey = KEY.PLAYERS.getKeyNumber();

	private int getPlayerIndex(FreerailsPrincipal p) {
		for (int i = 0; i < lists[playerKey].size(); i++) {
			if (p.equals(((Player) (lists[playerKey].get(i))).getPrincipal())) {
				return i;
			}
		}

		throw new ArrayIndexOutOfBoundsException("No matching principal for "
				+ p.toString());
	}

	private synchronized void writeObject(ObjectOutputStream out)
			throws IOException {
		out.defaultWriteObject();
	}

	private synchronized void readObject(ObjectInputStream in)
			throws IOException, ClassNotFoundException {
		in.defaultReadObject();
	}
}
