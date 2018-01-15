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

package freerails.controller;

import freerails.world.KEY;
import freerails.world.ReadOnlyWorld;
import freerails.world.game.GameTime;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.track.TrackSection;
import freerails.world.train.TrainModel;

import java.util.*;

/**
 *
 */
public class OccupiedTracks {

    public final Map<TrackSection, Integer> occupiedTrackSections;
    private final Map<Integer, List<TrackSection>> trainToTrackList;

    /**
     * @param principal
     * @param w
     */
    public OccupiedTracks(FreerailsPrincipal principal, ReadOnlyWorld w) {

        occupiedTrackSections = new HashMap<>();
        trainToTrackList = new HashMap<>();

        for (int i = 0; i < w.size(principal, KEY.TRAINS); i++) {
            TrainModel train = (TrainModel) w.get(principal, KEY.TRAINS, i);
            if (null == train) continue;

            TrainAccessor ta = new TrainAccessor(w, principal, i);
            GameTime gt = w.currentTime();

            if (ta.isMoving(gt.getTicks())) {

                HashSet<TrackSection> sections = ta.occupiedTrackSection(gt.getTicks());
                List<TrackSection> trackList = new ArrayList<>(sections);
                trainToTrackList.put(i, trackList);
                for (TrackSection section : sections) {
                    Integer count = occupiedTrackSections.get(section);
                    if (count == null) {
                        occupiedTrackSections.put(section, 1);
                    } else {
                        count++;
                        occupiedTrackSections.put(section, count);
                    }
                }
            }
        }
    }

    /**
     * @param i
     */
    public void stopTrain(int i) {
        List<TrackSection> trackList = trainToTrackList.remove(i);
        if (trackList == null) {
            return; // already removed
        }
        for (TrackSection section : trackList) {
            Integer count = occupiedTrackSections.get(section);
            if (count > 0) {
                count--;
                occupiedTrackSections.put(section, count);
            }
        }
    }
}