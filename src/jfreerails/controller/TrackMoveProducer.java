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

	public final static int BUILD_TRACK = 1;

	public final static int REMOVE_TRACK = 2;

	public final static int UPGRADE_TRACK = 3;

	/* Don't build any track */
	public final static int IGNORE_TRACK = 4;

	private int trackBuilderMode = BUILD_TRACK;

	private final Stack moveStack = new Stack();

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

		int ruleAID, ruleBID;
		TrackRule ruleA, ruleB;
		{
			int x = from.x;
			int y = from.y;
			FreerailsTile tile = (FreerailsTile) w.getTile(x, y);
			int tt = tile.getTerrainTypeID();
			ruleAID = buildTrackStrategy.getRule(tt);

			if (ruleAID == -1) {
				TerrainType terrainType = (TerrainType) w.get(
						SKEY.TERRAIN_TYPES, tt);
				return MoveStatus
						.moveFailed("Non of the selected track types can be built on "
								+ terrainType.getDisplayName());
			}
			ruleA = (TrackRule) w.get(SKEY.TRACK_RULES, ruleAID);
		}
		

		{
			int x = from.x + trackVector.deltaX;
			int y = from.y + trackVector.deltaY;
			FreerailsTile tile = (FreerailsTile) w.getTile(x, y);
			int tt = tile.getTerrainTypeID();
			ruleBID = buildTrackStrategy.getRule(tt);

			if (ruleBID == -1) {
				TerrainType terrainType = (TerrainType) w.get(
						SKEY.TERRAIN_TYPES, tt);
				return MoveStatus
						.moveFailed("Non of the selected track types can be built on "
								+ terrainType.getDisplayName());
			}
			ruleB = (TrackRule) w.get(SKEY.TRACK_RULES, ruleBID);
		}

		
		
		if (trackBuilderMode == UPGRADE_TRACK) {
			Point point = new Point(from.x + trackVector.getDx(), from.y
					+ trackVector.getDy());

			return upgradeTrack(point, ruleBID);
		}

		ChangeTrackPieceCompositeMove move = null;

		FreerailsPrincipal principal = executor.getPrincipal();

		switch (trackBuilderMode) {
		case BUILD_TRACK: {
			move = ChangeTrackPieceCompositeMove.generateBuildTrackMove(from,
					trackVector, ruleA, ruleB, w, principal);

			break;
		}

		case REMOVE_TRACK: {
			try {
				move = ChangeTrackPieceCompositeMove.generateRemoveTrackMove(
						from, trackVector, w, principal);
			} catch (Exception e) {
				// thrown when there is no track to remove.
				// Fix for bug [ 948670 ] Removing non-existant track
				return MoveStatus.moveFailed("No track to remove.");
			}

			break;
		}

		case IGNORE_TRACK:
			return MoveStatus.MOVE_OK;

		default:
			throw new IllegalArgumentException(String.valueOf(trackBuilderMode));
		}

		Move moveAndTransaction = transactionsGenerator.addTransactions(move);

		return sendMove(moveAndTransaction);
	}

	public MoveStatus upgradeTrack(Point point) {
		if (trackBuilderMode == UPGRADE_TRACK) {
			ReadOnlyWorld w = executor.getWorld();
			FreerailsTile tile = (FreerailsTile) w.getTile(point.x, point.y);
			int tt = tile.getTerrainTypeID();
			return upgradeTrack(point, buildTrackStrategy.getRule(tt));
		} else {
			throw new IllegalStateException(
					"Track builder not set to upgrade track!");
		}
	}

	/**
	 * Sets the current track rule. E.g. there are different rules governing the
	 * track-configurations that are legal for double and single track.
	 * 
	 * @param trackRuleNumber
	 *            The new trackRule value
	 */
	public void setTrackRule(int trackRuleNumber) {
		ReadOnlyWorld w = executor.getWorld();
		// this.trackRule = (TrackRule)w.get(SKEY.TRACK_RULES, trackRuleNumber);
	}

	public void setTrackBuilderMode(int i) {
		switch (i) {
		case BUILD_TRACK:
		case REMOVE_TRACK:
		case UPGRADE_TRACK:
		case IGNORE_TRACK:
			trackBuilderMode = i;

			break;

		default:
			throw new IllegalArgumentException();
		}
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
			Move m = (Move) moveStack.pop();
			UndoMove undoMove = new UndoMove(m);
			MoveStatus ms = executor.doMove(undoMove);

			if (!ms.ok) {
				return MoveStatus.moveFailed("Can not undo building track!");
			} else {
				return ms;
			}
		} else {
			return MoveStatus.moveFailed("No track to undo building!");
		}
	}

	/**
	 * Moves are only undoable if no game time has passed since they they were
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

	public int getTrackBuilderMode() {
		return trackBuilderMode;
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
}