/*
 * Created on Sep 11, 2004
 *
 */
package freerails.network;

import freerails.util.GameModel;
import freerails.world.top.World;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Defines methods on a GameModel that let the server load and initiate, and
 * save it.
 *
 * @author Luke
 */
public interface ServerGameModel extends GameModel, Serializable {

    /**
     *
     * @param w
     * @param passwords
     */
    void setWorld(World w, String[] passwords);

    /**
     *
     * @return
     */
    World getWorld();

    /**
     *
     * @return
     */
    String[] getPasswords();

    /**
     *
     * @param moveExecuter
     */
    void init(MoveReceiver moveExecuter);

    /**
     *
     * @param objectOut
     * @throws IOException
     */
    void write(ObjectOutputStream objectOut) throws IOException;

}