/*
 * Created on Aug 22, 2004
 *
 */
package freerails.controller;

import java.util.NoSuchElementException;

import freerails.world.common.ImPoint;
import freerails.world.common.PositionOnTrack;
import freerails.world.common.Step;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.SKEY;
import freerails.world.track.FreerailsTile;
import freerails.world.track.TrackConfiguration;
import freerails.world.track.TrackRule;

/**
 * GraphExplorer that explorers possible track placements, the ints it returns
 * are encoded PositionOnTrack objects.
 * 
 * @author Luke
 * 
 */
public class BuildTrackExplorer implements GraphExplorer {
    private static final TrackConfiguration TILE_CENTER = TrackConfiguration
            .getFlatInstance("000010000");

    private boolean beforeFirst = true;

    final PositionOnTrack currentBranch = PositionOnTrack.createComingFrom(0,
            0, Step.NORTH);

    final private PositionOnTrack currentPosition = PositionOnTrack
            .createComingFrom(0, 0, Step.NORTH);

    private int directionInt = 0;

    private final ImPoint target;

    private BuildTrackStrategy buildTrackStrategy;

    private boolean usingExistingTrack = false;

    private final ReadOnlyWorld world;

    private final FreerailsPrincipal principle;

    public BuildTrackExplorer(ReadOnlyWorld w, FreerailsPrincipal principle) {
        this(w, principle, null, new ImPoint(0, 0));
    }

    public BuildTrackExplorer(ReadOnlyWorld w, FreerailsPrincipal principle,
            ImPoint start, ImPoint target) {
        world = w;
        this.principle = principle;
        PositionOnTrack pos;

        if (null == start) {
            pos = new PositionOnTrack();
        } else {
            pos = PositionOnTrack
                    .createComingFrom(start.x, start.y, Step.NORTH);
        }

        currentPosition.setValuesFromInt(pos.toInt());
        directionInt = 0;
        this.target = target;
        buildTrackStrategy = BuildTrackStrategy.getDefault(w);
    }

    /**
     * <p>
     * Tests whether we can build track in the direction specified by
     * m_direction.
     * </p>
     * 
     * <p>
     * If we enter a tile from a given direction, the tiles we can build track
     * to depend on the following. (1) The terrain type of the surrounding tiles -
     * track can only be built on certain terrain types. (2) The direction we
     * entered the current tile from. (3) Any existing track on the current tile -
     * limits possible track configurations. (4) The terrain type of the current
     * tile - terrain type determines which track types and hence which track
     * configurations can be built.
     * </p>
     * 
     */
    private boolean canBuildTrack() {
        // Check that we are not doubling back on ourselves.
        Step opposite2current = currentPosition.cameFrom().getOpposite();
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
        Step directionOfNextTile = Step.getInstance(directionInt);

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

        Step goingTo = Step.getInstance(directionInt);
        fromConfig = TrackConfiguration.add(fromConfig, goingTo);

        if (!rule4lastTile.trackPieceIsLegal(fromConfig)) {
            return false;
        }

        // Check for diagonal conflicts.
        if (directionOfNextTile.isDiagonal()) {
            int x2check = currentX;
            int y2check = currentY + directionOfNextTile.deltaY;

            // We did a bounds check above.
            assert (world.boundsContain(x2check, y2check));

            FreerailsTile tile2Check = (FreerailsTile) world.getTile(x2check,
                    y2check);
            TrackConfiguration config2check = tile2Check.getTrackPiece()
                    .getTrackConfiguration();
            Step vector2check = Step.getInstance(directionOfNextTile.deltaX,
                    -directionOfNextTile.deltaY);

            if (config2check.contains(vector2check)) {
                // then we have a diagonal conflict.
                return false;
            }
        }

        // Check for illegal track configurations on the tile we are entering.
        TrackConfiguration fromConfig2 = trackAlreadyPresent2;

        fromConfig2 = TrackConfiguration.add(fromConfig2, TILE_CENTER);

        Step goingBack = Step.getInstance(directionInt).getOpposite();
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
     */
    public int getEdgeCost() {
        if (beforeFirst) {
            throw new IllegalStateException();
        }
        Step edgeDirection = Step.getInstance(directionInt - 1);
        double length = edgeDirection.getLength();
        final int DISTANCE_COST = 10000; // Same as the cost of standard
        // track.
        int cost = (int) Math.round(DISTANCE_COST * length);

        if (!usingExistingTrack) {
            int[] x = { currentPosition.getX(),
                    currentPosition.getX() + edgeDirection.deltaX };
            int[] y = { currentPosition.getY(),
                    currentPosition.getY() + edgeDirection.deltaY };
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
                cost += ruleA.getFixedCost().getAmount() * Step.TILE_DIAMETER;
            }
        }
        return cost;
    }

    public int getH() {
        int xDistance = (target.x - currentPosition.getX())
                * Step.TILE_DIAMETER;
        int yDistance = (target.y - currentPosition.getY())
                * Step.TILE_DIAMETER;
        int sumOfSquares = (xDistance * xDistance + yDistance * yDistance);

        return (int) Math.sqrt(sumOfSquares);
    }

    public int getPosition() {
        return currentPosition.toInt();
    }

    public int getVertexConnectedByEdge() {
        if (beforeFirst) {
            throw new IllegalStateException();
        }
        return currentBranch.toInt();
    }

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
        Step direction = Step.getInstance(directionInt);

        currentBranch.setCameFrom(direction);
        currentBranch.setX(currentPosition.getX() + direction.getDx());
        currentBranch.setY(currentPosition.getY() + direction.getDy());

        directionInt++;
        beforeFirst = false;
    }

    public void setPosition(int vertex) {
        currentPosition.setValuesFromInt(vertex);
        directionInt = 0;
    }

    public void setBuildTrackStrategy(BuildTrackStrategy trackStrategy) {
        if (null == trackStrategy)
            throw new NullPointerException();
        buildTrackStrategy = trackStrategy;
    }
}