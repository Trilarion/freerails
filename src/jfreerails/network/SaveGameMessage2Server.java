/*
 * Created on Apr 18, 2004
 */
package jfreerails.network;

import jfreerails.controller.Message2Server;
import jfreerails.controller.MessageStatus;
import jfreerails.controller.ServerControlInterface;

/**
 * A request to save the game.
 * 
 * @author Luke
 * 
 */
public class SaveGameMessage2Server implements Message2Server {
	private static final long serialVersionUID = 3257281452725777209L;

	private final int id;

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof SaveGameMessage2Server))
			return false;

		final SaveGameMessage2Server saveGameMessage2Server = (SaveGameMessage2Server) o;

		if (id != saveGameMessage2Server.id)
			return false;
		if (!filename.equals(saveGameMessage2Server.filename))
			return false;

		return true;
	}

	public int hashCode() {
		int result;
		result = id;
		result = 29 * result + filename.hashCode();
		return result;
	}

	private final String filename;

	public SaveGameMessage2Server(int id, String s) {
		this.id = id;
		this.filename = s;
	}

	public int getID() {
		return id;
	}

	public MessageStatus execute(ServerControlInterface server) {
		try {
			server.savegame(filename);

			return new MessageStatus(id, true);
		} catch (Exception e) {
			return new MessageStatus(id, false, e.getMessage());
		}
	}
}