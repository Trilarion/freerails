/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 *
 */
package freerails.controller;

import freerails.move.CompositeMove;
import freerails.move.Move;
import freerails.move.NextActivityMove;
import freerails.util.ImInts;
import freerails.util.ImPoint;
import freerails.world.*;
import freerails.world.cargo.CargoBundle;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.StationModel;
import freerails.world.terrain.FreerailsTile;
import freerails.world.track.TrackPiece;
import freerails.world.track.TrackSection;
import freerails.world.train.*;
import freerails.world.train.SpeedTimeAndStatus.TrainActivity;
import org.apache.log4j.Logger;

import java.util.HashMap;

import static freerails.world.train.SpeedTimeAndStatus.TrainActivity.STOPPED_AT_STATION;
import static freerails.world.train.SpeedTimeAndStatus.TrainActivity.WAITING_FOR_FULL_LOAD;

/**
 * Generates moves for changes in train position and stops at stations.
 */
public class MoveTrainPreMove implements PreMove {
    private static final long serialVersionUID = 3545516188269491250L;
    private static final Logger logger = Logger
            .getLogger(MoveTrainPreMove.class.getName());
    /**
     * 666 Performance cache must be cleared if track on map is build ! make a
     * change listener!
     */
    private static final HashMap<Integer, HashMap<Integer, TileTransition>> pathCache = new HashMap<>();
    private static int cacheCleared = 0;
    private static int cacheHit = 0;
    private static int cacheMiss = 0;
    final FreerailsPrincipal principal;
    private final int trainID;
    private final OccupiedTracks occupiedTracks;

    /**
     * @param id
     * @param p
     * @param occupiedTracks
     */
    public MoveTrainPreMove(int id, FreerailsPrincipal p,
                            OccupiedTracks occupiedTracks) {
        trainID = id;
        principal = p;
        this.occupiedTracks = occupiedTracks;
    }

    /**
     * Uses static method to make testing easier.
     *
     * @param world
     * @param currentPosition
     * @param target
     * @return
     * @throws NoTrackException if no track
     */
    public static TileTransition findNextStep(ReadOnlyWorld world,
                                              PositionOnTrack currentPosition, ImPoint target) {
        int startPos = PositionOnTrack.toInt(currentPosition.getX(),
                currentPosition.getY());
        int endPos = PositionOnTrack.toInt(target.x, target.y);
        HashMap<Integer, TileTransition> destPaths = pathCache.get(endPos);
        TileTransition nextTileTransition = null;
        if (destPaths != null) {
            nextTileTransition = destPaths.get(startPos);
            if (nextTileTransition != null) {
                cacheHit++;
                return nextTileTransition;
            }
        } else {
            destPaths = new HashMap<>();
            pathCache.put(endPos, destPaths);
        }
        cacheMiss++;
        PathOnTrackFinder pathFinder = new PathOnTrackFinder(world);

        try {
            ImPoint location = new ImPoint(currentPosition.getX(),
                    currentPosition.getY());
            pathFinder.setupSearch(location, target);
            pathFinder.search(-1);
            TileTransition[] pathAsVectors = pathFinder.pathAsVectors();
            int[] pathAsInts = pathFinder.pathAsInts();
            for (int i = 0; i < pathAsInts.length - 1; i++) {
                int calcPos = pathAsInts[i]
                        & (PositionOnTrack.MAX_COORDINATE | (PositionOnTrack.MAX_COORDINATE << PositionOnTrack.BITS_FOR_COORDINATE));
                destPaths.put(calcPos, pathAsVectors[i + 1]);
            }
            nextTileTransition = pathAsVectors[0];
            return nextTileTransition;
        } catch (PathNotFoundException e) {
            // The pathfinder couldn't find a path so we
            // go in any legal direction.
            FlatTrackExplorer explorer = new FlatTrackExplorer(world,
                    currentPosition);
            explorer.nextEdge();
            int next = explorer.getVertexConnectedByEdge();
            PositionOnTrack nextPosition = new PositionOnTrack(next);
            return nextPosition.cameFrom();
        }

    }

    /**
     *
     */
    public static void clearCache() {
        pathCache.clear();
        cacheCleared++;
        // System.out.println("CH:"+cacheHit+" CM:"+cacheMiss+"
        // CC:"+cacheCleared);
    }

    double acceleration(int wagons) {
        return 0.5d / (wagons + 1);
    }

