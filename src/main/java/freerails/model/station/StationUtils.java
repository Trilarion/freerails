/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

package freerails.model.station;

import freerails.model.ModelConstants;
import freerails.model.cargo.CargoConversion;
import freerails.model.cargo.CargoProductionOrConsumption;
import freerails.model.player.Player;
import freerails.model.terrain.Terrain;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TileTransition;
import freerails.model.track.TrackPiece;
import freerails.model.track.TrackType;
import freerails.model.world.UnmodifiableWorld;
import freerails.util.Vec2D;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * Provides station lookup, naming, and nearby-terrain cargo configuration
 * operations.
 */
public final class StationUtils {

    public static final int NOT_FOUND = Integer.MIN_VALUE;
    public static final int MAX_DISTANCE_TO_SELECT_SQUARED = 20 * 20;
    public static final List<String> stationAlternatives = Arrays.asList("Junction", "Siding", "North", "East", "South", "West");

    /** Prevents instantiation of this utility class. */
    private StationUtils() {
    }

    /**
     * Finds the closest station owned by the player at a squared distance
     * strictly below the configured selection threshold.
     *
     * @param world world containing the stations
     * @param player owner whose stations are searched
     * @param location map location from which distance is measured
     * @return nearest station identifier, or {@link #NOT_FOUND} if none is in
     *         range
     */
    public static int findNearestStation(@NotNull UnmodifiableWorld world, @NotNull Player player, @NotNull Vec2D location) {
        // Find nearest station.
        int distanceToClosestSquared = Integer.MAX_VALUE;

        int nearestStation = StationUtils.NOT_FOUND;

        for (Station station: world.getStations(player)) {
            Vec2D delta = Vec2D.subtract(location, station.getLocation());
            int distanceSquared = delta.x() * delta.x() + delta.y() * delta.y();

            if (distanceSquared < distanceToClosestSquared && StationUtils.MAX_DISTANCE_TO_SELECT_SQUARED > distanceSquared) {
                distanceToClosestSquared = distanceSquared;
                nearestStation = station.getId();
            }
        }

        return nearestStation;
    }

    /**
     * Finds the nearest other station owned by the player in the specified
     * direction from a starting station.
     *
     * @param world world containing the stations
     * @param player owner whose stations are searched
     * @param startStation identifier of the origin station
     * @param direction direction in which to search
     * @return nearest matching station identifier, or {@link #NOT_FOUND}
     */
    public static int findNearestStationInDirection(@NotNull UnmodifiableWorld world, @NotNull Player player, int startStation, TileTransition direction) {
        int distanceToClosestSquared = Integer.MAX_VALUE;
        Station currentStation = world.getStation(player, startStation);

        int nearestStation = NOT_FOUND;

        for (Station station: world.getStations(player)) {
            Vec2D delta = Vec2D.subtract(station.getLocation(), currentStation.getLocation());
            int distanceSquared = delta.x() * delta.x() + delta.y() * delta.y();
            boolean closer = distanceSquared < distanceToClosestSquared;
            boolean notTheSameStation = startStation != station.getId();
            boolean inRightDirection = isInRightDirection(direction, delta);

            if (closer && inRightDirection && notTheSameStation) {
                distanceToClosestSquared = distanceSquared;
                nearestStation = station.getId();
            }
        }

        return nearestStation;
    }

    /**
     * Tests whether a displacement is aligned with a direction: diagonal
     * directions require matching signs on both axes, while cardinal
     * directions require the displacement to be longer on the corresponding
     * axis.
     *
     * @param direction direction to test against
     * @param delta displacement from the starting station
     * @return {@code true} when the displacement is in the direction's sector
     */
    private static boolean isInRightDirection(TileTransition direction, Vec2D delta) {
        boolean isDiagonal = direction.deltaX * direction.deltaY != 0;
        boolean sameXDirection = direction.deltaX * delta.x() > 0;
        boolean sameYDirection = direction.deltaY * delta.y() > 0;
        boolean deltaXisLongerThanDeltaY = delta.x() * delta.x() < delta.y() * delta.y();

        if (isDiagonal) {
            return sameXDirection && sameYDirection;
        }
        if (0 == direction.deltaX) {
            return deltaXisLongerThanDeltaY && sameYDirection;
        }
        return !deltaXisLongerThanDeltaY && sameXDirection;
    }

