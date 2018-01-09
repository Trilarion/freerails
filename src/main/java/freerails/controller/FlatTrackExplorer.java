/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.controller;

import freerails.util.Point2D;
import freerails.world.train.PositionOnTrack;
import freerails.world.ReadOnlyWorld;
import freerails.world.terrain.TileTransition;
import freerails.world.terrain.FullTerrainTile;
import freerails.world.track.NullTrackType;
import freerails.world.track.TrackConfiguration;
import freerails.world.track.TrackPiece;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * GraphExplorer that explorers track, the ints it returns are encoded
 * PositionOnTrack objects.
 */
public class FlatTrackExplorer implements GraphExplorer, Serializable {
    private static final long serialVersionUID = 3834311713465185081L;
    final PositionOnTrack currentBranch = PositionOnTrack.createComingFrom(0,
            0, TileTransition.NORTH);
    private final ReadOnlyWorld w;
    private PositionOnTrack currentPosition = PositionOnTrack.createComingFrom(
            0, 0, TileTransition.NORTH);
    private boolean beforeFirst = true;

    /**
     * @param world
     * @param p
     * @throws NoTrackException
     */
    public FlatTrackExplorer(ReadOnlyWorld world, PositionOnTrack p)
            throws NoTrackException {
        w = world;
        FullTerrainTile tile = (FullTerrainTile) world.getTile(p.getX(), p.getY());
        if (tile.getTrackPiece().getTrackTypeID() == NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER) {
            throw new NoTrackException(p.toString());
        }

        this.currentPosition = PositionOnTrack.createComingFrom(p.getX(), p
                .getY(), p.cameFrom());
    }

    /**
     * @param w
     * @param p location of track to consider.
     * @return an array of PositionOnTrack objects describing the set of
     * possible orientations at this position (heading towards the
     * center of the tile)
     */
    public static PositionOnTrack[] getPossiblePositions(ReadOnlyWorld w,
                                                         Point2D p) {
        TrackPiece tp = ((FullTerrainTile) w.getTile(p.x, p.y)).getTrackPiece();
        TrackConfiguration conf = tp.getTrackConfiguration();
        TileTransition[] vectors = TileTransition.getList();

        // Count the number of possible positions.
        int n = 0;

        for (TileTransition vector1 : vectors) {
            if (conf.contains(vector1.get9bitTemplate())) {
                n++;
            }
        }

        PositionOnTrack[] possiblePositions = new PositionOnTrack[n];

        n = 0;

        for (TileTransition vector : vectors) {
            if (conf.contains(vector.get9bitTemplate())) {
                possiblePositions[n] = PositionOnTrack.createComingFrom(p.x,
                        p.y, vector.getOpposite());
                n++;
            }
        }

        return possiblePositions;
    }

    /**
     * @return
     */
    public ReadOnlyWorld getWorld() {
        return w;
    }

    public int getPosition() {
        return this.currentPosition.toInt();
    }

    /**
     * @param i
     */
    public void setPosition(int i) {
        beforeFirst = true;
        currentPosition.setValuesFromInt(i);
    }

    public void moveForward() {
        if (beforeFirst) {
            throw new IllegalStateException();
        }
        this.setPosition(this.getVertexConnectedByEdge());
    }

    public void nextEdge() {
        if (!hasNextEdge()) {
            throw new NoSuchElementException();
        }
        TileTransition v = this.getFirstVectorToTry();
        java.awt.Point p = new java.awt.Point(currentPosition.getX(), currentPosition.getY());
        FullTerrainTile ft = (FullTerrainTile) w.getTile(p.x, p.y);
        TrackPiece tp = ft.getTrackPiece();
        TrackConfiguration conf = tp.getTrackConfiguration();
        TileTransition[] vectors = TileTransition.getList();

        int i = v.getID();

        int loopCounter = 0;

        while (!conf.contains(vectors[i].get9bitTemplate())) {
            i++;
            i = i % 8;
            loopCounter++;

            if (8 < loopCounter) {
                throw new IllegalStateException();
                // This should never happen.. ..but it does happen when you
                // removed the track from under a train.
            }
        }

        TileTransition branchDirection = TileTransition.getInstance(i);
        this.currentBranch.setCameFrom(branchDirection);

        int x = this.currentPosition.getX() + branchDirection.deltaX;
        int y = this.currentPosition.getY() + branchDirection.deltaY;

        this.currentBranch.setX(x);
        this.currentBranch.setY(y);

        beforeFirst = false;
    }

    public int getVertexConnectedByEdge() {
        return currentBranch.toInt();
    }

    public int getEdgeCost() {
        return (int) Math.round(currentBranch.cameFrom().getLength());
    }

    /**
     * @return
     */
    public boolean hasNextEdge() {
        if (beforeFirst) {
            // We can always go back the way we have come, so if we are before
            // the first
            // branch, there must be a branch: the one we used to get here.
            return true;
        }
        // Since we can always go back the way we have come, if the direction of
        // current branch is not equal to the opposite of the current direction,
        // there must be another branch.
        TileTransition currentBranchDirection = this.currentBranch.cameFrom();
        TileTransition oppositeToCurrentDirection = this.currentPosition.cameFrom()
                .getOpposite();

        return oppositeToCurrentDirection.getID() != currentBranchDirection
                .getID();
    }

    TileTransition getFirstVectorToTry() {
        if (beforeFirst) {
            // Return the vector that is 45 degrees clockwise from the opposite
            // of the current position.
            TileTransition v = this.currentPosition.cameFrom();
            v = v.getOpposite();

            int i = v.getID();
            i++;
            i = i % 8;
            v = TileTransition.getInstance(i);

            return v;
        }
        // Return the vector that is 45 degrees clockwise from the direction
        // of the current branch.
        TileTransition v = this.currentBranch.cameFrom();
        int i = v.getID();
        i++;
        i = i % 8;
        v = TileTransition.getInstance(i);

        return v;
    }

}