/*
 * Created on May 23, 2004
 */
package jfreerails.world.top;

import java.awt.Point;
import java.util.Iterator;
import jfreerails.world.cargo.CargoType;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.player.Player;
import jfreerails.world.station.StationModel;
import jfreerails.world.track.FreerailsTile;
import junit.framework.TestCase;


/**
 * JUnit test for WorldDifferences.
 *  @author Luke
 *
 */
public class WorldDifferencesTest extends TestCase {
    Player player0 = new Player("player0", null, 0);
    Player player1 = new Player("player1", null, 1);
    Player player2 = new Player("player2", null, 2);

    public void testSharedLists() {
        WorldImpl underlyingWorld = new WorldImpl(10, 10);
        CargoType mailCT = new CargoType(10, "Mail", "Mail");
        CargoType passengersCT = new CargoType(10, "Passengers", "Passengers");
        underlyingWorld.add(SKEY.CARGO_TYPES, mailCT);

        WorldDifferences worldDiff = new WorldDifferences(underlyingWorld);

        assertEquals(0, worldDiff.numberOfNonMapDifferences());
        assertEquals(1, worldDiff.size(SKEY.CARGO_TYPES));

        FreerailsSerializable f = worldDiff.get(SKEY.CARGO_TYPES, 0);
        assertEquals("The mail cargotype should be accessible.", mailCT, f);
        worldDiff.add(SKEY.CARGO_TYPES, passengersCT);
        assertEquals(2, worldDiff.size(SKEY.CARGO_TYPES));
        assertEquals("2 Diffs: the length of the list + the actual element", 2,
            worldDiff.numberOfNonMapDifferences());
        f = worldDiff.removeLast(SKEY.CARGO_TYPES);
        assertEquals(passengersCT, f);
        assertEquals(0, worldDiff.numberOfNonMapDifferences());
        f = worldDiff.removeLast(SKEY.CARGO_TYPES);
        assertEquals(mailCT, f);
        assertEquals("1 Diff: the list length.", 1,
            worldDiff.numberOfNonMapDifferences());
        worldDiff.add(SKEY.CARGO_TYPES, mailCT);
        assertEquals(0, worldDiff.numberOfNonMapDifferences());
        worldDiff.set(SKEY.CARGO_TYPES, 0, passengersCT);
        assertEquals("1 Diff: element 0", 1,
            worldDiff.numberOfNonMapDifferences());
    }

    public void testPlayers() {
        WorldImpl underlyingWorld = new WorldImpl(10, 10);
        underlyingWorld.addPlayer(player0);

        WorldDifferences worldDiff = new WorldDifferences(underlyingWorld);
        assertEquals(0, worldDiff.numberOfNonMapDifferences());
        assertEquals(1, worldDiff.getNumberOfPlayers());

        Player p = worldDiff.getPlayer(0);
        assertEquals(player0, p);
        assertTrue(worldDiff.isPlayer(player0.getPrincipal()));

        //Test adding a player.
        int n = worldDiff.addPlayer(player1);
        assertEquals(1, n);

        int keys = KEY.getNumberOfKeys();
        assertEquals(
            ">3 diffs: the number of players + the player + the bank account + " +
            keys + " lists", keys + 3, worldDiff.numberOfNonMapDifferences());
        assertEquals(2, worldDiff.getNumberOfPlayers());

        p = worldDiff.getPlayer(1);
        assertEquals(player1, p);
        assertTrue(worldDiff.isPlayer(player1.getPrincipal()));
    }

