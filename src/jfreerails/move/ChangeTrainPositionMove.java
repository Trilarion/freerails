package jfreerails.move;

import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;
import jfreerails.world.train.PathWalker;
import jfreerails.world.train.PathWalkerImpl;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainPositionOnMap;

/**
 * This Move changes the position of a train.
 * @author Luke Lindsay 22-Oct-2002
 *
 */
public class ChangeTrainPositionMove implements Move {

	private final TrainPositionOnMap changeToHead, changeToTail;

	private final boolean addToHead, addToTail;

	final int trainPositionNumber;

	public ChangeTrainPositionMove(TrainPositionOnMap pieceToAdd, TrainPositionOnMap pieceToRemove, int trainNumber, boolean addToHead, boolean addToTail) {
		this.addToHead = addToHead;
		this.addToTail = addToTail;
		this.changeToHead = pieceToAdd;
		this.changeToTail = pieceToRemove;
		this.trainPositionNumber = trainNumber;
	}

	public static ChangeTrainPositionMove getNullMove(int trainNumber) {
		return new ChangeTrainPositionMove(null, null, trainNumber, false, false) {
			MoveStatus move(World w, boolean updateTrainPosition, boolean isDoMove) {
				return MoveStatus.MOVE_OK;
			}

		};
	}

	public static ChangeTrainPositionMove generate(World w, FreerailsPathIterator nextPathSection, int trainNumber) {
		
		if(!nextPathSection.hasNext()){
			return getNullMove(trainNumber);
		}

		TrainModel train = (TrainModel)w.get(KEY.TRAINS, trainNumber);

		TrainPositionOnMap currentPosition = train.getPosition();

		TrainPositionOnMap bitToAdd, intermediate, newPosition, bitToRemove;

		bitToAdd = getBitToAdd(nextPathSection);

		
		intermediate = currentPosition.addToHead(bitToAdd);

		double currentLength = (double) train.getLength();

		bitToRemove = getBitToRemove(intermediate, currentLength);

		return new ChangeTrainPositionMove(bitToAdd, bitToRemove, trainNumber, true, false);

	}

	static TrainPositionOnMap getBitToRemove(TrainPositionOnMap intermediate, double currentLength) {
		TrainPositionOnMap newPosition;
		TrainPositionOnMap bitToRemove;
		PathWalker pathWalker;
		pathWalker = new PathWalkerImpl(intermediate.path());

		pathWalker.stepForward((int) currentLength);

		newPosition = TrainPositionOnMap.createInSameDirectionAsPath(pathWalker);
		//System.out.println("newPosition " + newPosition);

		bitToRemove = intermediate.removeFromHead(newPosition);
		return bitToRemove;
	}

	static TrainPositionOnMap getBitToAdd(FreerailsPathIterator nextPathSection) {
		return TrainPositionOnMap.createInOppositeDirectionToPath(nextPathSection);
	}

	/*
	public static ChangeTrainPositionMove generate(
		TrainPosition currentPosition,
		FreerailsPathIterator nextPathSection,
		int trainNumber) {
		TrainPosition bitToAdd, intermediate, newPosition, bitToRemove;
	
		bitToAdd =
			TrainPosition.createInOppositeDirectionToPath(nextPathSection);
	
		//System.out.println(bitToAdd.toString());
	
		intermediate = TrainPosition.add(currentPosition, bitToAdd);
	
		PathWalker pathWalker = new PathWalkerImpl(intermediate.path());
	
		if (TrainPosition.headsAreEqual(intermediate, currentPosition)) {
			//Then we must have added a piece to the tail, 
			//so we need to remove a piece from the head.
	
			double lengthToRemove = bitToAdd.calulateDistance();
	
			pathWalker.stepForward((int) lengthToRemove);
	
			bitToRemove = TrainPosition.createInSameDirectionAsPath(pathWalker);
	
		} else {
			//We must have added a piece to the head, 
			//so we need to remove a piece from the tail.
	
			double currentLength = currentPosition.calulateDistance();
	
			pathWalker.stepForward((int) currentLength);
	
			newPosition = TrainPosition.createInSameDirectionAsPath(pathWalker);
	
			bitToRemove = TrainPosition.remove(intermediate, newPosition);
	
		}
	
		//System.out.println(bitToRemove.toString());
	
		return new ChangeTrainPositionMove(bitToAdd, bitToRemove, trainNumber);
	}
	*/

