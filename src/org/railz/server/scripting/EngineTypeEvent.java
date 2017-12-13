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
package org.railz.server.scripting;

import java.util.*;
import org.xml.sax.*;

import org.railz.move.*;
import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.top.*;
import org.railz.world.train.*;

/** This event causes a particular EngineType to be made available or
 * unavailable */
class EngineTypeEvent extends ScriptingEvent {
    private boolean newAvailability;
    private ObjectKey engineTypeKey;

    public Move getMove(ReadOnlyWorld w) {
	return ChangeEngineTypeMove.createAvailabilityMove
	    (engineTypeKey, w, newAvailability);
    }

    public EngineTypeEvent(ReadOnlyWorld w, GameTime startTime, GameTime
	    endTime, Map params) {
	super(startTime, endTime);
	engineTypeKey = null;
	Iterator i = params.keySet().iterator();
	while (i.hasNext()) {
	    String key = (String) i.next();
	    if ("available".equals(key)) {
		newAvailability="true".equals(params.get("available"));
	    }
	    if ("engineType".equals(key)) {
		String etName = (String) params.get("engineType");
		NonNullElements j = new NonNullElements(KEY.ENGINE_TYPES, w,
			Player.AUTHORITATIVE);
		while (j.next()) {
		    EngineType et = (EngineType) j.getElement();
		    if (et.getEngineTypeName().equals(etName)) {
			engineTypeKey = new ObjectKey
			    (KEY.ENGINE_TYPES, Player.AUTHORITATIVE,
			     j.getIndex());
		    }
		}
		if (engineTypeKey == null) {
		    throw new IllegalArgumentException
			("Unknown EngineType " + etName);
		}
	    }
	}
    }
}
