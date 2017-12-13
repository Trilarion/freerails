/*
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

package org.railz.client.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import org.railz.client.common.Painter;
import org.railz.client.model.ModelRoot;
import org.railz.client.renderer.TrainRenderer;
import org.railz.client.renderer.ViewLists;
import org.railz.world.common.*;
import org.railz.world.player.Player;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.top.*;
import org.railz.world.train.TrainModel;

public class OverHeadTrainView implements Painter {
    private ModelRoot modelRoot;
	
    final TrainRenderer trainPainter;

    ReadOnlyWorld w;

    public OverHeadTrainView(ModelRoot mr) {
	modelRoot = mr;
	this.w = modelRoot.getWorld();
	trainPainter = new TrainRenderer
	    (modelRoot.getViewLists().getTrainImages());
    }

    public void paint(Graphics2D g) {
	g.setColor(Color.BLUE);
	g.setStroke(new BasicStroke(10));
	Stroke st;
	GameTime t = (GameTime) w.get(ITEM.TIME, Player.AUTHORITATIVE);
	NonNullElements j = new NonNullElements(KEY.PLAYERS, w,
		Player.AUTHORITATIVE);
       
	while (j.next()) {
	    FreerailsPrincipal p = (FreerailsPrincipal)
	       ((Player) j.getElement()).getPrincipal();
	    for (int i = 0; i < w.size(KEY.TRAINS, p); i++) {
		TrainModel train = (TrainModel)w.get(KEY.TRAINS, i, p);

		trainPainter.paintTrain(g, train, t);
	    }
	}
    }
}
