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

/*
* MapView.java
*
* Created on 01 August 2001, 06:16
*/
package jfreerails.client.renderer;

import java.awt.Graphics;
import java.awt.Rectangle;


/**
*
* @author  Luke Lindsay
*/
public interface MapLayerRenderer {
    void paintTile(Graphics g, int tileX, int tileY);

    void refreshTile(int x, int y);

    void paintRect(Graphics g, Rectangle visibleRect);
}