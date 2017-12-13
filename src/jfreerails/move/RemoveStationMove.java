/*
 * Created on 15-Apr-2003
 *
 */
package jfreerails.move;

import java.awt.Rectangle;
import java.util.ArrayList;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.WorldIterator;
import jfreerails.world.train.ImmutableSchedule;
import jfreerails.world.train.MutableSchedule;


/**
 * This Move removes a station from the station list and from the map.
 * @author Luke
 *
 */
public class RemoveStationMove extends CompositeMove implements TrackMove {
    private RemoveStationMove(ArrayList moves) {
        super(moves);
    }

    static RemoveStationMove getInstance(ReadOnlyWorld w,
        ChangeTrackPieceMove removeTrackMove) {
        WorldIterator wi = new NonNullElements(KEY.STATIONS, w);
        int stationIndex = -1;

        while (wi.next()) {
            StationModel station = (StationModel)wi.getElement();

            if (station.x == removeTrackMove.getLocation().x &&
                    station.y == removeTrackMove.getLocation().y) {
                //We have found the station!
                stationIndex = wi.getIndex();

                break;
            }
        }

        if (-1 == stationIndex) {
            throw new IllegalArgumentException("Could find a station at " +
                removeTrackMove.getLocation().x + ", " +
                removeTrackMove.getLocation().y);
        }

        StationModel station2remove = (StationModel)w.get(KEY.STATIONS,
                stationIndex);
        ArrayList moves = new ArrayList();
        moves.add(removeTrackMove);
        moves.add(new RemoveItemFromListMove(KEY.STATIONS, stationIndex,
                station2remove));

        //Now update any train schedules that include this station.
        WorldIterator schedules = new NonNullElements(KEY.TRAIN_SCHEDULES, w);

        while (schedules.next()) {
            ImmutableSchedule schedule = (ImmutableSchedule)schedules.getElement();

            if (schedule.stopsAtStation(stationIndex)) {
                MutableSchedule mutableSchedule = new MutableSchedule(schedule);
                mutableSchedule.removeAllStopsAtStation(stationIndex);

                Move changeScheduleMove = new ChangeTrainScheduleMove(schedules.getIndex(),
                        schedule, mutableSchedule.toImmutableSchedule());
                moves.add(changeScheduleMove);
            }
        }

        return new RemoveStationMove(moves);
    }

    public Rectangle getUpdatedTiles() {
        TrackMove tm = (TrackMove)getMove(0);

        return tm.getUpdatedTiles();
    }
}