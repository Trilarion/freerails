/*
 * ChangeTrackPieceCompositeMove.java
 *
 * Created on 25 January 2002, 23:49
 */

package jfreerails.move;
import java.awt.Point;
import java.awt.Rectangle;


import jfreerails.world.common.Money;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.top.World;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.NullTrackPiece;
import jfreerails.world.track.NullTrackType;
import jfreerails.world.track.TrackConfiguration;
import jfreerails.world.track.TrackPiece;
import jfreerails.world.track.TrackRule;

/**
 * This Move changes adds, removes, or upgrades the track between two tiles.
 * @author  lindsal
 * 
 */
public final class ChangeTrackPieceCompositeMove extends CompositeMove implements TrackMove, MapUpdateMove {

	private final Rectangle updatedTiles;

	/** Creates new ChangeTrackPieceCompositeMove */
	public ChangeTrackPieceCompositeMove(TrackMove a, TrackMove b) {
		super(new Move[]{a, b});
		updatedTiles = a.getUpdatedTiles().union(b.getUpdatedTiles());						
	}

	public static ChangeTrackPieceCompositeMove generateBuildTrackMove(
		Point from,
		OneTileMoveVector direction,
		TrackRule trackRule,
		World w) {

		ChangeTrackPieceMove a, b;

		a = getBuildTrackChangeTrackPieceMove(from, direction, trackRule, w);
		b =
			getBuildTrackChangeTrackPieceMove(
				direction.createRelocatedPoint(from),
				direction.getOpposite(),
				trackRule,
				w);
		Money price = new Money(trackRule.getPrice().getAmount()*2);
	
		return new ChangeTrackPieceCompositeMove(a, b);
	}

	public static ChangeTrackPieceCompositeMove generateRemoveTrackMove(
		Point from,
		OneTileMoveVector direction,
		World w) {

		TrackMove a, b;

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
			oldTrackPiece = ((FreerailsTile) w.getTile(p.x, p.y)).getTrackPiece();
			if (oldTrackPiece.getTrackRule() != NullTrackType.getInstance()) {
				TrackConfiguration trackConfiguration =
					TrackConfiguration.add(oldTrackPiece.getTrackConfiguration(), direction);
				newTrackPiece = oldTrackPiece.getTrackRule().getTrackPiece(trackConfiguration);
			} else {
				newTrackPiece = getTrackPieceWhenOldTrackPieceIsNull(direction, trackRule);
			}
		} else {
			newTrackPiece = getTrackPieceWhenOldTrackPieceIsNull(direction, trackRule);
			oldTrackPiece = NullTrackPiece.getInstance();
		}
		
		return new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece, p);
	}

	//utility method.
	private static TrackMove getRemoveTrackChangeTrackPieceMove(
		Point p,
		OneTileMoveVector direction,
		World w) {

		TrackPiece oldTrackPiece, newTrackPiece;

		if (w.boundsContain(p.x, p.y)) {
			oldTrackPiece = (TrackPiece) w.getTile(p.x, p.y);
			if (oldTrackPiece.getTrackRule() != NullTrackType.getInstance()) {
				TrackConfiguration trackConfiguration =
					TrackConfiguration.subtract(oldTrackPiece.getTrackConfiguration(), direction);
				if (trackConfiguration != TrackConfiguration.getFlatInstance("000010000")) {
					newTrackPiece = oldTrackPiece.getTrackRule().getTrackPiece(trackConfiguration);
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
		
		
		ChangeTrackPieceMove m = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece, p);
		
		//If we are removing a station, we also need to remove the station from the staiton list.
		if (oldTrackPiece.getTrackRule().isStation()
			&& !newTrackPiece.getTrackRule().isStation()) {
			return RemoveStationMove.getInstance(w, m);
		} else {
			return m;
		}

	}

	private static TrackPiece getTrackPieceWhenOldTrackPieceIsNull(
		OneTileMoveVector direction,
		TrackRule trackRule) {
		TrackConfiguration simplestConfig = TrackConfiguration.getFlatInstance("000010000");
		TrackConfiguration trackConfiguration = TrackConfiguration.add(simplestConfig, direction);
		return trackRule.getTrackPiece(trackConfiguration);
	}

	public Rectangle getUpdatedTiles() {	
		return updatedTiles;
	}

}