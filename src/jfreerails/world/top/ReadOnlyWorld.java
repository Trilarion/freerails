package jfreerails.world.top;

import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.FreerailsMutableSerializable;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.GameTime;
import jfreerails.world.common.Money;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;


/**
<p>This interface defines a unified set of methods to access the elements that
  make up the game world. The game world is composed of the following specific-purpose
  collections into which one can put game world elements.</p>
<ul>
  A list of players.
</ul>
<ul>
  A 2D grid - the map.
</ul>
<ul>
  A series of lists that are accessible using the keys defined in {@link SKEY}
</ul>
<ul>
  Another series of lists indexed by player and accessible using the keys defined
  in {@link KEY}
</ul>
<ul>
  A collection items accessible using the keys defined in {@link ITEM}
</ul>
<ul>
  A list of financial transactions for each of the players
</ul>
<p>Example: the following code gets player1's train #5.</p>
<p><CODE>TrainModel t = (TrainModel)world.get(KEY.TRAINS, 5, player1);</CODE></p>
<p>The motivation for accessing lists using keys is that one does not need to
  add a new class or change the interface of the World class when a new list is
  added. Instead one can just add a new entry to the class KEY.</p>
<p>Code that loops through lists should handle null values gracefully</p>

* Note, this class has been annotated for use with ConstJava.
 * @author Luke
 * @author Rob
 */
public interface ReadOnlyWorld extends FreerailsMutableSerializable {
    /**
     * Returns the element mapped to the specified item.
     */

    /*=const*/ FreerailsSerializable get( /*=const*/
        ITEM item) /*=const*/;

    /**
     * Returns the element at the specified position in the specified list.
     */

    /*=const*/ FreerailsSerializable get( /*=const*/
        SKEY key, int index) /*=const*/;

    /**
     * Returns the element at the specified position in the specified list.
     */

    /*=const*/ FreerailsSerializable get( /*=const*/
        KEY key, int index, /*=const*/
        FreerailsPrincipal p) /*=const*/;

    /**
     * Returns the number of elements in the specified list.
     */
    int size( /*=const*/
        SKEY key) /*=const*/;

    /**
     * Returns the number of elements in the specified list.
     */
    int size(KEY key, /*=const*/
        FreerailsPrincipal p) /*=const*/;

    /** Returns the width of the map in tiles.
     */
    int getMapWidth() /*=const*/;

    /** Returns the height of the map in tiles.
     */
    int getMapHeight() /*=const*/;

    int getNumberOfPlayers() /*=const*/;

    boolean isPlayer(FreerailsPrincipal p) /*=const*/;

    /*=const*/ Player getPlayer(int i) /*=const*/;

    /** Returns the tile at the specified position on the map.
     */
    FreerailsSerializable getTile(int x, int y) /*=const*/;

    boolean boundsContain(int x, int y) /*=const*/;

    boolean boundsContain( /*=const*/
        SKEY k, int index) /*=const*/;

    boolean boundsContain( /*=const*/
        KEY k, int index, /*=const*/
        FreerailsPrincipal p) /*=const*/;

    /*=const*/ Transaction getTransaction(int i, /*=const*/
        FreerailsPrincipal p) /*=const*/;

    /*=const*/ GameTime getTransactionTimeStamp(int i, /*=const*/
        FreerailsPrincipal p) /*=const*/;

    /*=const*/ Money getCurrentBalance( /*=const*/
        FreerailsPrincipal p) /*=const*/;

    int getNumberOfTransactions( /*=const*/
        FreerailsPrincipal p) /*=const*/;
    
    int getID(FreerailsPrincipal p);
}