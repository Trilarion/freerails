package jfreerails.server;

import java.awt.Point;

import jfreerails.controller.FlatTrackExplorer;
import jfreerails.controller.IncrementalPathFinder;
import jfreerails.controller.SimpleAStarPathFinder;
import jfreerails.network.MoveReceiver;
import jfreerails.util.FreerailsIntIterator;
import jfreerails.world.common.PositionOnTrack;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ReadOnlyWorld;

/**
 * This class provides methods that generate a path to a target as a series of
 * PositionOnTrack objects encoded as ints, it also deals with stops at
 * stations.
 * 
 * @author Luke Lindsay 28-Nov-2002
 */
public class TrainPathFinder implements FreerailsIntIterator, ServerAutomaton {

	private static final long serialVersionUID = 3256446893302559280L;

	private final SimpleAStarPathFinder pathFinder = new SimpleAStarPathFinder();

	private final FreerailsPrincipal principal;

	private final TrainStopsHandler stopsHandler;

	private final FlatTrackExplorer trackExplorer;

	private final int trainId;

	private final ReadOnlyWorld world;

	public TrainPathFinder(FlatTrackExplorer tx, ReadOnlyWorld w,
			int trainNumber, MoveReceiver mr, FreerailsPrincipal p) {
		this.trackExplorer = tx;
		this.trainId = trainNumber;
		this.world = w;
		principal = p;
		stopsHandler = new TrainStopsHandler(trainId, principal, world, mr);
	}

	public boolean hasNextInt() {
		if (stopsHandler.isTrainMoving()) {
			return trackExplorer.hasNextEdge();
		}
		return false;
	}

	public void initAutomaton(MoveReceiver mr) {
		stopsHandler.initAutomaton(mr);
	}

	boolean isTrainMoving() {
		return stopsHandler.isTrainMoving();
	}

	/**
	 * @return a PositionOnTrack packed into an int
	 */
	public int nextInt() {
		PositionOnTrack tempP = new PositionOnTrack(trackExplorer.getPosition());
		int x = tempP.getX();
		int y = tempP.getY();		
		Point targetPoint = stopsHandler.arrivesAtPoint(x, y);

		int currentPosition = tempP.getOpposite().toInt();
		PositionOnTrack[] t = FlatTrackExplorer.getPossiblePositions(
				trackExplorer.getWorld(), new Point(targetPoint.x,
						targetPoint.y));
		int[] targets = new int[t.length];

		for (int i = 0; i < t.length; i++) {
			int target = t[i].getOpposite().toInt();

			if (target == currentPosition) {
				stopsHandler.updateTarget();
			}

			targets[i] = target;
		}

		FlatTrackExplorer tempExplorer = new FlatTrackExplorer(trackExplorer
				.getWorld(), tempP);
		int next = pathFinder.findstep(currentPosition, targets, tempExplorer);

		if (next == IncrementalPathFinder.PATH_NOT_FOUND) {
			trackExplorer.nextEdge();
			trackExplorer.moveForward();

			return trackExplorer.getVertexConnectedByEdge();
		}
		tempP.setValuesFromInt(next);
		tempP = tempP.getOpposite();

		int nextPosition = tempP.toInt();
		trackExplorer.setPosition(nextPosition);

		return nextPosition;
	}

	

}