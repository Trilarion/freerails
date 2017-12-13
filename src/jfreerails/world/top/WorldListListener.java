/*
 * Created on 25-Aug-2003
 *
 */
package jfreerails.world.top;

import jfreerails.world.player.FreerailsPrincipal;

/**
 * @author Luke Lindsay
 *
 */
public interface WorldListListener {
    void listUpdated(KEY key, int index, FreerailsPrincipal principal);

    void itemAdded(KEY key, int index, FreerailsPrincipal principal);

    void itemRemoved(KEY key, int index, FreerailsPrincipal principal);
}
