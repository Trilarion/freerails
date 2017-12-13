package jfreerails.move;

import jfreerails.world.common.*;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.train.PathWalker;
import jfreerails.world.train.PathWalkerImpl;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainPositionOnMap;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;

/**
 * This Move changes the position of a train.
 * @author Luke Lindsay 22-Oct-2002
 *
 */
public class ChangeTrainPositionMove implements Move {
    private final TrainPositionOnMap changeToHead;
    private final TrainPositionOnMap changeToTail;
    private final boolean addToHead;
    private final boolean addToTail;
    final int trainPositionNumber;

    public FreerailsPrincipal getPrincipal() {
	return Player.NOBODY;
    }

    public ChangeTrainPositionMove(TrainPositionOnMap pieceToAdd,
        TrainPositionOnMap pieceToRemove, int trainNumber, boolean addToHead,
        boolean addToTail) {
        this.addToHead = addToHead;
        this.addToTail = addToTail;
        this.changeToHead = pieceToAdd;
        this.changeToTail = pieceToRemove;
        this.trainPositionNumber = trainNumber;
    }

    public static ChangeTrainPositionMove getNullMove(int trainNumber) {
        return new ChangeTrainPositionMove(null, null, trainNumber, false, false) {
                MoveStatus move(World w, boolean updateTrainPosition,
                    boolean isDoMove) {
                    return MoveStatus.MOVE_OK;
                }
            };
    }

    public static ChangeTrainPositionMove generate(ReadOnlyWorld w,
        FreerailsPathIterator nextPathSection, int trainNumber) {
        if (!nextPathSection.hasNext()) {
            return getNullMove(trainNumber);
        }

        TrainModel train = (TrainModel)w.get(KEY.TRAINS, trainNumber);

        TrainPositionOnMap currentPosition = train.getPosition();

        TrainPositionOnMap bitToAdd;
        TrainPositionOnMap intermediate;
        TrainPositionOnMap newPosition;
        TrainPositionOnMap bitToRemove;

        bitToAdd = getBitToAdd(nextPathSection);

        //We need to check that adding this piece will not make the train shorter.  E.g.
        //when a train turns around.
        TrainPositionOnMap tpom = currentPosition.addToHead(bitToAdd);
        double bitToAddDistance = bitToAdd.calulateDistance();
        double currentPositionDistance = currentPosition.calulateDistance();
        double combinedDistance = tpom.calulateDistance();

        if (bitToAddDistance + currentPositionDistance > combinedDistance) {
            FreerailsPathIterator temp = currentPosition.path();
            IntLine line = new IntLine();
            temp.nextSegment(line);

            int x = line.x1;
            int y = line.y1;
            TrainPositionOnMap extraBit = TrainPositionOnMap.createInstance(new int[] {
                        x, x, x
                    }, new int[] {y, y, y});
            bitToAdd = bitToAdd.addToTail(extraBit);
        }

        intermediate = currentPosition.addToHead(bitToAdd);

        double currentLength = (double)train.getLength();

        bitToRemove = getBitToRemove(intermediate, currentLength);

        return new ChangeTrainPositionMove(bitToAdd, bitToRemove, trainNumber,
            true, false);
    }

    static TrainPositionOnMap getBitToRemove(TrainPositionOnMap intermediate,
        double currentLength) {
        TrainPositionOnMap newPosition;
        TrainPositionOnMap bitToRemove;
        PathWalker pathWalker;
        pathWalker = new PathWalkerImpl(intermediate.path());

        pathWalker.stepForward((int)currentLength);

        newPosition = TrainPositionOnMap.createInSameDirectionAsPath(pathWalker);

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



            return new ChangeTrainPositionMove(bitToAdd, bitToRemove, trainNumber);
    }
    */
    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        boolean updateTrainPosition = true;
        boolean isDoMove = true;

        return move(w, updateTrainPosition, isDoMove, p);
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        boolean updateTrainPosition = true;
        boolean isDoMove = false;

        return move(w, updateTrainPosition, isDoMove, p);
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        boolean updateTrainPosition = false;
        boolean isDoMove = true;

        return move(w, updateTrainPosition, isDoMove, p);
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        boolean updateTrainPosition = false;
        boolean isDoMove = false;

        return move(w, updateTrainPosition, isDoMove, p);
    }

    MoveStatus move(World w, boolean updateTrainPosition, boolean isDoMove,
	    FreerailsPrincipal p) {
        boolean localAddToHead;
        boolean localAddToTail;

        if (isDoMove) {
            localAddToHead = this.addToHead;
            localAddToTail = this.addToTail;
        } else {
            localAddToHead = !this.addToHead;
            localAddToTail = !this.addToTail;
        }

        TrainModel train = (TrainModel)w.get(KEY.TRAINS,
                this.trainPositionNumber, p);
        TrainPositionOnMap oldTrainPosition = train.getPosition();
        TrainPositionOnMap intermediatePosition;
        TrainPositionOnMap newTrainPosition;

        if (localAddToHead) {
            if (!oldTrainPosition.canAddToHead(changeToHead)) {
                return MoveStatus.MOVE_FAILED;
            }

            intermediatePosition = oldTrainPosition.addToHead(changeToHead);

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

            if (!intermediatePosition.canRemoveFromHead(changeToHead)) {
                return MoveStatus.MOVE_FAILED;
            }

            newTrainPosition = intermediatePosition.removeFromHead(changeToHead);
        }

        if (updateTrainPosition) {
            train.setPosition(newTrainPosition);
        }

        return MoveStatus.MOVE_OK;
    }

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