    /**
     * Checks every player's stations for an exact name match.
     *
     * @param world world containing station names
     * @param name name to search for
     * @return {@code true} if any station already uses the name
     */
    public static boolean existsStationName(@NotNull UnmodifiableWorld world, @NotNull String name) {
        // for all players
        for (Player player: world.getPlayers()) {
            // for all stations of the player
            for (Station station: world.getStations(player)) {
                if (name.equals(station.getStationName())) {
                    // station already exists with that name
                    return true;
                }
            }
        }
        // no stations exist with that name
        return false;
    }

    /**
     * Returns the requested name if unused, or appends a standard alternative
     * or numbered suffix to make it unique.
     *
     * @param world world containing station names
     * @param name preferred base name
     * @return a name not currently used by a station
     */
    public static String createStationName(@NotNull UnmodifiableWorld world, @NotNull String name) {
        boolean found;
        String tempName = null;

        found = StationUtils.existsStationName(world, name);

        if (!found) {
            return name;
        }
        // a station with that name already exists, so we need to find another
        // name
        for (String stationAlternative : StationUtils.stationAlternatives) {
            tempName = name + ' ' + stationAlternative;
            found = StationUtils.existsStationName(world, tempName);
            if (!found) {
                return tempName;
            }
        }

        int j = 7; // for number of names that have already been used

        while (found) {
            j++;
            tempName = name + "Station #" + j;
            found = StationUtils.existsStationName(world, tempName);
        }

        // TODO could it be we still don't have a valid one?
        return tempName;
    }

