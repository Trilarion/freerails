/*
 * Created on May 23, 2004
 */
package jfreerails.world.top;

import java.awt.Point;
import java.util.HashMap;
import java.util.Iterator;
import jfreerails.world.accounts.BankAccount;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.GameTime;
import jfreerails.world.common.Money;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.track.FreerailsTile;


/**
 * An implemenation of World that only stores differences relative to an underlying world object.
 * Below is some stylised code showing what this class does.  The <code>key</code> object could be
 * a location on the map, a position in a list etc.
 * <code><pre>
         HashMap underlyingWorldObject;
        HashMap differences;

        public void put(Object key, Object value){
                if(underlyingWorldObject.get(key).equals(value)){
                        if(differences.containsKey(key)){
                                differences.remove(key);
                        }
                }else{
                        differences.put(key, value);
                }
        }

        public Object get(Object key){
                if(differences.containsKey(key)){
                        return differences.get(key);
                }else{
                        return underlyingWorldObject.get(key);
                }
        }
        </code></pre>
 *
 * The advantages of using an instance of this class instead of a copy of the world object are as follows.
 * <p>(1) Uses less memory.</P>
 * <p>(2) Lets you pinpoint where differences on the map are, so you don't need to check every tile.</P>
 *
 *
 * @author Luke
 *
 *
 */
public class WorldDifferences implements World {
    private static final Object NUMBER_OF_PLAYERS_KEY = new Integer(0);
    private static int i = 0;
    private final int LIST = i++;
    private final int PLAYER = i++;
    private final int BANK_ACCOUNT = i++;
    private final int LIST_LENGTH = i++;

    /** Instances of this class are used as the keys in the hashmap listDifferences.*/
    public class DiffKey {
        final int keyType;
        final int a;
        final int b;
        final int c;

        /**
         * Parameters (if present) should be in the following order
         * keyType,
         * ListKeyNumber,
         * Player,
         * index
         */
        public DiffKey(int keyType, int a, int b, int c) {
            this.keyType = keyType;
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public DiffKey(int keyType, int a, int b) {
            this(keyType, a, b, -1);
        }

        public DiffKey(int keyType, int a) {
            this(keyType, a, -2, -3);
        }

        public boolean equals(Object arg0) {
            if (null == arg0) {
                return false;
            }

            if (!(arg0 instanceof DiffKey)) {
                return false;
            }

            DiffKey test = (DiffKey)arg0;

            return test.keyType == keyType && test.a == a && test.b == b &&
            test.c == c;
        }

        public int hashCode() {
            int result;
            result = keyType;
            result = 29 * result + a;
            result = 29 * result + b;
            result = 29 * result + c;

            return result;
        }
    }

    private final ReadOnlyWorld underlyingWorld;

    /** Stores the differences on the map, Points are used as keys. */
    private HashMap mapDifferences = new HashMap();

    /** Stores the differences not on the map, instances of DiffKey are used as keys. */
    private HashMap listDifferences = new HashMap();

    /**
     * Creates a new WorldDifferences object that stores differences relative to
     * the specified underlying ReadOnlyWorld object. Warning, if the underlying
     * world object's state changes, calls to methods on this object may produce
     * unpredictable results.
     */
    public WorldDifferences(ReadOnlyWorld world) {
        this.underlyingWorld = world;
        this.reset();
    }

    /**
     * After this method returns, all differences are cleared and calls to
     * methods on this object should produce the same results as calls the the
     * corresponding methods on the underlying world object.
     */
    public void reset() {
        mapDifferences.clear();
        listDifferences.clear();
    }

    /** The iterator returns instances of java.awt.Point that store the
     * coordinates of tiles that are different to the underlying world object.
     */
    public Iterator getMapDifferences() {
        return mapDifferences.keySet().iterator();
    }

    /** Used by unit tests.*/
    int numberOfMapDifferences() {
        return this.mapDifferences.size();
    }

    /** Used by unit tests.*/
    int numberOfNonMapDifferences() {
        return this.listDifferences.size();
    }

    public void set(ITEM item, FreerailsSerializable element) {
        if (element.equals(this.underlyingWorld.get(item))) {
            //Case 1: the item is restored to the same value as the underlying world object.		
            if (this.listDifferences.containsKey(item)) {
                this.listDifferences.remove(item);
            }
        } else {
            //Case 2: the item is changed to a value different to the underlying world object.
            this.listDifferences.put(item, element);
        }
    }

