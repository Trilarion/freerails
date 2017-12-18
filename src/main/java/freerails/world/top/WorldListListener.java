/*
 * Created on 25-Aug-2003
 *
 */
package freerails.world.top;

import freerails.world.player.FreerailsPrincipal;

/**
 * Classes that need to be notified of changes to the lists on the world object
 * should implement this interface.
 *
 * @author Luke Lindsay
 */
public interface WorldListListener {

    /**
     *
     * @param key
     * @param index
     * @param principal
     */
    void listUpdated(KEY key, int index, FreerailsPrincipal principal);

    /**
     *
     * @param key
     * @param index
     * @param principal
     */
    void itemAdded(KEY key, int index, FreerailsPrincipal principal);

    /**
     *
     * @param key
     * @param index
     * @param principal
     */
    void itemRemoved(KEY key, int index, FreerailsPrincipal principal);
}