    public void testNonSharedLists() {
        WorldImpl underlyingWorld = new WorldImpl(10, 10);
        underlyingWorld.addPlayer(player0);

        StationModel station0 = new StationModel();
        underlyingWorld.add(KEY.STATIONS, station0, player0.getPrincipal());

        WorldDifferences worldDiff = new WorldDifferences(underlyingWorld);
        assertEquals(0, worldDiff.numberOfNonMapDifferences());

        //First, for an existing player
        assertEquals(1, worldDiff.size(KEY.STATIONS, player0.getPrincipal()));

        FreerailsSerializable fs = worldDiff.get(KEY.STATIONS, 0,
                player0.getPrincipal());
        assertEquals(station0, fs);

        //Add a station.
        StationModel station1 = new StationModel();
        worldDiff.add(KEY.STATIONS, station1, player0.getPrincipal());
        assertEquals(2, worldDiff.size(KEY.STATIONS, player0.getPrincipal()));
        fs = worldDiff.get(KEY.STATIONS, 1, player0.getPrincipal());
        assertEquals(station1, fs);

        //Change a station.
        StationModel station2 = new StationModel();
        worldDiff.set(KEY.STATIONS, 0, station2, player0.getPrincipal());
        fs = worldDiff.get(KEY.STATIONS, 0, player0.getPrincipal());
        assertEquals(station2, fs);

        //Remove both stations.
        fs = worldDiff.removeLast(KEY.STATIONS, player0.getPrincipal());
        assertEquals(station1, fs);
        fs = worldDiff.removeLast(KEY.STATIONS, player0.getPrincipal());
        assertEquals(station2, fs);
        assertEquals(0, worldDiff.size(KEY.STATIONS, player0.getPrincipal()));

        //Add a station again.
        int i = worldDiff.add(KEY.STATIONS, station1, player0.getPrincipal());
        assertEquals(0, i);
        assertEquals(1, worldDiff.size(KEY.STATIONS, player0.getPrincipal()));

        //Second, for a new player
        worldDiff.addPlayer(player1);
        assertEquals(0, worldDiff.size(KEY.STATIONS, player1.getPrincipal()));
        worldDiff.add(KEY.STATIONS, station1, player1.getPrincipal());
        assertEquals(1, worldDiff.size(KEY.STATIONS, player1.getPrincipal()));

        worldDiff.set(KEY.STATIONS, 0, station2, player1.getPrincipal());

        worldDiff.add(KEY.STATIONS, station1, player1.getPrincipal());
        
        
    }
    
    public void testUsingNullElements(){
    	 WorldImpl underlyingWorld = new WorldImpl(10, 10);    	
    	 underlyingWorld.addPlayer(player0);    	 
    	 StationModel station0 = new StationModel();
         StationModel station1 = null;
         underlyingWorld.add(KEY.STATIONS, station0, player0.getPrincipal());
         underlyingWorld.add(KEY.STATIONS, station1, player0.getPrincipal());         
         WorldDifferences worldDiff = new WorldDifferences(underlyingWorld);
         assertEquals(station0, worldDiff.get(KEY.STATIONS, 0, player0.getPrincipal()) );   
         assertEquals(station1, worldDiff.get(KEY.STATIONS, 1, player0.getPrincipal()) );
         worldDiff.set(KEY.STATIONS, 0, station1, player0.getPrincipal());
         worldDiff.set(KEY.STATIONS, 1, station0, player0.getPrincipal()); 
    }

    public void testItem() {
        WorldImpl underlyingWorld = new WorldImpl(10, 10);
        StationModel station0 = new StationModel();
        StationModel station1 = new StationModel();
        underlyingWorld.set(ITEM.GAME_RULES, station0); //why not!

        WorldDifferences worldDiff = new WorldDifferences(underlyingWorld);
        assertEquals(station0, worldDiff.get(ITEM.GAME_RULES));
        worldDiff.set(ITEM.GAME_RULES, station1);
        assertEquals(station1, worldDiff.get(ITEM.GAME_RULES));
    }

    public void testMap() {
        WorldImpl underlyingWorld = new WorldImpl(21, 8);
        WorldDifferences worldDiff = new WorldDifferences(underlyingWorld);
        assertEquals(21, worldDiff.getMapWidth());
        assertEquals(8, worldDiff.getMapHeight());

        FreerailsTile tile = (FreerailsTile)underlyingWorld.getTile(2, 2);
        assertNotNull(tile);
        assertEquals(tile, worldDiff.getTile(2, 2));

        FreerailsTile newTile = FreerailsTile.getInstance(999);
        worldDiff.setTile(3, 5, newTile);
        assertEquals(newTile, worldDiff.getTile(3, 5));

        Iterator it = worldDiff.getMapDifferences();
        assertEquals(new Point(3, 5), it.next());
    }
}