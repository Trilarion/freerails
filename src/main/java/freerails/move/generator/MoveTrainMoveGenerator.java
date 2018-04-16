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
package freerails.move.generator;

import freerails.model.track.explorer.FlatTrackExplorer;
import freerails.model.track.explorer.GraphExplorer;
import freerails.model.track.pathfinding.PathNotFoundException;
import freerails.model.track.pathfinding.PathOnTrackFinder;
import freerails.model.track.OccupiedTracks;
import freerails.move.*;
import freerails.util.ImmutableList;
import freerails.util.Vec2D;
import freerails.model.ActivityIterator;
import freerails.model.world.PlayerKey;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.cargo.CargoBatchBundle;
import freerails.model.game.GameTime;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.station.Station;
import freerails.model.terrain.FullTerrainTile;
import freerails.model.terrain.TileTransition;
import freerails.model.track.NoTrackException;
import freerails.model.track.TrackPiece;
import freerails.model.track.TrackSection;
import freerails.model.train.*;
import freerails.model.train.motion.CompositeMotion;
import freerails.model.train.motion.ConstantAccelerationMotion;
import freerails.model.train.motion.Motion;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates moves for changes in train position and stops at stations.
 */
public class MoveTrainMoveGenerator implements MoveGenerator {

    private static final long serialVersionUID = 3545516188269491250L;
    private static final Logger logger = Logger.getLogger(MoveTrainMoveGenerator.class.getName());
    // TODO Performance cache must be cleared if track on map is build ! make a change listener!
    private static final Map<Integer, HashMap<Integer, TileTransition>> pathCache = new HashMap<>();
    private static int cacheCleared = 0;
    private static int cacheHit = 0;
    private static int cacheMiss = 0;
    private final FreerailsPrincipal principal;
    private final int trainID;
    private final OccupiedTracks occupiedTracks;

    /**
     * @param id
     * @param p
     * @param occupiedTracks
     */
    public MoveTrainMoveGenerator(int id, FreerailsPrincipal p, OccupiedTracks occupiedTracks) {
        trainID = id;
        principal = p;
        this.occupiedTracks = occupiedTracks;
    }

    /**
     * Uses static method to make testing easier.
     *
     * @throws NoTrackException if no track
     */
    public static TileTransition findNextStep(ReadOnlyWorld world, PositionOnTrack currentPosition, Vec2D target) {
        int startPos = PositionOnTrack.toInt(currentPosition.getLocation());
        int endPos = PositionOnTrack.toInt(target);
        HashMap<Integer, TileTransition> destPaths = pathCache.get(endPos);
        TileTransition nextTileTransition;
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
            pathFinder.setupSearch(currentPosition.getLocation(), target);
            pathFinder.search(-1);
            TileTransition[] pathAsVectors = pathFinder.pathAsVectors();
            List<Integer> pathAsInts = pathFinder.pathAsInts();
            for (int i = 0; i < pathAsInts.size() - 1; i++) {
                int calcPos = pathAsInts.get(i) & (PositionOnTrack.MAX_COORDINATE | (PositionOnTrack.MAX_COORDINATE << PositionOnTrack.BITS_FOR_COORDINATE));
                destPaths.put(calcPos, pathAsVectors[i + 1]);
            }
            nextTileTransition = pathAsVectors[0];
            return nextTileTransition;
        } catch (PathNotFoundException e) {
            // The pathfinder couldn't find a path so we go in any legal direction.
            GraphExplorer explorer = new FlatTrackExplorer(world, currentPosition);
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
    }

    /**
     * Returns true if an updated is due.
     */
    public boolean isUpdateDue(ReadOnlyWorld world) {
        GameTime currentTime = world.currentTime();
        TrainAccessor ta = new TrainAccessor(world, principal, trainID);
        ActivityIterator ai = world.getActivities(principal, trainID);
        ai.gotoLastActivity();

        double finishTime = ai.getFinishTime();
        double ticks = currentTime.getTicks();

        boolean hasFinishedLastActivity = Math.floor(finishTime) <= ticks;
        TrainState trainState = ta.getStatus(finishTime);
        if (trainState == TrainState.WAITING_FOR_FULL_LOAD) {
            // Check whether there is any cargo that can be added to the train.
            ImmutableList<Integer> spaceAvailable = ta.spaceAvailable();
            int stationId = ta.getStationId(ticks);
            if (stationId == -1) throw new IllegalStateException();

            Station station = (Station) world.get(principal, PlayerKey.Stations, stationId);
            CargoBatchBundle cb = (CargoBatchBundle) world.get(principal, PlayerKey.CargoBundles, station.getCargoBundleID());

            for (int i = 0; i < spaceAvailable.size(); i++) {
                int space = spaceAvailable.get(i);
                int atStation = cb.getAmountOfType(i);
                if (space * atStation > 0) {

                    logger.debug("There is cargo to transfer!");

                    return true;
                }
            }

            return !ta.keepWaiting();
        }
        return hasFinishedLastActivity;
    }

