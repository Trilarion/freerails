/*
 * Created on Apr 18, 2004
 */
package jfreerails.network.specifics;

import jfreerails.controller.ClientControlInterface;
import jfreerails.controller.Message2Client;
import jfreerails.controller.MessageStatus;
import jfreerails.util.Immutable;
import jfreerails.world.top.World;

/**
 * Sent from the server to the client when (i) a new game is started, (ii) a
 * game is loaded, or (iii) the client connects to a game in progress.
 * 
 * @author Luke
 * 
 */
@Immutable
public class SetWorldMessage2Client implements Message2Client {
	private static final long serialVersionUID = 3257570619972269362L;

	private final int id;

	private final World world;

	/**
	 * Note, makes a defensive copy of the world object passed to it.
	 */
	public SetWorldMessage2Client(int id, World world) {
		this.id = id;
		this.world = world.defensiveCopy();
	}

	public MessageStatus execute(ClientControlInterface client) {
		client.setGameModel(world.defensiveCopy());

		return new MessageStatus(id, true);
	}

	public int getID() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof SetWorldMessage2Client))
			return false;

		final SetWorldMessage2Client setWorldMessage2Client = (SetWorldMessage2Client) o;

		if (id != setWorldMessage2Client.id)
			return false;
		if (!world.equals(setWorldMessage2Client.world))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result;
		result = id;
		result = 29 * result + world.hashCode();
		return result;
	}
}