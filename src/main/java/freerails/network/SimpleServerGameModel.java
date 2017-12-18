/*
 * Created on Sep 12, 2004
 *
 */
package freerails.network;

import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.KEY;
import freerails.world.top.World;

import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * A ServerGameModel that has a world object but no automation.
 *
 * @author Luke
 */
public class SimpleServerGameModel implements ServerGameModel {
    private static final long serialVersionUID = 3546074757457131826L;

    private World w;

    private String[] passwords;

    /**
     *
     * @param w
     * @param passwords
     */
    public void setWorld(World w, String[] passwords) {
        this.w = w;
        this.passwords = passwords.clone();
    }

    /**
     *
     * @return
     */
    public World getWorld() {
        return w;
    }

    /**
     *
     * @param moveExecuter
     */
    public void init(MoveReceiver moveExecuter) {
    }

    /**
     *
     * @param objectOut
     * @throws IOException
     */
    public void write(ObjectOutputStream objectOut) throws IOException {
    }

    /**
     *
     */
    public void update() {
    }

    /**
     *
     * @param key
     * @param index
     * @param principal
     */
    public void listUpdated(KEY key, int index, FreerailsPrincipal principal) {
    }

    /**
     *
     * @param key
     * @param index
     * @param principal
     */
    public void itemAdded(KEY key, int index, FreerailsPrincipal principal) {
    }

    /**
     *
     * @param key
     * @param index
     * @param principal
     */
    public void itemRemoved(KEY key, int index, FreerailsPrincipal principal) {
    }

    /**
     *
     * @return
     */
    public String[] getPasswords() {
        return passwords.clone();
    }

}