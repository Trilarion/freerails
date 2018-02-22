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

package freerails.move.mapupdatemove;

import freerails.model.world.*;
import freerails.move.MoveStatus;
import freerails.move.generator.MoveTrainMoveGenerator;
import freerails.util.Vector2D;
import freerails.model.*;
import freerails.model.game.GameRules;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.station.Station;
import freerails.model.terrain.FullTerrainTile;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TerrainType;
import freerails.model.track.NullTrackType;
import freerails.model.track.TrackPiece;
import freerails.model.track.TrackRule;

import java.awt.*;

/**
 * This Move adds, removes, or upgrades the track on a single tile.
 */
public final class ChangeTrackPieceMove implements TrackMove {

    private static final long serialVersionUID = 4120849958418591801L;
    public final TrackPiece trackPieceBefore;
    private final TrackPiece trackPieceAfter;
    private final Vector2D location;

    /**
     * @param before
     * @param after
     * @param p
     */
    public ChangeTrackPieceMove(TrackPiece before, TrackPiece after, Vector2D p) {
        trackPieceBefore = before;
        trackPieceAfter = after;
        location = p;
    }

    /**
     * @param world
     * @return
     */
    public static boolean canConnectToOtherRRsTrack(ReadOnlyWorld world) {
        GameRules rules = (GameRules) world.get(WorldItem.GameRules);

        return rules.isCanConnectToOtherRRTrack();
    }

    /**
     * This method may be called under 3 possible conditions: (1) when a station
     * is getting built, (2) when a station is getting upgraded, (3) when a
     * station is getting removed.
     */
    private static MoveStatus checkForOverlap(World world, Vector2D location, TrackPiece trackPiece) {
        /*
         * Fix for 915945 (Stations should not overlap) Check that there is not
         * another station whose radius overlaps with the one we are building.
         */
        TrackRule thisStationType = trackPiece.getTrackRule();
        assert thisStationType.isStation();

        for (int player = 0; player < world.getNumberOfPlayers(); player++) {
            FreerailsPrincipal principal = world.getPlayer(player).getPrincipal();
            WorldIterator wi = new NonNullElementWorldIterator(PlayerKey.Stations, world, principal);

            while (wi.next()) {
                Station station = (Station) wi.getElement();

                /*
                 * Fix for bug 948675 - Can't upgrade station types If locations
                 * are the same, then we are upgrading a station so it doesn't
                 * matter if the radii overlap.
                 */

                if (location.equals(station.location)) {
                    continue;
                }

                FullTerrainTile tile = (FullTerrainTile) world.getTile(station.location);
                TrackRule otherStationType = tile.getTrackPiece().getTrackRule();
                assert otherStationType.isStation();

                int sumOfRadii = otherStationType.getStationRadius() + thisStationType.getStationRadius();
                int sumOfRadiiSquared = sumOfRadii * sumOfRadii;
                Vector2D delta = Vector2D.subtract(station.location, location);

                // Do radii overlap?
                boolean xOverlap = sumOfRadiiSquared >= delta.x * delta.x;
                boolean yOverlap = sumOfRadiiSquared >= delta.y * delta.y;

                if (xOverlap && yOverlap) {
                    String message = "Too close to " + station.getStationName();

                    return MoveStatus.moveFailed(message);
                }
            }
        }

        return MoveStatus.MOVE_OK;
    }

