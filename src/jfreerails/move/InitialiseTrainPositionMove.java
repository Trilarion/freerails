package jfreerails.move;

import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;
import jfreerails.world.train.TrainPositionOnMap;

/**
 * Initialises the trains position.
 * 
 * @author Luke
 */
public class InitialiseTrainPositionMove implements Move {
	private static final long serialVersionUID = 3256727294587320632L;

	private final TrainPositionOnMap newPosition;

	private final int trainNo;

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof InitialiseTrainPositionMove))
			return false;

		final InitialiseTrainPositionMove initialiseTrainPositionMove = (InitialiseTrainPositionMove) o;

		if (trainNo != initialiseTrainPositionMove.trainNo)
			return false;
		if (!m_principal.equals(initialiseTrainPositionMove.m_principal))
			return false;
		if (!newPosition.equals(initialiseTrainPositionMove.newPosition))
			return false;

		return true;
	}

	public int hashCode() {
		int result;
		result = newPosition.hashCode();
		result = 29 * result + trainNo;
		result = 29 * result + m_principal.hashCode();
		return result;
	}

	private final FreerailsPrincipal m_principal;

	public InitialiseTrainPositionMove(int trainNumber,
			TrainPositionOnMap position, FreerailsPrincipal p) {
		newPosition = position;
		trainNo = trainNumber;
		m_principal = p;
	}

	public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
		/* the train must not have any previous position */
		if (getTrainPosition(w) == null) {
			return MoveStatus.MOVE_OK;
		}
		return MoveStatus.moveFailed("The train already has a position.");
	}

	public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
		if (newPosition.equals(getTrainPosition(w))) {
			return MoveStatus.MOVE_OK;
		}
		return MoveStatus
				.moveFailed("The train did not have the expected position.");
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
		w.set(KEY.TRAIN_POSITIONS, trainNo, p, m_principal);
	}

	private TrainPositionOnMap getTrainPosition(World w) {
		TrainPositionOnMap pos = (TrainPositionOnMap) w.get(
				KEY.TRAIN_POSITIONS, trainNo, m_principal);

		return pos;
	}
}