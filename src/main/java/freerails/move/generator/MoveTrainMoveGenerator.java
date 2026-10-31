/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

import freerails.model.ModelConstants;
import freerails.model.track.*;
import freerails.model.operations.station.StationUtils;
import freerails.model.operations.train.motion.*;
import freerails.move.*;

import freerails.util.Vec2D;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TileTransition;
import freerails.model.operations.train.*;

import java.io.Serial;
import java.util.*;

/**
 * Generates moves for changes in train position and stops at stations.
 */
public class MoveTrainMoveGenerator implements MoveGenerator {

    @Serial
    private static final long serialVersionUID = 3545516188269491250L;
    private final Player player;
    private final int trainId;
    private final OccupiedTracks occupiedTracks;
    private final MotionFidelity motionFidelity;

    /** Selects how train speed changes while a train advances one track segment. */
    public enum MotionFidelity {
        /** Moves at the consist's configured top speed for each segment. */
        CONSTANT_SPEED,
        /** Uses the detailed acceleration-and-cruise profile. */
        DETAILED_ACCELERATION
    }

    /**
     * @param trainId the identifier of the train to advance
     * @param player the player who owns or performs the operation
     * @param occupiedTracks the track sections currently occupied by trains
     */
    public MoveTrainMoveGenerator(int trainId, Player player, OccupiedTracks occupiedTracks) {
        this(trainId, player, occupiedTracks, MotionFidelity.CONSTANT_SPEED);
    }

    /**
     * Creates a train move generator with an explicit motion fidelity.
     *
     * @param trainId train identifier
     * @param player train owner
     * @param occupiedTracks track occupancy for this player
     * @param motionFidelity speed profile to use while moving
     */
    public MoveTrainMoveGenerator(int trainId, Player player, OccupiedTracks occupiedTracks, MotionFidelity motionFidelity) {
        this.trainId = trainId;
        this.player = player;
        this.occupiedTracks = occupiedTracks;
        this.motionFidelity = Objects.requireNonNull(motionFidelity);
    }

    /**
     * Returns whether this object is equal to the specified object.
     *
     * @param obj the object to compare with this object
     * @return true if this object is equal to the specified object, otherwise false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MoveTrainMoveGenerator moveTrainPreMove)) return false;

        if (trainId != moveTrainPreMove.trainId) return false;
        return player.equals(moveTrainPreMove.player)
                && getMotionFidelity() == moveTrainPreMove.getMotionFidelity();
    }

    /**
     * @param world the game world used by this operation
     * @return the move that advances the train along its route
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

    /**
     * Returns a hash code for this object.
     *
     * @return the hash code for this object
     */
    @Override
    public int hashCode() {
        int result;
        result = trainId;
        result = 29 * result + player.hashCode();
        result = 29 * result + getMotionFidelity().hashCode();
        return result;
    }

    // TODO parts of this should be in model.operations.train
    /**
     * Creates the move that advances the train along its next track section.
     *
     * @param world the world whose state is used or updated
     * @param occupiedTracks the occupied tracks
     * @return the move train
     */
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
        TrackPiece tpa = fta.trackPiece();
        TrackPiece tpb = ftb.trackPiece();
        int tracks = 1;
        if (tpa.trackType().isDouble() && tpb.trackType().isDouble()) {
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

    /**
     * Calculates the train speeds for its next movement activity.
     *
     * @param world the world whose state is used or updated
     * @param tileTransition the tile transition
     * @return the next speeds
     */
    public Motion nextSpeeds(UnmodifiableWorld world, TileTransition tileTransition) {
        TrainMotion lastMotion = MotionUtils.lastMotion(world, player, trainId);

        double s = tileTransition.getLength();

        int wagons = world.getTrain(player, trainId).getNumberOfWagons();
        double topSpeed = ModelConstants.MODEL_UNITS_PER_TILE / 3.0d / (wagons + 1);

        if (getMotionFidelity() == MotionFidelity.CONSTANT_SPEED) {
            return ConstantAccelerationMotion.fromSpeedAccelerationDistance(topSpeed, 0, s);
        }

        double u = lastMotion.getSpeedAtEnd();
        double a0 = ModelConstants.MODEL_UNITS_PER_TILE / 60.0d / (wagons + 1);
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

    /**
     * Returns the motion fidelity.
     *
     * @return the motion fidelity
     */
    private MotionFidelity getMotionFidelity() {
        // A missing field in generators serialized by older versions means the default mode.
        return motionFidelity == null ? MotionFidelity.CONSTANT_SPEED : motionFidelity;
    }

    /**
     * Returns the next track transition along the train route.
     *
     * @param world the world whose state is used or updated
     * @return the next step
     */
    public TileTransition nextStep(UnmodifiableWorld world) {
        // Find current position.
        TrainMotion currentMotion = MotionUtils.lastMotion(world, player, trainId);
        PositionOnTrack currentPosition = currentMotion.getFinalPosition();
        // Find targets
        Vec2D targetPoint = TrainUtils.getTargetLocation(world, player, trainId);
        return TrackUtils.findNextStep(world, currentPosition, targetPoint);
    }

}