    private static boolean noDiagonalTrackConflicts(Vector2D point, int trackTemplate, World world) {
        /*
         * This method is needs replacing. It only deals with flat track pieces,
         * and is rather hard to make sense of. LL
         */

        // int trackTemplate = (1 << (3 * (1 + tv.getY()) + (1 + tv.getX())));
        int trackTemplateAbove;
        int trackTemplateBelow;
        String templateString = "101000101";
        // Hack - so that result is as expected by earlier written code.
        StringBuffer strb = new StringBuffer(templateString);
        strb = strb.reverse();
        templateString = strb.toString();

        // End of hack
        int cornersTemplate = Integer.parseInt(templateString, 2);
        trackTemplate = trackTemplate & cornersTemplate;

        Dimension mapSize = new Dimension(world.getMapWidth(), world.getMapHeight());

        // Avoid array-out-of-bounds exceptions.
        if (point.y > 0) {
            FullTerrainTile ft = (FullTerrainTile) world.getTile(new Vector2D(point.x, point.y - 1));
            TrackPiece tp = ft.getTrackPiece();
            trackTemplateAbove = tp.getTrackGraphicID();
        } else {
            trackTemplateAbove = 0;
        }

        if ((point.y + 1) < mapSize.height) {
            FullTerrainTile ft = (FullTerrainTile) world.getTile(new Vector2D(point.x, point.y + 1));
            TrackPiece tp = ft.getTrackPiece();
            trackTemplateBelow = tp.getTrackGraphicID();
        } else {
            trackTemplateBelow = 0;
        }

        trackTemplateAbove = trackTemplateAbove >> 6;
        trackTemplateBelow = trackTemplateBelow << 6;
        trackTemplate = trackTemplate & (trackTemplateAbove | trackTemplateBelow);

        return trackTemplate == 0;
        // Things are success.
    }

    /**
     * @return
     */
    public Vector2D getLocation() {
        return location;
    }

    @Override
    public int hashCode() {
        int result;
        result = (trackPieceBefore != null ? trackPieceBefore.hashCode() : 0);
        result = 29 * result + (trackPieceAfter != null ? trackPieceAfter.hashCode() : 0);
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
        return tryMove(world, trackPieceBefore, trackPieceAfter);
    }

    private MoveStatus tryMove(World world, TrackPiece oldTrackPiece, TrackPiece newTrackPiece) {
        // Check that location is on the map.
        if (!world.boundsContain(location)) {
            return MoveStatus.moveFailed("Tried to build track outside the map.");
        }

        // Check that we are not changing another players track if this is not
        // allowed.
        if (!canConnectToOtherRRsTrack(world)) {
            // If either the new or old track piece is null, we are success.
            int oldRuleNumber = oldTrackPiece.getTrackTypeID();
            int newRuleNumber = newTrackPiece.getTrackTypeID();

            if (NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER != oldRuleNumber && NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER != newRuleNumber) {
                int oldOwner = oldTrackPiece.getOwnerID();
                int newOwner = newTrackPiece.getOwnerID();

                if (oldOwner != newOwner) {
                    return MoveStatus.moveFailed("Not allowed to connect to other RR");
                }
            }
        }

        // Check that the current track piece at this.location is
        // the same as this.oldTrackPiece.
        TrackPiece currentTrackPieceAtLocation = ((FullTerrainTile) world.getTile(location)).getTrackPiece();

        TrackRule expectedTrackRule = oldTrackPiece.getTrackRule();
        TrackRule actualTrackRule = currentTrackPieceAtLocation.getTrackRule();

        if (!expectedTrackRule.equals(actualTrackRule)) {
            return MoveStatus.moveFailed("Expected '" + expectedTrackRule.getTypeName() + "' but found '" + actualTrackRule.getTypeName() + "' at " + location.x + " ," + location.y);
        }

        if (currentTrackPieceAtLocation.getTrackConfiguration() != oldTrackPiece.getTrackConfiguration()) {
            return MoveStatus.moveFailed("Unexpected track piece found at location: " + location.x + " ," + location.y);
        }

        // Check that oldTrackPiece is not the same as newTrackPiece
        if ((oldTrackPiece.getTrackConfiguration() == newTrackPiece.getTrackConfiguration()) && (oldTrackPiece.getTrackRule() == newTrackPiece.getTrackRule())) {
            return MoveStatus.moveFailed("Already track here!");
        }

        // Check for illegal track configurations.
        if (!(oldTrackPiece.getTrackRule().trackPieceIsLegal(oldTrackPiece.getTrackConfiguration()) && newTrackPiece.getTrackRule().trackPieceIsLegal(newTrackPiece.getTrackConfiguration()))) {
            return MoveStatus.moveFailed("Illegal track configuration.");
        }

        // Check for diagonal conflicts.
        if (!(noDiagonalTrackConflicts(location, oldTrackPiece.getTrackGraphicID(), world) && noDiagonalTrackConflicts(location, newTrackPiece.getTrackGraphicID(), world))) {
            return MoveStatus.moveFailed("Illegal track configuration - diagonal conflict");
        }

        int terrainType = ((FullTerrainTile) world.getTile(location)).getTerrainTypeID();
        TerrainType tt = (TerrainType) world.get(SharedKey.TerrainTypes, terrainType);

        if (!newTrackPiece.getTrackRule().canBuildOnThisTerrainType(tt.getCategory())) {
            String thisTrackType = newTrackPiece.getTrackRule().getTypeName();
            String terrainCategory = tt.getCategory().toString().toLowerCase();

            return MoveStatus.moveFailed("Can't build " + thisTrackType + " on " + terrainCategory);
        }

        // Check 4 overlapping stations.
        if (newTrackPiece.getTrackRule().isStation()) {
            MoveStatus moveStatus = ChangeTrackPieceMove.checkForOverlap(world, location, newTrackPiece);
            if (!moveStatus.succeeds()) return moveStatus;
        }

        return MoveStatus.MOVE_OK;
    }

