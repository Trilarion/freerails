package jfreerails.controller;

import java.awt.Point;
import java.util.Stack;

import jfreerails.move.ChangeTrackPieceCompositeMove;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.move.TrackMoveTransactionsGenerator;
import jfreerails.move.UndoMove;
import jfreerails.move.UpgradeTrackMove;
import jfreerails.world.common.GameTime;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.NullTrackType;
import jfreerails.world.track.TrackPiece;
import jfreerails.world.track.TrackPieceImpl;
import jfreerails.world.track.TrackRule;

/**
 * Provides methods that generate moves that build, upgrade, and remove track.
 * 
 * @author Luke
 */
final public class TrackMoveProducer {
	private BuildTrackStrategy buildTrackStrategy;
	
	private final MoveExecutor executor;
	
	//TODO Replace ints with enum.
	public enum BuildMode{BUILD_TRACK, REMOVE_TRACK, UPGRADE_TRACK, IGNORE_TRACK, BUILD_STATION};
	
//	public final static int BUILD_TRACK = 1;
//
//	public final static int REMOVE_TRACK = 2;
//
//	public final static int UPGRADE_TRACK = 3;
//
//	/* Don't build any track */
//	public final static int IGNORE_TRACK = 4;

	private BuildMode buildMode = BuildMode.BUILD_TRACK;

	private final Stack<Move> moveStack = new Stack<Move>();

	private GameTime lastMoveTime = GameTime.BIG_BANG;

	/**
	 * This generates the transactions - the charge - for the track being built.
	 */
	private final TrackMoveTransactionsGenerator transactionsGenerator;
	
	public MoveStatus buildTrack(Point from, OneTileMoveVector[] path){		
		MoveStatus returnValue = MoveStatus.MOVE_OK;	
		int x = from.x;
		int y = from.y;
		for(int i = 0; i < path.length; i++){
			
			returnValue = buildTrack(new Point(x, y), path[i]);			
			x+= path[i].deltaX;
			y+= path[i].deltaY;
			if(!returnValue.ok){
				return returnValue;
			}
		}
		return returnValue;
	}

	public MoveStatus buildTrack(Point from, OneTileMoveVector trackVector) {
		
		ReadOnlyWorld w = executor.getWorld();
		FreerailsPrincipal principal = executor.getPrincipal();
		switch (buildMode) {
		case IGNORE_TRACK: {
			return MoveStatus.MOVE_OK;
		}
		case REMOVE_TRACK: {
			try {
				ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove
						.generateRemoveTrackMove(from, trackVector, w,
								principal);

				Move moveAndTransaction = transactionsGenerator
						.addTransactions(move);

				return sendMove(moveAndTransaction);
			} catch (Exception e) {				
				// thrown when there is no track to remove.
				// Fix for bug [ 948670 ] Removing non-existant track
				return MoveStatus.moveFailed("No track to remove.");
			}
		}
		case BUILD_TRACK:
		case UPGRADE_TRACK:
			/*
			 * Do nothing yet since we need to work out what type of track to
			 * build.
			 */
			break;

		}
		assert (buildMode == BuildMode.BUILD_TRACK || buildMode == BuildMode.UPGRADE_TRACK);

		int[] ruleIDs = new int[2];
		TrackRule[] rules = new TrackRule[2];
		int[] xs = { from.x, from.x + trackVector.deltaX};
		int[] ys = {from.y, from.y + trackVector.deltaY};
		for(int i = 0; i < ruleIDs.length ; i++){
			int x = xs[i];
			int y = ys[i];
			FreerailsTile tile = (FreerailsTile) w.getTile(x, y);
			int tt = tile.getTerrainTypeID();
			ruleIDs[i] = buildTrackStrategy.getRule(tt);

			if (ruleIDs[i] == -1) {
				TerrainType terrainType = (TerrainType) w.get(
						SKEY.TERRAIN_TYPES, tt);
				String message = "Non of the selected track types can be built on "
								+ terrainType.getDisplayName();
				return MoveStatus.moveFailed(message);
			}
			rules[i] = (TrackRule) w.get(SKEY.TRACK_RULES, ruleIDs[i]);
		}				

		switch (buildMode) {
		case UPGRADE_TRACK: {
			//upgrade the from tile if necessary.			
			FreerailsTile tileA = (FreerailsTile) w.getTile(from.x, from.y);
			if(tileA.getTrackTypeID() != ruleIDs[0]  && !isStationHere(from)){
				MoveStatus ms = upgradeTrack(new Point(from), ruleIDs[0]);
				if(!ms.ok){
					return ms;
				}				
			}
			Point point = new Point(from.x + trackVector.getDx(), from.y
					+ trackVector.getDy());					
			FreerailsTile tileB = (FreerailsTile) w.getTile(point.x, point.y);
			if(tileB.getTrackTypeID() != ruleIDs[1] && !isStationHere(point)){
				MoveStatus ms = upgradeTrack(point, ruleIDs[1]);
				if(!ms.ok){
					return ms;
				}				
			}					
			return MoveStatus.MOVE_OK;			
		}
		case BUILD_TRACK: {
			ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove.generateBuildTrackMove(from,
					trackVector, rules[0], rules[1], w, principal);

			Move moveAndTransaction = transactionsGenerator.addTransactions(move);

			return sendMove(moveAndTransaction);
		}	
		default:
			throw new IllegalArgumentException(String.valueOf(buildMode));
		}

		
	}

