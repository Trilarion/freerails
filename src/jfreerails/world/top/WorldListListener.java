/*
 * Created on 25-Aug-2003
 *
 */
package jfreerails.world.top;

import jfreerails.world.player.FreerailsPrincipal;


/**
 * Classes that need to be notified of changes to the lists on the world object should implement this interface.
 *
 * @author Luke Lindsay
 *
 */
public interface WorldListListener {
    void listUpdated(KEY key, int index, FreerailsPrincipal principal);

    void itemAdded(KEY key, int index, FreerailsPrincipal principal);

    void itemRemoved(KEY key, int index, FreerailsPrincipal principal);
}