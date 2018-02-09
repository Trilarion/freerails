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
import freerails.world.ReadOnlyWorld;
import freerails.world.terrain.FullTerrainTile;
import freerails.world.terrain.TileTransition;
import freerails.world.track.NullTrackType;
import freerails.world.track.TrackConfiguration;
import freerails.world.track.TrackPiece;
import freerails.world.train.PositionOnTrack;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * GraphExplorer that explorers track, the ints it returns are encoded
 * PositionOnTrack objects.
 */
public class FlatTrackExplorer implements GraphExplorer, Serializable {

    private static final long serialVersionUID = 3834311713465185081L;
    final PositionOnTrack currentBranch = PositionOnTrack.createComingFrom(Point2D.ZERO, TileTransition.NORTH);
    private final ReadOnlyWorld world;
    private PositionOnTrack currentPosition;
    private boolean beforeFirst = true;

    /**
     * @param world
     * @param positionOnTrack
     * @throws NoTrackException
     */
    public FlatTrackExplorer(ReadOnlyWorld world, PositionOnTrack positionOnTrack) throws NoTrackException {
        this.world = world;
        FullTerrainTile tile = (FullTerrainTile) world.getTile(positionOnTrack.getLocation());
        if (tile.getTrackPiece().getTrackTypeID() == NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER) {
            throw new NoTrackException(positionOnTrack.toString());
        }

        currentPosition = PositionOnTrack.createComingFrom(positionOnTrack.getLocation(), positionOnTrack.cameFrom());
    }

    /**
     * @param p location of track to consider.
     * @return an array of PositionOnTrack objects describing the set of
     * possible orientations at this position (heading towards the
     * center of the tile)
     */
    public static PositionOnTrack[] getPossiblePositions(ReadOnlyWorld w, Point2D p) {
        TrackPiece tp = ((FullTerrainTile) w.getTile(p)).getTrackPiece();
        TrackConfiguration conf = tp.getTrackConfiguration();
        TileTransition[] vectors = TileTransition.getTransitions();

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
                possiblePositions[n] = PositionOnTrack.createComingFrom(p, vector.getOpposite());
                n++;
            }
        }

        return possiblePositions;
    }

    public int getPosition() {
        return currentPosition.toInt();
    }

    /**
     * @param vertex
     */
    public void setPosition(int vertex) {
        beforeFirst = true;
        currentPosition.setValuesFromInt(vertex);
    }

    public void moveForward() {
        if (beforeFirst) {
            throw new IllegalStateException();
        }
        setPosition(getVertexConnectedByEdge());
    }

    public void nextEdge() {
        if (!hasNextEdge()) {
            throw new NoSuchElementException();
        }
        TileTransition v = getFirstVectorToTry();
        FullTerrainTile ft = (FullTerrainTile) world.getTile(currentPosition.getLocation());
        TrackPiece tp = ft.getTrackPiece();
        TrackConfiguration conf = tp.getTrackConfiguration();
        TileTransition[] vectors = TileTransition.getTransitions();

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
        currentBranch.setCameFrom(branchDirection);

        // TODO addition of two points
        int x = currentPosition.getLocation().x + branchDirection.deltaX;
        int y = currentPosition.getLocation().y + branchDirection.deltaY;
        currentBranch.setLocation(new Point2D(x, y));

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
        TileTransition currentBranchDirection = currentBranch.cameFrom();
        TileTransition oppositeToCurrentDirection = currentPosition.cameFrom().getOpposite();

        return oppositeToCurrentDirection.getID() != currentBranchDirection.getID();
    }

    TileTransition getFirstVectorToTry() {
        if (beforeFirst) {
            // Return the vector that is 45 degrees clockwise from the opposite
            // of the current position.
            TileTransition v = currentPosition.cameFrom();
            v = v.getOpposite();

            int i = v.getID();
            i++;
            i = i % 8;
            v = TileTransition.getInstance(i);

            return v;
        }
        // Return the vector that is 45 degrees clockwise from the direction
        // of the current branch.
        TileTransition v = currentBranch.cameFrom();
        int i = v.getID();
        i++;
        i = i % 8;
        v = TileTransition.getInstance(i);

        return v;
    }

}