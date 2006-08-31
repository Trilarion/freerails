/*
 * Created on 24 Jul 2006
 * 
 */
package jfreerails.network;

import jfreerails.controller.Message2Server;
import jfreerails.controller.MessageStatus;
import jfreerails.controller.ServerControlInterface;

/** Tells the server to check the filesystem for changes to the available new maps and saved games.
 * @author Luke
 * */
public class RefreshListOfGamesMessage2Server implements Message2Server {
	
	private static final long serialVersionUID = -8745171955732354168L;
	private final int id;

	public MessageStatus execute(ServerControlInterface server) {		
		server.refreshSavedGames();
		return new MessageStatus(id, true);
	}

	public int getID() {
		return id;
	}

	public RefreshListOfGamesMessage2Server(final int id) {
		super();
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final RefreshListOfGamesMessage2Server other = (RefreshListOfGamesMessage2Server) obj;
		if (id != other.id)
			return false;
		return true;
	}

}
