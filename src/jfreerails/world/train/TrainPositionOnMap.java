package jfreerails.world.train;

import jfreerails.util.IntArray;
import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.IntLine;

/**
 * This class represents the position of a train as a series of 
 * straight lines connecting a series of points on a plane.
 *
 * Coordinates are expressed in display coordinates relative to the map origin
 * (as opposed to map squares).
 * 
 * 
<p>Train positions can be combined and divided as illustrated below (notice what 
  happens to the head and tail that are combined)</p>
<table width="100%" border="0">
  <tr> 
    <td>if</td>
    <td><code> a</code></td>
    <td><code>=</code></td>
    <td><code>{<strong>(10, 10)</strong>, (20,20), (30,30), (40,40) }</code></td>
  </tr>
  <tr> 
    <td>and</td>
    <td><code> b</code></td>
    <td><code>=</code></td>
    <td><code>{(1,1), (4,4), (5,5), <strong>(10, 10)</strong>}</code></td>
  </tr>
  <tr> 
    <td>then</td>
    <td><code>a.addToHead(b)</code></td>
    <td><code>=</code></td>
    <td><code>{(1,1), (4,4), (5,5), (20,20), (30,30), (40,40) }</code></td>
  </tr>
  <tr> 
    <td>and</td>
    <td><code>b.addToTail(a)</code></td>
    <td><code>=</code></td>
    <td><code>{(1,1), (4,4), (5,5), (20,20), (30,30), (40,40) }</code></td>
  </tr>
  <tr> 
    <td>and if</td>
    <td><code> c</code></td>
    <td><code>=</code></td>
    <td><code>{(1,1), (4,4), (5,5), (20,20), (30,30), (40,40) }</code></td>
  </tr>
  <tr> 
    <td>then</td>
    <td><code>c.removeFromTail(a)</code></td>
    <td><code>=</code></td>
    <td><code>{(1,1), (4,4), (5,5), (10, 10)}</code></td>
  </tr>
  <tr> 
    <td>and</td>
    <td><code>c.removeFromHead(b)</code></td>
    <td><code>=</code></td>
    <td><code>{(10, 10), (20,20), (30,30), (40,40) }</code></td>
  </tr>
</table>

 * 
 * @author Luke Lindsay 26-Oct-2002
 *
 */
public class TrainPositionOnMap implements FreerailsSerializable {

	private final int[] xpoints, ypoints;

