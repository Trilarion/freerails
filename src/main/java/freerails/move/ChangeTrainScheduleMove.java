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

package freerails.move;

import freerails.world.KEY;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.train.ImmutableSchedule;

/**
 * This Move changes a train's schedule.
 */
public class ChangeTrainScheduleMove extends ChangeItemInListMove {
    private static final long serialVersionUID = 3691043187930052149L;

    /**
     * @param id
     * @param before
     * @param after
     * @param p
     */
    public ChangeTrainScheduleMove(int id, ImmutableSchedule before,
                                   ImmutableSchedule after, FreerailsPrincipal p) {
        super(KEY.TRAIN_SCHEDULES, id, before, after, p);
    }
}