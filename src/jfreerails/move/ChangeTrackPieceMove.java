package jfreerails.move;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.LegalTrackConfigurations;
import jfreerails.world.track.TrackPiece;
import jfreerails.world.track.TrackRule;
/**
 * This Move adds, removes, or upgrades the track on a single tile. 
 * @author Luke
 *
 */

final public class ChangeTrackPieceMove implements TrackMove, MapUpdateMove {

	final TrackPiece trackPieceBefore;

	final TrackPiece trackPieceAfter;

	final Point location;

	public Point getLocation() {
		return location;
	}

	public TrackPiece getOldTrackPiece() {
		return trackPieceBefore;
	}

	public TrackPiece getNewTrackPiece() {
		return trackPieceAfter;
	}

	public ChangeTrackPieceMove(TrackPiece before, TrackPiece after, Point p) {		
		
		trackPieceBefore = before;
		trackPieceAfter = after;
		location = new Point(p);
	}

	public MoveStatus tryDoMove(World w) {
		return tryMove(w, this.trackPieceBefore, this.trackPieceAfter);
	}

	private MoveStatus tryMove(World w, TrackPiece oldTrackPiece, TrackPiece newTrackPiece) {
		//Check that location is on the map.
		if (!w.boundsContain(location.x, location.y)) {
			return MoveStatus.moveFailed("Tried to build track outside the map.");
		}

		//Check that the current track piece at this.location is
		//the same as this.oldTrackPiece.
		
		TrackPiece currentTrackPieceAtLocation = ((FreerailsTile) w.getTile(location.x, location.y)).getTrackPiece();

		TrackRule expectedTrackRule = oldTrackPiece.getTrackRule();
		TrackRule actualTrackRule = currentTrackPieceAtLocation.getTrackRule();

		if ( !expectedTrackRule.equals(actualTrackRule) ) {
			return MoveStatus.moveFailed(
				"Expected '"+expectedTrackRule.getTypeName()+"' but found '"+ actualTrackRule.getTypeName()+"' at " + location.x + " ," + location.y);
		}

		if (currentTrackPieceAtLocation.getTrackConfiguration()
			!= oldTrackPiece.getTrackConfiguration()) {
			return MoveStatus.moveFailed(
				"Unexpected track piece found at location: " + location.x + " ," + location.y);
		}

		//Check that oldTrackPiece is not the same as newTrackPiece
		if ((oldTrackPiece.getTrackConfiguration() == newTrackPiece.getTrackConfiguration())
			&& (oldTrackPiece.getTrackRule() == newTrackPiece.getTrackRule())) {
			return MoveStatus.moveFailed(
				"Tried to replace a one track piece with another identical one.");
		}

		//Check for illegal track configurations.
		if (!(oldTrackPiece.getTrackRule().trackPieceIsLegal(oldTrackPiece.getTrackConfiguration())
			&& newTrackPiece.getTrackRule().trackPieceIsLegal(
				newTrackPiece.getTrackConfiguration()))) {
			return MoveStatus.moveFailed("Illegal track configuration.");
		}
		//Check for diagonal conflicts.
		if (!(noDiagonalTrackConflicts(location, oldTrackPiece.getTrackGraphicNumber(), w)
			&& noDiagonalTrackConflicts(location, newTrackPiece.getTrackGraphicNumber(), w))) {
			return MoveStatus.moveFailed("Illegal track configuration - diagonal conflict");
		}
		int terrainType = w.getTile(location.x, location.y).getTerrainTypeNumber();
		TerrainType tt = (TerrainType) w.get(KEY.TERRAIN_TYPES, terrainType);
		if (!newTrackPiece.getTrackRule().canBuildOnThisTerrainType(tt.getTerrainCategory())) {
			return MoveStatus.moveFailed("Can't build track on water!");
		}
		return MoveStatus.MOVE_OK;
	}

	public MoveStatus tryUndoMove(World w) {
		return tryMove(w, this.trackPieceAfter, this.trackPieceBefore);
	}

	public MoveStatus doMove(World w) {

		MoveStatus moveStatus = tryDoMove(w);
		if (!moveStatus.isOk()) {
			return moveStatus;
		} else {
			move(w, this.trackPieceBefore, this.trackPieceAfter);
			return moveStatus;
		}
	}

	private void move(World w, TrackPiece oldTrackPiece, TrackPiece newTrackPiece) {
		FreerailsTile oldTile = (FreerailsTile) w.getTile(location.x, location.y);
		int terrain = oldTile.getTerrainTypeNumber();
		FreerailsTile newTile = new FreerailsTile(terrain, newTrackPiece);
		w.setTile(location.x, location.y, newTile);
	}

	public MoveStatus undoMove(World w) {
		MoveStatus moveStatus = tryUndoMove(w);
		if (!moveStatus.isOk()) {
			return moveStatus;
		} else {
			move(w, this.trackPieceAfter, this.trackPieceBefore);
			return moveStatus;
		}
	}
	private boolean noDiagonalTrackConflicts(Point point, int trackTemplate, World w) {
		/*This method is needs replacing.  It only deals with flat track pieces, and
		 *is rather hard to make sense of.  LL
		 */
		//int trackTemplate = (1 << (3 * (1 + tv.getY()) + (1 + tv.getX())));
		int trackTemplateAbove;
		int trackTemplateBelow;
		int cornersTemplate = LegalTrackConfigurations.stringTemplate2Int("101000101");
		trackTemplate = trackTemplate & cornersTemplate;
		Dimension mapSize = new Dimension(w.getMapWidth(), w.getMapHeight());
		//Avoid array-out-of-bounds exceptions.
		if (point.y > 0) {
			TrackPiece tp = (TrackPiece) w.getTile(point.x, point.y - 1);
			trackTemplateAbove = tp.getTrackGraphicNumber();
		} else {
			trackTemplateAbove = 0;
		}
		if ((point.y + 1) < mapSize.height) {
			TrackPiece tp = (TrackPiece) w.getTile(point.x, point.y + 1);
			trackTemplateBelow = tp.getTrackGraphicNumber();
		} else {
			trackTemplateBelow = 0;
		}
		trackTemplateAbove = trackTemplateAbove >> 6;
		trackTemplateBelow = trackTemplateBelow << 6;
		trackTemplate = trackTemplate & (trackTemplateAbove | trackTemplateBelow);
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

	public boolean equals(Object o) {
		if (o instanceof ChangeTrackPieceMove) {
			ChangeTrackPieceMove m = (ChangeTrackPieceMove) o;
			boolean fieldPointEqual = this.location.equals(m.location);
			boolean fieldoldTrackPieceEqual = this.trackPieceBefore.equals(m.trackPieceBefore);
			boolean fieldnewTrackPieceEqual = this.trackPieceAfter.equals(m.trackPieceAfter);
			if (fieldPointEqual && fieldoldTrackPieceEqual && fieldnewTrackPieceEqual) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	
}