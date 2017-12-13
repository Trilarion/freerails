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

package jfreerails.client.renderer;

import jfreerails.world.top.ReadOnlyWorld;


/**
*  Description of the Interface
*
*@author     Luke Lindsay
*     09 October 2001
*/
public interface TileRendererList {
    TileRenderer getTileViewWithNumber(int i);

    /** Checks whether this tile view list has tile views for all
     * the terrain types in the specifed list.
     */
    boolean validate(ReadOnlyWorld world);
}