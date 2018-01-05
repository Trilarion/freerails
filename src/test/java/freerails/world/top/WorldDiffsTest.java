/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.world.top;

import freerails.util.ImPoint;
import freerails.util.ListKey;
import freerails.world.*;
import freerails.world.cargo.CargoCategory;
import freerails.world.cargo.CargoType;
import freerails.world.player.Player;
import freerails.world.station.StationModel;
import freerails.world.terrain.City;
import freerails.world.terrain.FreerailsTile;
import junit.framework.TestCase;

import java.io.Serializable;
import java.util.Iterator;

/**
 * JUnit test for WorldDifferences.
 */
public class WorldDiffsTest extends TestCase {
    final Player player0 = new Player("player0", 0);

    final Player player1 = new Player("player1", 1);

    /**
     *
     */
    public void testSharedLists() {
        WorldImpl underlyingWorld = new WorldImpl(10, 10);
        CargoType mailCT = new CargoType(10, "Mail", CargoCategory.Mail);
        CargoType passengersCT = new CargoType(10, "Passengers",
                CargoCategory.Passengers);
        underlyingWorld.add(SKEY.CARGO_TYPES, mailCT);

        WorldDiffs worldDiff = new WorldDiffs(underlyingWorld);

        assertEquals(0, worldDiff.listDiffs());
        assertEquals(1, worldDiff.size(SKEY.CARGO_TYPES));

        Serializable f = worldDiff.get(SKEY.CARGO_TYPES, 0);
        assertEquals("The mail cargotype should be accessible.", mailCT, f);
        worldDiff.add(SKEY.CARGO_TYPES, passengersCT);
        assertEquals(2, worldDiff.size(SKEY.CARGO_TYPES));
        assertEquals("2 Diffs: the length of the list + the actual element", 2,
                worldDiff.listDiffs());
        f = worldDiff.removeLast(SKEY.CARGO_TYPES);
        assertEquals(passengersCT, f);

        assertEquals(0, worldDiff.listDiffs());
        f = worldDiff.removeLast(SKEY.CARGO_TYPES);
        assertEquals(mailCT, f);
        assertEquals("1 Diff: the list length.", 1, worldDiff.listDiffs());
        assertEquals(0, worldDiff.size(SKEY.CARGO_TYPES));
        worldDiff.add(SKEY.CARGO_TYPES, mailCT);
        assertEquals(0, worldDiff.listDiffs());
        worldDiff.set(SKEY.CARGO_TYPES, 0, passengersCT);
        assertEquals("1 Diff: element 0", 1, worldDiff.listDiffs());
    }

    /**
     *
     */
    public void testPlayers() {
        WorldImpl underlyingWorld = new WorldImpl(10, 10);
        underlyingWorld.addPlayer(player0);

        WorldDiffs worldDiff = new WorldDiffs(underlyingWorld);
        assertEquals(0, worldDiff.listDiffs());
        assertEquals(1, worldDiff.getNumberOfPlayers());

        Player p = worldDiff.getPlayer(0);
        assertEquals(player0, p);
        assertTrue(worldDiff.isPlayer(player0.getPrincipal()));

        // Test adding a player.
        int n = worldDiff.addPlayer(player1);
        assertEquals(1, n);

        assertEquals(2, worldDiff.getNumberOfPlayers());

        p = worldDiff.getPlayer(1);
        assertEquals(player1, p);
        assertTrue(worldDiff.isPlayer(player1.getPrincipal()));
    }

    /**
     *
     */
    public void testNonSharedLists() {
        WorldImpl underlyingWorld = new WorldImpl(10, 10);
        underlyingWorld.addPlayer(player0);

        StationModel station0 = new StationModel();
        underlyingWorld.add(player0.getPrincipal(), KEY.STATIONS, station0);

        WorldDiffs worldDiff = new WorldDiffs(underlyingWorld);
        assertEquals(0, worldDiff.listDiffs());

        // First, for an existing player
        assertEquals(1, worldDiff.size(player0.getPrincipal(), KEY.STATIONS));

        Serializable fs = worldDiff.get(player0.getPrincipal(),
                KEY.STATIONS, 0);
        assertEquals(station0, fs);

        // Add a station.
        StationModel station1 = new StationModel();
        worldDiff.add(player0.getPrincipal(), KEY.STATIONS, station1);
        assertEquals(2, worldDiff.size(player0.getPrincipal(), KEY.STATIONS));
        fs = worldDiff.get(player0.getPrincipal(), KEY.STATIONS, 1);
        assertEquals(station1, fs);

        // Change a station.
        StationModel station2 = new StationModel();
        worldDiff.set(player0.getPrincipal(), KEY.STATIONS, 0, station2);
        fs = worldDiff.get(player0.getPrincipal(), KEY.STATIONS, 0);
        assertEquals(station2, fs);

        // Remove both stations.
        fs = worldDiff.removeLast(player0.getPrincipal(), KEY.STATIONS);
        assertEquals(station1, fs);
        fs = worldDiff.removeLast(player0.getPrincipal(), KEY.STATIONS);
        assertEquals(station2, fs);
        assertEquals(0, worldDiff.size(player0.getPrincipal(), KEY.STATIONS));

        // Add a station again.
        int i = worldDiff.add(player0.getPrincipal(), KEY.STATIONS, station1);
        assertEquals(0, i);
        assertEquals(1, worldDiff.size(player0.getPrincipal(), KEY.STATIONS));

        // Second, for a new player
        worldDiff.addPlayer(player1);
        assertEquals(2, worldDiff.getNumberOfPlayers());

        assertEquals(player1, worldDiff.getPlayer(1));
        assertEquals(0, worldDiff.size(player1.getPrincipal(), KEY.STATIONS));
        worldDiff.add(player1.getPrincipal(), KEY.STATIONS, station1);
        assertEquals(1, worldDiff.size(player1.getPrincipal(), KEY.STATIONS));

        worldDiff.set(player1.getPrincipal(), KEY.STATIONS, 0, station2);

        worldDiff.add(player1.getPrincipal(), KEY.STATIONS, station1);

    }

