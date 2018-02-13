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

import freerails.client.ClientConfig;
import freerails.client.common.Painter;
import freerails.controller.ModelRoot;
import freerails.util.Vector2D;
import freerails.world.FullWorldDiffs;
import freerails.world.ReadOnlyWorld;
import freerails.world.terrain.FullTerrainTile;
import freerails.world.track.TrackPiece;

import java.awt.*;
import java.util.Iterator;

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

    private FullWorldDiffs getWorldDiffs() {
        if (modelRoot == null) {
            return null;
        }
        return (FullWorldDiffs) modelRoot.getProperty(ModelRoot.Property.PROPOSED_TRACK);
    }

    /**
     * Paints the proposed track and dots to distinguish the proposed track from
     * any existing track.
     */
    public void paint(Graphics2D g, Rectangle newVisibleRectangle) {

        FullWorldDiffs worldDiffs = getWorldDiffs();
        if (null != worldDiffs) {
            for (Iterator<Vector2D> iter = worldDiffs.getMapDiffs(); iter.hasNext(); ) {
                Vector2D point = iter.next();
                FullTerrainTile fp = (FullTerrainTile) worldDiffs.getTile(point);
                TrackPiece trackPiece = fp.getTrackPiece();

                int graphicsNumber = trackPiece.getTrackGraphicID();

                int ruleNumber = trackPiece.getTrackTypeID();
                TrackPieceRenderer trackPieceView = rendererRoot.getTrackPieceView(ruleNumber);
                trackPieceView.drawTrackPieceIcon(graphicsNumber, g, point.x, point.y, ClientConfig.tileSize);
            }

            ReadOnlyWorld realWorld = modelRoot.getWorld();
            /*
             * Draw small dots for each tile whose track has changed. The dots
             * are white if track has been added or upgraded and red if it has
             * been removed.
             */
            for (Iterator<Vector2D> iter = worldDiffs.getMapDiffs(); iter.hasNext(); ) {
                Vector2D p = iter.next();
                int x = p.x * ClientConfig.tileSize.width + (ClientConfig.tileSize.width - ClientConfig.SMALL_DOT_WIDTH) / 2;
                int y = p.y * ClientConfig.tileSize.height + (ClientConfig.tileSize.height - ClientConfig.SMALL_DOT_WIDTH) / 2;
                FullTerrainTile before = (FullTerrainTile) realWorld.getTile(p);
                FullTerrainTile after = (FullTerrainTile) worldDiffs.getTile(p);

                boolean trackRemoved = !after.getTrackPiece().getTrackConfiguration().contains(before.getTrackPiece().getTrackConfiguration());
                Color dotColor = trackRemoved ? Color.RED : Color.WHITE;
                g.setColor(dotColor);
                g.fillOval(x, y, ClientConfig.SMALL_DOT_WIDTH, ClientConfig.SMALL_DOT_WIDTH);
            }
        }
    }

}