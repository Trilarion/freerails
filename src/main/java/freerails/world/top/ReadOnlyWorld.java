package freerails.world.top;

import freerails.util.Pair;
import freerails.world.accounts.Transaction;
import freerails.world.common.*;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.player.Player;

/**
 * <p>
 * This interface defines a unified set of methods to access the elements that
 * make up the game world. The game world is composed of the following
 * specific-purpose collections into which one can put game world elements.
 * </p>
 * <ul>
 * A list of players.
 * </ul>
 * <ul>
 * A 2D grid - the map.
 * </ul>
 * <ul>
 * A series of lists that are accessible using the keys defined in {@link SKEY}
 * </ul>
 * <ul>
 * Another series of lists indexed by player and accessible using the keys
 * defined in {@link KEY}
 * </ul>
 * <ul>
 * A collection items accessible using the keys defined in {@link ITEM}
 * </ul>
 * <ul>
 * A list of financial transactions for each of the players
 * </ul>
 * <p>
 * Example: the following code gets player1's train #5.
 * </p>
 * <p>
 * <CODE>TrainModel t = (TrainModel)world.get(KEY.TRAINS, 5, player1);</CODE>
 * </p>
 * <p>
 * The motivation for accessing lists using keys is that one does not need to
 * add a new class or change the interface of the World class when a new list is
 * added. Instead one can just add a new entry to the class KEY.
 * </p>
 * <p>
 * Code that loops through lists should handle null values gracefully
 * </p>
 *
 * @author Luke
 * @author Rob
 */
public interface ReadOnlyWorld extends FreerailsMutableSerializable {
    boolean boundsContain(int x, int y);

    boolean boundsContain(FreerailsPrincipal p, KEY k, int index);

    boolean boundsContain(SKEY k, int index);

    GameTime currentTime();

    /**
     * Returns the element mapped to the specified item.
     */

    FreerailsSerializable get(ITEM item);

    /**
     * Returns the element at the specified position in the specified list.
     */

    FreerailsSerializable get(FreerailsPrincipal p, KEY key, int index);

    /**
     * Returns the element at the specified position in the specified list.
     */

    FreerailsSerializable get(SKEY key, int index);

    ActivityIterator getActivities(FreerailsPrincipal p, int index);

    Money getCurrentBalance(FreerailsPrincipal p);

    int getID(FreerailsPrincipal p);

    /**
     * Returns the height of the map in tiles.
     */
    int getMapHeight();

    /**
     * Returns the width of the map in tiles.
     */
    int getMapWidth();

    int getNumberOfPlayers();

    int getNumberOfTransactions(FreerailsPrincipal p);

    int getNumberOfActiveEntities(FreerailsPrincipal p);

    Player getPlayer(int i);

    /**
     * Returns the tile at the specified position on the map.
     */
    FreerailsSerializable getTile(int x, int y);

    Transaction getTransaction(FreerailsPrincipal p, int i);

    GameTime getTransactionTimeStamp(FreerailsPrincipal p, int i);

    Pair<Transaction, GameTime> getTransactionAndTimeStamp(
            FreerailsPrincipal p, int i);

    boolean isPlayer(FreerailsPrincipal p);

    /**
     * Returns the number of elements in the specified list.
     */
    int size(FreerailsPrincipal p, KEY key);

    /**
     * Returns the number of elements in the specified list.
     */
    int size(SKEY key);

    /**
     * Returns number of active entities belonging to the specified principal.
     */
    int size(FreerailsPrincipal p);
}