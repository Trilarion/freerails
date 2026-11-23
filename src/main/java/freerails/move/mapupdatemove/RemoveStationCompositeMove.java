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

package freerails.move.mapupdatemove;

import freerails.model.operations.station.Station;
import freerails.model.operations.train.Train;
import freerails.model.operations.train.schedule.ScheduleState;
import freerails.model.player.Player;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import freerails.move.CompositeMove;
import freerails.move.Move;
import freerails.move.RemoveStationMove;
import freerails.move.Status;
import freerails.move.UpdateTrainMove;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * This move removes a station from the station list and from the map,
 * together with the schedule updates of the owner's trains that stop at it.
 *
 * <p>It is the only move that may remove station track. {@link #applicable(UnmodifiableWorld)}
 * checks that the components form one consistent station removal: the track change
 * removes station track, the removed station is the track owner's station at that
 * location, and every train of the owner that stops at the station gets exactly the
 * schedule without those stops, with no other change.
 */
public class RemoveStationCompositeMove extends CompositeMove implements TrackMove {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ChangeTrackPieceMove trackChange;
    private final RemoveStationMove stationRemoval;
    private final List<UpdateTrainMove> scheduleUpdates;
    private final Rectangle rectangle;

    /**
     * Creates a remove station composite move from the supplied values.
     *
     * @param trackChange the change that removes the station track
     * @param stationRemoval the removal of the station at the track change location
     * @param scheduleUpdates the schedule updates of the trains that stop at the station
     */
    public RemoveStationCompositeMove(@NotNull ChangeTrackPieceMove trackChange,
                                      @NotNull RemoveStationMove stationRemoval,
                                      @NotNull List<UpdateTrainMove> scheduleUpdates) {
        super(components(trackChange, stationRemoval, scheduleUpdates));
        this.trackChange = trackChange;
        this.stationRemoval = stationRemoval;
        this.scheduleUpdates = List.copyOf(scheduleUpdates);
        rectangle = trackChange.getUpdatedTiles();
    }

    /**
     * Lists the components in application order.
     *
     * @param trackChange the track change
     * @param stationRemoval the station removal
     * @param scheduleUpdates the schedule updates
     * @return the components
     */
    private static List<Move> components(ChangeTrackPieceMove trackChange, RemoveStationMove stationRemoval,
                                         List<UpdateTrainMove> scheduleUpdates) {
        List<Move> moves = new ArrayList<>();
        moves.add(Objects.requireNonNull(trackChange));
        moves.add(Objects.requireNonNull(stationRemoval));
        moves.addAll(scheduleUpdates);
        return moves;
    }

    /**
     * @return the change that removes the station track
     */
    public ChangeTrackPieceMove trackChange() {
        return trackChange;
    }

    /**
     * @return the bounds of the map tiles updated by station removal
     */
    @Override
    public Rectangle getUpdatedTiles() {
        return rectangle;
    }

    /**
     * Checks the components and that they form one consistent station removal.
     *
     * @param world the world whose state is used
     * @return a status indicating whether the move can be applied
     */
    @NotNull
    @Override
    public Status applicable(@NotNull UnmodifiableWorld world) {
        if (!trackChange.removesStationTrack()) {
            return Status.fail("A station removal must remove station track.");
        }
        Status status = trackChange.checkTrackChange(world);
        if (!status.isSuccess()) {
            return status;
        }

        Player owner = stationRemoval.getPlayer();
        if (owner.getId() != trackChange.trackPieceBefore().playerId()) {
            return Status.fail("The removed station must belong to the owner of the station track.");
        }
        status = stationRemoval.applicable(world);
        if (!status.isSuccess()) {
            return status;
        }
        int stationId = stationRemoval.getStationId();
        Optional<Station> station = world.getStations(owner).stream()
                .filter(candidate -> candidate.getId() == stationId)
                .findFirst();
        if (station.isEmpty() || !station.get().getLocation().equals(trackChange.location())) {
            return Status.fail("The removed station must be at the removed station track.");
        }

        return checkScheduleUpdates(world, owner, stationId);
    }

    /**
     * Checks that the schedule updates remove the station from exactly the owner's
     * trains that stop at it, and change nothing else.
     *
     * @param world the world whose state is used
     * @param owner the station owner
     * @param stationId the removed station
     * @return a status indicating whether the schedule updates are consistent
     */
    private Status checkScheduleUpdates(UnmodifiableWorld world, Player owner, int stationId) {
        Map<Integer, UpdateTrainMove> updatesByTrainId = new HashMap<>();
        for (UpdateTrainMove update : scheduleUpdates) {
            if (!update.getPlayer().equals(owner) || !update.changesOnlySchedule()
                    || updatesByTrainId.put(update.getTrainId(), update) != null) {
                return Status.fail("A station removal may only update the schedules of the owner's trains.");
            }
            Status status = update.applicable(world);
            if (!status.isSuccess()) {
                return status;
            }
        }

        for (Train train : world.getTrains(owner)) {
            UpdateTrainMove update = updatesByTrainId.remove(train.getId());
            if (!train.getSchedule().stopsAtStation(stationId)) {
                if (update != null) {
                    return Status.fail("A station removal may only update trains that stop at the station.");
                }
                continue;
            }
            ScheduleState expected = new ScheduleState(train.getSchedule()).removeAllStopsAtStation(stationId);
            if (update == null || !expected.equals(update.getSchedule())) {
                return Status.fail("A station removal must remove the station from every schedule that stops at it.");
            }
        }
        return Status.OK;
    }

    /**
     * Applies the station track removal, the station removal, and the schedule updates.
     *
     * @param world the world whose state is updated
     */
    @Override
    public void apply(@NotNull World world) {
        Status status = applicable(world);
        if (!status.isSuccess()) {
            throw new RuntimeException(status.getMessage());
        }
        trackChange.applyTrackChange(world);
        stationRemoval.apply(world);
        for (UpdateTrainMove update : scheduleUpdates) {
            update.apply(world);
        }
    }
}
