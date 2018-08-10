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
package freerails.model.track.explorer;

import freerails.model.track.*;
import freerails.util.Vec2D;
import freerails.util.Utils;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.ModelConstants;
import freerails.model.player.Player;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TileTransition;
import freerails.model.train.PositionOnTrack;

import java.util.NoSuchElementException;

/**
 * GraphExplorer that explorers possible track placements, the int values it returns
 * are encoded PositionOnTrack objects.
 */
public class BuildTrackExplorer implements GraphExplorer {

    private static final TrackConfiguration TILE_CENTER = TrackConfiguration.getFlatInstance("000010000");
    private final PositionOnTrack currentBranch = PositionOnTrack.createComingFrom(Vec2D.ZERO, TileTransition.NORTH);
    private final PositionOnTrack currentPosition = PositionOnTrack.createComingFrom(Vec2D.ZERO, TileTransition.NORTH);
    private final UnmodifiableWorld world;
    private final Player player;
    private boolean beforeFirst = true;
    private int directionInt = 0;
    private BuildTrackStrategy buildTrackStrategy;
    private boolean usingExistingTrack = false;

    /**
     * @param world
     * @param player
     */
    public BuildTrackExplorer(UnmodifiableWorld world, Player player) {
        this(world, player, null);
    }

    /**
     * @param world
     * @param player
     * @param start
     */
    public BuildTrackExplorer(UnmodifiableWorld world, Player player, Vec2D start) {
        this.world = world;
        this.player = player;
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
     * Tests whether we can build track in the direction specified by m_direction.
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
        Vec2D currentP = currentPosition.getLocation();
        int directionWeCameFrom = opposite2current.getID();
        int directionWeCameFromPlus = (directionWeCameFrom + 1) % 8;
        int directionWeCameFromMinus = (directionWeCameFrom + 7) % 8;

        if (directionInt == directionWeCameFrom || directionInt == directionWeCameFromPlus || directionInt == directionWeCameFromMinus) {
            return false;
        }

        // Check that we are not going off the map.
        TileTransition directionOfNextTile = TileTransition.getInstance(directionInt);
        Vec2D newP = Vec2D.add(currentP, directionOfNextTile.getD());

        if (!world.boundsContain(newP)) {
            return false;
        }

        TrackType ruleForNextTile;
        TrackType ruleForLastTile;

        // Determine the track rule for the next tile.
        final TerrainTile nextTile = (TerrainTile) world.getTile(newP);

        // Check there is not another players track at nextTile.
        if (nextTile.hasTrack()) {
            if (nextTile.getTrackPiece().getOwnerID() != world.getID(player)) {
                return false;
            }
        }

        ruleForNextTile = getAppropriateTrackType(newP);

        if (null == ruleForNextTile) {
            return false; // We can't build track on the tile.
        }

        ruleForLastTile = getAppropriateTrackType(currentP);

        if (null == ruleForLastTile) {
            return false; // We can't build track on the tile.
        }
        // Determine the track rule for the current tile.
        TerrainTile currentTile = (TerrainTile) world.getTile(currentP);

        // Check for illegal track configurations.
        TrackPiece currentTileTrackPiece = currentTile.getTrackPiece();
        TrackPiece nextTileTrackPiece = nextTile.getTrackPiece();

        if (currentTileTrackPiece == null || nextTileTrackPiece == null) {
            return true;
        }

        final TrackConfiguration trackAlreadyPresent1 = currentTileTrackPiece.getTrackConfiguration();
        final TrackConfiguration trackAlreadyPresent2 = nextTileTrackPiece.getTrackConfiguration();
        TrackConfiguration fromConfig = trackAlreadyPresent1;

        fromConfig = TrackConfiguration.add(fromConfig, opposite2current);
        fromConfig = TrackConfiguration.add(fromConfig, TILE_CENTER);

        TileTransition goingTo = TileTransition.getInstance(directionInt);
        fromConfig = TrackConfiguration.add(fromConfig, goingTo);

        if (!ruleForLastTile.trackPieceIsLegal(fromConfig)) {
            return false;
        }

        // Check for diagonal conflicts.
        if (directionOfNextTile.isDiagonal()) {
            int y2check = currentP.y + directionOfNextTile.deltaY;

            // We did a bounds check above.
            assert (world.boundsContain(new Vec2D(currentP.x, y2check)));

            TerrainTile tileToCheck = (TerrainTile) world.getTile(new Vec2D(currentP.x, y2check));
            TrackPiece trackPiece = tileToCheck.getTrackPiece();
            if (trackPiece != null) {
                TrackConfiguration configToCheck = tileToCheck.getTrackPiece().getTrackConfiguration();
                TileTransition vectorToCheck = TileTransition.getInstance(new Vec2D(directionOfNextTile.deltaX, -directionOfNextTile.deltaY));

                if (configToCheck.contains(vectorToCheck)) {
                    // then we have a diagonal conflict.
                    return false;
                }
            }
        }

        // Check for illegal track configurations on the tile we are entering.
        TrackConfiguration fromConfig2 = trackAlreadyPresent2;

        fromConfig2 = TrackConfiguration.add(fromConfig2, TILE_CENTER);

        TileTransition goingBack = TileTransition.getInstance(directionInt).getOpposite();
        fromConfig2 = TrackConfiguration.add(fromConfig2, goingBack);

        if (!ruleForNextTile.trackPieceIsLegal(fromConfig2)) {
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
    private TrackType getAppropriateTrackType(Vec2D p) {
        final TerrainTile tile = (TerrainTile) world.getTile(p);
        TrackType type;
        if (!tile.hasTrack()) {
            int terrainTypeID = tile.getTerrainTypeId();
            int trackRuleID = buildTrackStrategy.getRule(terrainTypeID);
            if (trackRuleID == -1) {
                return null; // Can't build on this terrain!
            }
            // rule = (TrackRule) world.get(SharedKey.TrackRules, trackRuleID);
            type = world.getTrackType(trackRuleID);
        } else {
            type = tile.getTrackPiece().getTrackType();
        }
        return type;
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
            Vec2D p = currentPosition.getLocation();
            int[] x = {p.x, p.x + edgeDirection.deltaX};
            int[] y = {p.y, p.y + edgeDirection.deltaY};
            TrackType ruleA = getAppropriateTrackType(new Vec2D(x[0], y[0]));
            TrackType ruleB = getAppropriateTrackType(new Vec2D(x[1], y[1]));
            /*
             * If there is a station at either of the points, don't include its
             * price in the cost calculation since it has already been paid.
             * Otherwise, add the cost of building the track.
             */
            // TODO use Money arithmetic
            long priceA = ruleA.getPurchasingPrice().amount;
            long priceB = ruleB.getPurchasingPrice().amount;
            cost += length * (priceA + priceB);
            // Add fixed cost if tile b does not have the desired track type.
            TerrainTile a = (TerrainTile) world.getTile(new Vec2D(x[0], y[0]));
            TrackPiece trackPiece = a.getTrackPiece();
            if (trackPiece != null) {
                TrackType currentRuleA = a.getTrackPiece().getTrackType();
                if (!currentRuleA.equals(ruleA)) {
                    assert (!currentRuleA.isStation()); // We shouldn't be upgrading a station.
                    // TODO was getFixedCost(), meaning?,
                    cost += ruleA.getYearlyMaintenance().amount * ModelConstants.TILE_SIZE;
                }
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

    // TODO this is bad style because it changes the internal state, not expected for hasXXX functions
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
        currentBranch.setLocation(Vec2D.add(currentPosition.getLocation(), direction.getD()));

        directionInt++;
        beforeFirst = false;
    }

    /**
     * @param buildTrackStrategy
     */
    public void setBuildTrackStrategy(BuildTrackStrategy buildTrackStrategy) {
        this.buildTrackStrategy = Utils.verifyNotNull(buildTrackStrategy);
    }
}