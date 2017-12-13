package jfreerails.client.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import jfreerails.client.common.Painter;
import jfreerails.client.model.ModelRoot;
import jfreerails.client.renderer.TrainRenderer;
import jfreerails.client.renderer.ViewLists;
import jfreerails.world.player.Player;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.train.TrainModel;

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
	NonNullElements j = new NonNullElements(KEY.PLAYERS, w,
		Player.AUTHORITATIVE);
       
	while (j.next()) {
	    FreerailsPrincipal p = (FreerailsPrincipal)
	       ((Player) j.getElement()).getPrincipal();
	    for (int i = 0; i < w.size(KEY.TRAINS, p); i++) {
		TrainModel train = (TrainModel)w.get(KEY.TRAINS, i, p);

		trainPainter.paintTrain(g, train);
	    }
	}
    }
}
