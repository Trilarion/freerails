package jfreerails.move;

import java.awt.Point;
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
    private final TrainPositionOnMap m_changeToHead;
    private final TrainPositionOnMap m_changeToTail;
    private final boolean m_addToHead;
    private final boolean m_addToTail;
    private final int m_trainPositionNumber;
    private final FreerailsPrincipal m_principal;

    public ChangeTrainPositionMove(TrainPositionOnMap pieceToAdd,
        TrainPositionOnMap pieceToRemove, int trainNumber, boolean addToHead,
        boolean addToTail, FreerailsPrincipal p) {
        m_addToHead = addToHead;
        m_addToTail = addToTail;
        m_changeToHead = pieceToAdd;
        m_changeToTail = pieceToRemove;
        m_trainPositionNumber = trainNumber;
        m_principal = p;
    }

    public /*=const*/ Point newHead() {
        return new Point(m_changeToHead.getX(0), m_changeToHead.getY(0));
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
        FreerailsPrincipal p) throws PreMoveException {
        try {
            if (!nextPathSection.hasNext()) {
                return getNullMove(trainNumber, p);
            }

            TrainModel train = (TrainModel)w.get(KEY.TRAINS, trainNumber, p);
            TrainPositionOnMap currentPosition = (TrainPositionOnMap)w.get(KEY.TRAIN_POSITIONS,
                    trainNumber, p);

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

            double currentLength = train.getLength();

            bitToRemove = getBitToRemove(intermediate, currentLength);

            return new ChangeTrainPositionMove(bitToAdd, bitToRemove,
                trainNumber, true, false, p);
        } catch (Exception e) {
        	//We end up here if the track under the train is removed.           
            throw new PreMoveException(e.getMessage());
        }
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
        return TrainPositionOnMap.createInSameDirectionAsPath(nextPathSection)
                                 .reverse();
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

    private MoveStatus move(World w, boolean updateTrainPosition,
        boolean isDoMove) {
        boolean localAddToHead;
        boolean localAddToTail;

        if (isDoMove) {
            localAddToHead = this.m_addToHead;
            localAddToTail = this.m_addToTail;
        } else {
            localAddToHead = !this.m_addToHead;
            localAddToTail = !this.m_addToTail;
        }

        TrainPositionOnMap oldTrainPosition = (TrainPositionOnMap)w.get(KEY.TRAIN_POSITIONS,
                this.m_trainPositionNumber, m_principal);

        TrainPositionOnMap intermediatePosition;
        TrainPositionOnMap newTrainPosition;

        if (localAddToHead) {
            if (!oldTrainPosition.canAddToHead(m_changeToHead)) {
                return MoveStatus.moveFailed(
                    "!oldTrainPosition.canAddToHead(changeToHead)");
            }

            intermediatePosition = oldTrainPosition.addToHead(m_changeToHead);

            if (localAddToTail) {
                if (!intermediatePosition.canAddToTail(m_changeToTail)) {
                    return MoveStatus.moveFailed(
                        "!intermediatePosition.canAddToTail(changeToTail)");
                }

                newTrainPosition = intermediatePosition.addToTail(m_changeToTail);
            } else {
                if (!intermediatePosition.canRemoveFromTail(m_changeToTail)) {
                    return MoveStatus.moveFailed(
                        "!intermediatePosition.canRemoveFromTail(changeToTail)");
                }

                newTrainPosition = intermediatePosition.removeFromTail(m_changeToTail);
            }
        } else {
            if (localAddToTail) {
                if (!oldTrainPosition.canRemoveFromTail(m_changeToTail)) {
                    return MoveStatus.moveFailed(
                        "!oldTrainPosition.canRemoveFromTail(changeToTail)");
                }

                intermediatePosition = oldTrainPosition.addToTail(m_changeToTail);
            } else {
                if (!oldTrainPosition.canRemoveFromTail(m_changeToTail)) {
                    return MoveStatus.moveFailed(
                        "!oldTrainPosition.canRemoveFromTail(changeToTail)");
                }

                intermediatePosition = oldTrainPosition.removeFromTail(m_changeToTail);
            }

            if (!intermediatePosition.canRemoveFromHead(m_changeToHead)) {
                return MoveStatus.moveFailed(
                    "!intermediatePosition.canRemoveFromHead(changeToHead)");
            }

            newTrainPosition = intermediatePosition.removeFromHead(m_changeToHead);
        }

        if (updateTrainPosition) {
            w.set(KEY.TRAIN_POSITIONS, this.m_trainPositionNumber,
                newTrainPosition, m_principal);
        }

        return MoveStatus.MOVE_OK;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("ChangeTrainPositionMove:\n");
        sb.append("Bit to add = ");
        sb.append(m_changeToHead.toString());
        sb.append("\nBit to remove = ");
        sb.append(m_changeToTail.toString());
        sb.append("\nTrain no = ");
        sb.append(this.m_trainPositionNumber);

        return sb.toString();
    }
}