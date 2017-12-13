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
import java.util.logging.*;

import org.railz.move.*;
import org.railz.server.scripting.*;
import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.top.*;
/**
 * This class processes pre-scripted gameworld events. Currently this class
 * is merely responsible for generating events at pre-determined times. At
 * some future point in time this may be extended to more complex conditional
 * event generation.
 */
class ScriptingEngine {
    private ReadOnlyWorld world;
    private AuthoritativeMoveExecuter moveExecuter;
    private static final Logger logger = Logger.getLogger("global");

    private LinkedList pendingEvents = new LinkedList();

    ScriptingEngine(ReadOnlyWorld w, AuthoritativeMoveExecuter me) {
	world = w;
	moveExecuter = me;
	NonNullElements i = new NonNullElements(KEY.SCRIPTING_EVENTS,
		w, Player.AUTHORITATIVE);
	while (i.next()) {
	    pendingEvents.add(i.getElement());
	}
    }

    public void processScripts() {
	ListIterator i = pendingEvents.listIterator(0);
	int now = ((GameTime) world.get(ITEM.TIME,
		    Player.AUTHORITATIVE)).getTime();
	while (i.hasNext()) {
	    ScriptingEvent se = (ScriptingEvent) i.next();
	    logger.log(Level.FINE, "event start time " +
		    se.getStartTime().getTime() + " now " + now);
	    if (now >= se.getStartTime().getTime() &&
		    se.getEndTime().getTime() >= now) {
		Move m = se.getMove(world);
		logger.log(Level.INFO, "sending move " + m);
		moveExecuter.processMove(m);
	    }
	    if (now > se.getEndTime().getTime())
		i.remove();
	}
    }
}
