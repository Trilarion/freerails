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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides methods that find the nearest station in a given direction, used by
 * the select station popup window.
 *
 * Class to verify that the chosen name for a station hasn't already been taken
 * by another station. If the name has been used, a minor alteration in the name
 * is required, by adding perhaps "Junction" or "Siding" to the name.
 */
public final class StationUtils {

    public static final int NOT_FOUND = Integer.MIN_VALUE;
    public static final int MAX_DISTANCE_TO_SELECT_SQUARED = 20 * 20;
    public static final List<String> stationAlternatives = Arrays.asList("Junction", "Siding", "North", "East", "South", "West");

    private StationUtils() {
    }

    /**
     * @param location
     * @return
     */
    public static int findNearestStation(@NotNull UnmodifiableWorld world, @NotNull Player player, @NotNull Vec2D location) {
        // Find nearest station.
        int distanceToClosestSquared = Integer.MAX_VALUE;

        int nearestStation = StationUtils.NOT_FOUND;

        for (Station station: world.getStations(player)) {
            Vec2D delta = Vec2D.subtract(location, station.getLocation());
            int distanceSquared = delta.x * delta.x + delta.y * delta.y;

            if (distanceSquared < distanceToClosestSquared && StationUtils.MAX_DISTANCE_TO_SELECT_SQUARED > distanceSquared) {
                distanceToClosestSquared = distanceSquared;
                nearestStation = station.getId();
            }
        }

        return nearestStation;
    }

    /**
     * @param startStation
     * @param direction
     * @return
     */
    public static int findNearestStationInDirection(@NotNull UnmodifiableWorld world, @NotNull Player player, int startStation, TileTransition direction) {
        int distanceToClosestSquared = Integer.MAX_VALUE;
        Station currentStation = world.getStation(player, startStation);

        int nearestStation = NOT_FOUND;

        for (Station station: world.getStations(player)) {
            Vec2D delta = Vec2D.subtract(station.getLocation(), currentStation.getLocation());
            int distanceSquared = delta.x * delta.x + delta.y * delta.y;
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
     * Returns true if the angle between direction and the vector (deltaX, deltaY) is less than 45 degrees.
     */
    private static boolean isInRightDirection(TileTransition direction, Vec2D delta) {
        boolean isDiagonal = direction.deltaX * direction.deltaY != 0;
        boolean sameXDirection = direction.deltaX * delta.x > 0;
        boolean sameYDirection = direction.deltaY * delta.y > 0;
        boolean deltaXisLongerThanDeltaY = delta.x * delta.x < delta.y * delta.y;

        if (isDiagonal) {
            return sameXDirection && sameYDirection;
        }
        if (0 == direction.deltaX) {
            return deltaXisLongerThanDeltaY && sameYDirection;
        }
        return !deltaXisLongerThanDeltaY && sameXDirection;
    }

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
     * @return
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
     *  Probes the tiles adjacent to a station for what cargo they supply,
     *  demand, and convert and then returns a vector of these rates.
     *
     * @param world
     * @param trackRuleId
     * @param station
     * @return
     */
    public static Station calculateCargoSupplyRateAtStation(UnmodifiableWorld world, int trackRuleId, Station station) {
        TrackType trackType = world.getTrackType(trackRuleId);
        int stationRadius = trackType.getStationRadius();
        Vec2D location = station.getLocation();

        List<CargoProductionOrConsumption> supplies = new ArrayList<>();
        // populate supplies vector
        // fill supplies vector with 0 values for all cargo types
        // get the correct list of cargoes from the world object
        CargoProductionOrConsumption tempCargoElement;

        for (int i = 0; i < world.getCargos().size(); i++) {
            // cT = (CargoType) world.get(SKEY.CARGO_TYPES, i);
            tempCargoElement = new CargoProductionOrConsumption(0, i);
            supplies.add(tempCargoElement);
        }

        // TODO demand and converts should be a MAP instead
        int numCargoTypes = world.getCargos().size();
        int[] demand = new int[numCargoTypes];
        Integer[] converts = StationCargoConversion.emptyConversionArray(numCargoTypes);

        Integer[] cargoSupplied = new Integer[world.getCargos().size()];

        // scan adjacent tiles and compute List<CargoProductionOrConsumption>
        int stationDiameter = stationRadius * 2 + 1;

        Rectangle stationRadiusRect = new Rectangle(location.x - stationRadius, location.y - stationRadius, stationDiameter, stationDiameter);
        Vec2D mapSize = world.getMapSize();
        Rectangle mapRect = new Rectangle(0, 0, mapSize.x, mapSize.y);
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
                int tileTypeNumber = world.getTile(new Vec2D(i1, j)).getTerrainTypeId();

                Terrain terrainType = world.getTerrain(tileTypeNumber);

                // Calculate supply.
                List<CargoProductionOrConsumption> production = terrainType.getProductions();

                // loop through the production array and increment
                // the supply rates for the station
                for (CargoProductionOrConsumption aProduction : production) {
                    int type = aProduction.getCargoId();

                    // loop through supplies vector and increment the cargo values as
                    // required
                    // loop through supplies vector and increment the cargo values as
                    // required
                    for (CargoProductionOrConsumption tempElement : supplies) {
                        if (tempElement.getCargoId() == type) {
                            // cargo types are the same, so increment the rate in supply
                            // with the rate.
                            // TODO that doesn't work that easily because CargoSupplyOrDemand is immutable
                            // tempElement.setRate(tempElement.getRate() + rate);

                            break; // no need to go through the rest if we've found a match
                        }
                    }
                }

                // Now calculate demand.
                List<CargoProductionOrConsumption> consumption = terrainType.getConsumptions();

                // TODO take into account pre-requisite for station demand (minimum number of tiles of certain type...)
                for (CargoProductionOrConsumption aConsumption : consumption) {
                    int type = aConsumption.getCargoId();

                    demand[type] += 1;
                }

                List<CargoConversion> conversion = terrainType.getConversions();

                for (CargoConversion aConversion : conversion) {
                    int type = aConversion.getSourceCargoId();

                    // TODO Only one tile that converts the cargo type is needed for the station to demand the cargo type.
                    demand[type] += 1;
                    converts[type] = aConversion.getProductCargoId();
                }
            }
        }

        // return the supplied cargo rates
        // grab the supply rates from the vector
        for (int i = 0; i < supplies.size(); i++) {
            // TODO where should the rounding take place? here?
            cargoSupplied[i] = (int) supplies.get(i).getRate();
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

    public static boolean isStationHere(UnmodifiableWorld world, Vec2D location) {
        TerrainTile tile = world.getTile(location);
        return tile.getTrackPiece().getTrackType().isStation();
    }

    /**
     * @return the id of the station at the location or null if there is none
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
     * Return Station number if station exists at location or -1
     */
    public static int getStationIdAtLocation(UnmodifiableWorld world, Player player, Vec2D location) {
        TerrainTile tile = world.getTile(location);
        TrackPiece trackPiece = tile.getTrackPiece();

        if (trackPiece != null) {
            TrackType trackType = trackPiece.getTrackType();
            if (trackType.isStation() && trackPiece.getPlayerId() == player.getId()) {

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
