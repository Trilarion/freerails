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

import freerails.util.IntLine;
import freerails.world.FreerailsPathIterator;
import freerails.world.TileTransition;
import freerails.world.train.PathWalker;
import freerails.world.train.PathWalkerImpl;
import freerails.world.train.TrainModel;
import freerails.world.train.TrainPositionOnMap;

import java.awt.*;

/**
 * This class draws a train from an overhead view.
 */
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

        FreerailsPathIterator it = s.path();

        PathWalker pw = new PathWalkerImpl(it);

        // renderer engine.
        renderWagon(g, pw, train.getEngineType(), true);

        // renderer wagons.
        for (int i = 0; i < train.getNumberOfWagons(); i++) {
            int wagonType = train.getWagon(i);
            renderWagon(g, pw, wagonType, false);
        }
    }

    // @SonnyZ
    // This code renders the explosion that occurs when 2 trains crash on the
    // map

    /**
     * @param g
     * @param s
     */
    public void paintTrainCrash(Graphics g, TrainPositionOnMap s) {
        // check to see if there is a train
        // if (s == null) {
        // return;
        // }
        // // Get the image for that frame of the explosion
        // Image explosionImage = rr
        // .getExplosionImage(s.getFrameCt() - 1);
        // // draw the image
        // for (int i = 0; i < s.getLength() - 1; i++) {
        // Point p = new Point(s.getX(i), s.getY(i));
        // g.drawImage(explosionImage, p.x - 15, p.y - 15, null);
        //
        // }
        // // increment the frame count
        // s.incrementFramCt();
    }

    private void renderWagon(Graphics g, PathWalker pw, int type, boolean engine) {
        IntLine wagon = new IntLine();

        IntLine line = new IntLine();

        pw.stepForward(16);

        boolean firstIteration = true;

        while (pw.hasNext()) {
            pw.nextSegment(line);

            if (firstIteration) {
                wagon.x1 = line.x1;
                wagon.y1 = line.y1;
                firstIteration = false;
            }
        }

        wagon.x2 = line.x2;
        wagon.y2 = line.y2;

        TileTransition v = TileTransition
                .getNearestVector(wagon.x2 - wagon.x1, wagon.y2 - wagon.y1);
        Point p = new Point((wagon.x2 + wagon.x1) / 2,
                (wagon.y2 + wagon.y1) / 2);

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