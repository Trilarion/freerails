package jfreerails.move;

import jfreerails.world.misc.FreerailsPathIterator;
import jfreerails.world.train.PathWalker;
import jfreerails.world.train.PathWalkerImpl;
import jfreerails.world.train.TrainList;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainPosition;

/**
 * @author Luke Lindsay 22-Oct-2002
 *
 */
public class ChangeTrainPositionMove {

	private final TrainPosition pieceToAdd, pieceToRemove;

	final int trainPositionNumber;

	public ChangeTrainPositionMove(
		TrainPosition pieceToAdd,
		TrainPosition pieceToRemove,
		int trainNumber) {
		this.pieceToAdd = pieceToAdd;
		this.pieceToRemove = pieceToRemove;
		this.trainPositionNumber = trainNumber;
	}

	public static ChangeTrainPositionMove generate(
		TrainPosition currentPosition,
		FreerailsPathIterator nextPathSection,
		int trainNumber) {
		TrainPosition bitToAdd, intermediate, newPosition, bitToRemove;

		bitToAdd = TrainPosition.createInstance(nextPathSection);

		

		intermediate = TrainPosition.add(currentPosition, bitToAdd);

		PathWalker pathWalker = new PathWalkerImpl(intermediate.path());

		if (TrainPosition.headsAreEqual(intermediate, currentPosition)) {
			//Then we must have added a piece to the tail, 
			//so we need to remove a piece from the head.
			
			double lengthToRemove = bitToAdd.calulateDistance();
			
			pathWalker.stepForward((int) lengthToRemove);

			bitToRemove = TrainPosition.createInstance(pathWalker);
			

		} else {
			//We must have added a piece to the head, 
			//so we need to remove a piece from the tail.
						
			
			double currentLength = currentPosition.calulateDistance();
			
			pathWalker.stepForward((int) currentLength);

			newPosition = TrainPosition.createInstance(pathWalker);

			bitToRemove = TrainPosition.remove(intermediate, newPosition);

		}

		

		return new ChangeTrainPositionMove(bitToAdd, bitToRemove, trainNumber);
	}

	public MoveStatus doMove(TrainList tl) {
		return doMove(tl, this.pieceToAdd, this.pieceToRemove);
	}

	public MoveStatus undoMove(TrainList tl) {
		return doMove(tl, this.pieceToRemove, this.pieceToAdd);
	}

	public MoveStatus tryDoMove(TrainList tl) {
		return tryMove(tl, this.pieceToAdd, this.pieceToRemove);
	}

	public MoveStatus tryUndoMove(TrainList tl) {
		return tryMove(tl, this.pieceToRemove, this.pieceToAdd);

	}

	MoveStatus doMove(TrainList tl, TrainPosition add, TrainPosition remove) {
		MoveStatus status = tryMove(tl, add, remove);

		if (status.ok) {

			TrainModel train = tl.getTrain(this.trainPositionNumber);
			TrainPosition oldTrainPosition = train.getPosition();
			TrainPosition intermediatePosition =
				TrainPosition.add(oldTrainPosition, add);
			TrainPosition newTrainPosition =
				TrainPosition.remove(intermediatePosition, remove);
			train.setPosition(newTrainPosition);

			return status;
		} else {
			return status;
		}
	}

	MoveStatus tryMove(
		TrainList tl,
		TrainPosition toAdd,
		TrainPosition toRemove) {
		TrainModel train = tl.getTrain(this.trainPositionNumber);
		TrainPosition currentPosition = train.getPosition();
		boolean canBeRemoved =
			TrainPosition.canBeRemoved(currentPosition, toRemove);
		boolean canBeAdded = TrainPosition.canBeAdded(currentPosition, toAdd);
		if (canBeRemoved && canBeAdded) {
			return MoveStatus.MOVE_ACCEPTED;
		} else {
			return MoveStatus.MOVE_REJECTED;
		}
	}

}
