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

/*
 *
 */
package freerails.controller;

import freerails.util.ImPoint;
import freerails.world.PositionOnTrack;
import freerails.world.ReadOnlyWorld;
import freerails.world.SKEY;
import freerails.world.TileTransition;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.terrain.FreerailsTile;
import freerails.world.track.TrackConfiguration;
import freerails.world.track.TrackRule;

import java.util.NoSuchElementException;

/**
 * GraphExplorer that explorers possible track placements, the int values it returns
 * are encoded PositionOnTrack objects.
 */
public class BuildTrackExplorer implements GraphExplorer {
    private static final TrackConfiguration TILE_CENTER = TrackConfiguration
            .getFlatInstance("000010000");
    final PositionOnTrack currentBranch = PositionOnTrack.createComingFrom(0,
            0, TileTransition.NORTH);
    final private PositionOnTrack currentPosition = PositionOnTrack
            .createComingFrom(0, 0, TileTransition.NORTH);
    private final ReadOnlyWorld world;
    private final FreerailsPrincipal principle;
    private boolean beforeFirst = true;
    private int directionInt = 0;
    private BuildTrackStrategy buildTrackStrategy;
    private boolean usingExistingTrack = false;

    /**
     * @param w
     * @param principle
     */
    public BuildTrackExplorer(ReadOnlyWorld w, FreerailsPrincipal principle) {
        this(w, principle, null);
    }

    /**
     * @param w
     * @param principle
     * @param start
     */
    public BuildTrackExplorer(ReadOnlyWorld w, FreerailsPrincipal principle,
                              ImPoint start) {
        world = w;
        this.principle = principle;
        PositionOnTrack pos;

        if (null == start) {
            pos = new PositionOnTrack();
        } else {
            pos = PositionOnTrack
                    .createComingFrom(start.x, start.y, TileTransition.NORTH);
        }

        currentPosition.setValuesFromInt(pos.toInt());
        directionInt = 0;
        buildTrackStrategy = BuildTrackStrategy.getDefault(w);
    }

    /**
     * Tests whether we can build track in the direction specified by
     * m_direction.
     *
     *
     *
     * If we enter a tile from a given direction, the tiles we can build track
     * to depend on the following. (1) The terrain type of the surrounding tiles -
     * track can only be built on certain terrain types. (2) The direction we
     * entered the current tile from. (3) Any existing track on the current tile -
     * limits possible track configurations. (4) The terrain type of the current
     * tile - terrain type determines which track types and hence which track
     * configurations can be built.
     */
    private boolean canBuildTrack() {
        // Check that we are not doubling back on ourselves.
        TileTransition opposite2current = currentPosition.cameFrom().getOpposite();
        int currentX = currentPosition.getX();
        int currentY = currentPosition.getY();
        int directionWeCameFrom = opposite2current.getID();
        int directionWeCameFromPlus = (directionWeCameFrom + 1) % 8;
        int directionWeCameFromMinus = (directionWeCameFrom + 7) % 8;

        if (directionInt == directionWeCameFrom
                || directionInt == directionWeCameFromPlus
                || directionInt == directionWeCameFromMinus) {
            return false;
        }

        // Check that we are not going off the map.
        TileTransition directionOfNextTile = TileTransition.getInstance(directionInt);

        int newX = currentX + directionOfNextTile.getDx();

        int newY = currentY + directionOfNextTile.getDy();

        if (!world.boundsContain(newX, newY)) {
            return false;
        }

        TrackRule rule4nextTile;
        TrackRule rule4lastTile;

        // Determine the track rule for the next tile.
        final FreerailsTile nextTile = (FreerailsTile) world
                .getTile(newX, newY);

        // Check there is not another players track at nextTile.
        if (nextTile.hasTrack()) {
            if (nextTile.getTrackPiece().getOwnerID() != world.getID(principle)) {
                return false;
            }
        }

        rule4nextTile = getAppropriateTrackRule(newX, newY);

        if (null == rule4nextTile) {
            return false; // We can't build track on the tile.
        }

        rule4lastTile = getAppropriateTrackRule(currentX, currentY);

        if (null == rule4lastTile) {
            return false; // We can't build track on the tile.
        }
        // Determine the track rule for the current tile.
        FreerailsTile currentTile = (FreerailsTile) world.getTile(currentX,
                currentY);

        // Check for illegal track configurations.
        final TrackConfiguration trackAlreadyPresent1 = currentTile
                .getTrackPiece().getTrackConfiguration();
        final TrackConfiguration trackAlreadyPresent2 = nextTile
                .getTrackPiece().getTrackConfiguration();
        TrackConfiguration fromConfig = trackAlreadyPresent1;

        fromConfig = TrackConfiguration.add(fromConfig, opposite2current);
        fromConfig = TrackConfiguration.add(fromConfig, TILE_CENTER);

        TileTransition goingTo = TileTransition.getInstance(directionInt);
        fromConfig = TrackConfiguration.add(fromConfig, goingTo);

        if (!rule4lastTile.trackPieceIsLegal(fromConfig)) {
            return false;
        }

        // Check for diagonal conflicts.
        if (directionOfNextTile.isDiagonal()) {
            int y2check = currentY + directionOfNextTile.deltaY;

            // We did a bounds check above.
            assert (world.boundsContain(currentX, y2check));

            FreerailsTile tile2Check = (FreerailsTile) world.getTile(currentX,
                    y2check);
            TrackConfiguration config2check = tile2Check.getTrackPiece()
                    .getTrackConfiguration();
            TileTransition vector2check = TileTransition.getInstance(directionOfNextTile.deltaX,
                    -directionOfNextTile.deltaY);

            if (config2check.contains(vector2check)) {
                // then we have a diagonal conflict.
                return false;
            }
        }

        // Check for illegal track configurations on the tile we are entering.
        TrackConfiguration fromConfig2 = trackAlreadyPresent2;

        fromConfig2 = TrackConfiguration.add(fromConfig2, TILE_CENTER);

        TileTransition goingBack = TileTransition.getInstance(directionInt).getOpposite();
        fromConfig2 = TrackConfiguration.add(fromConfig2, goingBack);

        if (!rule4nextTile.trackPieceIsLegal(fromConfig2)) {
            return false;
        }

        /*
         * Set the using existing track. We do this because a path that uses
         * existing track is cheaper to build.
         */
        usingExistingTrack = trackAlreadyPresent1.contains(goingTo);

        return true;
    }

