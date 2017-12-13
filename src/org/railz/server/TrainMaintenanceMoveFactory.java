/*
 * Copyright (C) 2004 Robert Tuck
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

package org.railz.server;

import org.railz.move.AddTransactionMove;
import org.railz.world.accounts.*;
import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.top.*;
import org.railz.world.train.*;
/**
 * Responsible for charging players for train maintenance
 *
 * @author rtuck99@users.berlios.de
 */
class TrainMaintenanceMoveFactory {
    private ReadOnlyWorld world;
    private AuthoritativeMoveExecuter moveReceiver;

    TrainMaintenanceMoveFactory(ReadOnlyWorld w, AuthoritativeMoveExecuter me) {
	world = w;
	moveReceiver = me;
    }

    void generateMoves() {
	NonNullElements i = new NonNullElements(KEY.PLAYERS, world,
		Player.AUTHORITATIVE);
	TrainModelViewer tmv = new TrainModelViewer(world);
	while (i.next()) {
	    FreerailsPrincipal p = ((Player) i.getElement()).getPrincipal();
	    NonNullElements trains = new NonNullElements(KEY.TRAINS, world,
		    p);
	    long totalMaintenance = 0;
	    while (trains.next()) {
		TrainModel train = (TrainModel) trains.getElement();
		tmv.setTrainModel(train);
		totalMaintenance += tmv.getMaintenance();
	    }
	    GameTime now = (GameTime) world.get(ITEM.TIME,
		    Player.AUTHORITATIVE);
	    Bill b = new Bill(now, totalMaintenance,
		    Bill.ROLLING_STOCK_MAINTENANCE);
	    AddTransactionMove m = new AddTransactionMove(0, b, p);
	    moveReceiver.processMove(m, p);
	}
    }
}

