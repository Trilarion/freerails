package jfreerails.move;

import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainPositionOnMap;


/**
 * Initialises the trains position.
 * @author Luke
 */
public class InitialiseTrainPositionMove implements Move {
    private final TrainPositionOnMap newPosition;
    private final int trainNo;
    private final FreerailsPrincipal principal;

    public InitialiseTrainPositionMove(int trainNumber,
        TrainPositionOnMap position, FreerailsPrincipal p) {
        newPosition = position;
        trainNo = trainNumber;
        this.principal = p;
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        /* the train must not have any previous position */
        if (getTrainPosition(w) == null) {
            return MoveStatus.MOVE_OK;
        } else {
            return MoveStatus.moveFailed("The train already has a position.");
        }
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        if (newPosition.equals(getTrainPosition(w))) {
            return MoveStatus.MOVE_OK;
        } else {
            return MoveStatus.moveFailed(
                "The train did not have the expected position.");
        }
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        MoveStatus status = tryDoMove(w, p);

        if (status.isOk()) {
            setTrainPosition(w, newPosition);
        }

        return status;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        MoveStatus status = tryUndoMove(w, p);

        if (status.isOk()) {
            setTrainPosition(w, null);
        }

        return status;
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