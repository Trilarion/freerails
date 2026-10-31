/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

import freerails.model.operations.train.*;
import freerails.model.ModelConstants;
import freerails.model.operations.train.motion.TrainPositionOnMap;
import freerails.client.ClientConstants;
import freerails.client.renderer.map.MapCoordinateAdapter;
import freerails.util.Segment;
import freerails.model.terrain.TileTransition;
import freerails.model.track.PathIterator;
import freerails.util.Vec2D;

import java.awt.*;

/**
 * Draws a train from an overhead view.
 */
public class TrainRenderer {

    private final RendererRoot rendererRoot;

    /**
     * Creates a train renderer instance.
     *
     * @param rendererRoot the renderer root parameter
     */
    public TrainRenderer(RendererRoot rendererRoot) {
        this.rendererRoot = rendererRoot;
    }

    /**
     * Performs the paint train operation.
     *
     * @param g the graphics context used for drawing
     * @param train the train parameter
     * @param s the s parameter
     */
    public void paintTrain(Graphics g, Train train, TrainPositionOnMap s) {
        // If the train has been removed, it will be null!
        if (train == null) {
            return;
        }

        PathIterator it = s.path();

        PathWalker pw = new PathWalkerImpl(it);

        // renderer engine.
        renderWagon(g, pw, train.getEngine(), true);

        // renderer wagons.
        for (int i = 0; i < train.getNumberOfWagons(); i++) {
            int wagonType = train.getWagonType(i);
            renderWagon(g, pw, wagonType, false);
        }
    }

    /**
     * Renders the wagon.
     * @param g the g
     * @param pathWalker the path walker
     * @param type the type
     * @param engine the engine
     */
    private void renderWagon(Graphics g, PathWalker pathWalker, int type, boolean engine) {
        pathWalker.stepForward(ModelConstants.WAGON_LENGTH * 2.0 / 3.0);

        boolean firstIteration = true;

        Vec2D p = null;
        // get the start of the first and the end of the last
        Segment line = null;
        while (pathWalker.hasNext()) {
            line = pathWalker.nextSegment();

            if (firstIteration) {
                p = line.a();
                firstIteration = false;
            }
        }

        Segment wagon = new Segment(p, line.b());

        Vec2D pixelA = MapCoordinateAdapter.modelToPixel(wagon.a());
        Vec2D pixelB = MapCoordinateAdapter.modelToPixel(wagon.b());
        Vec2D diff = Vec2D.subtract(pixelB, pixelA);
        TileTransition v = TileTransition.getNearestVector(diff.x(), diff.y());
        Vec2D center = Vec2D.divide(Vec2D.add(pixelA, pixelB), 2);
        Image image;

        if (engine) {
            image = rendererRoot.getEngineImages(type).getOverheadImage(v.getID());
        } else {
            image = rendererRoot.getWagonImages(type).getOverheadImage(v.getID());
        }

        int halfTilePixelSize = ClientConstants.TILE_PIXEL_SIZE / 2;
        g.drawImage(image, center.x() - halfTilePixelSize, center.y() - halfTilePixelSize,
                ClientConstants.TILE_PIXEL_SIZE, ClientConstants.TILE_PIXEL_SIZE, null);

        // The gap between wagons
        pathWalker.stepForward(ModelConstants.WAGON_LENGTH / 3.0);

        while (pathWalker.hasNext()) {
            line = pathWalker.nextSegment();
        }
    }
}