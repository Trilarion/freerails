/*
 * ChangeTrackPieceCompositeMove.java
 *
 * Created on 25 January 2002, 23:49
 */

package jfreerails.move;
import java.awt.Point;
import java.awt.Rectangle;

import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.top.World;
import jfreerails.world.track.NullTrackPiece;
import jfreerails.world.track.NullTrackType;
import jfreerails.world.track.TrackConfiguration;
import jfreerails.world.track.TrackPiece;
import jfreerails.world.track.TrackRule;

/**
 * This class represents the change that occurs when track between two tiles is 
 * built, removed, or upgraded. 
 * 
 * @author  lindsal
 * 
 */
public final class ChangeTrackPieceCompositeMove
	implements NewTrackMove, MapUpdateMove {

	private final ChangeTrackPieceMove moveA, moveB;

	/** Creates new ChangeTrackPieceCompositeMove */
	public ChangeTrackPieceCompositeMove(
		ChangeTrackPieceMove a,
		ChangeTrackPieceMove b) {
		moveA = a;
		moveB = b;
	}

	public MoveStatus doMove(World w) {
		
		MoveStatus moveStatus = tryDoMove(w);
		if (moveStatus.ok) {
			moveA.doMove(w);
			moveB.doMove(w);
			return moveStatus;
		} else {
			return moveStatus;
		}
	}

	public MoveStatus tryDoMove(World w) {

		
		MoveStatus moveStatusA, moveStatusB;
		moveStatusA = moveA.tryDoMove(w);
		moveStatusB = moveB.tryDoMove(w);
		if (moveStatusA.isOk() && moveStatusB.isOk()) {
			return MoveStatus.MOVE_ACCEPTED;
		} else {
			return MoveStatus.MOVE_REJECTED;
		}

	}

	public MoveStatus tryUndoMove(World w) {
	
		return MoveStatus.MOVE_RECEIVED;
	}

	public MoveStatus undoMove(World w) {

		return MoveStatus.MOVE_RECEIVED;
	}
	public static ChangeTrackPieceCompositeMove generateBuildTrackMove(
		Point from,
		OneTileMoveVector direction,
		TrackRule trackRule,
		World w) {
			
			
		ChangeTrackPieceMove a, b;

		a =
			getBuildTrackChangeTrackPieceMove(
				from,
				direction,
				trackRule,
				w);
		b =
			getBuildTrackChangeTrackPieceMove(
				direction.createRelocatedPoint(from),
				direction.getOpposite(),
				trackRule,
				w);

		return new ChangeTrackPieceCompositeMove(a, b);
	}

	public static ChangeTrackPieceCompositeMove generateRemoveTrackMove(
		Point from,
		OneTileMoveVector direction,
		World w) {

	
		ChangeTrackPieceMove a, b;

		a = getRemoveTrackChangeTrackPieceMove(from, direction, w);
		b =
			getRemoveTrackChangeTrackPieceMove(
				direction.createRelocatedPoint(from),
				direction.getOpposite(),
				w);

		return new ChangeTrackPieceCompositeMove(a, b);
	}
	//utility method.
	private static ChangeTrackPieceMove getBuildTrackChangeTrackPieceMove(
		Point p,
		OneTileMoveVector direction,
		TrackRule trackRule,
		World w) {

		TrackPiece oldTrackPiece, newTrackPiece;

		if (w.boundsContain(p.x, p.y)) {
			oldTrackPiece = (TrackPiece)w.getTile(p.x, p.y);
			if (oldTrackPiece.getTrackRule() != NullTrackType.getInstance()) {
				TrackConfiguration trackConfiguration =
					TrackConfiguration.add(
						oldTrackPiece.getTrackConfiguration(),
						direction);
				newTrackPiece =
					oldTrackPiece.getTrackRule().getTrackPiece(
						trackConfiguration);
			} else {
				newTrackPiece =
					getTrackPieceWhenOldTrackPieceIsNull(direction, trackRule);
			}
		} else {
			newTrackPiece =
				getTrackPieceWhenOldTrackPieceIsNull(direction, trackRule);
			oldTrackPiece = NullTrackPiece.getInstance();
		}
		return new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece, p);
	}

	//utility method.
	private static ChangeTrackPieceMove getRemoveTrackChangeTrackPieceMove(
		Point p,
		OneTileMoveVector direction,
		World w) {

		TrackPiece oldTrackPiece, newTrackPiece;

		if (w.boundsContain(p.x, p.y)) {
			oldTrackPiece = (TrackPiece)w.getTile(p.x, p.y);
			if (oldTrackPiece.getTrackRule() != NullTrackType.getInstance()) {
				TrackConfiguration trackConfiguration =
					TrackConfiguration.subtract(
						oldTrackPiece.getTrackConfiguration(),
						direction);
				if (trackConfiguration
					!= TrackConfiguration.getFlatInstance("000010000")) {
					newTrackPiece =
						oldTrackPiece.getTrackRule().getTrackPiece(
							trackConfiguration);
				} else {
					newTrackPiece = NullTrackPiece.getInstance();
				}
			} else {
				newTrackPiece = NullTrackPiece.getInstance();
			}
		} else {
			newTrackPiece = NullTrackPiece.getInstance();
			oldTrackPiece = NullTrackPiece.getInstance();
		}
		return new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece, p);
	}

	private static TrackPiece getTrackPieceWhenOldTrackPieceIsNull(
		OneTileMoveVector direction,
		TrackRule trackRule) {
		TrackConfiguration simplestConfig =
			TrackConfiguration.getFlatInstance("000010000");
		TrackConfiguration trackConfiguration =
			TrackConfiguration.add(simplestConfig, direction);
		return trackRule.getTrackPiece(trackConfiguration);
	}

	public Rectangle getUpdatedTiles() {
		int x, y, width, height;
		Point p = moveA.getLocation();
		x = p.x - 1;
		y = p.y - 1;
		width = 3;
		height = 3;
		return new Rectangle(x, y, width, height);

	}

}