    /**
     * Scans terrain within the station radius, updates its supply, demand, and
     * conversion settings, and returns the same station.
     *
     * @param world world providing map and cargo definitions
     * @param trackRuleId track type identifier used to determine station radius
     * @param station station whose cargo settings are recalculated
     * @return the updated station
     */
    public static Station calculateCargoSupplyRateAtStation(UnmodifiableWorld world, int trackRuleId, Station station) {
        TrackType trackType = world.getTrackType(trackRuleId);
        int stationRadius = trackType.getStationRadius();
        Vec2D location = station.getLocation();

        int numCargoTypes = world.getCargos().size();
        double[] supplies = new double[numCargoTypes];
        int[] demand = new int[numCargoTypes];
        Integer[] converts = StationCargoConversion.emptyConversionArray(numCargoTypes);

        // scan adjacent tiles and compute List<CargoProductionOrConsumption>
        int stationDiameter = stationRadius * 2 + 1;

        Rectangle stationRadiusRect = new Rectangle(location.x() - stationRadius, location.y() - stationRadius, stationDiameter, stationDiameter);
        Vec2D mapSize = world.getMapSize();
        Rectangle mapRect = new Rectangle(0, 0, mapSize.x(), mapSize.y());
        Rectangle tiles2scan = stationRadiusRect.intersection(mapRect);

        /*
        logger.debug("stationRadiusRect=" + stationRadiusRect);
        logger.debug("mapRect=" + mapRect);
        logger.debug("tiles2scan=" + tiles2scan); */


        // Look at the terrain type of each tile and retrieve the cargo supplied.
        // The station radius determines how many tiles each side we look at.
        for (int i1 = tiles2scan.x; i1 < tiles2scan.x + tiles2scan.width; i1++) {
            for (int j = tiles2scan.y; j < tiles2scan.y + tiles2scan.height; j++) {
                // increment supply and demand
                int tileTypeNumber = world.getTile(new Vec2D(i1, j)).terrainTypeId();

                Terrain terrainType = world.getTerrain(tileTypeNumber);

                // Calculate supply.
                List<CargoProductionOrConsumption> production = terrainType.getProductions();

                // loop through the production array and increment
                // the supply rates for the station
                for (CargoProductionOrConsumption aProduction : production) {
                    int cargoIndex = world.getCargoIndex(aProduction.cargoId());
                    supplies[cargoIndex] += aProduction.rate();
                }

                // Now calculate demand.
                List<CargoProductionOrConsumption> consumption = terrainType.getConsumptions();

                // TODO take into account pre-requisite for station demand (minimum number of tiles of certain type...)
                for (CargoProductionOrConsumption aConsumption : consumption) {
                    int cargoIndex = world.getCargoIndex(aConsumption.cargoId());
                    demand[cargoIndex] += 1;
                }

                List<CargoConversion> conversion = terrainType.getConversions();

                for (CargoConversion aConversion : conversion) {
                    int cargoIndex = world.getCargoIndex(aConversion.sourceCargoId());

                    // TODO Only one tile that converts the cargo type is needed for the station to demand the cargo type.
                    demand[cargoIndex] += 1;
                    converts[cargoIndex] = aConversion.productCargoId();
                }
            }
        }

        Integer[] cargoSupplied = new Integer[numCargoTypes];
        for (int i = 0; i < numCargoTypes; i++) {
            cargoSupplied[i] = (int) supplies[i];
        }

        // set the supply rates for the current station
        StationSupply stationSupply = new StationSupply(cargoSupplied);
        station.setSupply(stationSupply);


        final int n = world.getCargos().size();
        boolean[] demandboolean = new boolean[n];

        for (int i = 0; i < n; i++) {
            if (demand[i] >= ModelConstants.PREREQUISITE_FOR_DEMAND) {
                demandboolean[i] = true;
            }
        }

        station.setDemandForCargo(new StationDemand(demandboolean));
        station.setCargoConversion(new StationCargoConversion(converts));

        return station;
    }

    /**
     * Tests whether the map tile is occupied by a station track type.
     *
     * @param world world providing the map tile
     * @param location tile coordinates to inspect
     * @return {@code true} if the tile's track type is a station type
     */
    public static boolean isStationHere(UnmodifiableWorld world, Vec2D location) {
        TerrainTile tile = world.getTile(location);
        return tile.trackPiece().trackType().isStation();
    }

    /**
     * Finds a station owned by the player at the exact map location.
     *
     * @param world world containing stations
     * @param player owner whose stations are searched
     * @param location map coordinates to match
     * @return station identifier, or {@code null} if none is present
     */
    public static Integer getStationId(@NotNull UnmodifiableWorld world, @NotNull Player player, @NotNull Vec2D location) {
        // loop through all stations of a player to check if there is a station at the location
        for (Station station: world.getStations(player)) {
            if (location.equals(station.getLocation())) {
                return station.getId();
            }
        }
        return null;
    }

    /**
     * Finds the player's station at a location, provided the tile contains
     * track of a station type owned by that player.
     *
     * @param world world providing map and station data
     * @param player player whose station is sought
     * @param location map coordinates to inspect
     * @return station identifier, or {@code -1} if no matching station exists
     */
    public static int getStationIdAtLocation(UnmodifiableWorld world, Player player, Vec2D location) {
        TerrainTile tile = world.getTile(location);
        TrackPiece trackPiece = tile.trackPiece();

        if (trackPiece != null) {
            TrackType trackType = trackPiece.trackType();
            if (trackType.isStation() && trackPiece.playerId() == player.getId()) {

                for (Station station: world.getStations(player)) {
                    if (location.equals(station.getLocation())) {
                        return station.getId();
                    }
                }
            }
        }

        return -1;
        // Don't show terrain...
    }
}
