package jfreerails.world.top;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.track.FreerailsTile;

/** <p>This interface defines a unified set of methods to access the elements
 * that make up the game world.  One can think of it as a specific-purpose
 * Collection.  Game world elements are either placed on a 2D grid - the map -
 * or in one of a number of lists.  The lists are accessed using keys defined in
 * the class {@link KEY}.</p>
 *
 * <p>Example: the following code gets train #5.<br>
 * <CODE> TrainModel t = (TrainModel)world.get(KEY.TRAINS, 5);</CODE></p>
 *
 * <p>The motivation for accessing lists using keys is that one does not need to
 * add a new class or change the interface of the World class when a new list is
 * added.  Instead one can just add a new entry to the class KEY.</p>
 *
 * Code that loops through lists should handle null values gracefully</p>
 *
 */
public interface ReadOnlyWorld extends FreerailsSerializable {
    /** Returns the element mapped to the specified item.
     */
    FreerailsSerializable get(ITEM item);
    
    /** Returns the element at the specified position in the specified list.
     */
    FreerailsSerializable get(KEY key, int index);
    
    /** Returns the number of elements in the specified list.
     */
    int size(KEY key);
    
    /** Returns the width of the map in tiles.
     */
    int getMapWidth();
    
    /** Returns the height of the map in tiles.
     */
    int getMapHeight();
    
    /** Returns the tile at the specified position on the map.
     */
    FreerailsTile getTile(int x, int y);

    boolean boundsContain(int x, int y);

    boolean boundsContain(KEY k, int index);
}

