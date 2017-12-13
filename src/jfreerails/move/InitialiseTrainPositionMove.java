/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package jfreerails.move;

import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainPositionOnMap;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;

/**
 * Initialises the trains position
 */
public class InitialiseTrainPositionMove implements Move {
    private final TrainPositionOnMap newPosition;
    private final int trainNo;
    private final FreerailsPrincipal principal;

    public InitialiseTrainPositionMove(int trainNumber, FreerailsPrincipal p,
        TrainPositionOnMap position) {
        newPosition = position;
        trainNo = trainNumber;
	principal = p;
    }

    public FreerailsPrincipal getPrincipal() {
	return principal;
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        /* the train must not have any previous position */
        if (getTrainPosition(w) == null) {
            return MoveStatus.MOVE_OK;
        } else {
            return MoveStatus.MOVE_FAILED;
        }
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        if (newPosition.equals(getTrainPosition(w))) {
            return MoveStatus.MOVE_OK;
        } else {
            return MoveStatus.MOVE_FAILED;
        }
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        if (tryDoMove(w, p) == MoveStatus.MOVE_OK) {
            setTrainPosition(w, newPosition);

            return MoveStatus.MOVE_OK;
        } else {
            return MoveStatus.MOVE_FAILED;
        }
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        if (tryUndoMove(w, p) == MoveStatus.MOVE_OK) {
            setTrainPosition(w, null);

            return MoveStatus.MOVE_OK;
        } else {
            return MoveStatus.MOVE_FAILED;
        }
    }

    private void setTrainPosition(World w, TrainPositionOnMap p) {
        TrainModel train = (TrainModel)w.get(KEY.TRAINS, trainNo, principal);
        train.setPosition(p);
    }

    private TrainPositionOnMap getTrainPosition(World w) {
        TrainModel train = (TrainModel)w.get(KEY.TRAINS, trainNo, principal);

        return train.getPosition();
    }
}
