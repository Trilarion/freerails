package jfreerails.move;

import java.awt.Dimension;
import java.awt.Rectangle;

import jfreerails.world.common.ImPoint;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.station.StationModel;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.GameRules;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldIterator;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.NullTrackType;
import jfreerails.world.track.TrackConfiguration;
import jfreerails.world.track.TrackPiece;
import jfreerails.world.track.TrackRule;

/**
 * This Move adds, removes, or upgrades the track on a single tile.
 * 
 * @author Luke
 * 
 */
final public class ChangeTrackPieceMove implements TrackMove, MapUpdateMove {
	private static final long serialVersionUID = 4120849958418591801L;

	final TrackPiece trackPieceBefore;

	private final TrackPiece trackPieceAfter;

	private final ImPoint location;

	public ImPoint getLocation() {
		return location;
	}

	public int hashCode() {
		int result;
		result = (trackPieceBefore != null ? trackPieceBefore.hashCode() : 0);
		result = 29 * result
				+ (trackPieceAfter != null ? trackPieceAfter.hashCode() : 0);
		result = 29 * result + location.hashCode();

		return result;
	}

	public TrackPiece getOldTrackPiece() {
		return trackPieceBefore;
	}

	public TrackPiece getNewTrackPiece() {
		return trackPieceAfter;
	}

	public ChangeTrackPieceMove(TrackPiece before, TrackPiece after, ImPoint p) {
		trackPieceBefore = before;
		trackPieceAfter = after;
		location = p;
	}

