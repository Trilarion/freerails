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

import freerails.move.CompositeMove;
import freerails.move.Move;
import freerails.move.listmove.ChangeTrainScheduleMove;
import freerails.move.listmove.RemoveItemFromListMove;
import freerails.model.world.PlayerKey;
import freerails.model.NonNullElementWorldIterator;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.WorldIterator;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.station.Station;
import freerails.model.train.schedule.ImmutableSchedule;
import freerails.model.train.schedule.MutableSchedule;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This Move removes a station from the station list and from the map.
 */
public class RemoveStationMove extends CompositeMove implements TrackMove {

    private static final long serialVersionUID = 3760847865429702969L;

    private RemoveStationMove(List<Move> moves) {
        super(moves);
    }

    public static TrackMove getInstance(ReadOnlyWorld world, ChangeTrackPieceMove removeTrackMove, FreerailsPrincipal principal) {
        WorldIterator wi = new NonNullElementWorldIterator(PlayerKey.Stations, world, principal);
        int stationIndex = -1;

        while (wi.next()) {
            Station station = (Station) wi.getElement();

            if (station.location.equals(removeTrackMove.getLocation())) {
                // We have found the station!
                stationIndex = wi.getIndex();
                break;
            }
        }

        if (-1 == stationIndex) {
            throw new IllegalArgumentException("Could find a station at " + removeTrackMove.getLocation().x + ", " + removeTrackMove.getLocation().y);
        }

        Station station2remove = (Station) world.get(principal, PlayerKey.Stations, stationIndex);
        ArrayList<Move> moves = new ArrayList<>();
        moves.add(removeTrackMove);
        moves.add(new RemoveItemFromListMove(PlayerKey.Stations, stationIndex, station2remove, principal));

        // Now update any train schedules that include this station.
        WorldIterator schedules = new NonNullElementWorldIterator(PlayerKey.TrainSchedules, world, principal);

        while (schedules.next()) {
            ImmutableSchedule schedule = (ImmutableSchedule) schedules.getElement();

            if (schedule.stopsAtStation(stationIndex)) {
                MutableSchedule mutableSchedule = new MutableSchedule(schedule);
                mutableSchedule.removeAllStopsAtStation(stationIndex);

                Move changeScheduleMove = new ChangeTrainScheduleMove(schedules.getIndex(), schedule, mutableSchedule.toImmutableSchedule(), principal);
                moves.add(changeScheduleMove);
            }
        }

        return new RemoveStationMove(moves);
    }

    /**
     * @return
     */
    public Rectangle getUpdatedTiles() {

        MapUpdateMove tm = (TrackMove) getMove(0);
        return tm.getUpdatedTiles();
    }
}