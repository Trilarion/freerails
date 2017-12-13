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
 * Created on 28-Mar-2003
 *
 */
package jfreerails.move;

import jfreerails.world.common.GameTime;
import jfreerails.world.player.Player;
import jfreerails.world.station.ProductionAtEngineShop;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.*;


/**
 * Junit TestCase for ChangeProductionAtEngineShopMove.
 * @author Luke
 *
 */
public class ChangeProductionAtEngineShopMoveTest extends AbstractMoveTestCase {
    private ProductionAtEngineShop before;
    private ProductionAtEngineShop after;
    private int engineType;
    private int wagonType;
    private int[] wagons;

    protected void setUp() {
        super.setUp();
	GameTime gt = (GameTime) getWorld().get(ITEM.TIME, Player.AUTHORITATIVE);
        getWorld().add(KEY.STATIONS,
	       	new StationModel(0, 0, "No name", 0, 0, gt),
	       	testPlayer.getPrincipal());
        getWorld().add(KEY.STATIONS,
	       	new StationModel(0, 0, "No name", 0, 0, gt),
		testPlayer.getPrincipal());
        getWorld().add(KEY.STATIONS, 
		new StationModel(0, 0, "No name", 0, 0, gt),
		testPlayer.getPrincipal());

        WagonAndEngineTypesFactory wetf = new WagonAndEngineTypesFactory();
        wetf.addTypesToWorld(getWorld());
        engineType = 0;
        wagonType = 0;
        wagons = new int[] {wagonType, wagonType};
        after = new ProductionAtEngineShop(engineType, wagons);
    }

    public void testMove() {
        before = null;

        ChangeProductionAtEngineShopMove m;

        //Should fail because current production at station 0 is null;
        m = new ChangeProductionAtEngineShopMove(after, before, 0,
		testPlayer.getPrincipal());
        assertTryMoveFails(m);
        assertDoMoveFails(m);

        //Should fail because station 6 does not exist.
        m = new ChangeProductionAtEngineShopMove(before, after, 6,
		testPlayer.getPrincipal());
        assertTryMoveFails(m);
        assertDoMoveFails(m);

        //Should go through
        m = new ChangeProductionAtEngineShopMove(before, after, 0,
		testPlayer.getPrincipal());
        assertTryMoveIsOk(m);
        assertDoMoveIsOk(m);
        assertTryUndoMoveIsOk(m);
        assertUndoMoveIsOk(m);

        //It should not be repeatable.
        assertOkButNotRepeatable(m);

        assertEqualsSurvivesSerialisation(m);
    }

    public void testProductionAtEngineShopEquals() {
        ProductionAtEngineShop a;
        ProductionAtEngineShop b;
        ProductionAtEngineShop c;
        ProductionAtEngineShop d;
        a = null;
        b = new ProductionAtEngineShop(engineType, wagons);
        c = new ProductionAtEngineShop(engineType, wagons);
        assertEquals(c, b);
        assertEquals(b, c);
    }
}
