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
import freerails.world.track.PathIterator;
import freerails.world.terrain.TileTransition;
import freerails.world.train.PathWalker;
import freerails.world.train.PathWalkerImpl;
import freerails.world.train.TrainModel;
import freerails.world.train.TrainPositionOnMap;

import java.awt.*;

/**
 * Draws a train from an overhead view.
 */
@SuppressWarnings("unused")
public class TrainRenderer {
    private final RendererRoot rr;

    /**
     * @param trainImages
     */
    public TrainRenderer(RendererRoot trainImages) {
        this.rr = trainImages;
    }

    /**
     * @param g
     * @param train
     * @param s
     */
    public void paintTrain(Graphics g, TrainModel train, TrainPositionOnMap s) {
        // If the train has been removed, it will be null!
        if (train == null) {
            return;
        }

        /*
         * XXX HACK !! really our position ought to be defined at all times, but
         * this is a workaround until we can fix movement
         */
        if (s == null) {
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

        TileTransition v = TileTransition
                .getNearestVector(wagon.getX2() - wagon.getX1(), wagon.getY2() - wagon.getY1());
        Point p = new Point((wagon.getX2() + wagon.getX1()) / 2,
                (wagon.getY2() + wagon.getY1()) / 2);

        Image image;

        if (engine) {
            image = rr.getEngineImages(type).getOverheadImage(v.getID());
        } else {
            image = rr.getWagonImages(type).getOverheadImage(v.getID());
        }

        g.drawImage(image, p.x - 15, p.y - 15, null);

        // The gap between wagons
        pw.stepForward(8);

        while (pw.hasNext()) {
            pw.nextSegment(line);
        }
    }
}