    private Vec2D currentTrainTarget(ReadOnlyWorld world) {
        TrainAccessor ta = new TrainAccessor(world, principal, trainID);
        return ta.getTargetLocation();
    }

    // TODO optimize

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MoveTrainMoveGenerator)) return false;

        final MoveTrainMoveGenerator moveTrainPreMove = (MoveTrainMoveGenerator) obj;

        if (trainID != moveTrainPreMove.trainID) return false;
        return principal.equals(moveTrainPreMove.principal);
    }

    /**
     * @param world
     * @return
     */
    public Move generate(ReadOnlyWorld world) {

        // Check that we can generate a move.
        if (!isUpdateDue(world)) {
            throw new IllegalStateException();
        }

        TrainAccessor trainAccessor = new TrainAccessor(world, principal, trainID);
        TrainMotion trainMotion = trainAccessor.findCurrentMotion(Double.MAX_VALUE);

        TrainState trainState = trainMotion.getTrainState();

        switch (trainState) {
            case STOPPED_AT_STATION:
                return moveTrain(world, occupiedTracks);
            case READY: {
                // Are we at a station?
                TrainStopsHandler stopsHandler = new TrainStopsHandler(trainID, principal, world);
                trainAccessor.getStationId(Integer.MAX_VALUE);
                PositionOnTrack positionOnTrack = trainMotion.getFinalPosition();
                Vec2D location = positionOnTrack.getLocation();
                boolean atStation = stopsHandler.getStationId(location) >= 0;

                TrainMotion nextMotion;
                if (atStation) {
                    // We have just arrived at a station.
                    double durationOfStationStop = 10;

                    stopsHandler.arrivesAtPoint(location);

                    TrainState status = stopsHandler.isWaitingForFullLoad() ? TrainState.WAITING_FOR_FULL_LOAD : TrainState.STOPPED_AT_STATION;
                    PathOnTiles path = trainMotion.getPath();
                    int lastTrainLength = trainMotion.getTrainLength();
                    int currentTrainLength = stopsHandler.getTrainLength();

                    // If we are adding wagons we may need to lengthen the path.
                    if (lastTrainLength < currentTrainLength) {
                        path = TrainStopsHandler.lengthenPath(world, path, currentTrainLength);
                    }

                    nextMotion = new TrainMotion(path, currentTrainLength, durationOfStationStop, status);

                    // Create a new Move object.
                    Move trainMove = new NextActivityMove(nextMotion, trainID, principal);

                    ImmutableList<Move> cargoMoves = stopsHandler.getMoves();
                    return new CompositeMove(trainMove, cargoMoves);
                }
                return moveTrain(world, occupiedTracks);
            }
            case WAITING_FOR_FULL_LOAD: {
                TrainStopsHandler stopsHandler = new TrainStopsHandler(trainID, principal, world);

                boolean waitingForfullLoad = stopsHandler.refreshWaitingForFullLoad();
                ImmutableList<Move> cargoMoves = stopsHandler.getMoves();
                if (!waitingForfullLoad) {
                    Move trainMove = moveTrain(world, occupiedTracks);
                    if (null != trainMove) {
                        return new CompositeMove(trainMove, cargoMoves);
                    } else {
                        return new CompositeMove(cargoMoves);
                    }
                }
                stopsHandler.makeTrainWait(30);
                return new CompositeMove(cargoMoves);
            }
            default:
                throw new UnsupportedOperationException(trainState.toString());
        }
    }

    @Override
    public int hashCode() {
        int result;
        result = trainID;
        result = 29 * result + principal.hashCode();
        return result;
    }

    private TrainMotion lastMotion(ReadOnlyWorld world) {
        ActivityIterator ai = world.getActivities(principal, trainID);
        ai.gotoLastActivity();
        return (TrainMotion) ai.getActivity();
    }

    private Move moveTrain(ReadOnlyWorld world, OccupiedTracks occupiedTracks) {
        // Find the next vector.
        TileTransition nextVector = nextStep(world);

        TrainMotion motion = lastMotion(world);
        PositionOnTrack positionOnTrack = motion.getFinalPosition();
        TrackSection desiredTrackSection = new TrackSection(nextVector, positionOnTrack.getLocation());

        // Check whether the desired track section is single or double track.
        Vec2D tileA = desiredTrackSection.tileA();
        Vec2D tileB = desiredTrackSection.tileB();
        FullTerrainTile fta = (FullTerrainTile) world.getTile(tileA);
        FullTerrainTile ftb = (FullTerrainTile) world.getTile(tileB);
        TrackPiece tpa = fta.getTrackPiece();
        TrackPiece tpb = ftb.getTrackPiece();
        int tracks = 1;
        if (tpa.getTrackRule().isDouble() && tpb.getTrackRule().isDouble()) {
            tracks = 2;
        }
        Integer trains = occupiedTracks.occupiedTrackSections.get(desiredTrackSection);
        if (trains != null) {
            if (trains >= tracks) {
                // We need to wait for the track ahead to clear.
                occupiedTracks.stopTrain(trainID);
                return stopTrain(world);
            }
        }
        // Create a new train motion object.
        TrainMotion nextMotion = nextMotion(world, nextVector);
        return new NextActivityMove(nextMotion, trainID, principal);
    }

    private TrainMotion nextMotion(ReadOnlyWorld world, TileTransition tileTransition) {
        TrainMotion motion = lastMotion(world);

        Motion speeds = nextSpeeds(world, tileTransition);

        PathOnTiles currentTiles = motion.getTiles(motion.duration());
        PathOnTiles pathOnTiles = currentTiles.addStep(tileTransition);
        return new TrainMotion(pathOnTiles, currentTiles.steps(), motion.getTrainLength(), speeds);
    }

    public Motion nextSpeeds(ReadOnlyWorld world, TileTransition tileTransition) {
        TrainAccessor trainAccessor = new TrainAccessor(world, principal, trainID);
        TrainMotion lastMotion = lastMotion(world);

        double u = lastMotion.getSpeedAtEnd();
        double s = tileTransition.getLength();

        int wagons = trainAccessor.getTrain().getNumberOfWagons();
        double a0 = 0.5d / (wagons + 1);
        double topSpeed = (double) (10 / (wagons + 1));

        Motion newSpeeds;
        if (u < topSpeed) {
            double t = (topSpeed - u) / a0;
            Motion a = ConstantAccelerationMotion.fromSpeedAccelerationTime(u, a0, t);
            t = s / topSpeed + 1; // Slightly overestimate the time
            Motion b = ConstantAccelerationMotion.fromSpeedAccelerationTime(topSpeed, 0, t);
            newSpeeds = new CompositeMotion(a, b);
        } else {
            double t;
            t = s / topSpeed + 1; // Slightly overestimate the time
            newSpeeds = ConstantAccelerationMotion.fromSpeedAccelerationTime(topSpeed, 0, t);
        }

        return newSpeeds;
    }

    public TileTransition nextStep(ReadOnlyWorld world) {
        // Find current position.
        TrainMotion currentMotion = lastMotion(world);
        PositionOnTrack currentPosition = currentMotion.getFinalPosition();
        // Find targets
        Vec2D targetPoint = currentTrainTarget(world);
        return findNextStep(world, currentPosition, targetPoint);
    }

    /**
     * @param world
     * @return
     */
    public Move stopTrain(ReadOnlyWorld world) {
        TrainMotion motion = lastMotion(world);
        Motion stopped = ConstantAccelerationMotion.STOPPED;
        double duration = motion.duration();

        int trainLength = motion.getTrainLength();
        PathOnTiles tiles = motion.getTiles(duration);
        int engineDist = tiles.steps();
        TrainMotion nextMotion = new TrainMotion(tiles, engineDist, trainLength, stopped);
        return new NextActivityMove(nextMotion, trainID, principal);
    }
}