	public boolean equals(Object o) {
		if (null == o) {
			return false;
		}
		if (o == this) {
			return true;
		}
		if (o instanceof TrainPositionOnMap) {
			TrainPositionOnMap other = (TrainPositionOnMap) o;
			int thisLength = this.getLength();
			int otherLength = other.getLength();

			if (thisLength == otherLength) {
				FreerailsPathIterator path1, path2;
				IntLine line1 = new IntLine();
				IntLine line2 = new IntLine();

				path1 = other.path();
				path2 = this.path();

				while (path1.hasNext() && path2.hasNext()) {
					path1.nextSegment(line1);
					path2.nextSegment(line2);
					if (line1.x1 != line2.x1
						|| line1.y1 != line2.y1
						|| line1.x2 != line2.x2
						|| line1.y2 != line2.y2) {
						return false;
					}
				}

				if (path1.hasNext() || path2.hasNext()) {
					return false;
				} else {
					return true;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public double calulateDistance() {
		double distance = 0;
		IntLine line = new IntLine();
		FreerailsPathIterator path = this.path();
		while (path.hasNext()) {
			path.nextSegment(line);
			int sumOfSquares =
				(line.x1 - line.x2) * (line.x1 - line.x2)
					+ (line.y1 - line.y2) * (line.y1 - line.y2);
			distance += Math.sqrt((double) sumOfSquares);
		}
		return distance;
	}

	public int getLength() {
		return xpoints.length;
	}

	public int getX(int position) {
		return xpoints[position];
	}

	public int getY(int position) {
		return ypoints[position];
	}

	public FreerailsPathIterator path() {
		return new SimplePathIteratorImpl(this.xpoints, this.ypoints);
	}

	public FreerailsPathIterator reversePath() {
		int length = xpoints.length;
		int[] reversed_xpoints = new int[length];
		int[] reversed_ypoints = new int[length];
		for (int i = 0; i < length; i++) {
			reversed_xpoints[i] = xpoints[length - i - 1];
			reversed_ypoints[i] = ypoints[length - i - 1];
		}
		return new SimplePathIteratorImpl(reversed_xpoints, reversed_ypoints);
	}

	private TrainPositionOnMap(int[] xpoints, int[] ypoints) {
		if (xpoints.length != ypoints.length) {
			throw new IllegalArgumentException();
		}

		this.xpoints = xpoints;
		this.ypoints = ypoints;
	}

	public static TrainPositionOnMap createInstance(int[] xpoints, int[] ypoints) {
		return new TrainPositionOnMap(
			(int[]) xpoints.clone(),
			(int[]) ypoints.clone());
	}

	public TrainPositionOnMap addToHead(TrainPositionOnMap b) {
		TrainPositionOnMap a = this;

		return addBtoHeadOfA(b, a);

	}

	private TrainPositionOnMap addBtoHeadOfA(TrainPositionOnMap b, TrainPositionOnMap a) {
		if (aHeadEqualsBTail(a, b)) {

			int newLength = a.getLength() + b.getLength() - 2;

			int[] newXpoints = new int[newLength];
			int[] newYpoints = new int[newLength];

			int aLength = a.getLength();
			int bLength = b.getLength();

			//First copy the points from B
			for (int i = 0; i < bLength - 1; i++) {
				newXpoints[i] = b.getX(i);
				newYpoints[i] = b.getY(i);
			}

			//Second copy the points from A.
			for (int i = 1; i < aLength; i++) {
				newXpoints[i + bLength - 2] = a.getX(i);
				newYpoints[i + bLength - 2] = a.getY(i);
			}
			return new TrainPositionOnMap(newXpoints, newYpoints);
		} else {
			throw new IllegalArgumentException("Tried to add "+b.toString()+" to the head of "+a.toString());
		}
	}

	public boolean canAddToHead(TrainPositionOnMap b) {
		return aHeadEqualsBTail(this, b);
	}

	public TrainPositionOnMap addToTail(TrainPositionOnMap a) {
		TrainPositionOnMap b = this;
		return addBtoHeadOfA(b, a);
	}

	public boolean canAddToTail(TrainPositionOnMap b) {
		return aHeadEqualsBTail(b, this);
	}

	public TrainPositionOnMap removeFromHead(TrainPositionOnMap b) {

		if (headsAreEqual(this, b)) {

			int newLength = this.getLength() - b.getLength() + 2;

			int[] newXpoints = new int[newLength];
			int[] newYpoints = new int[newLength];

			int aLength = this.getLength();
			int bLength = b.getLength();

			//copy head from b
			int bHeadPosition = b.getLength() - 1;
			newXpoints[0] = b.getX(bHeadPosition);
			newYpoints[0] = b.getY(bHeadPosition);

			//Copy rest from this
			for (int i = 1; i < newLength; i++) {
				int position = bLength + i - 2;

				newXpoints[i] = this.getX(position);
				newYpoints[i] = this.getY(position);
			}

			return new TrainPositionOnMap(newXpoints, newYpoints);
		} else {
			throw new IllegalArgumentException();
		}
	}

	public boolean canRemoveFromHead(TrainPositionOnMap b) {

		if (headsAreEqual(this, b)) {
			FreerailsPathIterator path = b.path();
			int i = 0;
			IntLine line = new IntLine();

			while (path.hasNext()) {
				path.nextSegment(line);
				if (this.getX(i) != line.x1 || this.getY(i) != line.y1) {
					return false;
				}
				i++;
			}
			return true;
		} else {
			return false;
		}
	}

	public TrainPositionOnMap removeFromTail(TrainPositionOnMap b) {
		if (tailsAreEqual(this, b)) {
			int newLength = this.getLength() - b.getLength() + 2;

			int[] newXpoints = new int[newLength];
			int[] newYpoints = new int[newLength];

			int aLength = this.getLength();
			int bLength = b.getLength();

			//Copy from this
			for (int i = 0; i < newLength - 1; i++) {

				newXpoints[i] = this.getX(i);
				newYpoints[i] = this.getY(i);
			}

			//Copy tail from b

			newXpoints[newLength - 1] = b.getX(0);
			newYpoints[newLength - 1] = b.getY(0);

			return new TrainPositionOnMap(newXpoints, newYpoints);
		} else {
			throw new IllegalArgumentException();
		}
	}

	public boolean canRemoveFromTail(TrainPositionOnMap b) {

		if (tailsAreEqual(this, b)) {
			FreerailsPathIterator path = b.reversePath();
			int i = this.getLength() - 1;
			IntLine line = new IntLine();

			while (path.hasNext()) {
				path.nextSegment(line);

				if (this.getX(i) != line.x1 || this.getY(i) != line.y1) {
					return false;
				}
				i--;
			}
			return true;
		} else {
			return false;
		}
	}
	/*
	public static TrainPosition add(TrainPosition a, TrainPosition b) {
	
		if (canBeAdded(a, b)) {
	
			int newLength = a.getLength() + b.getLength() - 2;
	
			int[] newXpoints = new int[newLength];
			int[] newYpoints = new int[newLength];
	
			int aLength = a.getLength();
			int bLength = b.getLength();
	
			if (aHeadEqualsBTail(a, b)) {
	
				//First copy the points from B
				for (int i = 0; i < bLength - 1; i++) {
					newXpoints[i] = b.getX(i);
					newYpoints[i] = b.getY(i);
				}
	
				//Second copy the points from A.
				for (int i = 1; i < aLength; i++) {
					newXpoints[i + bLength - 2] = a.getX(i);
					newYpoints[i + bLength - 2] = a.getY(i);
				}
			} else {
	
				//First copy the points from A
				for (int i = 0; i < aLength - 1; i++) {
					newXpoints[i] = a.getX(i);
					newYpoints[i] = a.getY(i);
				}
	
				//Second copy the points from B.
				for (int i = 1; i < bLength; i++) {
					newXpoints[i + aLength - 2] = b.getX(i);
					newYpoints[i + aLength - 2] = b.getY(i);
				}
			}
			return new TrainPosition(newXpoints, newYpoints);
	
		} else {
			throw new IllegalArgumentException();
		}
	}
	*/

	/*
	public static TrainPosition remove(TrainPosition a, TrainPosition b) {
	
		if (canBeRemoved(a, b)) {
	
			int newLength = a.getLength() - b.getLength() + 2;
	
			int[] newXpoints = new int[newLength];
			int[] newYpoints = new int[newLength];
	
			int aLength = a.getLength();
			int bLength = b.getLength();
	
			if (headsAreEqual(a, b)) {
				//copy head from b
				int bHeadPosition = b.getLength() - 1;
				newXpoints[0] = b.getX(bHeadPosition);
				newYpoints[0] = b.getY(bHeadPosition);
	
				//Copy rest from a
				for (int i = 1; i < newLength; i++) {
					int position = bLength + i - 2;
	
					newXpoints[i] = a.getX(position);
					newYpoints[i] = a.getY(position);
				}
	
			} else {
				//Copy from a
				for (int i = 0; i < newLength - 1; i++) {
	
					newXpoints[i] = a.getX(i);
					newYpoints[i] = a.getY(i);
				}
	
				//Copy tail from b
	
				newXpoints[newLength - 1] = b.getX(0);
				newYpoints[newLength - 1] = b.getY(0);
			}
			return new TrainPosition(newXpoints, newYpoints);
	
		} else {
			throw new IllegalArgumentException();
		}
	}
	*/
	/*
	public static boolean canBeAdded(TrainPosition a, TrainPosition b) {
	
		if (aHeadEqualsBTail(a, b) || bHeadEqualsATail(a, b)) {
			return true;
		} else {
			return false;
		}
	}
	*/
	/*
	public static boolean canBeRemoved(TrainPosition a, TrainPosition b) {
	
		if (a.getLength() < b.getLength()) {
			return false;
		}
		if (a.getLength() == b.getLength()) {
			if (a.equals(b)) {
				return false;
			}
		}
	
		if (headsAreEqual(a, b)) {
			FreerailsPathIterator path = b.path();
			int i = 0;
			IntLine line = new IntLine();
	
			while (path.hasNext()) {
				path.nextSegment(line);
				if (a.getX(i) != line.x1 || a.getY(i) != line.y1) {
					return false;
				}
				i++;
			}
			return true;
		} else if (tailsAreEqual(a, b)) {
			FreerailsPathIterator path = b.reversePath();
			int i = a.getLength() - 1;
			IntLine line = new IntLine();
	
			while (path.hasNext()) {
				path.nextSegment(line);
	
				if (a.getX(i) != line.x1 || a.getY(i) != line.y1) {
					return false;
				}
				i--;
			}
			return true;
		} else {
			return false;
		}
	}
	*/

	public static TrainPositionOnMap createInSameDirectionAsPath(FreerailsPathIterator path) {
		return createInstanceInDirection(path, true);
	}
	public static TrainPositionOnMap createInOppositeDirectionToPath(FreerailsPathIterator path) {
		return createInstanceInDirection(path, false);
	}

	private static TrainPositionOnMap createInstanceInDirection(
		FreerailsPathIterator path,
		boolean sameDirectionAsPath) {
		IntArray xPointsIntArray = new IntArray();
		IntArray yPointsIntArray = new IntArray();
		IntLine line = new IntLine();
		int i = 0;
		while (path.hasNext()) {
			path.nextSegment(line);
			xPointsIntArray.add(i, line.x1);
			yPointsIntArray.add(i, line.y1);
			i++;
			if (i > 10000) {
				throw new IllegalStateException("The TrainPosition has more than 10,000 points, which suggests that something is wrong.");
			}
		}
		xPointsIntArray.add(i, line.x2);
		yPointsIntArray.add(i, line.y2);

		int[] xPoints;
		int[] yPoints;

		if (sameDirectionAsPath) {
			xPoints = xPointsIntArray.toArray();
			yPoints = yPointsIntArray.toArray();
		} else {
			int length = xPointsIntArray.size();
			xPoints = new int[length];
			yPoints = new int[length];
			for (int k = 0; k < length; k++) {
				int j = length - k - 1;
				xPoints[k] = xPointsIntArray.get(j);
				yPoints[k] = yPointsIntArray.get(j);
			}
		}
		return new TrainPositionOnMap(xPoints, yPoints);
	}

	public static boolean headsAreEqual(TrainPositionOnMap a, TrainPositionOnMap b) {

		int aHeadX = a.getX(0);
		int aHeadY = a.getY(0);
		int bHeadX = b.getX(0);
		int bHeadY = b.getY(0);

		if (aHeadX == bHeadX && aHeadY == bHeadY) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean tailsAreEqual(TrainPositionOnMap a, TrainPositionOnMap b) {

		int aTailX = a.getX(a.getLength() - 1);
		int aTailY = a.getY(a.getLength() - 1);
		int bTailX = b.getX(b.getLength() - 1);
		int bTailY = b.getY(b.getLength() - 1);

		if (aTailX == bTailX && aTailY == bTailY) {
			return true;
		} else {
			return false;
		}

	}

	public static boolean aHeadEqualsBTail(TrainPositionOnMap a, TrainPositionOnMap b) {
		int aHeadX = a.getX(0);
		int aHeadY = a.getY(0);

		int bTailX = b.getX(b.getLength() - 1);
		int bTailY = b.getY(b.getLength() - 1);

		if (aHeadX == bTailX && aHeadY == bTailY) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean bHeadEqualsATail(TrainPositionOnMap a, TrainPositionOnMap b) {
		return aHeadEqualsBTail(b, a);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("TrainPosition {");
		for (int i = 0; i < xpoints.length; i++) {
			sb.append("(");
			sb.append(xpoints[i]);
			sb.append(", ");
			sb.append(ypoints[i]);
			sb.append("), ");
		}
		sb.append("}");
		return sb.toString();
	}

	public boolean hasBeenBuilt() {	
		return true;
	}
}
