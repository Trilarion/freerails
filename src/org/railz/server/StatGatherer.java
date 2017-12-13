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

import java.util.*;

import org.railz.controller.*;
import org.railz.move.*;
import org.railz.server.stats.*;
import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.top.*;
/**
 * responsible for gathering statistics for each player.
 * TODO dynamic discovery of Statistics-gathering classes.
 */
class StatGatherer {
    private MoveReceiver moveReceiver;
    private ReadOnlyWorld world;

    private StatMonitor[] monitors = new StatMonitor[] {
	/* XXX insert StatMonitor instances here */
	new TotalAssets(),
	new Revenue()
    };

    public StatGatherer(ReadOnlyWorld w, MoveReceiver mr) {
	moveReceiver = mr;
	world = w;
    }

    public CompositeMove generateNewPlayerMove(FreerailsPrincipal p) {
	GameTime t = (GameTime) world.get(ITEM.TIME,
		Player.AUTHORITATIVE);
	ArrayList l = new ArrayList();
	int index = world.size(KEY.STATISTICS, p);
	for (int j = 0; j < monitors.length; j++) {
	    ObjectKey ok;
	    Statistic stat = new Statistic(monitors[j].getName(),
			    monitors[j].getDescription(),
			    monitors[j].getYUnit());
	    ok = new ObjectKey(KEY.STATISTICS, p, index++);
	    l.add(new AddStatisticMove(ok, stat));
	}
	return new CompositeMove((Move[]) l.toArray(new Move[l.size()]));
    }

    public void generateMoves() {
	NonNullElements i = new NonNullElements(KEY.PLAYERS, world,
		Player.AUTHORITATIVE);
	GameTime t = (GameTime) world.get(ITEM.TIME,
		Player.AUTHORITATIVE);
	while (i.next()) {
	    FreerailsPrincipal p = (FreerailsPrincipal) ((Player)
		    i.getElement()).getPrincipal();
	    for (int j = 0; j < monitors.length; j++) {
		Statistic stat = null; 
		NonNullElements k = new NonNullElements(KEY.STATISTICS, world,
			p);
		ObjectKey ok = null;
		while (k.next()) {
		    Statistic s = (Statistic) k.getElement();
		    if (s.getName().equals(monitors[j].getName())) {
			ok = new ObjectKey(KEY.STATISTICS, p, k.getIndex());
			stat = s;
			break;
		    }
		}
		if (stat == null) {
		    // statistic doesn't exist
		    stat = new Statistic(monitors[j].getName(),
			    monitors[j].getDescription(),
			    monitors[j].getYUnit());
		    ok = new ObjectKey(KEY.STATISTICS, p,
			    world.size(KEY.STATISTICS, p));
		    moveReceiver.processMove(new AddStatisticMove
			    (new ObjectKey(KEY.STATISTICS, p, ok.index), stat));
		}
		moveReceiver.processMove(new AddDataPointMove(ok, t,
			    monitors[j].calculateDataPoint(world, p), world));
	    }
	}
    }
}
