package freerails.server;

import freerails.controller.*;
import freerails.network.MoveReceiver;
import freerails.util.FreerailsIntIterator;
import freerails.world.common.ImPoint;
import freerails.world.common.PositionOnTrack;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.WorldDiffs;

/**
 * This class provides methods that generate a path to a target as a series of
 * PositionOnTrack objects encoded as ints, it also deals with stops at
 * stations.
 *
 * @author Luke Lindsay 28-Nov-2002
 */
public class TrainPathFinder implements FreerailsIntIterator, ServerAutomaton {

    private static final long serialVersionUID = 3256446893302559280L;
    final ReadOnlyWorld w;
    private final SimpleAStarPathFinder pathFinder = new SimpleAStarPathFinder();
    private final TrainStopsHandler stopsHandler;
    private final FlatTrackExplorer trackExplorer;
    private transient MoveReceiver mr = null;

    public TrainPathFinder(FlatTrackExplorer tx, ReadOnlyWorld w,
                           int trainNumber, MoveReceiver newMr, FreerailsPrincipal p) {
        this.trackExplorer = tx;
        stopsHandler = new TrainStopsHandler(trainNumber, p,
                new WorldDiffs(w));
        this.mr = newMr;
        this.w = w;
    }

    public boolean hasNextInt() {

        boolean moving = stopsHandler.isTrainMoving();

        if (moving) {
            return trackExplorer.hasNextEdge();
        }
        mr.processMove(stopsHandler.getMoves());
        return false;
    }

    public void initAutomaton(MoveReceiver newMr) {
        this.mr = newMr;
    }

    boolean isTrainMoving() {

        boolean moving = stopsHandler.isTrainMoving();

        mr.processMove(stopsHandler.getMoves());
        return moving;
    }

    /**
     * @return a PositionOnTrack packed into an int
     */
    public int nextInt() {

        PositionOnTrack tempP = new PositionOnTrack(trackExplorer.getPosition());
        int x = tempP.getX();
        int y = tempP.getY();
        ImPoint targetPoint = stopsHandler.arrivesAtPoint(x, y);

        int currentPosition = tempP.getOpposite().toInt();
        ReadOnlyWorld world = trackExplorer.getWorld();
        PositionOnTrack[] t = FlatTrackExplorer.getPossiblePositions(world,
                targetPoint);
        int[] targets = new int[t.length];

        for (int i = 0; i < t.length; i++) {
            int target = t[i].getOpposite().toInt();

            if (target == currentPosition) {
                stopsHandler.updateTarget();
            }

            targets[i] = target;
        }

        FlatTrackExplorer tempExplorer;
        try {
            tempExplorer = new FlatTrackExplorer(world, tempP);
        } catch (NoTrackException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
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

        mr.processMove(stopsHandler.getMoves());
        return nextPosition;
    }
}