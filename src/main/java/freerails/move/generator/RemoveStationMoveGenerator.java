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

package freerails.move.generator;

import freerails.model.operations.station.Station;
import freerails.model.operations.train.Train;
import freerails.model.operations.train.schedule.ScheduleState;
import freerails.model.operations.train.schedule.UnmodifiableSchedule;
import freerails.model.player.Player;
import freerails.model.world.UnmodifiableWorld;
import freerails.move.RemoveStationMove;
import freerails.move.UpdateTrainMove;
import freerails.move.mapupdatemove.ChangeTrackPieceMove;
import freerails.move.mapupdatemove.RemoveStationCompositeMove;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Generates the move that removes a station together with the station track.
 *
 * <p>The generated {@link RemoveStationCompositeMove} applies, in order, the
 * supplied track change, the removal of the player's station at that location,
 * and a schedule update for each of the player's trains that stops at the
 * station. Station and train identifiers are per player, so only the station
 * owner's trains are affected. This is the counterpart of
 * {@link AddStationMoveGenerator}. Unlike that generator it adds no
 * transactions; the caller charges for the complete track removal.
 */
public class RemoveStationMoveGenerator implements MoveGenerator {

    @Serial
    private static final long serialVersionUID = 1L;
    private final ChangeTrackPieceMove trackChange;
    private final Player player;

    /**
     * Creates a remove station move generator from the supplied values.
     *
     * @param trackChange the change that replaces the station track piece with non-station track or no track
     * @param player the player owning the station
     */
    public RemoveStationMoveGenerator(ChangeTrackPieceMove trackChange, Player player) {
        this.trackChange = Objects.requireNonNull(trackChange);
        this.player = Objects.requireNonNull(player);
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
        if (!(obj instanceof RemoveStationMoveGenerator other)) return false;
        return trackChange.equals(other.trackChange) && player.equals(other.player);
    }

    /**
     * Returns a hash code for this object.
     *
     * @return the hash code for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(trackChange, player);
    }

    /**
     * @param world the game world used by this operation
     * @return the move that removes the station track, the station, and the schedule stops at it
     * @throws IllegalArgumentException if the player has no station at the track change location
     */
    @Override
    public RemoveStationCompositeMove generate(UnmodifiableWorld world) {
        int stationId = world.getStations(player).stream()
                .filter(station -> station.getLocation().equals(trackChange.location()))
                .mapToInt(Station::getId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Could not find a station at " + trackChange.location()));

        List<UpdateTrainMove> scheduleUpdates = new ArrayList<>();

        // Station IDs are per player, so only the owner's trains can stop at this station.
        for (Train train : world.getTrains(player)) {
            UnmodifiableSchedule schedule = train.getSchedule();
            if (schedule.stopsAtStation(stationId)) {
                ScheduleState updatedSchedule = new ScheduleState(schedule).removeAllStopsAtStation(stationId);
                scheduleUpdates.add(new UpdateTrainMove(player, train.getId(), null, null, updatedSchedule));
            }
        }

        return new RemoveStationCompositeMove(trackChange, new RemoveStationMove(player, stationId), scheduleUpdates);
    }
}
