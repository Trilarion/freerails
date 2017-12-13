/*
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

package org.railz.client.view;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import org.railz.client.model.ModelRoot;
import org.railz.client.renderer.CityNamesRenderer;
import org.railz.client.renderer.MapBackgroundRender;
import org.railz.client.renderer.MapLayerRenderer;
import org.railz.client.renderer.MapRenderer;
import org.railz.client.renderer.SquareTileBackgroundRenderer;
import org.railz.client.renderer.StationNamesRenderer;
import org.railz.client.renderer.StationRadiusRenderer;
import org.railz.client.renderer.ViewLists;
import org.railz.client.common.Painter;
import org.railz.world.top.ReadOnlyWorld;


public class DetailMapView implements MapRenderer {
    private static boolean OSXWorkaround = (System.getProperty("OSXWorkaround") != null);
    private final MapLayerRenderer background;
    private final Dimension mapSizeInPixels;
    private final OverHeadTrainView trainsview;
    private final Painter cityNames;
    private final Painter stationNames;
    private final StationRadiusRenderer stationRadius;

    public DetailMapView(ModelRoot mr) {
	ReadOnlyWorld world = mr.getWorld();
	ViewLists vl = mr.getViewLists();
        trainsview = new OverHeadTrainView(mr);

        if (OSXWorkaround) {
            //Don't buffer the mapviews background.
            background = new MapBackgroundRender(world, vl);
        } else {
            background = new SquareTileBackgroundRenderer
		(new MapBackgroundRender(world, vl), 30);
        }

        Dimension mapSize = new Dimension(world.getMapWidth(),
                world.getMapHeight());
        mapSizeInPixels = new Dimension(mapSize.width * 30, mapSize.height * 30);

        cityNames = new CityNamesRenderer(world);
        stationNames = new StationNamesRenderer(world);
        stationRadius = new StationRadiusRenderer();
    }

    public StationRadiusRenderer getStationRadius() {
        return stationRadius;
    }

    public float getScale() {
        return 30;
    }

    public Dimension getMapSizeInPixels() {
        return mapSizeInPixels;
    }

    public void paintTile(Graphics g, int tileX, int tileY) {
        background.paintTile(g, tileX, tileY);
        trainsview.paint((Graphics2D)g);
        cityNames.paint((Graphics2D)g);
        stationNames.paint((Graphics2D)g);
        stationRadius.paint((Graphics2D)g);
    }

    public void refreshTile(int x, int y) {
        background.refreshTile(x, y);
    }

    /**
     * @param g Graphics context with origin pointing to map origin.
     * @param visibleRect rectangle defining area of map to draw relative to
     * origin 0,0 at top left of map, measured in pixels.
     */
    public void paintRect(Graphics g, Rectangle visibleRect) {
        background.paintRect(g, visibleRect);
        trainsview.paint((Graphics2D)g);
        cityNames.paint((Graphics2D)g);
        stationNames.paint((Graphics2D)g);
        stationRadius.paint((Graphics2D)g);
    }
}
