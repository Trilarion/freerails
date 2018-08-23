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
 * ChangeTrackPieceCompositeMove.java
 *
 */
package freerails.move.mapupdatemove;

import freerails.model.finance.TransactionUtils;
import freerails.model.station.Station;
import freerails.model.train.Train;
import freerails.model.train.schedule.Schedule;
import freerails.model.train.schedule.UnmodifiableSchedule;
import freerails.model.world.*;
import freerails.move.*;
import freerails.util.Vec2D;
import freerails.model.game.Rules;
import freerails.model.player.Player;
import freerails.model.terrain.TileTransition;
import freerails.model.track.*;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This Move changes adds, removes, or upgrades the track between two tiles.
 */
public final class ChangeTrackPieceCompositeMove extends CompositeMove implements TrackMove {

    private static final long serialVersionUID = 3616443518780978743L;
    private final Rectangle rectangle;
    private final Player player;

    private ChangeTrackPieceCompositeMove(TrackMove a, TrackMove b, Player player) {
        super(Arrays.asList(a, b));
        rectangle = a.getUpdatedTiles().union(b.getUpdatedTiles());
        this.player = player;
    }

    /**
     * @param from
     * @param direction
     * @param world
     * @param player
     * @return
     */
    public static ChangeTrackPieceCompositeMove generateBuildTrackMove(Vec2D from, TileTransition direction, TrackType typeA, TrackType typeB, UnmodifiableWorld world, Player player) {
        ChangeTrackPieceMove a = getBuildTrackChangeTrackPieceMove(from, direction, typeA, world, player);
        ChangeTrackPieceMove b = getBuildTrackChangeTrackPieceMove(direction.createRelocatedPoint(from), direction.getOpposite(), typeB, world, player);

        return new ChangeTrackPieceCompositeMove(a, b, player);
    }

    /**
     * @param from
     * @param direction
     * @param world
     * @param player
     * @return
     * @throws Exception
     */
    public static ChangeTrackPieceCompositeMove generateRemoveTrackMove(Vec2D from, TileTransition direction, UnmodifiableWorld world, Player player) throws Exception {
        TrackMove a = getRemoveTrackChangeTrackPieceMove(from, direction, world, player);
        TrackMove b = getRemoveTrackChangeTrackPieceMove(direction.createRelocatedPoint(from), direction.getOpposite(), world, player);

        return new ChangeTrackPieceCompositeMove(a, b, player);
    }

    // TODO put part of it in model
    // utility method.
    private static ChangeTrackPieceMove getBuildTrackChangeTrackPieceMove(Vec2D p, TrackConfigurations direction, TrackType trackType, UnmodifiableWorld world, Player player) {
        if (!world.boundsContain(p)) {
            throw new RuntimeException("Out of bounds");
        }

        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;

        int playerId = player.getId();

        oldTrackPiece = world.getTile(p).getTrackPiece();

        if (oldTrackPiece != null) {
            TrackConfiguration trackConfiguration = TrackConfiguration.add(oldTrackPiece.getTrackConfiguration(), direction);
            newTrackPiece = new TrackPiece(trackConfiguration, oldTrackPiece.getTrackType(), playerId);
        } else {
            newTrackPiece = TrackUtils.getTrackPieceWhenOldTrackPieceIsNull(direction, trackType, playerId);
        }

        return new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece, p);
    }

    // TODO put part of it in model
    // utility method.
    private static TrackMove getRemoveTrackChangeTrackPieceMove(Vec2D p, TrackConfigurations direction, UnmodifiableWorld world, Player player) throws Exception {
        if (!world.boundsContain(p)) {
            throw new RuntimeException("Out of bounds");
        }

        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;

        oldTrackPiece = world.getTile(p).getTrackPiece();

        if (oldTrackPiece != null) {
            TrackConfiguration trackConfiguration = TrackConfiguration.subtract(oldTrackPiece.getTrackConfiguration(), direction);

            if (trackConfiguration != TrackConfiguration.getFlatInstance("000010000")) {
                int playerId = player.getId();
                newTrackPiece = new TrackPiece(trackConfiguration, oldTrackPiece.getTrackType(), playerId);
            } else {
                newTrackPiece = null;
            }
        } else {
            // There is no track to remove.
            // Fix for bug [ 948670 ] Removing non-existent track
            throw new Exception();
        }


        ChangeTrackPieceMove changeTrackPieceMove = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece, p);

        // TODO maybe the removal of a station should be checked and induced somewhere else
        // If we are removing a station, we also need to remove the station from the station list.
        if (oldTrackPiece.getTrackType().isStation() && (newTrackPiece == null || !newTrackPiece.getTrackType().isStation())) {
            int stationIndex = -1;

            for (Station station: world.getStations(player)) {
                if (station.getLocation().equals(changeTrackPieceMove.getLocation())) {
                    // We have found the station!
                    stationIndex = station.getId();
                    break;
                }
            }

            if (-1 == stationIndex) {
                throw new IllegalArgumentException("Could find a station at " + changeTrackPieceMove.getLocation());
            }

            ArrayList<Move> moves = new ArrayList<>();
            moves.add(changeTrackPieceMove);
            moves.add(new RemoveStationMove(player, stationIndex));

            // Now update any train schedules that include this station by iterating over all trains
            for (Player player1: world.getPlayers()) {
                for (Train train: world.getTrains(player1)) {
                    UnmodifiableSchedule schedule = train.getSchedule();
                    if (schedule.stopsAtStation(stationIndex)) {
                        Schedule schedule1 = new Schedule(schedule);
                        schedule1.removeAllStopsAtStation(stationIndex);
                        train.setSchedule(schedule1);
                        Move changeScheduleMove = new UpdateTrainMove(player, train.getId(), null, null, schedule1);
                        moves.add(changeScheduleMove);
                    }
                }
            }

            return new RemoveStationCompositeMove(moves);
        } else {
            return changeTrackPieceMove;
        }
    }

    /**
     * @return
     */
    @Override
    public Rectangle getUpdatedTiles() {
        return rectangle;
    }

    @Override
    public Status compositeTest(World world) {
        // must connect to existing track
        Rules rules = world.getRules();

        if (rules.mustStayConnectedToExistingTrack()) {
            if (TransactionUtils.hasAnyTrackBeenBuilt(world, player)) {
                try {
                    ChangeTrackPieceMove a = (ChangeTrackPieceMove) super.getMove(0);
                    ChangeTrackPieceMove b = (ChangeTrackPieceMove) super.getMove(1);

                    if (a.trackPieceBefore == null && b.trackPieceBefore == null) {
                        return Status.fail("Must connect to existing track");
                    }
                } catch (ClassCastException e) {
                    // It was not the type of move we expected.
                    // We end up here when we are removing a station.
                    // TODO and what if it must indeed connect to other tracks???
                    return Status.OK;
                }
            }
        }
        return Status.OK;
    }

    @Override
    public void apply(@NotNull World world) {
        super.apply(world);
    }
}