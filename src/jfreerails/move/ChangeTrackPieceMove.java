package jfreerails.move;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.World;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.LegalTrackConfigurations;
import jfreerails.world.track.TrackPiece;
/**
 * This class represents the change that occurs when track 
 * is added to a tile, or the track on a tile is upgraded or removed.
 * @author Luke
 *
 */

final public class ChangeTrackPieceMove
	implements TrackMove, MapUpdateMove {

	private final TrackPiece oldTrackPiece;

	private final TrackPiece newTrackPiece;

	private final Point location;

	public Point getLocation() {
		return location;
	}

	public TrackPiece getOldTrackPiece() {
		return oldTrackPiece;
	}

	public TrackPiece getNewTrackPiece() {
		return newTrackPiece;
	}

	public ChangeTrackPieceMove(TrackPiece before, TrackPiece after, Point p) {
		oldTrackPiece = before;
		newTrackPiece = after;
		location = new Point(p);
	}

	public MoveStatus tryDoMove(World w) {

		

		//Check that location is on the map.
		if (!w.boundsContain(location.x ,location.y)) {
			return new MoveStatus(
				false,
				"Tried to build track outside the map.");
		}

		//Check that the current track piece at this.location is
		//the same as this.oldTrackPiece.
		TrackPiece currentTrackPieceAtLocation = (TrackPiece)w.getTile(location.x ,location.y);

		if ((currentTrackPieceAtLocation.getTrackConfiguration()
			!= oldTrackPiece.getTrackConfiguration())
			|| (currentTrackPieceAtLocation.getTrackRule()
				!= oldTrackPiece.getTrackRule())) {
			return new MoveStatus(
				false,
				"Unexpected track piece found at location: "
					+ location.x
					+ " ,"
					+ location.y);
		}

		//Check that oldTrackPiece is not hte same as newTrackPiece
		if ((oldTrackPiece.getTrackConfiguration()
			== newTrackPiece.getTrackConfiguration())
			&& (oldTrackPiece.getTrackRule() == newTrackPiece.getTrackRule())) {
			return new MoveStatus(
				false,
				"Tried to replace a one track piece with another identical one.");
		}

		//Check for illegal track configurations.
		if (!(oldTrackPiece
			.getTrackRule()
			.trackPieceIsLegal(oldTrackPiece.getTrackConfiguration())
			&& newTrackPiece.getTrackRule().trackPieceIsLegal(
				newTrackPiece.getTrackConfiguration()))) {
			return new MoveStatus(false, "Illegal track configuration.");
		}
		//Check for diagonal conflicts.
		if (!(noDiagonalTrackConflicts(location,
			oldTrackPiece.getTrackGraphicNumber(),
			w)
			&& noDiagonalTrackConflicts(
				location,
				newTrackPiece.getTrackGraphicNumber(),
				w))) {
			return new MoveStatus(
				false,
				"Illegal track configuration - diagonal conflict");
		}

		return MoveStatus.MOVE_ACCEPTED;
	}

	public MoveStatus tryUndoMove(World w) {

		return MoveStatus.MOVE_RECEIVED;

	}

	public MoveStatus doMove(World w) {
		
		MoveStatus moveStatus = tryDoMove(w);
		if (!moveStatus.isOk()) {
			return moveStatus;
		} else {
			FreerailsTile oldTile = (FreerailsTile)w.getTile(location.x, location.y);
			TerrainType terrain = oldTile.getTerrainType(); 
			FreerailsTile newTile = new FreerailsTile(terrain, newTrackPiece);			
			w.setTile(location.x, location.y, newTile);			
			return moveStatus;
		}

	}

	public MoveStatus undoMove(World w) {

		return MoveStatus.MOVE_RECEIVED;

	}
	private boolean noDiagonalTrackConflicts(
		Point point,
		int trackTemplate,
		World w) {
		/*This method is needs replacing.  It only deals with flat track pieces, and
		 *is rather hard to make sense of.  LL
		 */
		//int trackTemplate = (1 << (3 * (1 + tv.getY()) + (1 + tv.getX())));
		int trackTemplateAbove;
		int trackTemplateBelow;
		int cornersTemplate =
			LegalTrackConfigurations.stringTemplate2Int("101000101");
		trackTemplate = trackTemplate & cornersTemplate;
		Dimension mapSize = new Dimension(w.getMapWidth(), w.getMapHeight());
		//Avoid array-out-of-bounds exceptions.
		if (point.y > 0) {			
			TrackPiece tp = (TrackPiece)w.getTile(point.x, point.y-1);
			trackTemplateAbove = tp.getTrackGraphicNumber();				
		} else {
			trackTemplateAbove = 0;
		}
		if ((point.y + 1) < mapSize.height) {			
			TrackPiece tp = (TrackPiece)w.getTile(point.x, point.y+1);
			trackTemplateBelow = tp.getTrackGraphicNumber();	
		} else {
			trackTemplateBelow = 0;
		}
		trackTemplateAbove = trackTemplateAbove >> 6;
		trackTemplateBelow = trackTemplateBelow << 6;
		trackTemplate =
			trackTemplate & (trackTemplateAbove | trackTemplateBelow);
		if (trackTemplate != 0) {
			return false;
			//There is a clash.
		} else {
			return true;
			//Things are ok.
		}
	}
	public Rectangle getUpdatedTiles() {
		int x, y, width, height;

		x = location.x - 1;
		y = location.y - 1;
		width = 3;
		height = 3;
		return new Rectangle(x, y, width, height);

	}

}