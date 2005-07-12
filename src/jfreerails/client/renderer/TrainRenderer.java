package jfreerails.client.renderer;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;

import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.common.IntLine;
import jfreerails.world.common.Step;
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

	public void paintTrain(Graphics g, TrainModel train, TrainPositionOnMap s) {
		// If the train has been removed, it will be null!
		if (train == null) {
			return;
		}

		/*
		 * XXX HACK !! really our position ought to be defined at all times, but
		 * this is a workaround until we can fix movement
		 */
		if (s == null) {
			return;
		}

		FreerailsPathIterator it = s.path();

		PathWalker pw = new PathWalkerImpl(it);

		// renderer engine.
		renderWagon(g, pw, train.getEngineType(), true);

		// renderer wagons.
		for (int i = 0; i < train.getNumberOfWagons(); i++) {
			int wagonType = train.getWagon(i);
			renderWagon(g, pw, wagonType, false);
		}
	}

	// @SonnyZ
	// This code renders the explosion that occurs when 2 trains crash on the
	// map
	public void paintTrainCrash(Graphics g, TrainPositionOnMap s) {
		// check to see if there is a train
		if (s == null) {
			return;
		}
		// Get the image for that frame of the explosion
		Image explosionImage = trainImages
				.getExplosionImage(s.getFrameCt() - 1);
		// draw the image
		for (int i = 0; i < s.getLength() - 1; i++) {
			Point p = new Point(s.getX(i), s.getY(i));
			g.drawImage(explosionImage, p.x - 15, p.y - 15, null);

		}
		// increment the frame count
		s.incrementFramCt();
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

		Step v = Step
				.getNearestVector(wagon.x2 - wagon.x1, wagon.y2 - wagon.y1);
		Point p = new Point((wagon.x2 + wagon.x1) / 2,
				(wagon.y2 + wagon.y1) / 2);

		Image image;

		if (engine) {
			image = trainImages.getOverheadEngineImage(type, v.getID());
		} else {
			image = trainImages.getOverheadWagonImage(type, v.getID());
		}

		g.drawImage(image, p.x - 15, p.y - 15, null);

		// The gap between wagons
		pw.stepForward(8);

		while (pw.hasNext()) {
			pw.nextSegment(line);
		}
	}
}