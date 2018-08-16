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

import freerails.model.train.*;
import freerails.model.train.motion.TrainPositionOnMap;
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
     * @param rendererRoot
     */
    public TrainRenderer(RendererRoot rendererRoot) {
        this.rendererRoot = rendererRoot;
    }

    /**
     * @param g
     * @param train
     * @param s
     */
    public void paintTrain(Graphics g, Train train, TrainPositionOnMap s) {
        // If the train has been removed, it will be null!
        if (train == null) {
            return;
        }

        PathIterator it = s.path();

        PathWalker pw = new PathWalkerImpl(it);

        // renderer engine.
        renderWagon(g, pw, train.getEngineId(), true);

        // renderer wagons.
        for (int i = 0; i < train.getNumberOfWagons(); i++) {
            int wagonType = train.getWagonType(i);
            renderWagon(g, pw, wagonType, false);
        }
    }

    private void renderWagon(Graphics g, PathWalker pathWalker, int type, boolean engine) {
        pathWalker.stepForward(16);

        boolean firstIteration = true;

        Vec2D p = null;
        // get the start of the first and the end of the last
        Segment line = null;
        while (pathWalker.hasNext()) {
            line = pathWalker.nextSegment();

            if (firstIteration) {
                p = line.getA();
                firstIteration = false;
            }
        }

        Segment wagon = new Segment(p, line.getB());

        Vec2D diff = Vec2D.subtract(wagon.getB(), wagon.getA());
        TileTransition v = TileTransition.getNearestVector(diff.x, diff.y);
        Vec2D center = Vec2D.divide(Vec2D.add(wagon.getA(), wagon.getB()), 2);
        Image image;

        if (engine) {
            image = rendererRoot.getEngineImages(type).getOverheadImage(v.getID());
        } else {
            image = rendererRoot.getWagonImages(type).getOverheadImage(v.getID());
        }

        g.drawImage(image, center.x - 15, center.y - 15, null);

        // The gap between wagons
        pathWalker.stepForward(8);

        while (pathWalker.hasNext()) {
            line = pathWalker.nextSegment();
        }
    }
}