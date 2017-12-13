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
package org.railz.world.common;

import org.railz.util.Resources.*;
import org.railz.world.player.*;
import org.railz.world.top.*;

/**
 * Provides a description of victory conditions for the game
 */
public class VictoryConditions implements FreerailsSerializable {
    private final ResourceKey description;
    private final String name;

    /**
     * @return a resource key to the name of this scenario.
     */
    public String getName() {
	return name;
    }

    /**
     * @return a resource key to a textual description of the conditions which
     * must be met in order to be victorious
     */
    public ResourceKey getDescription() {
	return description;
    }

    public VictoryConditions(String name, ResourceKey description) {
	this.description = description;
	this.name = name;
    }
}

