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
        renderWagon(g, s, 0, train.getEngineType(), true);

	int n = train.getNumberOfWagons() + 1;
        //renderer wagons.				
        for (int i = 1; i < n; i++) {
            int wagonType = train.getWagon(i - 1);
            renderWagon(g, s, i, wagonType, false);
        }
    }

    private void renderWagon(Graphics g, TrainPath s, int n, int type, boolean
	    engine) {
	final Point p = new Point();
	byte direction = s.getDirectionAtDistance(p, (int) ((n + 0.5) *
		    WAGON_LENGTH * TrackTile.DELTAS_PER_TILE * 0.8 /
		    TileRenderer.TILE_SIZE.width));
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