	public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
		return tryMove(w, this.trackPieceBefore, this.trackPieceAfter);
	}

	private MoveStatus tryMove(World w, TrackPiece oldTrackPiece,
			TrackPiece newTrackPiece) {
		// Check that location is on the map.
		if (!w.boundsContain(location.x, location.y)) {
			return MoveStatus
					.moveFailed("Tried to build track outside the map.");
		}

		// Check that we are not changing another players track if this is not
		// allowed.
		if (!canConnect2OtherRRsTrack(w)) {
			// If either the new or old track piece is null, we are ok.
			int oldRuleNumber = oldTrackPiece.getTrackTypeID();
			int newRuleNumber = newTrackPiece.getTrackTypeID();

			if (NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER != oldRuleNumber
					&& NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER != newRuleNumber) {
				int oldOwner = oldTrackPiece.getOwnerID();
				int newOwner = newTrackPiece.getOwnerID();

				if (oldOwner != newOwner) {
					return MoveStatus
							.moveFailed("Not allowed to connect to other RR");
				}
			}
		}

		// Check that the current track piece at this.location is
		// the same as this.oldTrackPiece.
		TrackPiece currentTrackPieceAtLocation = ((FreerailsTile) w.getTile(
				location.x, location.y)).getTrackPiece();

		TrackRule expectedTrackRule = oldTrackPiece.getTrackRule();
		TrackRule actualTrackRule = currentTrackPieceAtLocation.getTrackRule();

		if (!expectedTrackRule.equals(actualTrackRule)) {
			return MoveStatus.moveFailed("Expected '"
					+ expectedTrackRule.getTypeName() + "' but found '"
					+ actualTrackRule.getTypeName() + "' at " + location.x
					+ " ," + location.y);
		}

		if (currentTrackPieceAtLocation.getTrackConfiguration() != oldTrackPiece
				.getTrackConfiguration()) {
			return MoveStatus
					.moveFailed("Unexpected track piece found at location: "
							+ location.x + " ," + location.y);
		}

		// Check that oldTrackPiece is not the same as newTrackPiece
		if ((oldTrackPiece.getTrackConfiguration() == newTrackPiece
				.getTrackConfiguration())
				&& (oldTrackPiece.getTrackRule() == newTrackPiece
						.getTrackRule())) {
			return MoveStatus.moveFailed("Already track here!");
		}

		// Check for illegal track configurations.
		if (!(oldTrackPiece.getTrackRule().trackPieceIsLegal(
				oldTrackPiece.getTrackConfiguration()) && newTrackPiece
				.getTrackRule().trackPieceIsLegal(
						newTrackPiece.getTrackConfiguration()))) {
			return MoveStatus.moveFailed("Illegal track configuration.");
		}

		// Check for diagonal conflicts.
		if (!(noDiagonalTrackConflicts(location, oldTrackPiece
				.getTrackGraphicID(), w) && noDiagonalTrackConflicts(location,
				newTrackPiece.getTrackGraphicID(), w))) {
			return MoveStatus
					.moveFailed("Illegal track configuration - diagonal conflict");
		}

		int terrainType = ((FreerailsTile) w.getTile(location.x, location.y))
				.getTerrainTypeID();
		TerrainType tt = (TerrainType) w.get(SKEY.TERRAIN_TYPES, terrainType);

		if (!newTrackPiece.getTrackRule().canBuildOnThisTerrainType(
				tt.getCategory())) {
			String thisTrackType = newTrackPiece.getTrackRule().getTypeName();
			String terrainCategory = tt.getCategory().toString().toLowerCase();

			return MoveStatus.moveFailed("Can't build " + thisTrackType
					+ " on " + terrainCategory);
		}

		// Check 4 overlapping stations.
		if (newTrackPiece.getTrackRule().isStation()) {
			MoveStatus ms = ChangeTrackPieceMove.check4overlap(w, location,
					newTrackPiece);
			if (!ms.ok)
				return ms;
		}

		return MoveStatus.MOVE_OK;
	}

	public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
		return tryMove(w, this.trackPieceAfter, this.trackPieceBefore);
	}

	public MoveStatus doMove(World w, FreerailsPrincipal p) {
		MoveStatus moveStatus = tryDoMove(w, p);

		if (!moveStatus.isOk()) {
			return moveStatus;
		}
		move(w, this.trackPieceBefore, this.trackPieceAfter);

		return moveStatus;
	}

	private void move(World w, TrackPiece oldTrackPiece,
			TrackPiece newTrackPiece) {
		// FIXME why is oldTrackPiece not used???
		FreerailsTile oldTile = (FreerailsTile) w.getTile(location.x,
				location.y);
		int terrain = oldTile.getTerrainTypeID();
		FreerailsTile newTile = FreerailsTile.getInstance(terrain,
				newTrackPiece);
		w.setTile(location.x, location.y, newTile);
	}

	public MoveStatus undoMove(World w, FreerailsPrincipal p) {
		MoveStatus moveStatus = tryUndoMove(w, p);

		if (!moveStatus.isOk()) {
			return moveStatus;
		}
		move(w, this.trackPieceAfter, this.trackPieceBefore);

		return moveStatus;
	}

	private boolean noDiagonalTrackConflicts(ImPoint point, int trackTemplate,
			World w) {
		/*
		 * This method is needs replacing. It only deals with flat track pieces,
		 * and is rather hard to make sense of. LL
		 */

		// int trackTemplate = (1 << (3 * (1 + tv.getY()) + (1 + tv.getX())));
		int trackTemplateAbove;
		int trackTemplateBelow;
		int cornersTemplate = TrackConfiguration
				.stringTemplate2Int("101000101");
		trackTemplate = trackTemplate & cornersTemplate;

		Dimension mapSize = new Dimension(w.getMapWidth(), w.getMapHeight());

		// Avoid array-out-of-bounds exceptions.
		if (point.y > 0) {
			FreerailsTile ft = (FreerailsTile)w.getTile(point.x, point.y - 1);
			TrackPiece tp = ft.getTrackPiece();
			trackTemplateAbove = tp.getTrackGraphicID();
		} else {
			trackTemplateAbove = 0;
		}

		if ((point.y + 1) < mapSize.height) {
			FreerailsTile ft = (FreerailsTile)w.getTile(point.x, point.y + 1);
			TrackPiece tp = ft.getTrackPiece();
			trackTemplateBelow = tp.getTrackGraphicID();
		} else {
			trackTemplateBelow = 0;
		}

		trackTemplateAbove = trackTemplateAbove >> 6;
		trackTemplateBelow = trackTemplateBelow << 6;
		trackTemplate = trackTemplate
				& (trackTemplateAbove | trackTemplateBelow);

		if (trackTemplate != 0) {
			return false;
			// There is a clash.
		}
		return true;
		// Things are ok.
	}

	public Rectangle getUpdatedTiles() {
		// If we are building or removing a station,
		// we need to repaint/remove the station radius
		// that appears on the map.
		int radius = 1;
		TrackRule trackRuleAfter = this.trackPieceAfter.getTrackRule();

		if (trackRuleAfter.isStation()) {
			radius = Math.max(radius, trackRuleAfter.getStationRadius());
		}

		TrackRule trackRuleBefore = this.trackPieceBefore.getTrackRule();

		if (trackRuleBefore.isStation()) {
			radius = Math.max(radius, trackRuleBefore.getStationRadius());
		}

		// Just to be safe.
		radius++;

		int x;
		int y;
		int width;
		int height;

		x = location.x - radius;
		y = location.y - radius;
		width = radius * 2 + 1;
		height = radius * 2 + 1;

		return new Rectangle(x, y, width, height);
	}

	public boolean equals(Object o) {
		if (o instanceof ChangeTrackPieceMove) {
			ChangeTrackPieceMove m = (ChangeTrackPieceMove) o;
			boolean fieldPointEqual = this.location.equals(m.location);
			boolean fieldoldTrackPieceEqual = this.trackPieceBefore
					.equals(m.trackPieceBefore);
			boolean fieldnewTrackPieceEqual = this.trackPieceAfter
					.equals(m.trackPieceAfter);

			if (fieldPointEqual && fieldoldTrackPieceEqual
					&& fieldnewTrackPieceEqual) {
				return true;
			}
			return false;
		}
		return false;
	}

	protected static boolean canConnect2OtherRRsTrack(ReadOnlyWorld world) {
		GameRules rules = (GameRules) world.get(ITEM.GAME_RULES);

		return rules.isCanConnect2OtherRRTrack();
	}

	/**
	 * This method may be called under 3 possible conditions: (1) when a station
	 * is getting built, (2) when a station is getting upgraded, (3) when a
	 * staton is getting removed.
	 */
	protected static MoveStatus check4overlap(World w, ImPoint location,
			TrackPiece trackPiece) {
		/*
		 * Fix for 915945 (Stations should not overlap) Check that there is not
		 * another station whose radius overlaps with the one we are building.
		 */
		TrackRule thisStationType = trackPiece.getTrackRule();
		assert thisStationType.isStation();

		for (int player = 0; player < w.getNumberOfPlayers(); player++) {
			FreerailsPrincipal principal = w.getPlayer(player).getPrincipal();
			WorldIterator wi = new NonNullElements(KEY.STATIONS, w, principal);

			while (wi.next()) {
				StationModel station = (StationModel) wi.getElement();

				/*
				 * Fix for bug 948675 - Can't upgrade station types If locations
				 * are the same, then we are upgrading a station so it doesn't
				 * matter if the radii overlap.
				 */

				if (location.x == station.x && location.y == station.y) {
					continue;
				}

				FreerailsTile tile = (FreerailsTile) w.getTile(station.x,
						station.y);
				TrackRule otherStationType = tile.getTrackPiece().getTrackRule();
				assert otherStationType.isStation();

				int sumOfRadii = otherStationType.getStationRadius()
						+ thisStationType.getStationRadius();
				int sumOfRadiiSquared = sumOfRadii * sumOfRadii;
				int xDistance = station.x - location.x;
				int yDistance = station.y - location.y;

				// Do radii overlap?
				boolean xOverlap = sumOfRadiiSquared >= (xDistance * xDistance);
				boolean yOverlap = sumOfRadiiSquared >= (yDistance * yDistance);

				if (xOverlap && yOverlap) {
					String message = "Too close to " + station.getStationName();

					return MoveStatus.moveFailed(message);
				}
			}
		}

		return MoveStatus.MOVE_OK;
	}
}