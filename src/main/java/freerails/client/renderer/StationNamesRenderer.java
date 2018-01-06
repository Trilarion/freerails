/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.client.renderer;

import freerails.client.ClientConstants;
import freerails.client.common.Painter;
import freerails.controller.ModelRoot;
import freerails.world.KEY;
import freerails.world.NonNullElementWorldIterator;
import freerails.world.ReadOnlyWorld;
import freerails.world.WorldIterator;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.Station;
import freerails.world.terrain.FullTerrainTile;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.RoundRectangle2D;

/**
 * Class to render the station names and spheres of influence on the game map.
 * Names are retrieved from the KEY.STATIONS object. Date: 14th April 2003 28
 * May 2004 updated to also show station sphere of influence.
 */
public class StationNamesRenderer implements Painter {
    static final float[] dash1 = {5.0f};
    static final BasicStroke dashed = new BasicStroke(1.0f,
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
    private final ReadOnlyWorld w;
    private final ModelRoot modelRoot;
    private final int fontSize;
    private final Color bgColor;
    private final Color textColor;
    private final Font font;

    /**
     * @param world
     * @param modelRoot
     */
    public StationNamesRenderer(ReadOnlyWorld world, ModelRoot modelRoot) {
        this.w = world;
        this.modelRoot = modelRoot;
        this.fontSize = 10;
        this.bgColor = Color.BLACK;
        this.textColor = Color.WHITE;
        font = new Font("Arial", 0, fontSize);
    }

    /**
     * @param g
     * @param newVisibleRectangle
     */
    public void paint(Graphics2D g, Rectangle newVisibleRectangle) {
        int rectWidth;
        int rectHeight;
        int rectX;
        int rectY;
        float visibleAdvance;
        float textX;
        float textY;

        Station tempStation;
        String stationName;
        int positionX;
        int positionY;

        Boolean showStationNames = (Boolean) modelRoot
                .getProperty(ModelRoot.Property.SHOW_STATION_NAMES);
        Boolean showStationBorders = (Boolean) modelRoot
                .getProperty(ModelRoot.Property.SHOW_STATION_BORDERS);

        FontRenderContext frc = g.getFontRenderContext();
        TextLayout layout;

        for (int i = 0; i < w.getNumberOfPlayers(); i++) {
            FreerailsPrincipal principal = w.getPlayer(i).getPrincipal();

            // draw station names onto map
            WorldIterator wi = new NonNullElementWorldIterator(KEY.STATIONS, w, principal);

            while (wi.next()) { // loop over non null stations
                tempStation = (Station) wi.getElement();

                int x = tempStation.getStationX();
                int y = tempStation.getStationY();
                int xdisp = x * ClientConstants.TILE_SIZE;
                int ydisp = y * ClientConstants.TILE_SIZE;
                Rectangle stationBox = new Rectangle(xdisp
                        - ClientConstants.TILE_SIZE * 3, ydisp - ClientConstants.TILE_SIZE
                        * 3, ClientConstants.TILE_SIZE * 7, ClientConstants.TILE_SIZE * 7);
                if (newVisibleRectangle != null
                        && !newVisibleRectangle.intersects(stationBox)) {
                    continue; // station box not visible
                }
                // First draw station sphere of influence
                if (showStationBorders) {
                    FullTerrainTile tile = (FullTerrainTile) w.getTile(x, y);
                    int radius = tile.getTrackPiece().getTrackRule()
                            .getStationRadius();
                    int diameterInPixels = (radius * 2 + 1)
                            * ClientConstants.TILE_SIZE;
                    int radiusX = (x - radius) * ClientConstants.TILE_SIZE;
                    int radiusY = (y - radius) * ClientConstants.TILE_SIZE;
                    g.setColor(Color.WHITE);
                    g.setStroke(dashed);
                    g.draw(new RoundRectangle2D.Double(radiusX, radiusY,
                            diameterInPixels, diameterInPixels, 10, 10));
                }

                // Then draw the station name.
                if (showStationNames) {
                    stationName = tempStation.getStationName();

                    positionX = xdisp + ClientConstants.TILE_SIZE / 2;
                    positionY = ydisp + ClientConstants.TILE_SIZE;

                    layout = new TextLayout(stationName, font, frc);
                    visibleAdvance = layout.getVisibleAdvance();

                    rectWidth = (int) (visibleAdvance * 1.2);
                    rectHeight = (int) (fontSize * 1.5);
                    rectX = (positionX - (rectWidth / 2));
                    rectY = positionY;

                    g.setColor(bgColor);
                    g.fillRect(rectX, rectY, rectWidth, rectHeight);

                    textX = (positionX - (visibleAdvance / 2));
                    textY = positionY + fontSize + 1;

                    g.setColor(textColor);
                    layout.draw(g, textX, textY);

                    g.setStroke(new BasicStroke(1.0f));
                    // draw a border 1 pixel inside the edges of the rectangle
                    g.draw(new Rectangle(rectX + 1, rectY + 1, rectWidth - 3,
                            rectHeight - 3));
                }
            }
        }

        // end FOR loop
    }
    // paint method
}