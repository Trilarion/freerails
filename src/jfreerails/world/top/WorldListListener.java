/*
 * Created on 25-Aug-2003
 *
 */
package jfreerails.world.top;


/**
 * @author Luke Lindsay
 *
 */
public interface WorldListListener {
    void listUpdated(KEY key, int index);

    void itemAdded(KEY key, int index);

    void itemRemoved(KEY key, int index);
}