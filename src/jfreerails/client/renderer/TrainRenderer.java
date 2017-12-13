/*
 * Copyright (C) 2002 Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package jfreerails.client.renderer;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.common.IntLine;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.train.PathWalker;
import jfreerails.world.train.PathWalkerImpl;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainPositionOnMap;


/**
 * This class draws a train from an overhead view.
 *
 * @author Luke Lindsay 13-Oct-2002
 *
 */
public class TrainRenderer {
    private final TrainImages trainImages;

    public TrainRenderer(TrainImages trainImages) {
        this.trainImages = trainImages;
    }

    public void paintTrain(Graphics g, TrainModel train) {
        TrainPositionOnMap s = train.getPosition();

        /*
         * XXX HACK !!
         * really our position ought to be defined at all times, but
         * this is a workaround until we can fix movement
         */
        if (s == null) {
            return;
        }

        FreerailsPathIterator it = s.path();

        PathWalker pw = new PathWalkerImpl(it);

        Graphics2D g2 = (Graphics2D)g;

        //renderer engine.
        renderWagon(g, pw, train.getEngineType(), true);

        //renderer wagons.				
        for (int i = 0; i < train.getNumberOfWagons(); i++) {
            int wagonType = train.getWagon(i);
            renderWagon(g, pw, wagonType, false);
        }
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

        OneTileMoveVector v = OneTileMoveVector.getNearestVector(wagon.x2 -
                wagon.x1, wagon.y2 - wagon.y1);
        Point p = new Point((wagon.x2 + wagon.x1) / 2, (wagon.y2 + wagon.y1) / 2);

        Image image;

        if (engine) {
            image = trainImages.getOverheadEngineImage(type, v.getNumber());
        } else {
            image = trainImages.getOverheadWagonImage(type, v.getNumber());
        }

        g.drawImage(image, p.x - 15, p.y - 15, null);

        //The gap between wagons
        pw.stepForward(8);

        while (pw.hasNext()) {
            pw.nextSegment(line);
        }
    }
}