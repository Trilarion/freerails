package jfreerails.world.top;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.logging.Logger;
import jfreerails.world.accounts.BankAccount;
import jfreerails.world.accounts.EconomicClimate;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.GameCalendar;
import jfreerails.world.common.GameTime;
import jfreerails.world.common.Money;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.track.FreerailsTile;


/**
 * An implementation of World that uses standard java.util collections internally.
 *  @author Luke
 *
 */
public class WorldImpl implements World {
    private static final Logger logger = Logger.getLogger(WorldImpl.class.getName());

    public int hashCode() {
        int result;
        result = players.size();

        return result;
    }

    /**
    * An array of ArrayList indexed by keyNumber.
    * If the key is shared, then the ArrayList consists of instances of the
    * class corresponding to the KEY type. Otherwise, the ArrayList is
    * indexed by Player index, and contains instances of ArrayList
    * which themselves contain instances of the class corresponding to the
    * KEY type.
    */
    private final ArrayList<Player> players = new ArrayList<Player>();
    private final ArrayList<BankAccount> bankAccounts = new ArrayList<BankAccount>();
    private final ArrayList<ArrayList<FreerailsSerializable>>[] lists = new ArrayList[KEY.getNumberOfKeys()];
    private final ArrayList<FreerailsSerializable>[] sharedLists = new ArrayList[SKEY.getNumberOfKeys()];
    private final FreerailsSerializable[] items = new FreerailsSerializable[ITEM.getNumberOfKeys()];
    private FreerailsSerializable[][] map;

    public WorldImpl() {
        setupItems();
        this.setupMap(0, 0);
        this.setupLists();
    }

    private void setupItems() {
        this.set(ITEM.CALENDAR, new GameCalendar(1200, 1840));
        this.set(ITEM.TIME, new GameTime(0));
        this.set(ITEM.ECONOMIC_CLIMATE, EconomicClimate.MODERATION);
    }

    public WorldImpl(int mapWidth, int mapHeight) {
        setupItems();
        this.setupMap(mapWidth, mapHeight);
        this.setupLists();
    }

