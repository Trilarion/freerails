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

package freerails.model.track.explorer;

import freerails.model.track.NoTrackException;
import freerails.util.Vec2D;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TileTransition;
import freerails.model.track.TrackConfiguration;
import freerails.model.track.TrackPiece;
import freerails.model.train.PositionOnTrack;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * GraphExplorer that explorers track, the ints it returns are encoded
 * PositionOnTrack objects.
 */
public class FlatTrackExplorer implements GraphExplorer, Serializable {

    private static final long serialVersionUID = 3834311713465185081L;
    public final PositionOnTrack currentBranch = new PositionOnTrack(Vec2D.ZERO, TileTransition.NORTH);
    private final UnmodifiableWorld world;
    private PositionOnTrack currentPosition;
    private boolean beforeFirst = true;

    /**
     * @param world
     * @param positionOnTrack
     * @throws NoTrackException
     */
    public FlatTrackExplorer(UnmodifiableWorld world, PositionOnTrack positionOnTrack) throws NoTrackException {
        this.world = world;
        TerrainTile tile = world.getTile(positionOnTrack.getLocation());
        if (tile.getTrackPiece() == null) {
            throw new NoTrackException(positionOnTrack.toString());
        }

        currentPosition = new PositionOnTrack(positionOnTrack.getLocation(), positionOnTrack.getComingFrom());
    }

    /**
     * @param location location of track to consider.
     * @return an array of PositionOnTrack objects describing the set of
     * possible orientations at this position (heading towards the
     * center of the tile)
     */
    public static PositionOnTrack[] getPossiblePositions(UnmodifiableWorld world, Vec2D location) {
        TrackPiece trackPiece = world.getTile(location).getTrackPiece();
        TrackConfiguration conf = trackPiece.getTrackConfiguration();
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
                possiblePositions[n] = new PositionOnTrack(location, vector.getOpposite());
                n++;
            }
        }

        return possiblePositions;
    }

    @Override
    public int getPosition() {
        return currentPosition.toInt();
    }

    /**
     * @param vertex
     */
    @Override
    public void setPosition(int vertex) {
        beforeFirst = true;
        currentPosition.setValuesFromInt(vertex);
    }

    @Override
    public void moveForward() {
        if (beforeFirst) {
            throw new IllegalStateException();
        }
        setPosition(getVertexConnectedByEdge());
    }

    @Override
    public void nextEdge() {
        if (!hasNextEdge()) {
            throw new NoSuchElementException();
        }
        TileTransition v = getFirstVectorToTry();
        TerrainTile ft = world.getTile(currentPosition.getLocation());
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
        currentBranch.setComingFrom(branchDirection);
        currentBranch.setLocation(Vec2D.add(currentPosition.getLocation(), branchDirection.getD()));

        beforeFirst = false;
    }

    @Override
    public int getVertexConnectedByEdge() {
        return currentBranch.toInt();
    }

    @Override
    public int getEdgeCost() {
        return (int) Math.round(currentBranch.getComingFrom().getLength());
    }

    /**
     * @return
     */
    @Override
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
        TileTransition currentBranchDirection = currentBranch.getComingFrom();
        TileTransition oppositeToCurrentDirection = currentPosition.getComingFrom().getOpposite();

        return oppositeToCurrentDirection.getID() != currentBranchDirection.getID();
    }

    public TileTransition getFirstVectorToTry() {
        if (beforeFirst) {
            // Return the vector that is 45 degrees clockwise from the opposite
            // of the current position.
            TileTransition v = currentPosition.getComingFrom();
            v = v.getOpposite();

            int i = v.getID();
            i++;
            i = i % 8;
            v = TileTransition.getInstance(i);

            return v;
        }
        // Return the vector that is 45 degrees clockwise from the direction
        // of the current branch.
        TileTransition v = currentBranch.getComingFrom();
        int i = v.getID();
        i++;
        i = i % 8;
        v = TileTransition.getInstance(i);

        return v;
    }

}