    /**
     * Returns true if an updated is due.
     *
     * @param w
     * @return
     */
    public boolean isUpdateDue(ReadOnlyWorld w) {
        GameTime currentTime = w.currentTime();
        TrainAccessor ta = new TrainAccessor(w, principal, trainID);
        ActivityIterator ai = w.getActivities(principal, trainID);
        ai.gotoLastActivity();

        double finishTime = ai.getFinishTime();
        double ticks = currentTime.getTicks();

        boolean hasFinishedLastActivity = Math.floor(finishTime) <= ticks;
        TrainActivity trainActivity = ta.getStatus(finishTime);
        if (trainActivity == TrainActivity.WAITING_FOR_FULL_LOAD) {
            // Check whether there is any cargo that can be added to the train.
            ImInts spaceAvailable = ta.spaceAvailable();
            int stationId = ta.getStationId(ticks);
            if (stationId == -1)
                throw new IllegalStateException();

            StationModel station = (StationModel) w.get(principal,
                    KEY.STATIONS, stationId);
            CargoBundle cb = (CargoBundle) w.get(principal, KEY.CARGO_BUNDLES,
                    station.getCargoBundleID());

            for (int i = 0; i < spaceAvailable.size(); i++) {
                int space = spaceAvailable.get(i);
                int atStation = cb.getAmount(i);
                if (space * atStation > 0) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("There is cargo to transfer!");
                    }
                    return true;
                }
            }