    /**
     *
     */
    public void testUsingNullElements() {
        WorldImpl underlyingWorld = new WorldImpl(10, 10);
        underlyingWorld.addPlayer(player0);
        StationModel station0 = new StationModel();
        StationModel station1 = null;
        underlyingWorld.add(player0.getPrincipal(), KEY.STATIONS, station0);
        underlyingWorld.add(player0.getPrincipal(), KEY.STATIONS, station1);
        WorldDiffs worldDiff = new WorldDiffs(underlyingWorld);
        assertEquals(station0, worldDiff.get(player0.getPrincipal(),
                KEY.STATIONS, 0));
        assertEquals(station1, worldDiff.get(player0.getPrincipal(),
                KEY.STATIONS, 1));
        worldDiff.set(player0.getPrincipal(), KEY.STATIONS, 0, station1);
        worldDiff.set(player0.getPrincipal(), KEY.STATIONS, 1, station0);
    }

    /**
     *
     */
    public void testItem() {
        WorldImpl underlyingWorld = new WorldImpl(10, 10);
        StationModel station0 = new StationModel();
        StationModel station1 = new StationModel();
        underlyingWorld.set(ITEM.GAME_RULES, station0); // why not!

        WorldDiffs worldDiff = new WorldDiffs(underlyingWorld);
        assertEquals(station0, worldDiff.get(ITEM.GAME_RULES));
        worldDiff.set(ITEM.GAME_RULES, station1);
        assertEquals(station1, worldDiff.get(ITEM.GAME_RULES));
    }

    /**
     *
     */
    public void testMap() {
        WorldImpl underlyingWorld = new WorldImpl(21, 8);
        WorldDiffs worldDiff = new WorldDiffs(underlyingWorld);
        assertEquals(21, worldDiff.getMapWidth());
        assertEquals(8, worldDiff.getMapHeight());

        FreerailsTile tile = (FreerailsTile) underlyingWorld.getTile(2, 2);
        assertNotNull(tile);
        assertEquals(tile, worldDiff.getTile(2, 2));

        FreerailsTile newTile = FreerailsTile.getInstance(999);
        worldDiff.setTile(3, 5, newTile);
        assertEquals(newTile, worldDiff.getTile(3, 5));

        Iterator<ImPoint> it = worldDiff.getMapDiffs();
        assertEquals(new ImPoint(3, 5), it.next());
    }

    /**
     *
     */
    public void testAccount() {
        WorldImpl underlyingWorld = new WorldImpl(10, 10);
        underlyingWorld.addPlayer(player0);

        WorldDiffs worldDiff = new WorldDiffs(underlyingWorld);
        assertEquals(0, worldDiff.numberOfMapDifferences());
        assertEquals(0, worldDiff.listDiffs());

    }

    /**
     *
     */
    public void testEquals() {
        WorldImpl underlyingWorld = new WorldImpl(10, 10);
        underlyingWorld.addPlayer(player0);
        WorldDiffs a = new WorldDiffs(underlyingWorld);
        WorldDiffs b = new WorldDiffs(underlyingWorld);
        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(a, underlyingWorld);
        assertEquals(underlyingWorld, a);
    }

    /**
     *
     */
    public void testGetListDiffs() {
        WorldImpl underlyingWorld = new WorldImpl(10, 10);
        underlyingWorld.addPlayer(player0);
        WorldDiffs diffs = new WorldDiffs(underlyingWorld);
        City city = new City("Bristol", 10, 4);
        diffs.add(SKEY.CITIES, city);

        Iterator<ListKey> it = diffs.getListDiffs();
        @SuppressWarnings("unused")
        ListKey lk1 = it.next();
        ListKey lk2 = it.next();
        assertFalse(it.hasNext());
        ListKey expected = new ListKey(ListKey.Type.Element, WorldDiffs.LISTID.SHARED_LISTS, SKEY.CITIES.getKeyID(), 0);
        assertEquals(expected, lk2);

    }
}