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

import freerails.model.track.*;
import freerails.model.station.StationUtils;
import freerails.model.train.motion.*;
import freerails.move.*;

import freerails.util.Vec2D;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TileTransition;
import freerails.model.train.*;

import java.util.*;

/**
 * Generates moves for changes in train position and stops at stations.
 */
public class MoveTrainMoveGenerator implements MoveGenerator {

    private static final long serialVersionUID = 3545516188269491250L;
    private final Player player;
    private final int trainId;
    private final OccupiedTracks occupiedTracks;

    /**
     * @param trainId
     * @param player
     * @param occupiedTracks
     */
    public MoveTrainMoveGenerator(int trainId, Player player, OccupiedTracks occupiedTracks) {
        this.trainId = trainId;
        this.player = player;
        this.occupiedTracks = occupiedTracks;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MoveTrainMoveGenerator)) return false;

        final MoveTrainMoveGenerator moveTrainPreMove = (MoveTrainMoveGenerator) obj;

        if (trainId != moveTrainPreMove.trainId) return false;
        return player.equals(moveTrainPreMove.player);
    }

    /**
     * @param world
     * @return
     */
    @Override
    public Move generate(UnmodifiableWorld world) {

        // Check that we can generate a move.
        if (!TrainUtils.isUpdateDue(world, player, trainId)) {
            throw new IllegalStateException();
        }

        Train train = world.getTrain(player, trainId);
        TrainMotion trainMotion = train.findCurrentMotion(Double.MAX_VALUE);

        TrainState trainState = trainMotion.getTrainState();

        switch (trainState) {
            case STOPPED_AT_STATION:
                return moveTrain(world, occupiedTracks);
            case READY: {
                // Are we at a station?
                TrainStopsHandler stopsHandler = new TrainStopsHandler(trainId, player, world);
                PositionOnTrack positionOnTrack = trainMotion.getFinalPosition();
                Vec2D location = positionOnTrack.getLocation();
                boolean atStation = StationUtils.getStationId(world, player, location) != null;

                TrainMotion nextMotion;
                if (atStation) {
                    // We have just arrived at a station.
                    double durationOfStationStop = 10;

                    stopsHandler.arrivesAtPoint(location);

                    TrainState status = TrainUtils.isWaitingForFullLoad(world, player, trainId) ? TrainState.WAITING_FOR_FULL_LOAD : TrainState.STOPPED_AT_STATION;
                    PathOnTiles path = trainMotion.getPath();
                    int lastTrainLength = trainMotion.getTrainLength();
                    int currentTrainLength = stopsHandler.getTrainLength();

                    // If we are adding wagons we may need to lengthen the path.
                    if (lastTrainLength < currentTrainLength) {
                        path = TrainUtils.lengthenPath(world, path, currentTrainLength);
                    }

                    nextMotion = new TrainMotion(path, currentTrainLength, durationOfStationStop, status);

                    // Create a new Move object.
                    // TODO needed dedicated move for train moves
                    // Move trainMove = new NextActivityMove(nextMotion, trainId, player);
                    List<Move> moves = new LinkedList<>();
                    moves.add(new AddActivityMove(player, trainId, nextMotion));
                    moves.addAll(stopsHandler.getMoves());
                    return new CompositeMove(moves);
                }
                return moveTrain(world, occupiedTracks);
            }
            case WAITING_FOR_FULL_LOAD: {
                TrainStopsHandler stopsHandler = new TrainStopsHandler(trainId, player, world);

                boolean waitingForfullLoad = stopsHandler.refreshWaitingForFullLoad();
                List<Move> cargoMoves = stopsHandler.getMoves();
                if (!waitingForfullLoad) {
                    Move trainMove = moveTrain(world, occupiedTracks);
                    if (null != trainMove) {
                        List<Move> moves = new ArrayList<>();
                        moves.add(trainMove);
                        moves.addAll(cargoMoves);
                        return new CompositeMove(moves);
                    } else {
                        return new CompositeMove(cargoMoves);
                    }
                }
                // TODO makeTrainWait not implemented
                // stopsHandler.makeTrainWait(30);
                return new CompositeMove(cargoMoves);
            }
            default:
                throw new UnsupportedOperationException(trainState.toString());
        }
    }

    @Override
    public int hashCode() {
        int result;
        result = trainId;
        result = 29 * result + player.hashCode();
        return result;
    }

    // TODO parts of this should be in the model.train
    private Move moveTrain(UnmodifiableWorld world, OccupiedTracks occupiedTracks) {
        // Find the next vector.
        TileTransition nextVector = nextStep(world);

        TrainMotion motion = MotionUtils.lastMotion(world, player, trainId);
        PositionOnTrack positionOnTrack = motion.getFinalPosition();
        TrackSection desiredTrackSection = new TrackSection(nextVector, positionOnTrack.getLocation());

        // Check whether the desired track section is single or double track.
        Vec2D tileA = desiredTrackSection.tileA();
        Vec2D tileB = desiredTrackSection.tileB();
        TerrainTile fta = world.getTile(tileA);
        TerrainTile ftb = world.getTile(tileB);
        TrackPiece tpa = fta.getTrackPiece();
        TrackPiece tpb = ftb.getTrackPiece();
        int tracks = 1;
        if (tpa.getTrackType().isDouble() && tpb.getTrackType().isDouble()) {
            tracks = 2;
        }
        Integer trains = occupiedTracks.occupiedTrackSections.get(desiredTrackSection);
        if (trains != null) {
            if (trains >= tracks) {
                // We need to wait for the track ahead to clear.
                occupiedTracks.stopTrain(trainId);
                // stop train
                TrainMotion motion1 = MotionUtils.lastMotion(world, player, trainId);
                Motion stopped = ConstantAccelerationMotion.STOPPED;
                double duration = motion1.getDuration();

                int trainLength = motion1.getTrainLength();
                PathOnTiles tiles = motion1.getTiles(duration);
                int engineDist = tiles.steps();
                TrainMotion nextMotion = new TrainMotion(tiles, engineDist, trainLength, stopped);
                return new AddActivityMove(player, trainId, nextMotion);
            }
        }
        // Create a new train motion object.
        // next motion
        TrainMotion motion1 = MotionUtils.lastMotion(world, player, trainId);

        Motion speeds = nextSpeeds(world, nextVector);

        PathOnTiles currentTiles = motion1.getTiles(motion1.getDuration());
        PathOnTiles pathOnTiles = currentTiles.addStep(nextVector);
        TrainMotion nextMotion = new TrainMotion(pathOnTiles, currentTiles.steps(), motion1.getTrainLength(), speeds);
        return new AddActivityMove(player, trainId, nextMotion);
    }

    public Motion nextSpeeds(UnmodifiableWorld world, TileTransition tileTransition) {
        TrainMotion lastMotion = MotionUtils.lastMotion(world, player, trainId);

        double u = lastMotion.getSpeedAtEnd();
        double s = tileTransition.getLength();

        int wagons = world.getTrain(player, trainId).getNumberOfWagons();
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

    public TileTransition nextStep(UnmodifiableWorld world) {
        // Find current position.
        TrainMotion currentMotion = MotionUtils.lastMotion(world, player, trainId);
        PositionOnTrack currentPosition = currentMotion.getFinalPosition();
        // Find targets
        Vec2D targetPoint = TrainUtils.getTargetLocation(world, player, trainId);
        return TrackUtils.findNextStep(world, currentPosition, targetPoint);
    }

}