            return !ta.keepWaiting();
        }
        return hasFinishedLastActivity;
    }

    private ImPoint currentTrainTarget(ReadOnlyWorld w) {
        TrainAccessor ta = new TrainAccessor(w, principal, trainID);
        return ta.getTarget();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof MoveTrainPreMove))
            return false;

        final MoveTrainPreMove moveTrainPreMove = (MoveTrainPreMove) o;

        if (trainID != moveTrainPreMove.trainID)
            return false;
        return principal.equals(moveTrainPreMove.principal);
    }

    // 666 optimize

    /**
     * @param w
     * @return
     */
    public Move generateMove(ReadOnlyWorld w) {

        // Check that we can generate a move.
        if (!isUpdateDue(w)) {
            throw new IllegalStateException();
        }

        TrainAccessor ta = new TrainAccessor(w, principal, trainID);
        TrainMotion tm = ta.findCurrentMotion(Double.MAX_VALUE);

        SpeedTimeAndStatus.TrainActivity activity = tm.getActivity();

        switch (activity) {
            case STOPPED_AT_STATION:
                return moveTrain(w, occupiedTracks);
            case READY: {
                // Are we at a station?
                TrainStopsHandler stopsHandler = new TrainStopsHandler(trainID,
                        principal, new WorldDiffs(w));
                ta.getStationId(Integer.MAX_VALUE);
                PositionOnTrack pot = tm.getFinalPosition();
                int x = pot.getX();
                int y = pot.getY();
                boolean atStation = stopsHandler.getStationID(x, y) >= 0;

                TrainMotion nextMotion;
                if (atStation) {
                    // We have just arrived at a station.
                    double durationOfStationStop = 10;

                    stopsHandler.arrivesAtPoint(x, y);

                    SpeedTimeAndStatus.TrainActivity status = stopsHandler
                            .isWaiting4FullLoad() ? WAITING_FOR_FULL_LOAD
                            : STOPPED_AT_STATION;
                    PathOnTiles path = tm.getPath();
                    int lastTrainLength = tm.getTrainLength();
                    int currentTrainLength = stopsHandler.getTrainLength();

                    // If we are adding wagons we may need to lengthen the path.
                    if (lastTrainLength < currentTrainLength) {
                        path = TrainStopsHandler.lengthenPath(w, path,
                                currentTrainLength);
                    }

                    nextMotion = new TrainMotion(path, currentTrainLength,
                            durationOfStationStop, status);

                    // Create a new Move object.
                    Move trainMove = new NextActivityMove(nextMotion, trainID,
                            principal);

                    Move cargoMove = stopsHandler.getMoves();
                    return new CompositeMove(trainMove, cargoMove);
                }
                return moveTrain(w, occupiedTracks);
            }
            case WAITING_FOR_FULL_LOAD: {
                TrainStopsHandler stopsHandler = new TrainStopsHandler(trainID,
                        principal, new WorldDiffs(w));

                boolean waiting4fullLoad = stopsHandler.refreshWaitingForFullLoad();
                Move cargoMove = stopsHandler.getMoves();
                if (!waiting4fullLoad) {
                    Move trainMove = moveTrain(w, occupiedTracks);
                    if (null != trainMove) {
                        return new CompositeMove(trainMove, cargoMove);
                    } else {
                        return cargoMove;
                    }
                }
                stopsHandler.makeTrainWait(30);
                return cargoMove;

            }
            default:
                throw new UnsupportedOperationException(activity.toString());
        }
    }

    @Override
    public int hashCode() {
        int result;
        result = trainID;
        result = 29 * result + principal.hashCode();
        return result;
    }

    private TrainMotion lastMotion(ReadOnlyWorld w) {
        ActivityIterator ai = w.getActivities(principal, trainID);
        ai.gotoLastActivity();
        return (TrainMotion) ai.getActivity();
    }

    private Move moveTrain(ReadOnlyWorld w, OccupiedTracks occupiedTracks) {
        // Find the next vector.
        TileTransition nextVector = nextStep(w);

        TrainMotion motion = lastMotion(w);
        PositionOnTrack pot = motion.getFinalPosition();
        ImPoint tile = new ImPoint(pot.getX(), pot.getY());
        TrackSection desiredTrackSection = new TrackSection(nextVector, tile);

        // Check whether the desired track section is single or double track.
        ImPoint tileA = desiredTrackSection.tileA();
        ImPoint tileB = desiredTrackSection.tileB();
        FreerailsTile fta = (FreerailsTile) w.getTile(tileA.x, tileA.y);
        FreerailsTile ftb = (FreerailsTile) w.getTile(tileB.x, tileB.y);
        TrackPiece tpa = fta.getTrackPiece();
        TrackPiece tpb = ftb.getTrackPiece();
        int tracks = 1;
        if (tpa.getTrackRule().isDouble() && tpb.getTrackRule().isDouble()) {
            tracks = 2;
        }
        Integer trains = occupiedTracks.occupiedTrackSections
                .get(desiredTrackSection);
        if (trains != null) {
            if (trains >= tracks) {
                // We need to wait for the track ahead to clear.
                occupiedTracks.stopTrain(trainID);
                return stopTrain(w);
            }
        }
        // Create a new train motion object.
        TrainMotion nextMotion = nextMotion(w, nextVector);
        return new NextActivityMove(nextMotion, trainID, principal);

    }

    TrainMotion nextMotion(ReadOnlyWorld w, TileTransition v) {
        TrainMotion motion = lastMotion(w);

        SpeedAgainstTime speeds = nextSpeeds(w, v);

        PathOnTiles currentTiles = motion.getTiles(motion.duration());
        PathOnTiles pathOnTiles = currentTiles.addSteps(v);
        return new TrainMotion(pathOnTiles, currentTiles.steps(), motion
                .getTrainLength(), speeds);
    }

    SpeedAgainstTime nextSpeeds(ReadOnlyWorld w, TileTransition v) {
        TrainAccessor ta = new TrainAccessor(w, principal, trainID);
        TrainMotion lastMotion = lastMotion(w);

        double u = lastMotion.getSpeedAtEnd();
        double s = v.getLength();

        int wagons = ta.getTrain().getNumberOfWagons();
        double a0 = acceleration(wagons);
        double topSpeed = topSpeed(wagons);

        SpeedAgainstTime newSpeeds;
        if (u < topSpeed) {
            double t = (topSpeed - u) / a0;
            SpeedAgainstTime a = ConstAcc.uat(u, a0, t);
            t = s / topSpeed + 1; // Slightly overestimate the time
            SpeedAgainstTime b = ConstAcc.uat(topSpeed, 0, t);
            newSpeeds = new CompositeSpeedAgainstTime(a, b);
        } else {
            double t;
            t = s / topSpeed + 1; // Slightly overestimate the time
            newSpeeds = ConstAcc.uat(topSpeed, 0, t);
        }

        return newSpeeds;
    }

    TileTransition nextStep(ReadOnlyWorld w) {
        // Find current position.
        TrainMotion currentMotion = lastMotion(w);
        PositionOnTrack currentPosition = currentMotion.getFinalPosition();
        // Find targets
        ImPoint targetPoint = currentTrainTarget(w);
        return findNextStep(w, currentPosition, targetPoint);
    }

    /**
     * @param w
     * @return
     */
    public Move stopTrain(ReadOnlyWorld w) {
        TrainMotion motion = lastMotion(w);
        SpeedAgainstTime stopped = ConstAcc.STOPPED;
        double duration = motion.duration();

        int trainLength = motion.getTrainLength();
        PathOnTiles tiles = motion.getTiles(duration);
        int engineDist = tiles.steps();
        TrainMotion nextMotion = new TrainMotion(tiles, engineDist,
                trainLength, stopped);
        return new NextActivityMove(nextMotion, trainID, principal);
    }

    double topSpeed(int wagons) {
        return 10 / (wagons + 1);
    }
}
