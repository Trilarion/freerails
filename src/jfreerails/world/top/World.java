package jfreerails.world.top;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.track.FreerailsTile;

/** <p>This class implements methods which the server can use to alter the
 * world. Notice that incontrast to, say, <CODE>java.util.List</CODE> there is no remove()
 * method that shifts any subsequent elements to the left (subtracts one from their indices).
 * This means that an elements' position in a list can be used as an address space independent
 * way to reference the element.  If you want to remove an element from a list, you should
 * set it to null, e.g. <br>
 * <CODE>world.set(KEY.TRAINS, 5, null);</CODE><br>
 * Code that loops through lists should handle null values gracefully</p>
 *
 */
public interface World extends ReadOnlyWorld {
	
	/** Replaces the element mapped to the specified item with the specified element.
	*/
	void set(ITEM item, FreerailsSerializable element);
    
    /** Replaces the element at the specified position in the specified list with the specified element.
     */
    void set(KEY key, int index, FreerailsSerializable element);
    
    /** Appends the specified element to the end of the specifed list and returns the index
     * that can be used to retrieve it.
     */
    int add(KEY key, FreerailsSerializable element);
    
    /** Removes the last element from the specified list. */
    FreerailsSerializable removeLast(KEY key);
    
    /** Replaces the tile at the specified position on the map with the specified tile.
     */
    void setTile(int x, int y, FreerailsTile tile);
}
