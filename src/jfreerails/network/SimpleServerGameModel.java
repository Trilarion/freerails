/*
 * Created on Sep 12, 2004
 *
 */
package jfreerails.network;

import java.io.IOException;
import java.io.ObjectOutputStream;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;


/**
 *  A ServerGameModel that has a world object but no automation. 
 * 
 * @author Luke
 *
 */
public class SimpleServerGameModel implements ServerGameModel {
    private World w;

    public void setWorld(World w) {
        this.w = w;
    }

    public World getWorld() {
        return w;
    }

    public void init(MoveReceiver moveExecuter) {
    }

    public void write(ObjectOutputStream objectOut) throws IOException {
    }

    public void update() {
    }

    public void listUpdated(KEY key, int index, FreerailsPrincipal principal) {
    }

    public void itemAdded(KEY key, int index, FreerailsPrincipal principal) {
    }

    public void itemRemoved(KEY key, int index, FreerailsPrincipal principal) {
    }
}