/*
 * Created on Apr 18, 2004
 */
package jfreerails.network.specifics;

import jfreerails.controller.Message2Server;
import jfreerails.controller.MessageStatus;
import jfreerails.controller.ServerControlInterface;

/**
 * Request to start a game on a new map.
 * 
 * @author Luke
 * 
 */
public class NewGameMessage2Server implements Message2Server {
	private static final long serialVersionUID = 3256723961743422513L;

	private final int id;

	private final String mapName;

	public NewGameMessage2Server(int id, String s) {
		this.id = id;
		this.mapName = s;
	}

	public int getID() {
		return id;
	}

	public MessageStatus execute(ServerControlInterface server) {
		try {
			server.newGame(mapName);

			return new MessageStatus(id, true);
		} catch (Exception e) {
			return new MessageStatus(id, false, e.getMessage());
		}
	}

	/**
	 * TODO This would be better implemented in a config file, or better still
	 * dynamically determined by scanning the directory.
	 */
	public static String[] getMapNames() {
		return new String[] { "South America", "Small South America" };
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof NewGameMessage2Server))
			return false;

		final NewGameMessage2Server newGameMessage2Server = (NewGameMessage2Server) o;

		if (id != newGameMessage2Server.id)
			return false;
		if (!mapName.equals(newGameMessage2Server.mapName))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result;
		result = id;
		result = 29 * result + mapName.hashCode();
		return result;
	}
}