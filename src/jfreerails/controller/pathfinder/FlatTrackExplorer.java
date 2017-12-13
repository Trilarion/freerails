package jfreerails.controller.pathfinder;

import java.awt.Point;
import java.util.NoSuchElementException;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.common.PositionOnTrack;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.track.TrackConfiguration;
import jfreerails.world.track.TrackPiece;


public class FlatTrackExplorer implements GraphExplorer, FreerailsSerializable {
    PositionOnTrack currentPosition = new PositionOnTrack(0, 0,
            OneTileMoveVector.NORTH);
    PositionOnTrack currentBranch = new PositionOnTrack(0, 0,
            OneTileMoveVector.NORTH);
    boolean beforeFirst = true;
    private ReadOnlyWorld w;

    public ReadOnlyWorld getWorld() {
        return w;
    }

    public void setPosition(int i) {
        beforeFirst = true;
        currentPosition.setValuesFromInt(i);
    }

    public int getPosition() {
        return this.currentPosition.toInt();
    }

    public void moveForward() {
        this.setPosition(this.getVertexConnectedByEdge());
    }

    public void nextEdge() {
        if (!hasNextEdge()) {
            throw new NoSuchElementException();
        } else {
            OneTileMoveVector v = this.getFirstVectorToTry();
            OneTileMoveVector lastToTry = this.currentPosition.getDirection()
                                                              .getOpposite();

            Point p = new Point(currentPosition.getX(), currentPosition.getY());
            TrackPiece tp = (TrackPiece)w.getTile(p.x, p.y);
            TrackConfiguration conf = tp.getTrackConfiguration();
            OneTileMoveVector[] vectors = OneTileMoveVector.getList();

            int i = v.getNumber();

            int loopCounter = 0;

            while (!conf.contains(vectors[i].getTemplate())) {
                i++;
                i = i % 8;
                loopCounter++;

                if (8 < loopCounter) {
                    throw new IllegalStateException();
                    //This should never happen..  ..but it does happen when you removed the track from under a train.
                }
            }

            OneTileMoveVector branchDirection = OneTileMoveVector.getInstance(i);
            this.currentBranch.setDirection(branchDirection);

            int x = this.currentPosition.getX() + branchDirection.deltaX;
            int y = this.currentPosition.getY() + branchDirection.deltaY;

            this.currentBranch.setX(x);
            this.currentBranch.setY(y);
        }

        beforeFirst = false;
    }

    public int getVertexConnectedByEdge() {
        return currentBranch.toInt();
    }

    public int getEdgeLength() {
        return currentBranch.getDirection().getLength();
    }

    public boolean hasNextEdge() {
        if (beforeFirst) {
            //We can always go back the way we have come, so if we are before the first
            //branch, there must be a branch: the one we used to get here.
            return true;
        } else {
            //Since we can always go back the way we have come, if the direction of 
            //current branch is not equal to the opposite of the current direction,
            //there must be another branch.
            OneTileMoveVector currentBranchDirection = this.currentBranch.getDirection();
            OneTileMoveVector oppositeToCurrentDirection = this.currentPosition.getDirection()
                                                                               .getOpposite();

            if (oppositeToCurrentDirection.getNumber() == currentBranchDirection.getNumber()) {
                return false;
            } else {
                return true;
            }
        }
    }

    public FlatTrackExplorer(ReadOnlyWorld world, PositionOnTrack p) {
        w = world;
        this.currentPosition = new PositionOnTrack(p.getX(), p.getY(),
                p.getDirection());
    }

    /******************************************************************************************/

    //scott bennett 15/03/03
    public FlatTrackExplorer(PositionOnTrack p, ReadOnlyWorld world) {
        this.currentPosition = new PositionOnTrack(p.getX(), p.getY(),
                p.getDirection());
        this.w = world;
    }

    /******************************************************************************************/
    /**
     * @return an array of PositionOnTrack objects describing the set of
     * possible orientations at this position (heading towards the center of
     * the tile)
     * @param p location of track to consider.
     */
    public static PositionOnTrack[] getPossiblePositions(ReadOnlyWorld w,
        Point p) {
        TrackPiece tp = (TrackPiece)w.getTile(p.x, p.y);
        TrackConfiguration conf = tp.getTrackConfiguration();
        OneTileMoveVector[] vectors = OneTileMoveVector.getList();

        //Count the number of possible positions.
        int n = 0;

        for (int i = 0; i < vectors.length; i++) {
            if (conf.contains(vectors[i].getTemplate())) {
                n++;
            }
        }

        PositionOnTrack[] possiblePositions = new PositionOnTrack[n];

        n = 0;

        for (int i = 0; i < vectors.length; i++) {
            if (conf.contains(vectors[i].getTemplate())) {
                possiblePositions[n] = new PositionOnTrack(p.x, p.y,
                        vectors[i].getOpposite());
                n++;
            }
        }

        return possiblePositions;
    }

    OneTileMoveVector getFirstVectorToTry() {
        if (beforeFirst) {
            //Return the vector that is 45 degrees clockwise from the oppposite 
            //of the current position.
            OneTileMoveVector v = this.currentPosition.getDirection();
            v = v.getOpposite();

            int i = v.getNumber();
            i++;
            i = i % 8;
            v = OneTileMoveVector.getInstance(i);

            return v;
        } else {
            //Return the vector that is 45 degrees clockwise from the direction  
            //of the current branch.
            OneTileMoveVector v = this.currentBranch.getDirection();
            int i = v.getNumber();
            i++;
            i = i % 8;
            v = OneTileMoveVector.getInstance(i);

            return v;
        }
    }
}