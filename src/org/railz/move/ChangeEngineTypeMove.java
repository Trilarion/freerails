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
package org.railz.move;

import org.railz.world.player.*;
import org.railz.world.top.*;
import org.railz.world.train.*;

public class ChangeEngineTypeMove extends ChangeItemInListMove {
    public static ChangeEngineTypeMove createAvailabilityMove(ObjectKey
	    engineTypeKey, ReadOnlyWorld w, boolean newAvailability) {
	EngineType et = (EngineType) w.get(KEY.ENGINE_TYPES,
		engineTypeKey.index, Player.AUTHORITATIVE);
	EngineType newEt = et.setAvailable(newAvailability);
	return new ChangeEngineTypeMove(engineTypeKey, et, newEt);
    }

    private ChangeEngineTypeMove(ObjectKey engineTypeKey, EngineType oldEt,
	    EngineType newEt) {
	super(engineTypeKey.key, engineTypeKey.index, oldEt, newEt,
		engineTypeKey.principal);
    }
}
