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

package freerails.model.world;

import freerails.util.Vector2D;
import freerails.util.ListKey;
import freerails.model.cargo.CargoCategory;
import freerails.model.cargo.CargoType;
import freerails.model.player.Player;
import freerails.model.station.Station;
import freerails.model.terrain.City;
import freerails.model.terrain.FullTerrainTile;
import junit.framework.TestCase;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Test for WorldDifferences.
 */
public class FullWorldDiffsTest extends TestCase {

    private final Player player0 = new Player("player0", 0);
    private final Player player1 = new Player("player1", 1);

    /**
     *
     */
    public void testSharedLists() {
        FullWorld underlyingWorld = new FullWorld(new Vector2D(10, 10));
        CargoType mailCT = new CargoType(10, "Mail", CargoCategory.Mail);
        CargoType passengersCT = new CargoType(10, "Passengers", CargoCategory.Passengers);
        underlyingWorld.add(SharedKey.CargoTypes, mailCT);

        FullWorldDiffs worldDiff = new FullWorldDiffs(underlyingWorld);

        assertEquals(0, worldDiff.listDiffs());
        assertEquals(1, worldDiff.size(SharedKey.CargoTypes));

        Serializable f = worldDiff.get(SharedKey.CargoTypes, 0);
        assertEquals("The mail cargotype should be accessible.", mailCT, f);
        worldDiff.add(SharedKey.CargoTypes, passengersCT);
        assertEquals(2, worldDiff.size(SharedKey.CargoTypes));
        assertEquals("2 Diffs: the length of the list + the actual element", 2, worldDiff.listDiffs());
        f = worldDiff.removeLast(SharedKey.CargoTypes);
        assertEquals(passengersCT, f);

        assertEquals(0, worldDiff.listDiffs());
        f = worldDiff.removeLast(SharedKey.CargoTypes);
        assertEquals(mailCT, f);
        assertEquals("1 Diff: the list length.", 1, worldDiff.listDiffs());
        assertEquals(0, worldDiff.size(SharedKey.CargoTypes));
        worldDiff.add(SharedKey.CargoTypes, mailCT);
        assertEquals(0, worldDiff.listDiffs());
        worldDiff.set(SharedKey.CargoTypes, 0, passengersCT);
        assertEquals("1 Diff: element 0", 1, worldDiff.listDiffs());
    }

    /**
     *
     */
    public void testPlayers() {
        FullWorld underlyingWorld = new FullWorld(new Vector2D(10, 10));
        underlyingWorld.addPlayer(player0);

        FullWorldDiffs worldDiff = new FullWorldDiffs(underlyingWorld);
        assertEquals(0, worldDiff.listDiffs());
        assertEquals(1, worldDiff.getNumberOfPlayers());

        Player player = worldDiff.getPlayer(0);
        assertEquals(player0, player);
        assertTrue(worldDiff.isPlayer(player0.getPrincipal()));

        // Test adding a player.
        int n = worldDiff.addPlayer(player1);
        assertEquals(1, n);

        assertEquals(2, worldDiff.getNumberOfPlayers());

        player = worldDiff.getPlayer(1);
        assertEquals(player1, player);
        assertTrue(worldDiff.isPlayer(player1.getPrincipal()));
    }

    /**
     *
     */
    public void testNonSharedLists() {
        FullWorld underlyingWorld = new FullWorld(new Vector2D(10, 10));
        underlyingWorld.addPlayer(player0);

        Station station0 = new Station();
        underlyingWorld.add(player0.getPrincipal(), PlayerKey.Stations, station0);

        FullWorldDiffs worldDiff = new FullWorldDiffs(underlyingWorld);
        assertEquals(0, worldDiff.listDiffs());

        // First, for an existing player
        assertEquals(1, worldDiff.size(player0.getPrincipal(), PlayerKey.Stations));

        Serializable fs = worldDiff.get(player0.getPrincipal(),
                PlayerKey.Stations, 0);
        assertEquals(station0, fs);

        // Add a station.
        Station station1 = new Station();
        worldDiff.add(player0.getPrincipal(), PlayerKey.Stations, station1);
        assertEquals(2, worldDiff.size(player0.getPrincipal(), PlayerKey.Stations));
        fs = worldDiff.get(player0.getPrincipal(), PlayerKey.Stations, 1);
        assertEquals(station1, fs);

        // Change a station.
        Station station2 = new Station();
        worldDiff.set(player0.getPrincipal(), PlayerKey.Stations, 0, station2);
        fs = worldDiff.get(player0.getPrincipal(), PlayerKey.Stations, 0);
        assertEquals(station2, fs);

        // Remove both stations.
        fs = worldDiff.removeLast(player0.getPrincipal(), PlayerKey.Stations);
        assertEquals(station1, fs);
        fs = worldDiff.removeLast(player0.getPrincipal(), PlayerKey.Stations);
        assertEquals(station2, fs);
        assertEquals(0, worldDiff.size(player0.getPrincipal(), PlayerKey.Stations));

        // Add a station again.
        int i = worldDiff.add(player0.getPrincipal(), PlayerKey.Stations, station1);
        assertEquals(0, i);
        assertEquals(1, worldDiff.size(player0.getPrincipal(), PlayerKey.Stations));

        // Second, for a new player
        worldDiff.addPlayer(player1);
        assertEquals(2, worldDiff.getNumberOfPlayers());

        assertEquals(player1, worldDiff.getPlayer(1));
        assertEquals(0, worldDiff.size(player1.getPrincipal(), PlayerKey.Stations));
        worldDiff.add(player1.getPrincipal(), PlayerKey.Stations, station1);
        assertEquals(1, worldDiff.size(player1.getPrincipal(), PlayerKey.Stations));

        worldDiff.set(player1.getPrincipal(), PlayerKey.Stations, 0, station2);

        worldDiff.add(player1.getPrincipal(), PlayerKey.Stations, station1);
    }

