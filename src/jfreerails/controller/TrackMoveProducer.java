
package jfreerails.controller;
import java.awt.Point;

import jfreerails.misc.TextMessageHandler;
import jfreerails.move.ChangeTrackPieceCompositeMove;
import jfreerails.move.ChangeTrackPieceMove;
import jfreerails.move.MoveStatus;
import jfreerails.world.misc.OneTileMoveVector;
import jfreerails.world.track.TrackPiece;
import jfreerails.world.track.TrackRule;
import jfreerails.world.track.TrackRuleList;
import jfreerails.world.track.TrackTileMap;

/**
 *
 *
 *
 * @author lindsal
 */

final public class TrackMoveProducer {

	private TrackRuleList trackRuleList;

	private TrackRule trackRule;

	private TrackTileMap trackSystem;

	private MoveReceiver moveReceiver;

	/**
	 *  Description of the Field
	 */
	public final static int REMOVE_TRACK = 2;

	/**
	 *  Description of the Field
	 */
	public final static int UPGRADE_TRACK = 3;

	/**
	 *  Description of the Field
	 */
	public final static int BUILD_TRACK = 1;

	private int trackBuilderMode = BUILD_TRACK;



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
					trackSystem);

			MoveStatus moveStatus = moveReceiver.processMove(move);
			TextMessageHandler.sendMessage(moveStatus.message);
			return true;
		}
		if (trackBuilderMode == REMOVE_TRACK) {
			//trackBuilder.removeTrack(from, trackVector);
			ChangeTrackPieceCompositeMove move =
				ChangeTrackPieceCompositeMove.generateRemoveTrackMove(
					from,
					trackVector,
					trackSystem);
			MoveStatus moveStatus = moveReceiver.processMove(move);
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
		this.trackRule = trackRuleList.getTrackRule(trackRuleNumber);
		TextMessageHandler.sendMessage(
			trackRuleList.getTrackRule(trackRuleNumber).getTypeName());
	}
	
	public int getTrackRule(){
		return this.trackRule.getRuleNumber();
	}
	
	
	public void setTrackBuilderMode(int i){
		if(BUILD_TRACK!=i && REMOVE_TRACK!=i && UPGRADE_TRACK!=i){
			throw new IllegalArgumentException();	
		}else{
			trackBuilderMode=i;
		}
	}
	
	public int getTrackBuilderMode(){
		return trackBuilderMode;
	}

	

	public TrackMoveProducer(TrackRuleList trackRuleList) {
		if (trackRuleList == null) {
			throw new java.lang.NullPointerException(
				"Tried to create new TrackBuilder, but trackRule==null");
		}
		this.trackRuleList = trackRuleList;
	}

	/**
	 *  Creates new TrackBuilder
	 *
	 *@param  map                     Description of the Parameter
	 *@param  trackRuleList           Description of the Parameter

	 */

	public TrackMoveProducer(TrackTileMap trackSys, MoveReceiver moveReceiver) {
		if (null == trackSys||null==moveReceiver) {
			throw new NullPointerException("Null pointer passed to TrackMoveProducer(TrackSystem trackSys)");
		}
		this.moveReceiver=moveReceiver;
		this.trackSystem = trackSys;
		this.trackRuleList = trackSys.getTrackRuleList();
		this.trackRule = trackRuleList.getTrackRule(0);

	}
	private void upgradeTrack(Point point, TrackRule trackRule) {

		TrackPiece before=trackSystem.getTrackPiece(point);
		TrackPiece after=trackRule.getTrackPiece(before.getTrackConfiguration());
		ChangeTrackPieceMove move = new ChangeTrackPieceMove( before, after, point);
		MoveStatus moveStatus = moveReceiver.processMove(move);
			TextMessageHandler.sendMessage(moveStatus.message);

//		TrackNode node = this.trackSystem.getTrackNode(point);
//
//		TrackRule oldtype;
//		if (node != null) {
//			oldtype = node.getTrackRule();
//		} else {
//			oldtype = trackSystem.getTrackRuleList().getTrackRule(0);
//
//		}
//		//trackUpgrader.upgradeTrack(point, oldtype, trackRule);
//		ChangeTrackTypeMove move = new ChangeTrackTypeMove(point, oldtype, trackRule);
//		MoveStatus moveStatus = moveReceiver.processMove(move);
//		TextMessageHandler.sendMessage(moveStatus.message);
//System.out.println("up grade track not yet implemented!");
	}

}