package jfreerails.move;

import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.common.FreerailsSerializable;


/**
 * This interface provides information about changes to the lists in the World database.
 */
public interface ListMove extends Move {
    /**
     * @return the type of object which was changed
     */
    KEY getKey();

    /**
     * @return the old item or null if not any.
     */
    FreerailsSerializable getBefore();

    /**
     * @return the new item or null if not any.
     */
    FreerailsSerializable getAfter();

    /**
     * @return the index of the item which changed.
     */
    int getIndex();

    FreerailsPrincipal getPrincipal();
}