/*
 * Created on Sep 11, 2004
 *
 */
package jfreerails.network.specifics;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import jfreerails.util.GameModel;
import jfreerails.world.top.World;

/**
 * Defines methods on a GameModel that let the server load and initiate, and
 * save it.
 * 
 * @author Luke
 * 
 */
public interface ServerGameModel extends GameModel, Serializable {
	void setWorld(World w, String[] passwords);

	World getWorld();

	String[] getPasswords();

	void init(MoveReceiver moveExecuter);

	void write(ObjectOutputStream objectOut) throws IOException;

}