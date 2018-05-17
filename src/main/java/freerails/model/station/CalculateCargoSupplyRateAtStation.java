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

import freerails.model.cargo.CargoConversion;
import freerails.model.cargo.CargoProductionOrConsumption;
import freerails.model.track.TrackRule;
import freerails.util.Vec2D;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.SharedKey;
import freerails.model.ModelConstants;
import freerails.model.terrain.*;
import org.apache.log4j.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Probes the tiles adjacent to a station for what cargo they supply,
 * demand, and convert and then returns a vector of these rates.
 */
public class CalculateCargoSupplyRateAtStation {

    private static final Logger logger = Logger.getLogger(CalculateCargoSupplyRateAtStation.class.getName());

    private final Integer[] converts;
    private final int[] demand;
    private final List<CargoProductionOrConsumption> supplies;
    private final UnmodifiableWorld world;
    private final Vec2D location;
    private final int stationRadius;

    /**
     * Call this constructor if the station does not exist yet.
     *
     * @param trackRuleNo the station type.
     */
    public CalculateCargoSupplyRateAtStation(UnmodifiableWorld world, Vec2D location, int trackRuleNo) {
        this.world = world;
        this.location = location;

        TrackRule trackRule = (TrackRule) this.world.get(SharedKey.TrackRules, trackRuleNo);
        stationRadius = trackRule.getStationRadius();

        supplies = new ArrayList<>();
        populateSuppliesVector();

        // TODO demand and converts should be a MAP instead
        int numCargoTypes = world.getCargos().size();
        demand = new int[numCargoTypes];
        converts = StationCargoConversion.emptyConversionArray(numCargoTypes);
    }

    /**
     * Call this constructor if the station already exists.
     */
    public CalculateCargoSupplyRateAtStation(UnmodifiableWorld world, Vec2D location) {
        this(world, location, findTrackRule(location, world));
    }

    // TODO inline this but be careful because this and super must be on the first line
    private static int findTrackRule(Vec2D location, UnmodifiableWorld world) {
        TerrainTile tile = (TerrainTile) world.getTile(location);
        return tile.getTrackPiece().getTrackTypeID();
    }

    /**
     * @return
     */
    private StationCargoConversion getConversion() {
        return new StationCargoConversion(converts);
    }

    /**
     * @return
     */
    private StationDemand getDemand() {
        final int n = world.getCargos().size();
        boolean[] demandboolean = new boolean[n];

        for (int i = 0; i < n; i++) {
            if (demand[i] >= ModelConstants.PREREQUISITE_FOR_DEMAND) {
                demandboolean[i] = true;
            }
        }

        return new StationDemand(demandboolean);
    }

    private void incrementSupplyAndDemand(Vec2D p) {
        int tileTypeNumber = ((TerrainTile) world.getTile(p)).getTerrainTypeId();

        Terrain terrainType = world.getTerrain(tileTypeNumber);

        // Calculate supply.
        List<CargoProductionOrConsumption> production = terrainType.getProductions();

        // loop through the production array and increment
        // the supply rates for the station
        for (CargoProductionOrConsumption aProduction : production) {
            int type = aProduction.getCargoId();
            double rate = aProduction.getRate();

            // loop through supplies vector and increment the cargo values as
            // required
            updateSupplyRate(type, rate);
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

    private void populateSuppliesVector() {
        // fill supplies vector with 0 values for all cargo types
        // get the correct list of cargoes from the world object
        CargoProductionOrConsumption tempCargoElement;

        for (int i = 0; i < world.getCargos().size(); i++) {
            // cT = (CargoType) world.get(SKEY.CARGO_TYPES, i);
            tempCargoElement = new CargoProductionOrConsumption(0, i);
            supplies.add(tempCargoElement);
        }
    }

    /**
     * @return
     */
    private List<CargoProductionOrConsumption> scanAdjacentTiles() {
        int stationDiameter = stationRadius * 2 + 1;

        Rectangle stationRadiusRect = new Rectangle(location.x - stationRadius, location.y - stationRadius, stationDiameter, stationDiameter);
        Vec2D mapSize = world.getMapSize();
        Rectangle mapRect = new Rectangle(0, 0, mapSize.x, mapSize.y);
        Rectangle tiles2scan = stationRadiusRect.intersection(mapRect);

        logger.debug("stationRadiusRect=" + stationRadiusRect);
        logger.debug("mapRect=" + mapRect);
        logger.debug("tiles2scan=" + tiles2scan);


        // Look at the terrain type of each tile and retrieve the cargo supplied.
        // The station radius determines how many tiles each side we look at.
        for (int i = tiles2scan.x; i < (tiles2scan.x + tiles2scan.width); i++) {
            for (int j = tiles2scan.y; j < (tiles2scan.y + tiles2scan.height); j++) {
                incrementSupplyAndDemand(new Vec2D(i, j));
            }
        }

        // return the supplied cargo rates
        return supplies;
    }

    private void updateSupplyRate(int type, double rate) {
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

    /**
     * Process each existing station, updating what is supplied to it.
     *
     * @param station A Station object to be processed
     */
    public Station calculations(Station station) {
        Integer[] cargoSupplied = new Integer[world.getCargos().size()];

        List<CargoProductionOrConsumption> supply = scanAdjacentTiles();

        // grab the supply rates from the vector
        for (int i = 0; i < supply.size(); i++) {
            // TODO where should the rounding take place? here?
            cargoSupplied[i] = (int) supply.get(i).getRate();
        }

        // set the supply rates for the current station
        StationSupply stationSupply = new StationSupply(cargoSupplied);
        station.setSupply(stationSupply);
        station.setDemandForCargo(getDemand());
        station.setCargoConversion(getConversion());

        return station;
    }
}