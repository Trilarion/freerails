/*
 * Copyright (C) 2003 Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/*
 * Created on 15-Apr-2003
 *
 */
package org.railz.world.top;

import java.util.NoSuchElementException;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.railz.world.common.GameTime;
import org.railz.world.player.Player;
import org.railz.world.station.StationModel;
import org.railz.world.top.ITEM;

/**
 * This junit TestCase tests NonNullElements.
 * @author Luke
 *
 */
public class NonNullElementsTest extends TestCase {
    World w;
    StationModel station1;
    StationModel station2;
    StationModel station3;

    private Player testPlayer = new Player ("test player", (new Player ("test"
		    + " player")).getPublicKey(), 0);


    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite testSuite = new TestSuite(NonNullElementsTest.class);

        return testSuite;
    }

    protected void setUp() {
        w = new WorldImpl();
	w.add(KEY.PLAYERS, testPlayer, Player.AUTHORITATIVE);
	GameTime now = new GameTime(0);
	w.set(ITEM.TIME, now);

        station1 = new StationModel(10, 20, "Station1", 4, 0, now);
        station2 = new StationModel(15, 16, "Station2", 4, 1, now);
        station3 = new StationModel(30, 50, "Station3", 4, 2, now);
        w.add(KEY.STATIONS, station1, testPlayer.getPrincipal());
        w.add(KEY.STATIONS, null, testPlayer.getPrincipal());
        w.add(KEY.STATIONS, station2, testPlayer.getPrincipal());
        w.add(KEY.STATIONS, null, testPlayer.getPrincipal());
        w.add(KEY.STATIONS, null, testPlayer.getPrincipal());
        w.add(KEY.STATIONS, station3, testPlayer.getPrincipal());
    }

    public void testNext() {
	WorldIterator wi = new NonNullElements(KEY.STATIONS, w,
		testPlayer.getPrincipal());
        assertEquals(WorldIterator.BEFORE_FIRST, wi.getRowNumber());
        assertEquals(WorldIterator.BEFORE_FIRST, wi.getIndex());

        //Look at first station
        boolean b = wi.next();
        assertTrue(b);

        int index = wi.getIndex();
        assertEquals(0, index);
        assertEquals(0, wi.getRowNumber());
        assertEquals(station1, wi.getElement());

        //Look at seond station
        assertTrue(wi.next());
        assertEquals(2, wi.getIndex());
        assertEquals(1, wi.getRowNumber());
        assertEquals(station2, wi.getElement());

        WorldIterator wi2 = new NonNullElements(KEY.TRACK_RULES, w);
        assertTrue(!wi2.next());
    }

    public void testGotoIndex() {
	WorldIterator wi = new NonNullElements(KEY.STATIONS, w,
		testPlayer.getPrincipal());
        assertEquals(WorldIterator.BEFORE_FIRST, wi.getRowNumber());
        assertEquals(WorldIterator.BEFORE_FIRST, wi.getIndex());

        wi.gotoIndex(2);
        assertEquals(2, wi.getIndex());
        assertEquals(1, wi.getRowNumber());

        try {
            wi.gotoIndex(100);
            assertTrue(false);
        } catch (NoSuchElementException e) {
        }
    }
}
