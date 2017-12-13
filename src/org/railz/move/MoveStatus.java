/*
 * Copyright (C) Luke Lindsay
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

import org.railz.world.common.FreerailsSerializable;


/**
 * XXX DO NOT TEST == AGAINST MOVE_FAILED XXX
 *
 * @author lindsal
 */
final public class MoveStatus implements FreerailsSerializable {
    public static final MoveStatus MOVE_OK = new MoveStatus(true,
            "Move accepted");

    /**
     * Not public - only instances of Move should need to access this.
     */
    static final MoveStatus MOVE_FAILED = new MoveStatus(false, "Move rejected");
    
    static final boolean debug =
	(System.getProperty("org.railz.move.MoveStatus.debug") != null);
    
    public final boolean ok;
    public final String message;

    /**
     * Avoid creating a duplicate when deserializing.
     */
    private Object readResolve() {
        if (ok) {
            return MOVE_OK;
        } else {
            return this;
        }
    }

    private MoveStatus(boolean ok, String mess) {
        this.ok = ok;
        this.message = mess;
    }

    public static MoveStatus moveFailed(String reason) {
	if (debug) {
	    System.err.println("Move failed becase: " + reason + " in:");
	    Thread.currentThread().dumpStack();
	}
        return new MoveStatus(false, reason);
    }

    public boolean isOk() {
        return ok;
    }

    public String toString() {
        return message;
    }
}
