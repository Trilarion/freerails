package jfreerails.client.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

import jfreerails.lib.GameJFrame;
import jfreerails.lib.Painter;
import jfreerails.world.misc.FreerailsPathIterator;
import jfreerails.world.misc.IntLine;
import jfreerails.world.train.PathWalker;
import jfreerails.world.train.PathWalkerImpl;
import jfreerails.world.train.TrainList;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainPosition;

import experimental.TrainPainter;

public class TestOverHeadTrainView implements Painter {
	
	TrainPainter trainPainter=new TrainPainter();

	TrainList trainList = new TrainList();

	public TestOverHeadTrainView() {
		addtrainsTotrainlist(trainList);

	}

	public static void addtrainsTotrainlist(TrainList trains) {
		
	}
	
	public TestOverHeadTrainView(TrainList tl) {
		this.trainList = tl;
	}

	public static void main(String[] args) {

		GameJFrame f = new GameJFrame(new TestOverHeadTrainView());
		f.startGameLoop();
	}

	public void paint(Graphics2D g) {

		g.setColor(Color.BLUE);
		g.setStroke(new BasicStroke(10));
		Stroke st;

		for (int i = 0; i < trainList.size(); i++) {

			TrainModel train = trainList.getTrain(i);
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
