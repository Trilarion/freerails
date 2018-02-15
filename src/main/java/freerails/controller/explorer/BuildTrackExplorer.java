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
package freerails.controller.explorer;

import freerails.controller.BuildTrackStrategy;
import freerails.util.Vector2D;
import freerails.util.Utils;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.SKEY;
import freerails.model.WorldConstants;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.terrain.FullTerrainTile;
import freerails.model.terrain.TileTransition;
import freerails.model.track.TrackConfiguration;
import freerails.model.track.TrackRule;
import freerails.model.train.PositionOnTrack;

import java.util.NoSuchElementException;

/**
 * GraphExplorer that explorers possible track placements, the int values it returns
 * are encoded PositionOnTrack objects.
 */
public class BuildTrackExplorer implements GraphExplorer {

    private static final TrackConfiguration TILE_CENTER = TrackConfiguration.getFlatInstance("000010000");
    private final PositionOnTrack currentBranch = PositionOnTrack.createComingFrom(Vector2D.ZERO, TileTransition.NORTH);
    private final PositionOnTrack currentPosition = PositionOnTrack.createComingFrom(Vector2D.ZERO, TileTransition.NORTH);
    private final ReadOnlyWorld world;
    private final FreerailsPrincipal principal;
    private boolean beforeFirst = true;
    private int directionInt = 0;
    private BuildTrackStrategy buildTrackStrategy;
    private boolean usingExistingTrack = false;

    /**
     * @param world
     * @param principal
     */
    public BuildTrackExplorer(ReadOnlyWorld world, FreerailsPrincipal principal) {
        this(world, principal, null);
    }

    /**
     * @param world
     * @param principal
     * @param start
     */
    public BuildTrackExplorer(ReadOnlyWorld world, FreerailsPrincipal principal, Vector2D start) {
        this.world = world;
        this.principal = principal;
        PositionOnTrack pos;

        if (null == start) {
            pos = new PositionOnTrack();
        } else {
            pos = PositionOnTrack.createComingFrom(start, TileTransition.NORTH);
        }

        currentPosition.setValuesFromInt(pos.toInt());
        directionInt = 0;
        buildTrackStrategy = BuildTrackStrategy.getDefault(world);
    }

    /**
     * Tests whether we can build track in the direction specified by
     * m_direction.
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
        Vector2D currentP = currentPosition.getLocation();
        int directionWeCameFrom = opposite2current.getID();
        int directionWeCameFromPlus = (directionWeCameFrom + 1) % 8;
        int directionWeCameFromMinus = (directionWeCameFrom + 7) % 8;

        if (directionInt == directionWeCameFrom || directionInt == directionWeCameFromPlus || directionInt == directionWeCameFromMinus) {
            return false;
        }

        // Check that we are not going off the map.
        TileTransition directionOfNextTile = TileTransition.getInstance(directionInt);
        Vector2D newP = Vector2D.add(currentP, directionOfNextTile.getD());

        if (!world.boundsContain(newP)) {
            return false;
        }

        TrackRule rule4nextTile;
        TrackRule rule4lastTile;

        // Determine the track rule for the next tile.
        final FullTerrainTile nextTile = (FullTerrainTile) world.getTile(newP);

        // Check there is not another players track at nextTile.
        if (nextTile.hasTrack()) {
            if (nextTile.getTrackPiece().getOwnerID() != world.getID(principal)) {
                return false;
            }
        }

        rule4nextTile = getAppropriateTrackRule(newP);

        if (null == rule4nextTile) {
            return false; // We can't build track on the tile.
        }

        rule4lastTile = getAppropriateTrackRule(currentP);

        if (null == rule4lastTile) {
            return false; // We can't build track on the tile.
        }
        // Determine the track rule for the current tile.
        FullTerrainTile currentTile = (FullTerrainTile) world.getTile(currentP);

        // Check for illegal track configurations.
        final TrackConfiguration trackAlreadyPresent1 = currentTile.getTrackPiece().getTrackConfiguration();
        final TrackConfiguration trackAlreadyPresent2 = nextTile.getTrackPiece().getTrackConfiguration();
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
            int y2check = currentP.y + directionOfNextTile.deltaY;

            // We did a bounds check above.
            assert (world.boundsContain(new Vector2D(currentP.x, y2check)));

            FullTerrainTile tile2Check = (FullTerrainTile) world.getTile(new Vector2D(currentP.x, y2check));
            TrackConfiguration config2check = tile2Check.getTrackPiece().getTrackConfiguration();
            TileTransition vector2check = TileTransition.getInstance(new Vector2D(directionOfNextTile.deltaX, -directionOfNextTile.deltaY));

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

    /**
     *
     * @param p
     * @return
     */
    private TrackRule getAppropriateTrackRule(Vector2D p) {
        final FullTerrainTile tile = (FullTerrainTile) world.getTile(p);
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
            Vector2D p = currentPosition.getLocation();
            int[] x = {p.x, p.x + edgeDirection.deltaX};
            int[] y = {p.y, p.y + edgeDirection.deltaY};
            TrackRule ruleA = getAppropriateTrackRule(new Vector2D(x[0], y[0]));
            TrackRule ruleB = getAppropriateTrackRule(new Vector2D(x[1], y[1]));
            /*
             * If there is a station at either of the points, don't include its
             * price in the cost calculation since it has already been paid.
             * Otherwise, add the cost of building the track.
             */
            // TODO use Money arithmetics
            long priceA = ruleA.getPrice().amount;
            long priceB = ruleB.getPrice().amount;
            cost += length * (priceA + priceB);
            // Add fixed cost if tile b does not have the desired track type.
            FullTerrainTile a = (FullTerrainTile) world.getTile(new Vector2D(x[0], y[0]));
            TrackRule currentRuleA = a.getTrackPiece().getTrackRule();
            if (!currentRuleA.equals(ruleA)) {
                assert (!currentRuleA.isStation()); // We shouldn't be upgrading
                // a station.
                cost += ruleA.getFixedCost().amount * WorldConstants.TILE_SIZE;
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
        setPosition(getVertexConnectedByEdge());
    }

    public void nextEdge() {
        if (!hasNextEdge()) {
            throw new NoSuchElementException();
        }
        // The direction we are moving relative to the current position.
        TileTransition direction = TileTransition.getInstance(directionInt);

        currentBranch.setCameFrom(direction);
        currentBranch.setLocation(Vector2D.add(currentPosition.getLocation(), direction.getD()));

        directionInt++;
        beforeFirst = false;
    }

    /**
     * @param trackStrategy
     */
    public void setBuildTrackStrategy(BuildTrackStrategy trackStrategy) {
        buildTrackStrategy = Utils.verifyNotNull(trackStrategy);
    }
}