    /**
     *
     */
    public void testUsingNullElements() {
        FullWorld underlyingWorld = new FullWorld(new Vector2D(10, 10));
        underlyingWorld.addPlayer(player0);
        Station station0 = new Station();
        Station station1 = null;
        underlyingWorld.add(player0.getPrincipal(), PlayerKey.Stations, station0);
        underlyingWorld.add(player0.getPrincipal(), PlayerKey.Stations, station1);
        FullWorldDiffs worldDiff = new FullWorldDiffs(underlyingWorld);
        assertEquals(station0, worldDiff.get(player0.getPrincipal(),
                PlayerKey.Stations, 0));
        assertEquals(station1, worldDiff.get(player0.getPrincipal(),
                PlayerKey.Stations, 1));
        worldDiff.set(player0.getPrincipal(), PlayerKey.Stations, 0, station1);
        worldDiff.set(player0.getPrincipal(), PlayerKey.Stations, 1, station0);
    }

    /**
     *
     */
    public void testItem() {
        FullWorld underlyingWorld = new FullWorld(new Vector2D(10, 10));
        Station station0 = new Station();
        Station station1 = new Station();
        underlyingWorld.set(WorldItem.GameRules, station0); // why not!

        FullWorldDiffs worldDiff = new FullWorldDiffs(underlyingWorld);
        assertEquals(station0, worldDiff.get(WorldItem.GameRules));
        worldDiff.set(WorldItem.GameRules, station1);
        assertEquals(station1, worldDiff.get(WorldItem.GameRules));
    }

    /**
     *
     */
    public void testMap() {
        FullWorld underlyingWorld = new FullWorld(new Vector2D(21, 8));
        FullWorldDiffs worldDiff = new FullWorldDiffs(underlyingWorld);
        assertEquals(21, worldDiff.getMapWidth());
        assertEquals(8, worldDiff.getMapHeight());

        FullTerrainTile tile = (FullTerrainTile) underlyingWorld.getTile(new Vector2D(2, 2));
        assertNotNull(tile);
        assertEquals(tile, worldDiff.getTile(new Vector2D(2, 2)));

        FullTerrainTile newTile = FullTerrainTile.getInstance(999);
        worldDiff.setTile(new Vector2D(3, 5), newTile);
        assertEquals(newTile, worldDiff.getTile(new Vector2D(3, 5)));

        Iterator<Vector2D> it = worldDiff.getMapDiffs();
        assertEquals(new Vector2D(3, 5), it.next());
    }

    /**
     *
     */
    public void testAccount() {
        FullWorld underlyingWorld = new FullWorld(new Vector2D(10, 10));
        underlyingWorld.addPlayer(player0);

        FullWorldDiffs worldDiff = new FullWorldDiffs(underlyingWorld);
        assertEquals(0, worldDiff.numberOfMapDifferences());
        assertEquals(0, worldDiff.listDiffs());
    }

    /**
     *
     */
    public void testEquals() {
        FullWorld underlyingWorld = new FullWorld(new Vector2D(10, 10));
        underlyingWorld.addPlayer(player0);
        FullWorldDiffs a = new FullWorldDiffs(underlyingWorld);
        FullWorldDiffs b = new FullWorldDiffs(underlyingWorld);
        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(a, underlyingWorld);
        assertEquals(underlyingWorld, a);
    }

    /**
     *
     */
    public void testGetListDiffs() {
        FullWorld underlyingWorld = new FullWorld(new Vector2D(10, 10));
        underlyingWorld.addPlayer(player0);
        FullWorldDiffs diffs = new FullWorldDiffs(underlyingWorld);
        City city = new City("Bristol", new Vector2D(10, 4));
        diffs.add(SharedKey.Cities, city);

        Iterator<ListKey> it = diffs.getListDiffs();

        ListKey lk1 = it.next();
        ListKey lk2 = it.next();
        assertFalse(it.hasNext());
        ListKey expected = new ListKey(ListKey.Type.Element, FullWorldDiffsListID.SHARED_LISTS, SharedKey.Cities.getId(), 0);
        assertEquals(expected, lk2);
    }
}