    public void set(KEY key, int index, FreerailsSerializable element,
        FreerailsPrincipal principal) {
        //TODO add bounds checking.
        DiffKey diffKey = new DiffKey(LIST, key.getKeyNumber(),
                getPlayerNumber(principal), index);

        if (underlyingWorld.boundsContain(key, index, principal) &&
                element.equals(underlyingWorld.get(key, index, principal))) {
            //Case 1: the element is restored to the same value as the underlying world object.
            if (this.listDifferences.containsKey(diffKey)) {
                this.listDifferences.remove(diffKey);
            }
        } else {
            //Case 2: the element is changed to a value different to the underlying world object.
            this.listDifferences.put(diffKey, element);
        }
    }

    public void set(SKEY key, int index, FreerailsSerializable element) {
        DiffKey diffKey = new DiffKey(LIST, key.getKeyNumber(), index);

        if (underlyingWorld.boundsContain(key, index) &&
                element.equals(underlyingWorld.get(key, index))) {
            //Case 1: the element is restored to the same value as the underlying world object.	
            if (this.listDifferences.containsKey(diffKey)) {
                this.listDifferences.remove(diffKey);
            }
        } else {
            //Case 2: the element is changed to a value different to the underlying world object.
            this.listDifferences.put(diffKey, element);
        }
    }

    public int add(KEY key, FreerailsSerializable element,
        FreerailsPrincipal principal) {
        int playerNumber = getPlayerNumber(principal);
        DiffKey lengthKey = new DiffKey(LIST_LENGTH, key.getKeyNumber(),
                playerNumber);
        int index;
        int newLength;
        int underLyingSize;

        if (underlyingWorld.isPlayer(principal)) {
            underLyingSize = underlyingWorld.size(key, principal);
        } else {
            underLyingSize = Integer.MIN_VALUE;
        }

        if (listDifferences.containsKey(lengthKey)) {
            Integer oldLength = (Integer)listDifferences.get(lengthKey);
            index = oldLength.intValue();
        } else {
            index = underLyingSize;
        }

        newLength = index + 1;

        if (underLyingSize == newLength) {
            listDifferences.remove(lengthKey);
        } else {
            listDifferences.put(lengthKey, new Integer(newLength));
        }

        DiffKey elementKey = new DiffKey(LIST, key.getKeyNumber(),
                playerNumber, index);
        assert !this.listDifferences.containsKey(elementKey);
        listDifferences.put(elementKey, element);

        return index;
    }

    public int add(SKEY key, FreerailsSerializable element) {
        DiffKey lengthKey = new DiffKey(LIST_LENGTH, key.getKeyNumber());
        int index;
        int newLength;
        int underLyingSize = underlyingWorld.size(key);

        if (listDifferences.containsKey(lengthKey)) {
            Integer oldLength = (Integer)listDifferences.get(lengthKey);
            index = oldLength.intValue();
        } else {
            index = underLyingSize;
        }

        newLength = index + 1;

        if (underLyingSize == newLength) {
            listDifferences.remove(lengthKey);
        } else {
            listDifferences.put(lengthKey, new Integer(newLength));
        }

        DiffKey elementKey = new DiffKey(LIST, key.getKeyNumber(), index);
        assert !this.listDifferences.containsKey(elementKey);

        if (underLyingSize > index) {
            //We are restoring an element that is present in the underlying world,
            //so we need to check whether it is the same as the element we are adding.			
            FreerailsSerializable underlyingElement = underlyingWorld.get(key,
                    index);

            if (null == underlyingElement && null == element) {
                return index;
            }

            if (underlyingElement.equals(element)) {
                return index;
            }
        }

        listDifferences.put(elementKey, element);

        return index;
    }

    public FreerailsSerializable removeLast(KEY key,
        FreerailsPrincipal principal) {
        int playerNumber = getPlayerNumber(principal);
        DiffKey lengthKey = new DiffKey(LIST_LENGTH, key.getKeyNumber(),
                playerNumber);
        int index;
        int newLength;
        int underLyingSize;

        //The specified principle may exist on this object but not the underlying world object!
        if (underlyingWorld.isPlayer(principal)) {
            underLyingSize = underlyingWorld.size(key, principal);
        } else {
            underLyingSize = -1;
        }

        FreerailsSerializable elementRemoved;

        if (listDifferences.containsKey(lengthKey)) {
            Integer oldLength = (Integer)listDifferences.get(lengthKey);
            index = oldLength.intValue() - 1;
        } else {
            index = underLyingSize - 1;
        }

        newLength = index;
        elementRemoved = this.get(key, index, principal);

        if (underLyingSize == newLength) {
            assert listDifferences.containsKey(lengthKey);
            listDifferences.remove(lengthKey);
        } else {
            listDifferences.put(lengthKey, new Integer(newLength));
        }

        /* If the element to be reomoved is stored in the list differences hashmap we
         * need to remove it.
         */
        DiffKey elementKey = new DiffKey(LIST, key.getKeyNumber(),
                playerNumber, index);

        if (this.listDifferences.containsKey(elementKey)) {
            listDifferences.remove(elementKey);
        }

        return elementRemoved;
    }

