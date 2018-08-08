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
package freerails.move.mapupdatemove;

import freerails.model.train.schedule.UnmodifiableSchedule;
import freerails.move.CompositeMove;
import freerails.move.Move;
import freerails.move.RemoveStationMove;
import freerails.move.listmove.ChangeItemInListMove;
import freerails.model.world.PlayerKey;
import freerails.model.world.NonNullElementWorldIterator;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;
import freerails.model.station.Station;
import freerails.model.train.schedule.Schedule;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This Move removes a station from the station list and from the map.
 */
public class RemoveStationCompositeMove extends CompositeMove implements TrackMove {

    private static final long serialVersionUID = 3760847865429702969L;

    private RemoveStationCompositeMove(List<Move> moves) {
        super(moves);
    }

    // TODO move static code to model, something like get station by location
    public static TrackMove getInstance(UnmodifiableWorld world, ChangeTrackPieceMove removeTrackMove, Player player) {
        int stationIndex = -1;

        for (Station station: world.getStations(player)) {
            if (station.location.equals(removeTrackMove.getLocation())) {
                // We have found the station!
                stationIndex = station.getId();
                break;
            }
        }

        if (-1 == stationIndex) {
            throw new IllegalArgumentException("Could find a station at " + removeTrackMove.getLocation());
        }

        Station stationToRemove = world.getStation(player, stationIndex);
        ArrayList<Move> moves = new ArrayList<>();
        moves.add(removeTrackMove);
        moves.add(new RemoveStationMove(player, stationIndex));

        // Now update any train schedules that include this station.
        NonNullElementWorldIterator worldIterator = new NonNullElementWorldIterator(PlayerKey.TrainSchedules, world, player);

        while (worldIterator.next()) {
            UnmodifiableSchedule schedule = (UnmodifiableSchedule) worldIterator.getElement();

            if (schedule.stopsAtStation(stationIndex)) {
                Schedule mutableSchedule = new Schedule(schedule);
                mutableSchedule.removeAllStopsAtStation(stationIndex);

                Move changeScheduleMove = new ChangeItemInListMove(PlayerKey.TrainSchedules, worldIterator.getIndex(), schedule, mutableSchedule, player);
                moves.add(changeScheduleMove);
            }
        }

        return new RemoveStationCompositeMove(moves);
    }

    /**
     * @return
     */
    public Rectangle getUpdatedTiles() {

        MapUpdateMove mapUpdateMove = (TrackMove) getMove(0);
        return mapUpdateMove.getUpdatedTiles();
    }
}