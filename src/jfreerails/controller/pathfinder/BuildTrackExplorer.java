/*
 * Created on Aug 22, 2004
 *
 */
package jfreerails.controller.pathfinder;

import java.awt.Point;
import java.util.NoSuchElementException;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.common.PositionOnTrack;
import jfreerails.world.terrain.TileTypeImpl;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.TrackConfiguration;
import jfreerails.world.track.TrackRule;


/**
 * GraphExplorer that explorers possible track placements, the ints it returns
 * are encoded PositionOnTrack objects.
 *
 * @author Luke
 *
 */
public class BuildTrackExplorer implements GraphExplorer {
    private static final TrackConfiguration TILE_CENTER = TrackConfiguration.getFlatInstance(
            "000010000");
    final private PositionOnTrack m_currentPosition = new PositionOnTrack(0, 0,
            OneTileMoveVector.NORTH);
    final PositionOnTrack m_currentBranch = new PositionOnTrack(0, 0,
            OneTileMoveVector.NORTH);
    private boolean m_usingExistingTrack = false;
    private boolean m_beforeFirst = true;
    private final ReadOnlyWorld m_world;
    private int m_direction = 0;
    private final Point m_target;

    public BuildTrackExplorer(ReadOnlyWorld w) {
        this(w, null, new Point(0, 0));
    }

    public BuildTrackExplorer(ReadOnlyWorld w, Point start, Point target) {
        m_world = w;

        PositionOnTrack pos;

        if (null == start) {
            pos = new PositionOnTrack();
        } else {
            pos = new PositionOnTrack(start.x, start.y, OneTileMoveVector.NORTH);
        }

        m_currentPosition.setValuesFromInt(pos.toInt());
        m_direction = 0;
        m_target = target;
    }

    public void setPosition(int vertex) {
        m_currentPosition.setValuesFromInt(vertex);
        m_direction = 0;
    }

    public int getPosition() {
        return m_currentPosition.toInt();
    }

    public void nextEdge() {
        if (!hasNextEdge()) {
            throw new NoSuchElementException();
        } else {
            //The direction we are moving relative to the current position.
            OneTileMoveVector direction = OneTileMoveVector.getInstance(m_direction);

            m_currentBranch.setDirection(direction);
            m_currentBranch.setX(m_currentPosition.getX() + direction.getDx());
            m_currentBranch.setY(m_currentPosition.getY() + direction.getDy());

            m_direction++;
            m_beforeFirst = false;
        }
    }

    public int getVertexConnectedByEdge() {
        if (m_beforeFirst) {
            throw new IllegalStateException();
        } else {
            return m_currentBranch.toInt();
        }
    }

    public int getEdgeLength() {
        if (m_beforeFirst) {
            throw new IllegalStateException();
        } else {
            OneTileMoveVector edgeDirection = OneTileMoveVector.getInstance(m_direction -
                    1);

            int length = edgeDirection.getLength();

            //Using existing track is cheaper.
            if (m_usingExistingTrack) {
                length = length / 2;
            }

            return length;
        }
    }

    public boolean hasNextEdge() {
        while (m_direction < 8) {
            if (canBuildTrack()) {
                return true;
            }

            m_direction++;
        }

        return false;
    }

    public void moveForward() {
        if (m_beforeFirst) {
            throw new IllegalStateException();
        } else {
            setPosition(this.getVertexConnectedByEdge());
        }
    }