    public FreerailsSerializable removeLast(SKEY key) {
        DiffKey lengthKey = new DiffKey(LIST_LENGTH, key.getKeyNumber());

        int index;
        int newLength;
        int underLyingSize = underlyingWorld.size(key);
        FreerailsSerializable elementRemoved;

        if (listDifferences.containsKey(lengthKey)) {
            Integer oldLength = (Integer)listDifferences.get(lengthKey);
            index = oldLength.intValue() - 1;
        } else {
            index = underLyingSize - 1;
        }

        newLength = index;
        elementRemoved = this.get(key, index);

        if (underLyingSize == newLength) {
            assert listDifferences.containsKey(lengthKey);
            listDifferences.remove(lengthKey);
        } else {
            listDifferences.put(lengthKey, new Integer(newLength));
        }

        DiffKey elementKey = new DiffKey(LIST, key.getKeyNumber(), index);

        if (this.listDifferences.containsKey(elementKey)) {
            listDifferences.remove(elementKey);
        }

        return elementRemoved;
    }

    public void setTile(int x, int y, FreerailsSerializable tile) {
        Point p = new Point(x, y);

        if (underlyingWorld.getTile(x, y).equals(tile)) {
            if (this.mapDifferences.containsKey(p)) {
                this.mapDifferences.remove(p);

                return;
            }
        } else {
            this.mapDifferences.put(p, tile);
        }
    }

    public int addPlayer(Player player) {
        int playerID = this.getNumberOfPlayers();
        Integer newNumberOfPlayers = new Integer(playerID + 1);
        this.listDifferences.put(NUMBER_OF_PLAYERS_KEY, newNumberOfPlayers);

        DiffKey playerKey = new DiffKey(PLAYER, playerID);
        listDifferences.put(playerKey, player);

        DiffKey bankAccountKey = new DiffKey(BANK_ACCOUNT, playerID);
        listDifferences.put(bankAccountKey, new BankAccount());

        //We need to create lists of size 0 for the new player.
        for (int i = 0; i < KEY.getNumberOfKeys(); i++) {
            DiffKey listLengthKey = new DiffKey(LIST_LENGTH, i, playerID);
            listDifferences.put(listLengthKey, new Integer(0));
        }

        return playerID;
    }

    public void addTransaction(Transaction t, FreerailsPrincipal p) {
        GameTime time = (GameTime)this.get(ITEM.TIME);
        addAccountIfNecessary(p).addTransaction(t, time);
    }

    public Transaction removeLastTransaction(FreerailsPrincipal p) {
        return addAccountIfNecessary(p).removeLastTransaction();
    }

    public World defensiveCopy() {
        throw new UnsupportedOperationException();
    }

    public FreerailsSerializable get(ITEM item) {
        if (this.listDifferences.containsKey(item)) {
            return (FreerailsSerializable)this.listDifferences.get(item);
        } else {
            return this.underlyingWorld.get(item);
        }
    }

    public FreerailsSerializable get(SKEY key, int index) {
        DiffKey diffKey = new DiffKey(LIST, key.getKeyNumber(), index);

        if (this.listDifferences.containsKey(diffKey)) {
            return (FreerailsSerializable)listDifferences.get(diffKey);
        } else {
            return underlyingWorld.get(key, index);
        }
    }

    public FreerailsSerializable get(KEY key, int index, FreerailsPrincipal p) {
        //TODO add bounds check.
        DiffKey diffKey = new DiffKey(LIST, key.getKeyNumber(),
                this.getPlayerNumber(p), index);

        if (this.listDifferences.containsKey(diffKey)) {
            return (FreerailsSerializable)this.listDifferences.get(diffKey);
        } else {
            return this.underlyingWorld.get(key, index, p);
        }
    }

    public int size(SKEY key) {
        DiffKey lengthKey = new DiffKey(LIST_LENGTH, key.getKeyNumber());

        if (this.listDifferences.containsKey(lengthKey)) {
            Integer i = (Integer)listDifferences.get(lengthKey);

            return i.intValue();
        } else {
            return this.underlyingWorld.size(key);
        }
    }

