package jfreerails.controller;
import java.awt.Point;

import jfreerails.move.ChangeTrackPieceCompositeMove;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.move.UpgradeTrackMove;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;
import jfreerails.world.track.TrackPiece;
import jfreerails.world.track.TrackRule;



final public class TrackMoveProducer {


	private TrackRule trackRule;

	private World w;

	private MoveReceiver moveReceiver;

	public final static int BUILD_TRACK = 1;

	public final static int REMOVE_TRACK = 2;

	public final static int UPGRADE_TRACK = 3;

	/* Don't build any track */
	public final static int IGNORE_TRACK = 4;

	private int trackBuilderMode = BUILD_TRACK;
	
	/** This generates the transactions - the charge - for the track being built.*/
	private TrackMoveTransactionsGenerator transactionsGenerator;

	public MoveReceiver getMoveReceiver() {
		return moveReceiver;
	}

	public void setMoveReceiver(MoveReceiver moveReceiver) {
		this.moveReceiver = moveReceiver;
	}

	public boolean buildTrack(Point from, OneTileMoveVector trackVector) {

		if (trackBuilderMode == BUILD_TRACK) {
			//trackBuilder.buildTrack(from, trackVector, trackRule);
			ChangeTrackPieceCompositeMove move =
				ChangeTrackPieceCompositeMove.generateBuildTrackMove(
					from,
					trackVector,
					trackRule,
					w);

			MoveStatus moveStatus = moveReceiver.processMove(transactionsGenerator.addTransactions(move));
			TextMessageHandler.sendMessage(moveStatus.message);
			return true;
		}
		if (trackBuilderMode == REMOVE_TRACK) {
			//trackBuilder.removeTrack(from, trackVector);
			ChangeTrackPieceCompositeMove move =
				ChangeTrackPieceCompositeMove.generateRemoveTrackMove(
					from,
					trackVector,
					w);
			MoveStatus moveStatus = moveReceiver.processMove(transactionsGenerator.addTransactions(move));
			TextMessageHandler.sendMessage(moveStatus.message);
			return true;
		}
		if (trackBuilderMode == UPGRADE_TRACK) {
			Point point =
				new Point(from.x + trackVector.getDx(), from.y + trackVector.getDy());
			upgradeTrack(point, trackRule);

			return true;
		} else {
			return false;
		}

	}
	public boolean upgradeTrack(Point point) {

		if (trackBuilderMode == UPGRADE_TRACK) {
			upgradeTrack(point, trackRule);

			return true;
		} else {
			return false;
		}

	}
	/**
	 *  Sets the current track rule. E.g. there are different rules governing
	 *  the track-configurations that are legal for double and single track.
	 *
	 *@param  trackRuleNumber  The new trackRule value
	 */

	public void setTrackRule(int trackRuleNumber) {
		this.trackRule = (TrackRule)w.get(KEY.TRACK_RULES, trackRuleNumber);
		TextMessageHandler.sendMessage(trackRule.getTypeName());
	}
	
	public int getTrackRule(){
		return this.trackRule.getRuleNumber();
	}
	
	
	public void setTrackBuilderMode(int i){
		switch (i) {
			case BUILD_TRACK:
			case REMOVE_TRACK:
			case UPGRADE_TRACK:
			case IGNORE_TRACK:
			trackBuilderMode=i;
				break;
			default:
				throw new IllegalArgumentException();
		}
	}
	
	public int getTrackBuilderMode(){
		return trackBuilderMode;
	}

	public TrackMoveProducer(World world) {
		transactionsGenerator = new TrackMoveTransactionsGenerator(world);
		if (world == null) {
			throw new java.lang.NullPointerException(
				"Tried to create new TrackBuilder, but world==null");
		}
		this.w = world;
	}
	
	public TrackMoveProducer(World  world, MoveReceiver moveReceiver) {
		if (null == world||null==moveReceiver) {
			throw new NullPointerException();
		}
		this.moveReceiver=moveReceiver;
		this.w = world;		
		this.trackRule = (TrackRule)w.get(KEY.TRACK_RULES, 0);
		transactionsGenerator = new TrackMoveTransactionsGenerator(world);
	}
	
	private void upgradeTrack(Point point, TrackRule trackRule) {

		TrackPiece before=(TrackPiece)w.getTile(point.x, point.y);
		TrackPiece after=trackRule.getTrackPiece(before.getTrackConfiguration());
		Move move = UpgradeTrackMove.generateMove( before, after, point);		
		MoveStatus moveStatus = moveReceiver.processMove(transactionsGenerator.addTransactions(move));
			TextMessageHandler.sendMessage(moveStatus.message);
	}

}