    private TrackRule getAppropriateTrackRule(int x, int y) {
        final FreerailsTile tile = (FreerailsTile) world.getTile(x, y);
        TrackRule rule;
        if (!tile.hasTrack()) {
            int terrainTypeID = tile.getTerrainTypeID();
            int trackRuleID = buildTrackStrategy.getRule(terrainTypeID);
            if (trackRuleID == -1) {
                return null; // Can't build on this terrain!
            }
            rule = (TrackRule) world.get(SKEY.TRACK_RULES, trackRuleID);

        } else {
            rule = tile.getTrackPiece().getTrackRule();
        }
        return rule;
    }

    /**
     * Calculates a cost figure incorporating the distance and the cost of any
     * new track.
     *
     * @return
     */
    public int getEdgeCost() {
        if (beforeFirst) {
            throw new IllegalStateException();
        }
        TileTransition edgeDirection = TileTransition.getInstance(directionInt - 1);
        double length = edgeDirection.getLength();
        final int DISTANCE_COST = 10000; // Same as the cost of standard
        // track.
        int cost = (int) Math.round(DISTANCE_COST * length);

        if (!usingExistingTrack) {
            int[] x = {currentPosition.getX(),
                    currentPosition.getX() + edgeDirection.deltaX};
            int[] y = {currentPosition.getY(),
                    currentPosition.getY() + edgeDirection.deltaY};
            TrackRule ruleA = getAppropriateTrackRule(x[0], y[0]);
            TrackRule ruleB = getAppropriateTrackRule(x[1], y[1]);
            /*
             * If there is a station at either of the points, don't include its
             * price in the cost calculation since it has already been paid.
             * Otherwise, add the cost of building the track.
             */
            long priceA = ruleA.getPrice().getAmount();
            long priceB = ruleB.getPrice().getAmount();
            cost += length * (priceA + priceB);
            // Add fixed cost if tile b does not have the desired track type.
            FreerailsTile a = (FreerailsTile) world.getTile(x[0], y[0]);
            TrackRule currentRuleA = a.getTrackPiece().getTrackRule();
            if (!currentRuleA.equals(ruleA)) {
                assert (!currentRuleA.isStation()); // We shouldn't be upgrading
                // a station.
                cost += ruleA.getFixedCost().getAmount() * TileTransition.TILE_DIAMETER;
            }
        }
        return cost;
    }

    public int getPosition() {
        return currentPosition.toInt();
    }

    /**
     * @param vertex
     */
    public void setPosition(int vertex) {
        currentPosition.setValuesFromInt(vertex);
        directionInt = 0;
    }

    public int getVertexConnectedByEdge() {
        if (beforeFirst) {
            throw new IllegalStateException();
        }
        return currentBranch.toInt();
    }

    /**
     * @return
     */
    public boolean hasNextEdge() {
        while (directionInt < 8) {
            if (canBuildTrack()) {
                return true;
            }

            directionInt++;
        }

        return false;
    }

    public void moveForward() {
        if (beforeFirst) {
            throw new IllegalStateException();
        }
        setPosition(this.getVertexConnectedByEdge());
    }

    public void nextEdge() {
        if (!hasNextEdge()) {
            throw new NoSuchElementException();
        }
        // The direction we are moving relative to the current position.
        TileTransition direction = TileTransition.getInstance(directionInt);

        currentBranch.setCameFrom(direction);
        currentBranch.setX(currentPosition.getX() + direction.getDx());
        currentBranch.setY(currentPosition.getY() + direction.getDy());

        directionInt++;
        beforeFirst = false;
    }

    /**
     * @param trackStrategy
     */
    public void setBuildTrackStrategy(BuildTrackStrategy trackStrategy) {
        if (null == trackStrategy)
            throw new NullPointerException();
        buildTrackStrategy = trackStrategy;
    }
}