/*
 * Created on Apr 18, 2004
 */
package jfreerails.network;

import jfreerails.world.top.World;


/**
 * Sent from the server to the client when (i) a new game is started, (ii) a game is loaded, or (iii)
 * the client connects to a game in progress.
 *  @author Luke
 *
 */
public class SetWorldMessage2Client implements Message2Client {
    private static final long serialVersionUID = 3257570619972269362L;
	private final int id;
    private final World world;

    /** Note, makes a defensive copy of the
     * world object passed to it.
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
}