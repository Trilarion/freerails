/*
 * ChangeTrackPieceCompositeMove.java
 *
 * Created on 25 January 2002, 23:49
 */

package jfreerails.move;
import java.awt.Point;
import java.awt.Rectangle;

import jfreerails.move.status.MoveStatus;
import jfreerails.type.TrackRule;
import jfreerails.world.flat.OneTileMoveVector;
import jfreerails.world.flat.TrackConfiguration;
import jfreerails.world.std_track.NullTrackPiece;
import jfreerails.world.std_track.NullTrackType;
import jfreerails.world.std_track.TrackPiece;
import jfreerails.world.std_track.TrackTileMap;

/**
 *
 * @author  lindsal
 * @version
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

	public MoveStatus doMove(TrackTileMap trackTileMap) {
		MoveStatus moveStatus = tryDoMove(trackTileMap);
		if (moveStatus.ok) {
			moveA.doMove(trackTileMap);
			moveB.doMove(trackTileMap);
			return moveStatus;
		} else {
			return moveStatus;
		}
	}

	public MoveStatus tryDoMove(TrackTileMap trackTileMap) {
		MoveStatus moveStatusA, moveStatusB;
		moveStatusA = moveA.tryDoMove(trackTileMap);
		moveStatusB = moveB.tryDoMove(trackTileMap);
		if (moveStatusA.isOk() && moveStatusB.isOk()) {
			return MoveStatus.MOVE_ACCEPTED;
		} else {
			return MoveStatus.MOVE_REJECTED;
		}

	}

	public MoveStatus tryUndoMove(TrackTileMap trackTileMap) {
		return MoveStatus.MOVE_RECEIVED;
	}

	public MoveStatus undoMove(TrackTileMap trackTileMap) {
		return MoveStatus.MOVE_RECEIVED;
	}
	public static ChangeTrackPieceCompositeMove generateBuildTrackMove(
		Point from,
		OneTileMoveVector direction,
		TrackRule trackRule,
		TrackTileMap trackTileMap) {

		ChangeTrackPieceMove a, b;

		a =
			getBuildTrackChangeTrackPieceMove(
				from,
				direction,
				trackRule,
				trackTileMap);
		b =
			getBuildTrackChangeTrackPieceMove(
				direction.createRelocatedPoint(from),
				direction.getOpposite(),
				trackRule,
				trackTileMap);

		return new ChangeTrackPieceCompositeMove(a, b);
	}

	public static ChangeTrackPieceCompositeMove generateRemoveTrackMove(
		Point from,
		OneTileMoveVector direction,
		TrackTileMap trackTileMap) {
		ChangeTrackPieceMove a, b;

		a = getRemoveTrackChangeTrackPieceMove(from, direction, trackTileMap);
		b =
			getRemoveTrackChangeTrackPieceMove(
				direction.createRelocatedPoint(from),
				direction.getOpposite(),
				trackTileMap);

		return new ChangeTrackPieceCompositeMove(a, b);
	}
	//utility method.
	private static ChangeTrackPieceMove getBuildTrackChangeTrackPieceMove(
		Point p,
		OneTileMoveVector direction,
		TrackRule trackRule,
		TrackTileMap trackTileMap) {

		TrackPiece oldTrackPiece, newTrackPiece;

		if (trackTileMap.boundsContain(p)) {
			oldTrackPiece = trackTileMap.getTrackPiece(p);
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
		TrackTileMap trackTileMap) {

		TrackPiece oldTrackPiece, newTrackPiece;

		if (trackTileMap.boundsContain(p)) {
			oldTrackPiece = trackTileMap.getTrackPiece(p);
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