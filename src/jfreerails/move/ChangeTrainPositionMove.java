package jfreerails.move;

import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.common.IntLine;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
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
    private final TrainPositionOnMap changeToHead;
    private final TrainPositionOnMap changeToTail;
    private final boolean addToHead;
    private final boolean addToTail;
    final int trainPositionNumber;
    private final FreerailsPrincipal principal;

    public ChangeTrainPositionMove(TrainPositionOnMap pieceToAdd,
        TrainPositionOnMap pieceToRemove, int trainNumber, boolean addToHead,
        boolean addToTail, FreerailsPrincipal p) {
        this.addToHead = addToHead;
        this.addToTail = addToTail;
        this.changeToHead = pieceToAdd;
        this.changeToTail = pieceToRemove;
        this.trainPositionNumber = trainNumber;
        this.principal = p;
    }

    public static ChangeTrainPositionMove getNullMove(int trainNumber,
        FreerailsPrincipal p) {
        return new ChangeTrainPositionMove(null, null, trainNumber, false,
            false, p) {
                MoveStatus move(World w, boolean updateTrainPosition,
                    boolean isDoMove) {
                    return MoveStatus.MOVE_OK;
                }
            };
    }

    public static ChangeTrainPositionMove generate(ReadOnlyWorld w,
        FreerailsPathIterator nextPathSection, int trainNumber,
        FreerailsPrincipal p) {
        if (!nextPathSection.hasNext()) {
            return getNullMove(trainNumber, p);
        }

        TrainModel train = (TrainModel)w.get(KEY.TRAINS, trainNumber, p);

        TrainPositionOnMap currentPosition = train.getPosition();

        TrainPositionOnMap bitToAdd;
        TrainPositionOnMap intermediate;
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
            true, false, p);
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

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        boolean updateTrainPosition = true;
        boolean isDoMove = true;

        return move(w, updateTrainPosition, isDoMove);
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        boolean updateTrainPosition = true;
        boolean isDoMove = false;

        return move(w, updateTrainPosition, isDoMove);
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        boolean updateTrainPosition = false;
        boolean isDoMove = true;

        return move(w, updateTrainPosition, isDoMove);
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        boolean updateTrainPosition = false;
        boolean isDoMove = false;

        return move(w, updateTrainPosition, isDoMove);
    }

    MoveStatus move(World w, boolean updateTrainPosition, boolean isDoMove) {
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
                this.trainPositionNumber, principal);
        TrainPositionOnMap oldTrainPosition = train.getPosition();
        TrainPositionOnMap intermediatePosition;
        TrainPositionOnMap newTrainPosition;

        if (localAddToHead) {
            if (!oldTrainPosition.canAddToHead(changeToHead)) {
                return MoveStatus.moveFailed(
                    "!oldTrainPosition.canAddToHead(changeToHead)");
            }

            intermediatePosition = oldTrainPosition.addToHead(changeToHead);

            if (localAddToTail) {
                if (!intermediatePosition.canAddToTail(changeToTail)) {
                    return MoveStatus.moveFailed(
                        "!intermediatePosition.canAddToTail(changeToTail)");
                }

                newTrainPosition = intermediatePosition.addToTail(changeToTail);
            } else {
                if (!intermediatePosition.canRemoveFromTail(changeToTail)) {
                    return MoveStatus.moveFailed(
                        "!intermediatePosition.canRemoveFromTail(changeToTail)");
                }

                newTrainPosition = intermediatePosition.removeFromTail(changeToTail);
            }
        } else {
            if (localAddToTail) {
                if (!oldTrainPosition.canRemoveFromTail(changeToTail)) {
                    return MoveStatus.moveFailed(
                        "!oldTrainPosition.canRemoveFromTail(changeToTail)");
                }

                intermediatePosition = oldTrainPosition.addToTail(changeToTail);
            } else {
                if (!oldTrainPosition.canRemoveFromTail(changeToTail)) {
                    return MoveStatus.moveFailed(
                        "!oldTrainPosition.canRemoveFromTail(changeToTail)");
                }

                intermediatePosition = oldTrainPosition.removeFromTail(changeToTail);
            }

            if (!intermediatePosition.canRemoveFromHead(changeToHead)) {
                return MoveStatus.moveFailed(
                    "!intermediatePosition.canRemoveFromHead(changeToHead)");
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