    public int size(KEY key, FreerailsPrincipal p) {
        DiffKey listSize = new DiffKey(LIST_LENGTH, key.getKeyNumber(),
                this.getPlayerNumber(p));

        if (this.listDifferences.containsKey(listSize)) {
            Integer i = (Integer)listDifferences.get(listSize);

            return i.intValue();
        } else {
            return underlyingWorld.size(key, p);
        }
    }

    public int getMapWidth() {
        return underlyingWorld.getMapWidth();
    }

    public int getMapHeight() {
        return underlyingWorld.getMapHeight();
    }

    public int getNumberOfPlayers() {
        if (this.listDifferences.containsKey(NUMBER_OF_PLAYERS_KEY)) {
            Integer i = (Integer)listDifferences.get(NUMBER_OF_PLAYERS_KEY);

            return i.intValue();
        } else {
            return underlyingWorld.getNumberOfPlayers();
        }
    }

    public boolean isPlayer(FreerailsPrincipal p) {
        return -1 < getPlayerNumber(p);
    }

    /** Returns the player number or -1 if the player is not found.*/
    private int getPlayerNumber(FreerailsPrincipal p) {
        int numberOfPlayers = this.getNumberOfPlayers();

        for (int i = 0; i < numberOfPlayers; i++) {
            Player player = this.getPlayer(i);

            if (player.getPrincipal().equals(p)) {
                return i;
            }
        }

        return -1;
    }

    public Player getPlayer(int i) {
        DiffKey diffKey = new DiffKey(PLAYER, i);

        if (this.listDifferences.containsKey(diffKey)) {
            return (Player)this.listDifferences.get(diffKey);
        } else {
            return this.underlyingWorld.getPlayer(i);
        }
    }

    public FreerailsSerializable getTile(int x, int y) {
        Point p = new Point(x, y);

        if (this.mapDifferences.containsKey(p)) {
            return (FreerailsTile)this.mapDifferences.get(p);
        } else {
            return underlyingWorld.getTile(x, y);
        }
    }

    public boolean boundsContain(int x, int y) {
        return underlyingWorld.boundsContain(x, y);
    }

    public boolean boundsContain(SKEY k, int index) {
        return this.size(k) > index && index >= 0;
    }

    public boolean boundsContain(KEY k, int index, FreerailsPrincipal p) {
        return this.size(k, p) > index && index >= 0;
    }

    public Transaction getTransaction(int i, FreerailsPrincipal p) {
        if (isAccountDiff(p)) {
            return this.getAccount(p).getTransaction(i);
        } else {
            return this.underlyingWorld.getTransaction(i, p);
        }
    }

    public GameTime getTransactionTimeStamp(int i, FreerailsPrincipal p) {
        if (isAccountDiff(p)) {
            return this.getAccount(p).getTimeStamp(i);
        } else {
            return this.underlyingWorld.getTransactionTimeStamp(i, p);
        }
    }

    public Money getCurrentBalance(FreerailsPrincipal p) {
        if (isAccountDiff(p)) {
            return this.getAccount(p).getCurrentBalance();
        } else {
            return this.underlyingWorld.getCurrentBalance(p);
        }
    }

    public int getNumberOfTransactions(FreerailsPrincipal p) {
        if (isAccountDiff(p)) {
            return this.getAccount(p).size();
        } else {
            return this.underlyingWorld.getNumberOfTransactions(p);
        }
    }

    private boolean isAccountDiff(FreerailsPrincipal p) {
        DiffKey accountKey = new DiffKey(BANK_ACCOUNT, this.getPlayerNumber(p));

        return this.listDifferences.containsKey(accountKey);
    }

    private BankAccount getAccount(FreerailsPrincipal p) {
        DiffKey accountKey = new DiffKey(BANK_ACCOUNT, this.getPlayerNumber(p));

        return (BankAccount)this.listDifferences.get(accountKey);
    }

    /** Copies the account from the underlying world object if this has not
     * already been done.
     */
    private BankAccount addAccountIfNecessary(FreerailsPrincipal p) {
        if (isAccountDiff(p)) {
            return getAccount(p);
        } else {
            //We need to copy the account..
            DiffKey accountKey = new DiffKey(BANK_ACCOUNT,
                    this.getPlayerNumber(p));
            BankAccount account = new BankAccount();

            for (int i = 0; i < underlyingWorld.getNumberOfTransactions(p);
                    i++) {
                account.addTransaction(underlyingWorld.getTransaction(i, p),
                    getTransactionTimeStamp(i, p));
            }

            listDifferences.put(accountKey, account);

            return account;
        }
    }
}