    public void setupMap(int mapWidth, int mapHeight) {
        map = new FreerailsSerializable[mapWidth][mapHeight];

        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                map[x][y] = FreerailsTile.NULL;
            }
        }
    }

    private void setupLists() {
        for (int i = 0; i < lists.length; i++) {
            lists[i] = new ArrayList<ArrayList<FreerailsSerializable>>();
        }

        for (int i = 0; i < sharedLists.length; i++) {
            sharedLists[i] = new ArrayList<FreerailsSerializable>();
        }
    }

    public FreerailsSerializable get(SKEY key, int index) {
        return sharedLists[key.getKeyID()].get(index);
    }

    public FreerailsSerializable get(KEY key, int index, FreerailsPrincipal p) {
        ArrayList arrayList2 = lists[key.getKeyID()];
		int playerIndex = getPlayerIndex(p);
		ArrayList arrayList = (ArrayList)arrayList2.get(playerIndex);
		return (FreerailsSerializable)(arrayList).get(index);
    }

    public void set(SKEY key, int index, FreerailsSerializable element) {
       sharedLists[key.getKeyID()].set(index, element);		
    }

    public void set(KEY key, int index, FreerailsSerializable element,
        FreerailsPrincipal p) {
        logger.finer("Setting " + element + " of type " + key + " at index " +
            index + " for " + p);

        int playerIndex = getPlayerIndex(p);
		ArrayList<FreerailsSerializable> arrayList = lists[key.getKeyID()].get(playerIndex);
		arrayList.set(index, element);
    }

    public int add(SKEY key, FreerailsSerializable element) {
        sharedLists[key.getKeyID()].add(element);		
        return size(key) - 1;
    }

    public int add(KEY key, FreerailsSerializable element, FreerailsPrincipal p) {
        logger.finer("Adding " + element + " to " + key + " for " + p);

        //        if (key == KEY.PLAYERS) {
        //            return addPlayer((Player)element, p);
        //        }
        lists[key.getKeyID()].get(getPlayerIndex(p)).add(element);

        return size(key, p) - 1;
    }

    public int size(SKEY key) {
        return sharedLists[key.getKeyID()].size();
    }

    public int size(KEY key, FreerailsPrincipal p) {
        return lists[key.getKeyID()].get(getPlayerIndex(p)).size();
    }

    public int getMapWidth() {
        return map.length;
    }

    public int getMapHeight() {
        if (map.length == 0) {
            //When the map size is 0*0 we get a java.lang.ArrayIndexOutOfBoundsException: 0
            // if we don't have check above.
            return 0;
        }
		return map[0].length;
    }

    public void setTile(int x, int y, FreerailsSerializable element) {
        map[x][y] = element;
    }

    public FreerailsSerializable getTile(int x, int y) {
        return map[x][y];
    }

    public boolean boundsContain(int x, int y) {
        if (x >= 0 && x < map.length && y >= 0 && y < map[0].length) {
            return true;
        }
		return false;
    }

    public boolean boundsContain(KEY k, int index, FreerailsPrincipal p) {
        if (!isPlayer(p)) {
            return false;
        } else if (index >= 0 && index < this.size(k, p)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean boundsContain(SKEY k, int index) {
        return (index >= 0 && index < this.size(k));
    }

    public FreerailsSerializable removeLast(SKEY key) {
        int size = lists[key.getKeyID()].size();

        return sharedLists[key.getKeyID()].remove(size -
            1);
    }

    public FreerailsSerializable removeLast(KEY key, FreerailsPrincipal p) {
        logger.finer("Removing last " + key + " for " + p);

        int size;
        size = lists[key.getKeyID()].get(getPlayerIndex(p)).size();

        int index = size - 1;

        return lists[key.getKeyID()].get(getPlayerIndex(
                p)).remove(index);
    }

    public boolean equals(Object o) {
        if (o instanceof WorldImpl) {
            WorldImpl test = (WorldImpl)o;

            if (lists.length != test.lists.length) {
                return false;
            }
			for (int i = 0; i < lists.length; i++) {
			    if (!lists[i].equals(test.lists[i])) {
			        return false;
			    }
			}

            if (sharedLists.length != test.sharedLists.length) {
                return false;
            }
			for (int i = 0; i < sharedLists.length; i++) {
			    if (!sharedLists[i].equals(test.sharedLists[i])) {
			        return false;
			    }
			}

            if ((this.getMapWidth() != test.getMapWidth()) ||
                    (this.getMapHeight() != test.getMapHeight())) {
                return false;
            }
			for (int x = 0; x < this.getMapWidth(); x++) {
			    for (int y = 0; y < this.getMapHeight(); y++) {
			        if (!getTile(x, y).equals(test.getTile(x, y))) {
			            return false;
			        }
			    }
			}

            if (this.items.length != test.items.length) {
                return false;
            }
			for (int i = 0; i < this.items.length; i++) {
			    //Some of the elements in the items array might be null, so we check for this before
			    //calling equals to avoid NullPointerExceptions.
			    if (!(null == items[i] ? null == test.items[i]
			                               : items[i].equals(test.items[i]))) {
			        return false;
			    }
			}

            /* Compare bank accounts.*/
            if (!this.bankAccounts.equals(test.bankAccounts)) {
                return false;
            }

            //phew!
            return true;
        }
		return false;
    }

    public FreerailsSerializable get(ITEM item) {
        return items[item.getKeyID()];
    }

    public void set(ITEM item, FreerailsSerializable element) {
        items[item.getKeyID()] = element;
    }

    /**
    * @param player Player to add
    * @return index of the player
    */
    public int addPlayer(Player player) {
        if (null == player) {
            throw new NullPointerException();
        }

        players.add(player);
        bankAccounts.add(new BankAccount());

        int index = players.size() - 1;

        for (int i = 0; i < KEY.getNumberOfKeys(); i++) {
            lists[i].add(new ArrayList<FreerailsSerializable>());
        }

        return index;
    }

    private int getPlayerIndex(FreerailsPrincipal p) {
        for (int i = 0; i < players.size(); i++) {
            Player player = (players.get(i));

            if (p.equals(player.getPrincipal())) {
                return i;
            }
        }

        throw new ArrayIndexOutOfBoundsException("No matching principal for " +
            p.toString());
    }

    public World defensiveCopy() {
        try {
            Object m = this;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream objectOut = new ObjectOutputStream(out);
            objectOut.writeObject(m);
            objectOut.flush();

            byte[] bytes = out.toByteArray();

            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            ObjectInputStream objectIn = new ObjectInputStream(in);
            Object o = objectIn.readObject();

            return (World)o;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e.getMessage());
        }
    }

    public int getNumberOfPlayers() {
        return players.size();
    }

    public Player getPlayer(int i) {
        return players.get(i);
    }

    public void addTransaction(Transaction t, FreerailsPrincipal p) {
        GameTime time = (GameTime)this.get(ITEM.TIME);
        getBankAccount(p).addTransaction(t, time);
    }

    public Transaction removeLastTransaction(FreerailsPrincipal p) {
        return getBankAccount(p).removeLastTransaction();
    }

    public Transaction getTransaction(int i, FreerailsPrincipal p) {
        return getBankAccount(p).getTransaction(i);
    }

    public Money getCurrentBalance(FreerailsPrincipal p) {
        return getBankAccount(p).getCurrentBalance();
    }

    public int getNumberOfTransactions(FreerailsPrincipal p) {
        return getBankAccount(p).size();
    }

    private BankAccount getBankAccount(FreerailsPrincipal p) {
        int index = this.getPlayerIndex(p);

        return bankAccounts.get(index);
    }

    public boolean isPlayer(FreerailsPrincipal p) {
        try {
            this.getPlayerIndex(p);

            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    public GameTime getTransactionTimeStamp(int i, FreerailsPrincipal p) {
        return getBankAccount(p).getTimeStamp(i);
    }

	
	public int getID(FreerailsPrincipal p) {
		
		return this.getPlayerIndex(p);
	}
}