package jfreerails.client.view;

import experimental.TrainPainter;
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
import jfreerails.world.train.Snake;
import jfreerails.world.train.TrainList;
import jfreerails.world.train.TrainModelPublic;

public class TestOverHeadTrainView implements Painter {
	
	TrainPainter trainPainter=new TrainPainter();

	TrainList trainList = new TrainList();

	public TestOverHeadTrainView() {
		addtrainsTotrainlist(trainList);

	}

	public static void addtrainsTotrainlist(TrainList trains) {
		TrainModelPublic train = new TrainModelPublic();
		
		Snake s = train.getPosition();
		
		s.moveHead(10, 10);
		s.addToHead(20, 40);
		s.addToHead(70, 50);
		
		trains.addTrain(train);
		
		train = new TrainModelPublic();
		
		s = train.getPosition();
		
		s.moveHead(300, 300);
		s.moveTail(320, 300);
		s.addToHead(320, 340);
		s.addToHead(350, 350);
		
		trains.addTrain(train);
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

			TrainModelPublic train = trainList.getTrain(i);
			Snake s = train.getPosition();
			IntLine line = new IntLine();
			FreerailsPathIterator it = s.pathIterator();
			
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
