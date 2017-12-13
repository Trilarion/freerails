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

import org.railz.world.player.*;
import org.railz.world.top.*;
/**
 * Defines an interface to be implemented by all statistics-gathering
 * classes.
 */
public interface StatMonitor {
    /** @return a resource key for the name of this statistic */
    public String getName();
    /** @return a resource key for the description of this statistic */
    public String getDescription();
    /** @return a resource key for the description of the y unit for this
     * statistic */
    public String getYUnit();
    /** Calculate the y-value at the current time for the given player */
    public int calculateDataPoint(ReadOnlyWorld w, FreerailsPrincipal p);
}