	public MoveStatus upgradeTrack(Point point) {
		if (buildMode == BuildMode.UPGRADE_TRACK) {
			ReadOnlyWorld w = executor.getWorld();
			FreerailsTile tile = (FreerailsTile) w.getTile(point.x, point.y);
			int tt = tile.getTerrainTypeID();
			return upgradeTrack(point, buildTrackStrategy.getRule(tt));
		}
		throw new IllegalStateException(
				"Track builder not set to upgrade track!");
	}

	

	public void setTrackBuilderMode(BuildMode i) {
		buildMode = i;
	}

	public TrackMoveProducer(MoveExecutor executor, ReadOnlyWorld world) {
		this.executor = executor;

		FreerailsPrincipal principal = executor.getPrincipal();
		transactionsGenerator = new TrackMoveTransactionsGenerator(world,
				principal);
		buildTrackStrategy = BuildTrackStrategy.getDefault(world);
	}

	public TrackMoveProducer(MoveExecutor executor) {
		this.executor = executor;

		ReadOnlyWorld world = executor.getWorld();

		FreerailsPrincipal principal = executor.getPrincipal();
		transactionsGenerator = new TrackMoveTransactionsGenerator(world,
				principal);
		buildTrackStrategy = BuildTrackStrategy.getDefault(world);
	}

	private MoveStatus upgradeTrack(Point point, int trackRuleID) {
		ReadOnlyWorld w = executor.getWorld();
		TrackPiece before = (TrackPiece) w.getTile(point.x, point.y);
		/* Check whether there is track here.*/
		if(before.getTrackTypeID() == NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER){
			return MoveStatus
			.moveFailed("No track to upgrade.");
		}
		 
		FreerailsPrincipal principal = executor.getPrincipal();
		int owner = ChangeTrackPieceCompositeMove.getOwner(principal, w);
		TrackRule trackRule = (TrackRule) w.get(SKEY.TRACK_RULES, trackRuleID);
		TrackPiece after = new TrackPieceImpl(before.getTrackConfiguration(),
				trackRule, owner, trackRuleID);

		/* We don't want to 'upgrade' a station to track. See bug 874416. */
		if (before.getTrackRule().isStation()) {
			return MoveStatus
					.moveFailed("No need to upgrade track at station.");
		}

		Move move = UpgradeTrackMove.generateMove(before, after, point);
		Move move2 = transactionsGenerator.addTransactions(move);

		return sendMove(move2);
	}

	public MoveStatus undoLastTrackMove() {
		clearStackIfStale();

		if (moveStack.size() > 0) {
			Move m = moveStack.pop();
			UndoMove undoMove = new UndoMove(m);
			MoveStatus ms = executor.doMove(undoMove);

			if (!ms.ok) {
				return MoveStatus.moveFailed("Can not undo building track!");
			}
			return ms;
		}
		return MoveStatus.moveFailed("No track to undo building!");
	}

	/**
	 * Moves are only un-doable if no game time has passed since they they were
	 * executed. This method clears the move stack if the moves were added to
	 * the stack at a time other than the current time.
	 */
	private void clearStackIfStale() {
		ReadOnlyWorld w = executor.getWorld();
		GameTime currentTime = (GameTime) w.get(ITEM.TIME);

		if (!currentTime.equals(lastMoveTime)) {
			moveStack.clear();
			lastMoveTime = currentTime;
		}
	}

	public BuildMode getTrackBuilderMode() {
		return buildMode;
	}

	private MoveStatus sendMove(Move m) {
		MoveStatus ms = executor.doMove(m);

		if (ms.isOk()) {
			clearStackIfStale();
			moveStack.add(m);
		}

		return ms;
	}
	public BuildTrackStrategy getBuildTrackStrategy() {
		return buildTrackStrategy;
	}
	public void setBuildTrackStrategy(BuildTrackStrategy buildTrackStrategy) {
		this.buildTrackStrategy = buildTrackStrategy;
	}
	
	
	private boolean isStationHere(Point p){		
		ReadOnlyWorld w = executor.getWorld();
		FreerailsTile tile = (FreerailsTile) w.getTile(p.x, p.y);
		return tile.getTrackRule().isStation();		
	}

}
