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

import freerails.util.LineSegment;
import freerails.model.terrain.TileTransition;
import freerails.model.track.PathIterator;
import freerails.model.train.PathWalker;
import freerails.model.train.PathWalkerImpl;
import freerails.model.train.Train;
import freerails.model.train.TrainPositionOnMap;

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
        renderWagon(g, pw, train.getEngineType(), true);

        // renderer wagons.
        for (int i = 0; i < train.getNumberOfWagons(); i++) {
            int wagonType = train.getWagon(i);
            renderWagon(g, pw, wagonType, false);
        }
    }

    private void renderWagon(Graphics g, PathWalker pw, int type, boolean engine) {
        LineSegment wagon = new LineSegment();

        LineSegment line = new LineSegment();

        pw.stepForward(16);

        boolean firstIteration = true;

        while (pw.hasNext()) {
            pw.nextSegment(line);

            if (firstIteration) {
                wagon.setX1(line.getX1());
                wagon.setY1(line.getY1());
                firstIteration = false;
            }
        }

        wagon.setX2(line.getX2());
        wagon.setY2(line.getY2());

        TileTransition v = TileTransition.getNearestVector(wagon.getX2() - wagon.getX1(), wagon.getY2() - wagon.getY1());
        Point p = new Point((wagon.getX2() + wagon.getX1()) / 2, (wagon.getY2() + wagon.getY1()) / 2);

        Image image;

        if (engine) {
            image = rendererRoot.getEngineImages(type).getOverheadImage(v.getID());
        } else {
            image = rendererRoot.getWagonImages(type).getOverheadImage(v.getID());
        }

        g.drawImage(image, p.x - 15, p.y - 15, null);

        // The gap between wagons
        pw.stepForward(8);

        while (pw.hasNext()) {
            pw.nextSegment(line);
        }
    }
}