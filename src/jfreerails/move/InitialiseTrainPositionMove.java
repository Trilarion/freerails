package jfreerails.move;

import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainPositionOnMap;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;

/**
 * Initialises the trains position
 */
public class InitialiseTrainPositionMove implements Move {
    private final TrainPositionOnMap newPosition;
    private final int trainNo;

    public InitialiseTrainPositionMove(int trainNumber, TrainPositionOnMap
    position) {
	newPosition = position;
	trainNo = trainNumber;
    }

    public MoveStatus tryDoMove(World w) {
	/* the train must not have any previous position */
	if (getTrainPosition(w) == null) {
	    return MoveStatus.MOVE_OK;
	} else {
	    return MoveStatus.MOVE_FAILED;
	}
    }

    public MoveStatus tryUndoMove(World w) {
	if (newPosition.equals(getTrainPosition(w))) {
	    return MoveStatus.MOVE_OK;
	} else {
	    return MoveStatus.MOVE_FAILED;
	}
    }

    public MoveStatus doMove(World w) {
	if (tryDoMove(w) == MoveStatus.MOVE_OK) {
	    setTrainPosition(w, newPosition);
	    return MoveStatus.MOVE_OK;
	} else {
	    return MoveStatus.MOVE_FAILED;
	}
    }

    public MoveStatus undoMove(World w) {
	if (tryUndoMove(w) == MoveStatus.MOVE_OK) {
	    setTrainPosition(w, null);
	    return MoveStatus.MOVE_OK;
	} else {
	    return MoveStatus.MOVE_FAILED;
	}
    }
    
    private void setTrainPosition(World w, TrainPositionOnMap p) {
	TrainModel train = (TrainModel)w.get(KEY.TRAINS, trainNo);
	train.setPosition(p);
	
    }

    private TrainPositionOnMap getTrainPosition(World w) {
	TrainModel train = (TrainModel)w.get(KEY.TRAINS, trainNo);
	return train.getPosition();
    }
}
