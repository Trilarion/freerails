/*
 * Created on Aug 22, 2004
 *
 */
package jfreerails.controller;

import java.awt.Point;
import java.util.NoSuchElementException;

import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.common.PositionOnTrack;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.NullTrackType;
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
    private boolean m_beforeFirst = true;
    final PositionOnTrack m_currentBranch = PositionOnTrack.createComingFrom(0, 0, OneTileMoveVector.NORTH);
    final private PositionOnTrack m_currentPosition = PositionOnTrack.createComingFrom(0, 0, OneTileMoveVector.NORTH);
    private int m_direction = 0;
    private final Point m_target;       
    private BuildTrackStrategy m_buildTrackStrategy;
    private boolean m_usingExistingTrack = false;
    private final ReadOnlyWorld m_world;    
    private final FreerailsPrincipal m_principle;

    public BuildTrackExplorer(ReadOnlyWorld w, FreerailsPrincipal principle) {
        this(w, principle, null, new Point(0, 0));
    }

    public BuildTrackExplorer(ReadOnlyWorld w, FreerailsPrincipal principle, Point start, Point target) {
        m_world = w;
        m_principle = principle;
        PositionOnTrack pos;

        if (null == start) {
            pos = new PositionOnTrack();
        } else {
            pos = PositionOnTrack.createComingFrom(start.x, start.y, OneTileMoveVector.NORTH);
        }

        m_currentPosition.setValuesFromInt(pos.toInt());
        m_direction = 0;
        m_target = target;
        m_buildTrackStrategy = BuildTrackStrategy.getDefault(w);
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
        //Check that we are not doubling back on ourselves.
        OneTileMoveVector opposite2current = m_currentPosition.cameFrom()
                                                              .getOpposite();
        int currentX = m_currentPosition.getX();
        int currentY = m_currentPosition.getY();
        int directionWeCameFrom = opposite2current.getID();
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
        
        
       
        TrackRule rule4nextTile;
        TrackRule rule4lastTile;

        //Determine the track rule for the next tile.
        final FreerailsTile nextTile = (FreerailsTile)m_world.getTile(newX, newY);
        
        //Check there is not another players track at nextTile.
        if(nextTile.getTrackTypeID() != NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER){
        	if(nextTile.getOwnerID() != m_world.getID(m_principle)){
        		return false;
        	}
        }
        
        
        rule4nextTile = getAppropriateTrackRule(newX, newY);
        
        if(null == rule4nextTile){
        	return false; //We can't build track on the tile.
        }

        rule4lastTile = getAppropriateTrackRule(currentX, currentY);
               
        if(null == rule4lastTile){
        	return false; //We can't build track on the tile.
        }
        //Determine the track rule for the current tile.
        FreerailsTile currentTile = (FreerailsTile)m_world.getTile(currentX,
                currentY);       

        //Check for illegal track configurations.
        final TrackConfiguration trackAlreadyPresent1 = currentTile.getTrackConfiguration();
        final TrackConfiguration trackAlreadyPresent2 = nextTile.getTrackConfiguration();
        TrackConfiguration fromConfig = trackAlreadyPresent1;

        fromConfig = TrackConfiguration.add(fromConfig, opposite2current);
        fromConfig = TrackConfiguration.add(fromConfig, TILE_CENTER);

        OneTileMoveVector goingTo = OneTileMoveVector.getInstance(m_direction);
        fromConfig = TrackConfiguration.add(fromConfig, goingTo);

        if (!rule4lastTile.trackPieceIsLegal(fromConfig)) {
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
        TrackConfiguration fromConfig2 = trackAlreadyPresent2;

        fromConfig2 = TrackConfiguration.add(fromConfig2, TILE_CENTER);

        OneTileMoveVector goingBack = OneTileMoveVector.getInstance(m_direction)
                                                       .getOpposite();
        fromConfig2 = TrackConfiguration.add(fromConfig2, goingBack);

        if (!rule4nextTile.trackPieceIsLegal(fromConfig2)) {
            return false;
        }

        /* Set the using existing track.  We do this because a path that uses existing track
         * is cheaper to build.
         */
        m_usingExistingTrack = trackAlreadyPresent1.contains(goingTo);

        return true;
    }

    private TrackRule getAppropriateTrackRule(int x, int y) {
    	 final FreerailsTile tile = (FreerailsTile)m_world.getTile(x, y);
		TrackRule rule;
		if (tile.getTrackRule().equals(NullTrackType.getInstance())) {           
            int terrainTypeID = tile.getTerrainTypeID();
            int trackRuleID = m_buildTrackStrategy.getRule(terrainTypeID);
            if(trackRuleID == -1){
            	return null; //Can't build on this terrain!
            }
            rule = (TrackRule)m_world.get(SKEY.TRACK_RULES,
            		trackRuleID);

        } else {
           rule = tile.getTrackRule();
        }
		return rule;
	}

    /** Calculates a cost figure incorporating the distance and the cost of any new track.*/ 
	public int getEdgeCost() {
        if (m_beforeFirst) {
            throw new IllegalStateException();
        }
		OneTileMoveVector edgeDirection = OneTileMoveVector.getInstance(m_direction -
		        1);
		int length = edgeDirection.getLength();                       
		final int DISTANCE_COST = 10000;  //Same as the cost of standard track.           
		int cost = DISTANCE_COST * length;
		
		if (!m_usingExistingTrack) {
			int[] x = {m_currentPosition.getX(), m_currentPosition.getX()+ edgeDirection.deltaX};
			int[] y = {m_currentPosition.getY(), m_currentPosition.getY() + edgeDirection.deltaY};
			TrackRule ruleA = getAppropriateTrackRule(x[0], y[0]);
			TrackRule ruleB = getAppropriateTrackRule(x[1], y[1]);
			/* If there is a station at either of the points, don't include its
			 * price in the cost calculation since it has already been paid.  Otherwise,
			 * add the cost of building the track.
			 */
			long priceA = ruleA.getPrice().getAmount();    			    			
			long priceB = ruleB.getPrice().getAmount();  
			cost += length*(priceA + priceB);
			//Add fixed cost if tile b does not have the desired track type.
			FreerailsTile a = (FreerailsTile)m_world.getTile(x[0], y[0]);
			TrackRule currentRuleA = a.getTrackRule();
			if(!currentRuleA.equals(ruleA)){
				assert( !currentRuleA.isStation()); //We shouldn't be upgrading a station. 
				cost+= ruleA.getFixedCost().getAmount() * OneTileMoveVector.TILE_DIAMETER;
			}
		}                        			
		return cost;
    }

    public int getH() {
        int xDistance = (m_target.x - m_currentPosition.getX()) * OneTileMoveVector.TILE_DIAMETER;
        int yDistance = (m_target.y - m_currentPosition.getY()) * OneTileMoveVector.TILE_DIAMETER;
        int sumOfSquares = (xDistance * xDistance + yDistance * yDistance);

        return (int)Math.sqrt(sumOfSquares);
    }

    public int getPosition() {
        return m_currentPosition.toInt();
    }
   

    public int getVertexConnectedByEdge() {
        if (m_beforeFirst) {
            throw new IllegalStateException();
        }
		return m_currentBranch.toInt();
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
        }
		setPosition(this.getVertexConnectedByEdge());
    }

    public void nextEdge() {
        if (!hasNextEdge()) {
            throw new NoSuchElementException();
        }
		//The direction we are moving relative to the current position.
		OneTileMoveVector direction = OneTileMoveVector.getInstance(m_direction);

		m_currentBranch.setDirection(direction);
		m_currentBranch.setX(m_currentPosition.getX() + direction.getDx());
		m_currentBranch.setY(m_currentPosition.getY() + direction.getDy());

		m_direction++;
		m_beforeFirst = false;
    }

    public void setPosition(int vertex) {
        m_currentPosition.setValuesFromInt(vertex);
        m_direction = 0;
    }
  
	public BuildTrackStrategy getBuildTrackStrategy() {
		return m_buildTrackStrategy;
	}
	public void setBuildTrackStrategy(BuildTrackStrategy trackStrategy) {
		if(null == trackStrategy) throw new NullPointerException();
		m_buildTrackStrategy = trackStrategy;
	}
}