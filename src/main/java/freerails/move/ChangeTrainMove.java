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
 * Created on 25-Aug-2003
 *
 */
package freerails.move;

import freerails.util.ImInts;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.KEY;
import freerails.world.train.TrainModel;

import java.io.Serializable;

/**
 * This Move can change a train's engine and wagons.
 */
public class ChangeTrainMove extends ChangeItemInListMove {
    private static final long serialVersionUID = 3257854272514242873L;

    private ChangeTrainMove(int index, Serializable before,
                            Serializable after, FreerailsPrincipal p) {
        super(KEY.TRAINS, index, before, after, p);
    }

    /**
     * @param id
     * @param before
     * @param newEngine
     * @param newWagons
     * @param p
     * @return
     */
    public static ChangeTrainMove generateMove(int id, TrainModel before,
                                               int newEngine, ImInts newWagons, FreerailsPrincipal p) {
        TrainModel after = before.getNewInstance(newEngine, newWagons);

        return new ChangeTrainMove(id, before, after, p);
    }
}