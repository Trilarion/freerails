package jfreerails.client.renderer;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import jfreerails.world.common.IntLine;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.train.PathWalker;
import jfreerails.world.train.WagonType;

/**
 * @author Luke Lindsay 13-Oct-2002
 *
 */
public class TrainRenderer {
	
	
	WagonRenderer localTrainView = new WagonRenderer();
	
	int[] train =
		{
			WagonType.SLOW_FREIGHT,
			WagonType.BULK_FREIGHT,
			WagonType.FAST_FREIGHT,
			WagonType.PASSENGER,
			WagonType.PASSENGER,
			WagonType.MAIL,
			WagonType.ENGINE };
	
	public TrainRenderer(){
		localTrainView.setViewPerspective(ViewPerspective.OVERHEAD);
	}

	public void paintTrain(Graphics g, PathWalker pw) {
		IntLine line = new IntLine();
		
		Graphics2D g2 = (Graphics2D) g;
		
		IntLine wagon = new IntLine();
		for (int i = 0; i < train.length; i++) {
			
			localTrainView.setTrainTypes(train[i]);
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
				OneTileMoveVector.getNearestVector(
					wagon.x2 - wagon.x1,
					wagon.y2 - wagon.y1);
			localTrainView.setDirection(v);
			Point p =
				new Point((wagon.x2 + wagon.x1) / 2, (wagon.y2 + wagon.y1) / 2);
			localTrainView.rendererTrain(g, p);
		
			//The gap between wagons
			pw.stepForward(8);
			while (pw.hasNext()) {
				pw.nextSegment(line);
			}
		}
	}


}