	public MoveStatus doMove(World w) {
		boolean updateTrainPosition = true;
		boolean isDoMove = true;
		return move(w, updateTrainPosition, isDoMove);
	}

	public MoveStatus undoMove(World w) {
		boolean updateTrainPosition = true;
		boolean isDoMove = false;
		return move(w, updateTrainPosition, isDoMove);
	}

	public MoveStatus tryDoMove(World w) {
		boolean updateTrainPosition = false;
		boolean isDoMove = true;
		return move(w, updateTrainPosition, isDoMove);
	}

	public MoveStatus tryUndoMove(World w) {
		boolean updateTrainPosition = false;
		boolean isDoMove = false;
		return move(w, updateTrainPosition, isDoMove);
	}

	MoveStatus move(World w, boolean updateTrainPosition, boolean isDoMove) {

		boolean localAddToHead, localAddToTail;

		if (isDoMove) {
			localAddToHead = this.addToHead;
			localAddToTail = this.addToTail;
		} else {
			localAddToHead = !this.addToHead;
			localAddToTail = !this.addToTail;
		}

		TrainModel train = (TrainModel)w.get(KEY.TRAINS, this.trainPositionNumber);
		TrainPositionOnMap oldTrainPosition = train.getPosition();
		TrainPositionOnMap intermediatePosition, newTrainPosition;

		if (localAddToHead) {

			if (!oldTrainPosition.canAddToHead(changeToHead)) {
				return MoveStatus.MOVE_FAILED;
			}
			intermediatePosition = oldTrainPosition.addToHead(changeToHead);
			//System.out.println(
			//	"intermediatePosition=" + intermediatePosition.toString());

			if (localAddToTail) {
				if (!intermediatePosition.canAddToTail(changeToTail)) {
					return MoveStatus.MOVE_FAILED;
				}
				newTrainPosition = intermediatePosition.addToTail(changeToTail);
			} else {
				if (!intermediatePosition.canRemoveFromTail(changeToTail)) {
					return MoveStatus.MOVE_FAILED;
				}
				newTrainPosition = intermediatePosition.removeFromTail(changeToTail);
			}
			//System.out.println(
			//	"newTrainPosition=" + newTrainPosition.toString());
		} else {
			if (localAddToTail) {
				if (!oldTrainPosition.canRemoveFromTail(changeToTail)) {
					return MoveStatus.MOVE_FAILED;
				}
				intermediatePosition = oldTrainPosition.addToTail(changeToTail);
			} else {
				if (!oldTrainPosition.canRemoveFromTail(changeToTail)) {
					return MoveStatus.MOVE_FAILED;
				}
				intermediatePosition = oldTrainPosition.removeFromTail(changeToTail);
			}
			//System.out.println(
			//	"intermediatePosition=" + intermediatePosition.toString());

			if (!intermediatePosition.canRemoveFromHead(changeToHead)) {
				return MoveStatus.MOVE_FAILED;
			}
			newTrainPosition = intermediatePosition.removeFromHead(changeToHead);
			//System.out.println(
			//	"newTrainPosition=" + newTrainPosition.toString());
		}
		if (updateTrainPosition) {
			train.setPosition(newTrainPosition);
		}

		return MoveStatus.MOVE_OK;
	}

	/*
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
		
		return null;
	}
	*/

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("ChangeTrainPositionMove:\n");
		sb.append("Bit to add = ");
		sb.append(changeToHead.toString());
		sb.append("\nBit to remove = ");
		sb.append(changeToTail.toString());
		sb.append("\nTrain no = ");
		sb.append(this.trainPositionNumber);
		return sb.toString();
	}

}
