/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.move;

import freerails.controller.PathCacheController;
import freerails.util.ImPoint;
import freerails.world.*;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.Station;
import freerails.world.terrain.FreerailsTile;
import freerails.world.terrain.TerrainType;
import freerails.world.track.NullTrackType;
import freerails.world.track.TrackConfiguration;
import freerails.world.track.TrackPiece;
import freerails.world.track.TrackRule;

import java.awt.*;

/**
 * This Move adds, removes, or upgrades the track on a single tile.
 */
public final class ChangeTrackPieceMove implements TrackMove, MapUpdateMove {
    private static final long serialVersionUID = 4120849958418591801L;

    final TrackPiece trackPieceBefore;

    private final TrackPiece trackPieceAfter;

    private final ImPoint location;

    /**
     * @param before
     * @param after
     * @param p
     */
    public ChangeTrackPieceMove(TrackPiece before, TrackPiece after, ImPoint p) {
        trackPieceBefore = before;
        trackPieceAfter = after;
        location = p;
    }

    /**
     * @param world
     * @return
     */
    protected static boolean canConnect2OtherRRsTrack(ReadOnlyWorld world) {
        GameRules rules = (GameRules) world.get(ITEM.GAME_RULES);

        return rules.isCanConnect2OtherRRTrack();
    }

    /**
     * This method may be called under 3 possible conditions: (1) when a station
     * is getting built, (2) when a station is getting upgraded, (3) when a
     * station is getting removed.
     *
     * @param w
     * @param location
     * @param trackPiece
     * @return
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
            WorldIterator wi = new NonNullElementWorldIterator(KEY.STATIONS, w, principal);

            while (wi.next()) {
                Station station = (Station) wi.getElement();

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
                TrackRule otherStationType = tile.getTrackPiece()
                        .getTrackRule();
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

    /**
     * @return
     */
    public ImPoint getLocation() {
        return location;
    }

    @Override
    public int hashCode() {
        int result;
        result = (trackPieceBefore != null ? trackPieceBefore.hashCode() : 0);
        result = 29 * result
                + (trackPieceAfter != null ? trackPieceAfter.hashCode() : 0);
        result = 29 * result + location.hashCode();

        return result;
    }

    /**
     * @return
     */
    public TrackPiece getOldTrackPiece() {
        return trackPieceBefore;
    }

    /**
     * @return
     */
    public TrackPiece getNewTrackPiece() {
        return trackPieceAfter;
    }

    public MoveStatus tryDoMove(World world, FreerailsPrincipal principal) {
        return tryMove(world, this.trackPieceBefore, this.trackPieceAfter);
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

    public MoveStatus tryUndoMove(World world, FreerailsPrincipal principal) {
        return tryMove(world, this.trackPieceAfter, this.trackPieceBefore);
    }

    public MoveStatus doMove(World world, FreerailsPrincipal principal) {
        PathCacheController.clearTrackCache();
        MoveStatus moveStatus = tryDoMove(world, principal);

        if (!moveStatus.isOk()) {
            return moveStatus;
        }
        move(world, this.trackPieceAfter);

        return moveStatus;
    }

    private void move(World w,
                      TrackPiece newTrackPiece) {
        // FIXME why is oldTrackPiece not used???
        FreerailsTile oldTile = (FreerailsTile) w.getTile(location.x,
                location.y);
        int terrain = oldTile.getTerrainTypeID();
        FreerailsTile newTile = FreerailsTile.getInstance(terrain,
                newTrackPiece);
        w.setTile(location.x, location.y, newTile);
    }

    public MoveStatus undoMove(World world, FreerailsPrincipal principal) {
        PathCacheController.clearTrackCache();
        MoveStatus moveStatus = tryUndoMove(world, principal);

        if (!moveStatus.isOk()) {
            return moveStatus;
        }
        move(world, this.trackPieceBefore);

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
            FreerailsTile ft = (FreerailsTile) w.getTile(point.x, point.y - 1);
            TrackPiece tp = ft.getTrackPiece();
            trackTemplateAbove = tp.getTrackGraphicID();
        } else {
            trackTemplateAbove = 0;
        }

        if ((point.y + 1) < mapSize.height) {
            FreerailsTile ft = (FreerailsTile) w.getTile(point.x, point.y + 1);
            TrackPiece tp = ft.getTrackPiece();
            trackTemplateBelow = tp.getTrackGraphicID();
        } else {
            trackTemplateBelow = 0;
        }

        trackTemplateAbove = trackTemplateAbove >> 6;
        trackTemplateBelow = trackTemplateBelow << 6;
        trackTemplate = trackTemplate
                & (trackTemplateAbove | trackTemplateBelow);

        return trackTemplate == 0;
        // Things are ok.
    }

    /**
     * @return
     */
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

    @Override
    public boolean equals(Object o) {
        if (o instanceof ChangeTrackPieceMove) {
            ChangeTrackPieceMove m = (ChangeTrackPieceMove) o;
            boolean fieldPointEqual = this.location.equals(m.location);
            boolean fieldoldTrackPieceEqual = this.trackPieceBefore
                    .equals(m.trackPieceBefore);
            boolean fieldnewTrackPieceEqual = this.trackPieceAfter
                    .equals(m.trackPieceAfter);

            return fieldPointEqual && fieldoldTrackPieceEqual
                    && fieldnewTrackPieceEqual;
        }
        return false;
    }
}