package jfreerails.world.top;

import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.Money;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.track.FreerailsTile;


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
 */
public interface ReadOnlyWorld extends FreerailsSerializable {
    /**
     * Returns the element mapped to the specified item.
     */
    FreerailsSerializable get(ITEM item);

    /**
     * Returns the element at the specified position in the specified list.
     */
    FreerailsSerializable get(SKEY key, int index);

    /**
     * Returns the element at the specified position in the specified list.
     */
    FreerailsSerializable get(KEY key, int index, FreerailsPrincipal p);

    /**
     * Returns the number of elements in the specified list.
     */
    int size(SKEY key);

    /**
     * Returns the number of elements in the specified list.
     */
    int size(KEY key, FreerailsPrincipal p);

    /** Returns the width of the map in tiles.
     */
    int getMapWidth();

    /** Returns the height of the map in tiles.
     */
    int getMapHeight();

    int getNumberOfPlayers();

    boolean isPlayer(FreerailsPrincipal p);

    Player getPlayer(int i);

    /** Returns the tile at the specified position on the map.
     */
    FreerailsTile getTile(int x, int y);

    boolean boundsContain(int x, int y);

    boolean boundsContain(SKEY k, int index);

    boolean boundsContain(KEY k, int index, FreerailsPrincipal p);

    Transaction getTransaction(int i, FreerailsPrincipal p);

    Money getCurrentBalance(FreerailsPrincipal p);

    int getNumberOfTransactions(FreerailsPrincipal p);
}