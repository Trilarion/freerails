package jfreerails.client.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import jfreerails.client.common.Painter;
import jfreerails.client.renderer.TrainRenderer;
import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.common.IntLine;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;
import jfreerails.world.train.PathWalker;
import jfreerails.world.train.PathWalkerImpl;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainPosition;


public class TestOverHeadTrainView implements Painter {
	
	TrainRenderer trainPainter=new TrainRenderer();

	World w;

	public TestOverHeadTrainView(World world) {
		this.w = world;
		
	}

	public void paint(Graphics2D g) {

		g.setColor(Color.BLUE);
		g.setStroke(new BasicStroke(10));
		Stroke st;

		for (int i = 0; i < w.size(KEY.TRAINS); i++) {

			TrainModel train = (TrainModel)w.get(KEY.TRAINS, i);
			TrainPosition s = train.getPosition();
			IntLine line = new IntLine();
			FreerailsPathIterator it = s.path();
			
			PathWalker pw = new PathWalkerImpl(it);
			
			trainPainter.paintTrain(g, pw);
			//int j = 0;
			//while (it.hasNext()) {
			//	j++;
			//	it.nextSegment(line);
			//	g.drawLine(line.x1, line.y1, line.x2, line.y2);
			//}
		}

	}

}
