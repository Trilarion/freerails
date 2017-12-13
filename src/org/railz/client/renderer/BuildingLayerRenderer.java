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
package org.railz.client.renderer;

import java.awt.Graphics;
import java.awt.Shape;
import java.awt.Rectangle;

import org.railz.world.building.*;
import org.railz.world.top.*;
import org.railz.world.track.*;

/**
 * Renders the building layer (duh!).
 * @author rtuck99@users.berlios.de
 */
class BuildingLayerRenderer implements MapLayerRenderer {
    private final ReadOnlyWorld world;
    private final TileRendererList buildingRenderers;

    public BuildingLayerRenderer(ReadOnlyWorld w, ViewLists vl) {
	world = w;
	buildingRenderers = vl.getBuildingViewList();
    }

    public void paintTile(Graphics g, int tileX, int tileY) {
	FreerailsTile t = world.getTile(tileX, tileY);
	BuildingTile bt = t.getBuildingTile();
	if (bt == null)
	    return;

	int type = bt.getType();
	TileRenderer tr = buildingRenderers.getTileViewWithNumber(type);
	tr.renderTile(g, tileX * TileRenderer.TILE_SIZE.width, tileY *
		TileRenderer.TILE_SIZE.height, tileX, tileY, world);
    }

    public void refreshTile(int x, int y) {
	/*
	 * none of the other implementations bother to implement this so we
	 * won't bother either...
	 */
    }

    public void paintRect(Graphics g, Rectangle visibleRect) {
	final Rectangle r = new Rectangle();
	Shape clipShape = g.getClip();
	g.setClip(0, 0, visibleRect.width, visibleRect.height);
	r.x = visibleRect.x / TileRenderer.TILE_SIZE.width;
	r.y = visibleRect.y / TileRenderer.TILE_SIZE.height;
	r.width = visibleRect.width / TileRenderer.TILE_SIZE.width + 1;
	r.height = visibleRect.width / TileRenderer.TILE_SIZE.height + 1;
	paintRectangleOfTiles(g, r);
	g.setClip(clipShape);
    }

    void paintRectangleOfTiles(Graphics g, int x, int y, int width, int
	    height) {
	for (int yy = y; yy < y + height; yy++) {
	    for (int xx = x; xx < x + width; xx++) {
		paintTile(g, xx, yy);
	    }
	}
    }

    void paintRectangleOfTiles(Graphics g, Rectangle tilesToPaint) {
	paintRectangleOfTiles(g, tilesToPaint.x, tilesToPaint.y,
		tilesToPaint.width, tilesToPaint.height);
    }
}