    public MoveStatus tryUndoMove(World world, FreerailsPrincipal principal) {
        return tryMove(world, trackPieceAfter, trackPieceBefore);
    }

    public MoveStatus doMove(World world, FreerailsPrincipal principal) {
        MoveTrainMoveGenerator.clearCache();
        MoveStatus moveStatus = tryDoMove(world, principal);

        if (!moveStatus.succeeds()) {
            return moveStatus;
        }
        move(world, trackPieceAfter);

        return moveStatus;
    }

    private void move(World world, TrackPiece newTrackPiece) {
        // FIXME why is oldTrackPiece not used???
        TerrainTile oldTile = (FullTerrainTile) world.getTile(location);
        int terrain = oldTile.getTerrainTypeID();
        FullTerrainTile newTile = FullTerrainTile.getInstance(terrain, newTrackPiece);
        world.setTile(location, newTile);
    }

    public MoveStatus undoMove(World world, FreerailsPrincipal principal) {
        MoveTrainMoveGenerator.clearCache();
        MoveStatus moveStatus = tryUndoMove(world, principal);

        if (!moveStatus.succeeds()) {
            return moveStatus;
        }
        move(world, trackPieceBefore);

        return moveStatus;
    }

    /**
     * @return
     */
    public Rectangle getUpdatedTiles() {
        // If we are building or removing a station,
        // we need to repaint/remove the station radius
        // that appears on the map.
        int radius = 1;
        TrackRule trackRuleAfter = trackPieceAfter.getTrackRule();

        if (trackRuleAfter.isStation()) {
            radius = Math.max(radius, trackRuleAfter.getStationRadius());
        }

        TrackRule trackRuleBefore = trackPieceBefore.getTrackRule();

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
    public boolean equals(Object obj) {
        if (obj instanceof ChangeTrackPieceMove) {
            ChangeTrackPieceMove changeTrackPieceMove = (ChangeTrackPieceMove) obj;
            boolean fieldPointEqual = location.equals(changeTrackPieceMove.location);
            boolean fieldoldTrackPieceEqual = trackPieceBefore.equals(changeTrackPieceMove.trackPieceBefore);
            boolean fieldnewTrackPieceEqual = trackPieceAfter.equals(changeTrackPieceMove.trackPieceAfter);

            return fieldPointEqual && fieldoldTrackPieceEqual && fieldnewTrackPieceEqual;
        }
        return false;
    }
}