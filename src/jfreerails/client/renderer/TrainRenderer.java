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
		if (s == null)
		    return;
		
		FreerailsPathIterator it = s.path();

		PathWalker pw = new PathWalkerImpl(it);
		
		Graphics2D g2 = (Graphics2D) g;

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
		OneTileMoveVector v =
			OneTileMoveVector.getNearestVector(wagon.x2 - wagon.x1, wagon.y2 - wagon.y1);	
		Point p = new Point((wagon.x2 + wagon.x1) / 2, (wagon.y2 + wagon.y1) / 2);
		
		Image image;
		if(engine){
			image = trainImages.getOverheadEngineImage(type, v.getNumber());
		}else{
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