    /**
     * <p>
     * Tests whether we can build track in the direction specifed by
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
        //Check that we are not doubling back on ourselves.
        OneTileMoveVector opposite2current = m_currentPosition.getDirection()
                                                              .getOpposite();
        int currentX = m_currentPosition.getX();
        int currentY = m_currentPosition.getY();
        int directionWeCameFrom = opposite2current.getNumber();
        int directionWeCameFromPlus = (directionWeCameFrom + 1) % 8;
        int directionWeCameFromMinus = (directionWeCameFrom + 7) % 8;

        if (m_direction == directionWeCameFrom ||
                m_direction == directionWeCameFromPlus ||
                m_direction == directionWeCameFromMinus) {
            return false;
        }

        //Check that we are not going off the map.
        OneTileMoveVector directionOfNextTile = OneTileMoveVector.getInstance(m_direction);

        int newX = currentX + directionOfNextTile.getDx();

        int newY = currentY + directionOfNextTile.getDy();

        if (!m_world.boundsContain(newX, newY)) {
            return false;
        }

        //Check that we can build track on the tile.
        FreerailsTile newTile = (FreerailsTile)m_world.getTile(newX, newY);
        int terrainTypeNumber = newTile.getTerrainTypeNumber();
        TileTypeImpl terrainType = (TileTypeImpl)m_world.get(SKEY.TERRAIN_TYPES,
                terrainTypeNumber);
        String category = terrainType.getTerrainCategory();

        //Always use track type 0 for now.
        TrackRule rule = (TrackRule)m_world.get(SKEY.TRACK_RULES, 0);

        if (!rule.canBuildOnThisTerrainType(category)) {
            return false;
        }

        //Check for illegal track configurations on the tile we are
        // leaving.
        FreerailsTile currentTile = (FreerailsTile)m_world.getTile(currentX,
                currentY);
        TrackConfiguration trackAlreadyPresent = currentTile.getTrackConfiguration();
        TrackConfiguration fromConfig = trackAlreadyPresent;

        fromConfig = TrackConfiguration.add(fromConfig, opposite2current);
        fromConfig = TrackConfiguration.add(fromConfig, TILE_CENTER);

        OneTileMoveVector goingTo = OneTileMoveVector.getInstance(m_direction);
        fromConfig = TrackConfiguration.add(fromConfig, goingTo);

        if (!rule.trackPieceIsLegal(fromConfig)) {
            return false;
        }

        //Check for diagonal conflicts.
        if (directionOfNextTile.isDiagonal()) {
            int x2check = currentX;
            int y2check = currentY + directionOfNextTile.deltaY;

            //We did a bounds check above.
            assert (m_world.boundsContain(x2check, y2check));

            FreerailsTile tile2Check = (FreerailsTile)m_world.getTile(x2check,
                    y2check);
            TrackConfiguration config2check = tile2Check.getTrackConfiguration();
            OneTileMoveVector vector2check = OneTileMoveVector.getInstance(directionOfNextTile.deltaX,
                    -directionOfNextTile.deltaY);

            if (config2check.contains(vector2check)) {
                //then we have a diagonal conflict.
                return false;
            }
        }

        //Check for illegal track configurations on the tile we are entering.
        FreerailsTile nextTile = (FreerailsTile)m_world.getTile(newX, newY);
        TrackConfiguration trackAlreadyPresent2 = nextTile.getTrackConfiguration();
        TrackConfiguration fromConfig2 = trackAlreadyPresent2;

        fromConfig2 = TrackConfiguration.add(fromConfig2, TILE_CENTER);

        OneTileMoveVector goingBack = OneTileMoveVector.getInstance(m_direction)
                                                       .getOpposite();
        fromConfig2 = TrackConfiguration.add(fromConfig2, goingBack);

        if (!rule.trackPieceIsLegal(fromConfig2)) {
            return false;
        }

        /* Set the using existing track track.  We do this becuase a path that uses existing track
         * is cheaper to build.
         */
        m_usingExistingTrack = trackAlreadyPresent.contains(goingTo);

        return true;
    }

    public int getH() {
        int xDistance = (m_target.x - m_currentPosition.getX()) * OneTileMoveVector.TILE_DIAMETER;
        int yDistance = (m_target.y - m_currentPosition.getY()) * OneTileMoveVector.TILE_DIAMETER;
        int sumOfSquares = (xDistance * xDistance + yDistance * yDistance);

        return (int)Math.sqrt((double)sumOfSquares);
    }
}