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
public class SetWorldClientCommand implements ClientCommand {
    private final int id;
    private final World world;

    /** Note, makes a defensive copy of the
     * world object passed to it.
     */
    public SetWorldClientCommand(int id, World world) {
        this.id = id;
        this.world = world.defensiveCopy();
    }

    public CommandStatus execute(ClientControlInterface client) {
        client.setGameModel(world.defensiveCopy());

        return new CommandStatus(id, true);
    }

    public int getID() {
        return id;
    }
}