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

package freerails.client.renderer.map;

import freerails.client.ModelRootProperty;
import freerails.model.world.NonNullElementWorldIterator;
import freerails.model.world.PlayerKey;
import freerails.model.world.WorldIterator;
import freerails.util.ui.Painter;
import freerails.client.ModelRoot;
import freerails.util.Vec2D;
import freerails.model.*;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.station.Station;
import freerails.model.terrain.TerrainTile;
import freerails.model.world.UnmodifiableWorld;

import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.geom.RoundRectangle2D;

/**
 * Class to render the station names and spheres of influence on the game map.
 * Names are retrieved from the KEY.Stations object. Updated to also show station sphere of influence.
 */
public class StationNamesRenderer implements Painter {

    private static final float[] dash1 = {5.0f};
    private static final Stroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
    private final UnmodifiableWorld world;
    private final ModelRoot modelRoot;
    private final int fontSize = 10;
    private final Color bgColor = Color.BLACK;
    private final Color textColor = Color.WHITE;
    private final Font font = new Font("Arial", Font.PLAIN, fontSize);

    /**
     * @param world
     * @param modelRoot
     */
    public StationNamesRenderer(UnmodifiableWorld world, ModelRoot modelRoot) {
        this.world = world;
        this.modelRoot = modelRoot;
    }

    /**
     * @param g
     * @param newVisibleRectangle
     */
    public void paint(Graphics2D g, Rectangle newVisibleRectangle) {

        Boolean showStationNames = (Boolean) modelRoot.getProperty(ModelRootProperty.SHOW_STATION_NAMES);
        Boolean showStationBorders = (Boolean) modelRoot.getProperty(ModelRootProperty.SHOW_STATION_BORDERS);

        // for all players
        for (int i = 0; i < world.getNumberOfPlayers(); i++) {
            FreerailsPrincipal principal = world.getPlayer(i).getPrincipal();

            // draw station names onto map

            // for all stations of this player
            WorldIterator worldIterator = new NonNullElementWorldIterator(PlayerKey.Stations, world, principal);
            while (worldIterator.next()) { // loop over non null stations
                Station station = (Station) worldIterator.getElement();

                Vec2D location = station.getLocation();
                Vec2D displayLocation = Vec2D.multiply(location, ModelConstants.TILE_SIZE);
                Rectangle stationBox = new Rectangle(displayLocation.x - ModelConstants.TILE_SIZE * 3, displayLocation.y - ModelConstants.TILE_SIZE * 3, ModelConstants.TILE_SIZE * 7, ModelConstants.TILE_SIZE * 7);
                if (newVisibleRectangle != null && !newVisibleRectangle.intersects(stationBox)) {
                    continue; // station box not visible
                }
                // First draw station sphere of influence
                if (showStationBorders) {
                    TerrainTile tile = (TerrainTile) world.getTile(location);
                    int radius = tile.getTrackPiece().getTrackType().getStationRadius();
                    int diameterInPixels = (radius * 2 + 1) * ModelConstants.TILE_SIZE;
                    int radiusX = (location.x - radius) * ModelConstants.TILE_SIZE;
                    int radiusY = (location.y - radius) * ModelConstants.TILE_SIZE;
                    g.setColor(Color.WHITE);
                    g.setStroke(dashed);
                    g.draw(new RoundRectangle2D.Double(radiusX, radiusY, diameterInPixels, diameterInPixels, 10, 10));
                }

                // Then draw the station name.
                if (showStationNames) {
                    String stationName = station.getStationName();

                    int positionX = displayLocation.x + ModelConstants.TILE_SIZE / 2;
                    int positionY = displayLocation.y + ModelConstants.TILE_SIZE;

                    TextLayout layout = new TextLayout(stationName, font, g.getFontRenderContext());
                    float visibleAdvance = layout.getVisibleAdvance();

                    int rectWidth = (int) (visibleAdvance * 1.2);
                    int rectHeight = (int) (fontSize * 1.5);
                    int rectX = (positionX - (rectWidth / 2));

                    g.setColor(bgColor);
                    g.fillRect(rectX, positionY, rectWidth, rectHeight);

                    float textX = (positionX - (visibleAdvance / 2));
                    float textY = positionY + fontSize + 1;

                    g.setColor(textColor);
                    layout.draw(g, textX, textY);

                    g.setStroke(new BasicStroke(1.0f));
                    // draw a border 1 pixel inside the edges of the rectangle
                    g.draw(new Rectangle(rectX + 1, positionY + 1, rectWidth - 3, rectHeight - 3));
                }
            }
        }
    }
}