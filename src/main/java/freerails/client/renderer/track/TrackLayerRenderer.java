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

package freerails.client.renderer.track;

import freerails.client.ClientConstants;
import freerails.client.renderer.RendererRoot;
import freerails.client.renderer.map.MapLayerRenderer;
import freerails.model.terrain.TerrainTile;
import freerails.model.track.TrackPiece;
import freerails.model.world.UnmodifiableWorld;
import freerails.util.Vec2D;

import java.awt.*;

/**
 * This inner class represents a view of the track on the map.
 */
public final class TrackLayerRenderer implements MapLayerRenderer {

    private final UnmodifiableWorld world;
    private final RendererRoot rendererRoot;
    private final Vec2D mapSize;

    /**
     * @param world
     * @param trackPieceViewList
     */
    public TrackLayerRenderer(UnmodifiableWorld world, RendererRoot trackPieceViewList) {
        rendererRoot = trackPieceViewList;
        this.world = world;
        mapSize = world.getMapSize();
    }

    /**
     * Paints a rectangle of tiles onto the supplied graphics context.
     *
     * @param g            The graphics context on which the tiles get painted.
     * @param tilesToPaint The rectangle, measured in tiles, to paint.
     */
    private void paintRectangleOfTiles(Graphics g, Rectangle tilesToPaint) {
        /*
         * Track can overlap the adjacent terrain tiles by half a tile. This
         * means that we need to paint the track from the tiles bordering
         * the specified rectangle of tiles (tilesToPaint). To prevent
         * unnecessary painting, we set the clip to expose only the rectangle
         * of tilesToPaint.
         */
        for (int tileX = tilesToPaint.x - 1; tileX < tilesToPaint.x + tilesToPaint.width + 1; tileX++) {
            for (int tileY = tilesToPaint.y - 1; tileY < tilesToPaint.y + tilesToPaint.height + 1; tileY++) {
                if (tileX >= 0 && tileX < mapSize.x && tileY >= 0 && tileY < mapSize.y) {
                    Vec2D tileLocation = new Vec2D(tileX, tileY);
                    TerrainTile terrainTile = world.getTile(tileLocation);
                    TrackPiece trackPiece = terrainTile.getTrackPiece();
                    if (trackPiece != null) {
                        int graphicsNumber = trackPiece.getTrackGraphicID();
                        int ruleNumber = trackPiece.getTrackType().getId();
                        TrackPieceRenderer trackPieceView = rendererRoot.getTrackPieceView(ruleNumber);
                        trackPieceView.drawTrackPieceIcon(g, graphicsNumber, tileLocation, ClientConstants.TILE_SIZE);
                    }
                }
            }
        }
    }

    /**
     * @param g
     * @param tileLocation
     */
    @Override
    public void paintTile(Graphics g, Vec2D tileLocation) {
        /*
         * Since track tiles overlap the adjacent terrain tiles, we create a
         * temporary Graphics object that only lets us draw on the selected
         * tile.
         */
        paintRectangleOfTiles(g, new Rectangle(tileLocation.x, tileLocation.y, 1, 1));
    }

    public void paintRectangleOfTiles(Graphics g, Vec2D p, int width, int height) {
        paintRectangleOfTiles(g, new Rectangle(p.x, p.y, width, height));
    }

    /**
     * @param tileLocation
     */
    @Override
    public void refreshTile(Vec2D tileLocation) {}

    /**
     * @param g
     * @param visibleRect
     */
    @Override
    public void paintRect(Graphics g, Rectangle visibleRect) {
        throw new UnsupportedOperationException("Method not yet implemented.");
    }

    /**
     *
     */
    @Override
    public void refreshAll() {
    }
}
