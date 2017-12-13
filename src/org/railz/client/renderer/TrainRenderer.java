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

package org.railz.client.renderer;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import org.railz.world.common.*;
import org.railz.world.track.*;
import org.railz.world.train.*;
import org.railz.world.top.*;

/**
 * This class draws a train from an overhead view.
 *
 * @author Luke Lindsay 13-Oct-2002
 *
 */
public class TrainRenderer {
    /**
     * length of a wagon in pixels. TODO calculate this from world model
     */
    private final static int WAGON_LENGTH = TileRenderer.TILE_SIZE.width;

    private final TrainImages trainImages;

    public TrainRenderer(TrainImages trainImages) {
        this.trainImages = trainImages;
    }

    public void paintTrain(Graphics g, TrainModel train, GameTime t) {
        TrainPath s = train.getPosition(t);
	if (s == null)
	    return;

        //renderer engine.
	int wagonPos = 0;
	int wagonLength = trainImages.getEngineLength(train.getEngineType());
        renderWagon(g, s, wagonPos, wagonLength, train.getEngineType(), true);

	int n = train.getNumberOfWagons();
        //renderer wagons.				
        for (int i = 0; i < n; i++) {
	    wagonPos += wagonLength;
            int wagonType = train.getWagon(i);
	    wagonLength = trainImages.getWagonLength(wagonType);
            renderWagon(g, s, wagonPos, wagonLength, wagonType, false);
        }
    }

    /** @param pos the distance from the start of the train of the front of
     * the current wagon in pixels.
     * @param length the length of the current wagon in pixels.
     * @param type index into the WAGON_TYPES or ENGINE_TYPES table
     * @param engine true if we render an engine, false if we render a wagon
     * @param s the TrainPath representing the position of the current train.
     * @param g graphics context to draw on.
     */
    private void renderWagon(Graphics g, TrainPath s, int pos, int length,
	    int type, boolean engine) {
	final Point p = new Point();
	int dist = (int) (((pos + length / 2) * TrackTile.DELTAS_PER_TILE) /
		    TileRenderer.TILE_SIZE.width);
	byte direction = s.getDirectionAtDistance(p, dist);
	// direction of TrainPath is head-to-tail, we reverse it to get the
	// correct orientation for the wagons
	direction = CompassPoints.invert(direction);

        Image image;
        if (engine) {
            image = trainImages.getOverheadEngineImage(type,
		    CompassPoints.eightBitToThreeBit(direction));
        } else {
            image = trainImages.getOverheadWagonImage(type,
		    CompassPoints.eightBitToThreeBit(direction));
        }
	p.x = (p.x * TileRenderer.TILE_SIZE.width / TrackTile.DELTAS_PER_TILE);
	p.y = (p.y * TileRenderer.TILE_SIZE.height / TrackTile.DELTAS_PER_TILE);

        g.drawImage(image, p.x - TileRenderer.TILE_SIZE.width / 2, p.y - TileRenderer.TILE_SIZE.height / 2, null);
    }
}
