package jfreerails.world.train;

import java.util.NoSuchElementException;

import jfreerails.world.misc.FreerailsPathIterator;
import jfreerails.world.misc.IntLine;

public class PathWalkerImpl implements PathWalker {

	FreerailsPathIterator it;

	IntLine currentSegment = new IntLine();

	double distanceAlongCurrentSegment = 0;

	double distanceOfThisStepRemaining = 0;

	public PathWalkerImpl(FreerailsPathIterator i) {
		it = i;
	}

	public boolean canStepForward() {
		if (currentSegment.getLength() > distanceAlongCurrentSegment) {
			return true;
		} else if (it.hasNext()) {
			return true;
		} else {
			return false;
		}
	}

	public void stepForward(int distance) {
		distanceOfThisStepRemaining += distance;
	}

	public boolean hasNext() {
		if (0 == distanceOfThisStepRemaining) {
			return false;
		} else if (distanceAlongCurrentSegment < currentSegment.getLength()) {
			return true;
		} else if (it.hasNext()) {
			return true;
		} else {
			return false;
		}
	}

	public void nextSegment(IntLine line) {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
        
        //If we are at the end of the current segemtn, start a new one.
		if (currentSegment.getLength() <= distanceAlongCurrentSegment) {
			startNewSegment(line);
		} else {
			startInMiddleOfSegment(line);
		}

		double remainingDistanceAlongCurrentSegment =
			currentSegment.getLength() - distanceAlongCurrentSegment;

        
		if (distanceOfThisStepRemaining > remainingDistanceAlongCurrentSegment) {
			endAtSegmentEnd(line, remainingDistanceAlongCurrentSegment);

		} else {
			endInMiddleOfSegment(line);
		}

		return;

	}

	public void endInMiddleOfSegment(IntLine line) {
		distanceAlongCurrentSegment += distanceOfThisStepRemaining;
		distanceOfThisStepRemaining = 0;
		line.x2 =
			getCoorinateOnSegment(
				distanceAlongCurrentSegment,
				currentSegment.x1,
				currentSegment.x2);
		line.y2 =
			getCoorinateOnSegment(
				distanceAlongCurrentSegment,
				currentSegment.y1,
				currentSegment.y2);
	}

	public void endAtSegmentEnd(IntLine line, double remainingDistanceAlongCurrentSegment) {
		line.x2 = this.currentSegment.x2;
		line.y2 = this.currentSegment.y2;
		this.distanceOfThisStepRemaining -= remainingDistanceAlongCurrentSegment;
        distanceAlongCurrentSegment = this.currentSegment.getLength();
	}

	private void startInMiddleOfSegment(IntLine line) {
		line.x1 =
			getCoorinateOnSegment(
				distanceAlongCurrentSegment,
				currentSegment.x1,
				currentSegment.x2);
		line.y1 =
			getCoorinateOnSegment(
				distanceAlongCurrentSegment,
				currentSegment.y1,
				currentSegment.y2);
	}

	private void startNewSegment(IntLine line) {
		it.nextSegment(currentSegment);
		distanceAlongCurrentSegment = 0;
		line.x1 = this.currentSegment.x1;
		line.y1 = this.currentSegment.y1;
	}

	private int getCoorinateOnSegment(
		double distanceAlongSegment,
		int coordinate1,
		int coordinate2) {

		double segmentLength = this.currentSegment.getLength();
		double delta = 0;
		if (0 != segmentLength) {

			delta = (coordinate2 - coordinate1) * distanceAlongSegment / segmentLength;
		}

		return coordinate1 + (int) delta;
	}

}
