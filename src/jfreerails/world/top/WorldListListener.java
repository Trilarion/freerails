/*
 * Created on 25-Aug-2003
 *
 */
package jfreerails.world.top;


/**
 * Classes that need to be notified of changes to the lists on the world object should implement this interface.
 *
 * TODO This interface needs updating to allow for multiple players.
 * @author Luke Lindsay
 *
 */
public interface WorldListListener {
    void listUpdated(KEY key, int index);

    void itemAdded(KEY key, int index);

    void itemRemoved(KEY key, int index);
}