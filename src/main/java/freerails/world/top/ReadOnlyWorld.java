package freerails.world.top;

import freerails.util.Pair;
import freerails.world.FreerailsSerializable;
import freerails.world.accounts.Transaction;
import freerails.world.common.*;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.player.Player;

/**
 *
 * This interface defines a unified set of methods to access the elements that
 * make up the game world. The game world is composed of the following
 * specific-purpose collections into which one can put game world elements.
 *
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
 *
 * Example: the following code gets player1's train #5.
 *
 *
 * {@code TrainModel t = (TrainModel)world.get(KEY.TRAINS, 5, player1);}
 *
 *
 * The motivation for accessing lists using keys is that one does not need to
 * add a new class or change the interface of the World class when a new list is
 * added. Instead one can just add a new entry to the class KEY.
 *
 *
 * Code that loops through lists should handle null values gracefully
 *
 *
 */
public interface ReadOnlyWorld extends FreerailsMutableSerializable {

    /**
     *
     * @param x
     * @param y
     * @return
     */
    boolean boundsContain(int x, int y);

    /**
     *
     * @param p
     * @param k
     * @param index
     * @return
     */
    boolean boundsContain(FreerailsPrincipal p, KEY k, int index);

    /**
     *
     * @param k
     * @param index
     * @return
     */
    boolean boundsContain(SKEY k, int index);

    /**
     *
     * @return
     */
    GameTime currentTime();

    /**
     * Returns the element mapped to the specified item.
     * @param item
     * @return 
     */

    FreerailsSerializable get(ITEM item);

    /**
     * Returns the element at the specified position in the specified list.
     * @param p
     * @param key
     * @param index
     * @return 
     */

    FreerailsSerializable get(FreerailsPrincipal p, KEY key, int index);

    /**
     * Returns the element at the specified position in the specified list.
     * @param key
     * @param index
     * @return 
     */

    FreerailsSerializable get(SKEY key, int index);

    /**
     *
     * @param p
     * @param index
     * @return
     */
    ActivityIterator getActivities(FreerailsPrincipal p, int index);

    /**
     *
     * @param p
     * @return
     */
    Money getCurrentBalance(FreerailsPrincipal p);

    /**
     *
     * @param p
     * @return
     */
    int getID(FreerailsPrincipal p);

    /**
     * Returns the height of the map in tiles.
     * @return 
     */
    int getMapHeight();

    /**
     * Returns the width of the map in tiles.
     * @return 
     */
    int getMapWidth();

    /**
     *
     * @return
     */
    int getNumberOfPlayers();

    /**
     *
     * @param p
     * @return
     */
    int getNumberOfTransactions(FreerailsPrincipal p);

    /**
     *
     * @param p
     * @return
     */
    int getNumberOfActiveEntities(FreerailsPrincipal p);

    /**
     *
     * @param i
     * @return
     */
    Player getPlayer(int i);

    /**
     * Returns the tile at the specified position on the map.
     * @param x
     * @param y
     * @return 
     */
    FreerailsSerializable getTile(int x, int y);

    /**
     *
     * @param p
     * @param i
     * @return
     */
    Transaction getTransaction(FreerailsPrincipal p, int i);

    /**
     *
     * @param p
     * @param i
     * @return
     */
    GameTime getTransactionTimeStamp(FreerailsPrincipal p, int i);

    /**
     *
     * @param p
     * @param i
     * @return
     */
    Pair<Transaction, GameTime> getTransactionAndTimeStamp(
            FreerailsPrincipal p, int i);

    /**
     *
     * @param p
     * @return
     */
    boolean isPlayer(FreerailsPrincipal p);

    /**
     * Returns the number of elements in the specified list.
     * @param p
     * @param key
     * @return 
     */
    int size(FreerailsPrincipal p, KEY key);

    /**
     * Returns the number of elements in the specified list.
     * @param key
     * @return 
     */
    int size(SKEY key);

    /**
     * Returns number of active entities belonging to the specified principal.
     * @param p
     * @return 
     */
    int size(FreerailsPrincipal p);
}