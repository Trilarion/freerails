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

package freerails.controller;

import freerails.util.ImmutableList;
import freerails.util.Point2D;
import freerails.world.ReadOnlyWorld;
import freerails.world.SKEY;
import freerails.world.WorldConstants;
import freerails.world.station.Station;
import freerails.world.station.StationConversion;
import freerails.world.station.StationDemand;
import freerails.world.station.StationSupply;
import freerails.world.terrain.*;
import freerails.world.track.TrackRule;
import org.apache.log4j.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Probes the tiles adjacent to a station for what cargo they supply,
 * demand, and convert and then returns a vector of these rates.
 */
public class CalcCargoSupplyRateAtStation {

    private static final Logger logger = Logger.getLogger(CalcCargoSupplyRateAtStation.class.getName());

    private final Integer[] converts;
    private final int[] demand;
    private final List<CargoElementObject> supplies;
    private final ReadOnlyWorld world;
    private final Point2D p;
    private final int stationRadius;

    /**
     * Call this constructor if the station does not exist yet.
     *
     * @param trackRuleNo the station type.
     */
    public CalcCargoSupplyRateAtStation(ReadOnlyWorld world, Point2D p, int trackRuleNo) {
        this.world = world;
        this.p = p;

        TrackRule trackRule = (TrackRule) this.world.get(SKEY.TRACK_RULES, trackRuleNo);
        stationRadius = trackRule.getStationRadius();

        supplies = new ArrayList<>();
        populateSuppliesVector();

        int numCargoTypes = this.world.size(SKEY.CARGO_TYPES);
        demand = new int[numCargoTypes];
        converts = StationConversion.emptyConversionArray(numCargoTypes);
    }

    /**
     * Call this constructor if the station already exists.
     */
    public CalcCargoSupplyRateAtStation(ReadOnlyWorld world, Point2D p) {
        this(world, p, findTrackRule(p, world));
    }

    private static int findTrackRule(Point2D p, ReadOnlyWorld world) {
        FullTerrainTile tile = (FullTerrainTile) world.getTile(p);
        return tile.getTrackPiece().getTrackTypeID();
    }

    /**
     * @return
     */
    private StationConversion getConversion() {
        return new StationConversion(converts);
    }

    /**
     * @return
     */
    private StationDemand getDemand() {
        boolean[] demandboolean = new boolean[world.size(SKEY.CARGO_TYPES)];

        for (int i = 0; i < world.size(SKEY.CARGO_TYPES); i++) {
            if (demand[i] >= WorldConstants.PREREQUISITE_FOR_DEMAND) {
                demandboolean[i] = true;
            }
        }

        return new StationDemand(demandboolean);
    }

    private void incrementSupplyAndDemand(Point2D p) {
        int tileTypeNumber = ((FullTerrainTile) world.getTile(p)).getTerrainTypeID();

        TerrainType terrainType = (TerrainType) world.get(SKEY.TERRAIN_TYPES, tileTypeNumber);

        // Calculate supply.
        ImmutableList<TileProduction> production = terrainType.getProduction();

        // loop through the production array and increment
        // the supply rates for the station
        for (int m = 0; m < production.size(); m++) {
            int type = production.get(m).getCargoType();
            int rate = production.get(m).getRate();

            // loop through supplies vector and increment the cargo values as
            // required
            updateSupplyRate(type, rate);
        }

        // Now calculate demand.
        ImmutableList<TileConsumption> consumption = terrainType.getConsumption();

        for (int m = 0; m < consumption.size(); m++) {
            int type = consumption.get(m).getCargoType();
            int prerequisite = consumption.get(m).getPrerequisite();

            // The prerequisite is the number tiles of this type that must
            // be within the station radius before the station demands the
            // cargo.
            demand[type] += WorldConstants.PREREQUISITE_FOR_DEMAND / prerequisite;
        }

        ImmutableList<TileConversion> conversion = terrainType.getConversion();

        for (int m = 0; m < conversion.size(); m++) {
            int type = conversion.get(m).getInput();

            // Only one tile that converts the cargo type is needed for the
            // station to demand the cargo type.
            demand[type] += WorldConstants.PREREQUISITE_FOR_DEMAND;
            converts[type] = conversion.get(m).getOutput();
        }
    }

    private void populateSuppliesVector() {
        // fill supplies vector with 0 values for all cargo types
        // get the correct list of cargoes from the world object
        CargoElementObject tempCargoElement;

        for (int i = 0; i < world.size(SKEY.CARGO_TYPES); i++) {
            // cT = (CargoType) world.get(SKEY.CARGO_TYPES, i);
            tempCargoElement = new CargoElementObject(0, i);
            supplies.add(tempCargoElement);
        }
    }

    /**
     * @return
     */
    private List<CargoElementObject> scanAdjacentTiles() {
        int stationDiameter = stationRadius * 2 + 1;

        Rectangle stationRadiusRect = new Rectangle(p.x - stationRadius, p.y - stationRadius, stationDiameter, stationDiameter);
        Rectangle mapRect = new Rectangle(0, 0, world.getMapWidth(), world.getMapHeight());
        Rectangle tiles2scan = stationRadiusRect.intersection(mapRect);

        logger.debug("stationRadiusRect=" + stationRadiusRect);
        logger.debug("mapRect=" + mapRect);
        logger.debug("tiles2scan=" + tiles2scan);


        // Look at the terrain type of each tile and retrieve the cargo supplied.
        // The station radius determines how many tiles each side we look at.
        for (int i = tiles2scan.x; i < (tiles2scan.x + tiles2scan.width); i++) {
            for (int j = tiles2scan.y; j < (tiles2scan.y + tiles2scan.height); j++) {
                incrementSupplyAndDemand(new Point2D(i, j));
            }
        }

        // return the supplied cargo rates
        return supplies;
    }

    private void updateSupplyRate(int type, int rate) {
        // loop through supplies vector and increment the cargo values as
        // required
        for (CargoElementObject tempElement : supplies) {
            if (tempElement.getType() == type) {
                // cargo types are the same, so increment the rate in supply
                // with the rate.
                tempElement.setRate(tempElement.getRate() + rate);

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
        Integer[] cargoSupplied = new Integer[world.size(SKEY.CARGO_TYPES)];

        List<CargoElementObject> supply = scanAdjacentTiles();

        // grab the supply rates from the vector
        for (int i = 0; i < supply.size(); i++) {
            cargoSupplied[i] = supply.get(i).getRate();
        }

        // set the supply rates for the current station
        StationSupply stationSupply = new StationSupply(cargoSupplied);
        station = new Station(station, stationSupply);
        station = new Station(station, getDemand());
        station = new Station(station, getConversion());

        return station;
    }
}