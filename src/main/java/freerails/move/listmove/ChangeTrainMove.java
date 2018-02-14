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
package freerails.move.listmove;

import freerails.move.Move;
import freerails.util.ImmutableList;
import freerails.world.KEY;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.train.TrainModel;

import java.io.Serializable;

/**
 * This Move can change a train's engine and wagons.
 */
public class ChangeTrainMove extends ChangeItemInListMove {
    
    private static final long serialVersionUID = 3257854272514242873L;

    private ChangeTrainMove(int index, Serializable before, Serializable after, FreerailsPrincipal principal) {
        super(KEY.TRAINS, index, before, after, principal);
    }

    /**
     * @param id
     * @param before
     * @param newEngine
     * @param newWagons
     * @param principal
     * @return
     */
    public static Move generateMove(int id, TrainModel before, int newEngine, ImmutableList<Integer> newWagons, FreerailsPrincipal principal) {
        TrainModel after = before.getNewInstance(newEngine, newWagons);

        return new ChangeTrainMove(id, before, after, principal);
    }
}