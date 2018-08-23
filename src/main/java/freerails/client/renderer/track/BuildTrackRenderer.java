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
import freerails.client.ModelRootProperty;
import freerails.client.renderer.RendererRoot;
import freerails.util.ui.Painter;
import freerails.client.ModelRoot;
import freerails.util.Vec2D;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.terrain.TerrainTile;
import freerails.model.track.TrackPiece;

import java.awt.*;
import java.util.Map;

/**
 * Draws the track being build.
 */
public class BuildTrackRenderer implements Painter {

    private final ModelRoot modelRoot;
    private final RendererRoot rendererRoot;

    /**
     * @param trackPieceViewList
     * @param modelRoot
     */
    public BuildTrackRenderer(RendererRoot trackPieceViewList, ModelRoot modelRoot) {
        this.modelRoot = modelRoot;
        rendererRoot = trackPieceViewList;
    }

    /**
     * Paints the proposed track and dots to distinguish the proposed track from
     * any existing track.
     */
    @Override
    public void paint(Graphics2D g, Rectangle newVisibleRectangle) {

        if (modelRoot == null) {
            return;
        }
        Map<Vec2D, TrackPiece> proposedTrack = (Map<Vec2D, TrackPiece>) modelRoot.getProperty(ModelRootProperty.PROPOSED_TRACK);

        if (proposedTrack == null) {
            return;
        }

        for (Vec2D point : proposedTrack.keySet()) {
            TrackPiece trackPiece = proposedTrack.get(point);
            if (trackPiece != null) {

                int graphicsNumber = trackPiece.getTrackGraphicID();

                int ruleNumber = trackPiece.getTrackType().getId();
                TrackPieceRenderer trackPieceView = rendererRoot.getTrackPieceView(ruleNumber);
                trackPieceView.drawTrackPieceIcon(g, graphicsNumber, point, ClientConstants.TILE_SIZE);
            }
        }

        UnmodifiableWorld world = modelRoot.getWorld();
        /*
         * Draw small dots for each tile whose track has changed. The dots
         * are white if track has been added or upgraded and red if it has
         * been removed.
         */
        for (Vec2D p : proposedTrack.keySet()) {
            Vec2D location = Vec2D.add(Vec2D.multiply(p, ClientConstants.TILE_SIZE), Vec2D.divide(Vec2D.subtract(ClientConstants.TILE_SIZE, ClientConstants.SMALL_DOT_WIDTH), 2));
            TerrainTile before = world.getTile(p);
            TrackPiece trackPieceBefore = before.getTrackPiece();
            TrackPiece trackPiece = proposedTrack.get(p);

            boolean trackAdded = true;
            if (trackPiece == null) {
                trackAdded = false;
            } else if(trackPieceBefore != null) {
                trackAdded = trackPiece.getTrackConfiguration().contains(trackPieceBefore.getTrackConfiguration());
            }
            Color dotColor = trackAdded ? Color.WHITE : Color.RED;
            g.setColor(dotColor);
            g.fillOval(location.x, location.y, ClientConstants.SMALL_DOT_WIDTH, ClientConstants.SMALL_DOT_WIDTH);
        }
    }

}