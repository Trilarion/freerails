package jfreerails.client.renderer;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.common.IntLine;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.train.PathWalker;
import jfreerails.world.train.PathWalkerImpl;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainPositionOnMap;
import jfreerails.world.train.WagonType;

/**
 * This class draws a train from an overhead view.
 * 
 * @author Luke Lindsay 13-Oct-2002
 *
 */
public class TrainRenderer {

	WagonRenderer localTrainView = new WagonRenderer();

	int[] trains =
		{
			WagonType.SLOW_FREIGHT,
			WagonType.BULK_FREIGHT,
			WagonType.FAST_FREIGHT,
			WagonType.PASSENGER,
			WagonType.PASSENGER,
			WagonType.MAIL,
			WagonType.ENGINE };

	public TrainRenderer() {
		localTrainView.setViewPerspective(ViewPerspective.OVERHEAD);
	}

	public void paintTrain(Graphics g, TrainModel train) {

		TrainPositionOnMap s = train.getPosition();
		
		FreerailsPathIterator it = s.path();

		PathWalker pw = new PathWalkerImpl(it);
		
		Graphics2D g2 = (Graphics2D) g;

		//renderer engine.
		localTrainView.setTrainTypes(WagonType.ENGINE);
		renderWagon(g, pw);
		
		//renderer wagons.				
		for (int i = 0; i < train.getNumberOfWagons(); i++) {
			
			int wagonType = train.getWagon(i);
			
			// TODO: remove this hardcoded stuff
			switch (wagonType){
				case 0:
					localTrainView.setTrainTypes(WagonType.MAIL);
					break;
				case 1:
					localTrainView.setTrainTypes(WagonType.PASSENGER);
					break;
				case 2:
					localTrainView.setTrainTypes(WagonType.FAST_FREIGHT);
					break;
				case 3:
					localTrainView.setTrainTypes(WagonType.SLOW_FREIGHT);
					break;
				case 4:
					localTrainView.setTrainTypes(WagonType.BULK_FREIGHT);
					break;
				default:
					throw new IllegalArgumentException(String.valueOf(wagonType));
			}
			
			renderWagon(g, pw);
		}
	}

	private void renderWagon(Graphics g, PathWalker pw) {
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
		OneTileMoveVector v =
			OneTileMoveVector.getNearestVector(wagon.x2 - wagon.x1, wagon.y2 - wagon.y1);
		localTrainView.setDirection(v);
		Point p = new Point((wagon.x2 + wagon.x1) / 2, (wagon.y2 + wagon.y1) / 2);
		localTrainView.rendererTrain(g, p);
		
		//The gap between wagons
		pw.stepForward(8);
		while (pw.hasNext()) {
			pw.nextSegment(line);
		}
	}

}
