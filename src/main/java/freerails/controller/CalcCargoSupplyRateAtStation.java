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
import freerails.world.ReadOnlyWorld;
import freerails.world.SKEY;
import freerails.world.station.StationConversion;
import freerails.world.station.StationDemand;
import freerails.world.station.Station;
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

    private static final Logger logger = Logger
            .getLogger(CalcCargoSupplyRateAtStation.class.getName());

    /**
     * The threshold that demand for a cargo must exceed before the station
     * demands the cargo.
     */
    private static final int PREREQUISITE_FOR_DEMAND = 16;
    private final Integer[] converts;
    private final int[] demand;
    private final List<CargoElementObject> supplies;
    private final ReadOnlyWorld w;
    private final int x;
    private final int y;
    private final int stationRadius;

    /**
     * Call this constructor if the station does not exist yet.
     *
     * @param world
     * @param X
     * @param trackRuleNo the station type.
     * @param Y
     */
    public CalcCargoSupplyRateAtStation(ReadOnlyWorld world, int X, int Y,
                                        int trackRuleNo) {
        w = world;
        x = X;
        y = Y;

        TrackRule trackRule = (TrackRule) w.get(SKEY.TRACK_RULES, trackRuleNo);
        stationRadius = trackRule.getStationRadius();

        supplies = new ArrayList<>();
        populateSuppliesVector();

        int numCargoTypes = w.size(SKEY.CARGO_TYPES);
        demand = new int[numCargoTypes];
        converts = StationConversion.emptyConversionArray(numCargoTypes);
    }

    /**
     * Call this constructor if the station already exists.
     *
     * @param world
     * @param X
     * @param Y
     */
    public CalcCargoSupplyRateAtStation(ReadOnlyWorld world, int X, int Y) {
        this(world, X, Y, findTrackRule(X, Y, world));
    }

    private static int findTrackRule(int xx, int yy, ReadOnlyWorld w) {
        FullTerrainTile tile = (FullTerrainTile) w.getTile(xx, yy);

        return tile.getTrackPiece().getTrackTypeID();
    }

    /**
     * @return
     */
    public StationConversion getConversion() {
        return new StationConversion(converts);
    }

    /**
     * @return
     */
    public StationDemand getDemand() {
        boolean[] demandboolean = new boolean[w.size(SKEY.CARGO_TYPES)];

        for (int i = 0; i < w.size(SKEY.CARGO_TYPES); i++) {
            if (demand[i] >= PREREQUISITE_FOR_DEMAND) {
                demandboolean[i] = true;
            }
        }

        return new StationDemand(demandboolean);
    }

    private void incrementSupplyAndDemand(int i, int j) {
        int tileTypeNumber = ((FullTerrainTile) w.getTile(i, j))
                .getTerrainTypeID();

        TerrainType terrainType = (TerrainType) w.get(SKEY.TERRAIN_TYPES,
                tileTypeNumber);

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
            demand[type] += PREREQUISITE_FOR_DEMAND / prerequisite;
        }

        ImmutableList<TileConversion> conversion = terrainType.getConversion();

        for (int m = 0; m < conversion.size(); m++) {
            int type = conversion.get(m).getInput();

            // Only one tile that converts the cargo type is needed for the
            // station to demand the cargo type.
            demand[type] += PREREQUISITE_FOR_DEMAND;
            converts[type] = conversion.get(m).getOutput();
        }
    }

    private void populateSuppliesVector() {
        // fill supplies vector with 0 values for all cargo types
        // get the correct list of cargoes from the world object
        CargoElementObject tempCargoElement;

        for (int i = 0; i < w.size(SKEY.CARGO_TYPES); i++) {
            // cT = (CargoType) w.get(SKEY.CARGO_TYPES, i);
            tempCargoElement = new CargoElementObject(0, i);
            supplies.add(tempCargoElement);
        }
    }

    /**
     * @return
     */
    public List<CargoElementObject> scanAdjacentTiles() {
        int stationDiameter = stationRadius * 2 + 1;

        Rectangle stationRadiusRect = new Rectangle(x - stationRadius, y
                - stationRadius, stationDiameter, stationDiameter);
        Rectangle mapRect = new Rectangle(0, 0, w.getMapWidth(), w
                .getMapHeight());
        Rectangle tiles2scan = stationRadiusRect.intersection(mapRect);
        if (logger.isDebugEnabled()) {
            logger.debug("stationRadiusRect=" + stationRadiusRect);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("mapRect=" + mapRect);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("tiles2scan=" + tiles2scan);
        }

        // Look at the terrain type of each tile and retrieve the cargo
        // supplied.
        // The station radius determines how many tiles each side we look at.
        for (int i = tiles2scan.x; i < (tiles2scan.x + tiles2scan.width); i++) {
            for (int j = tiles2scan.y; j < (tiles2scan.y + tiles2scan.height); j++) {
                incrementSupplyAndDemand(i, j);
            }
        }

        // return the supplied cargo rates
        return supplies;
    }

    private void updateSupplyRate(int type, int rate) {
        // loop through supplies vector and increment the cargo values as
        // required
        for (int n = 0; n < supplies.size(); n++) {
            CargoElementObject tempElement = supplies.get(n);

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
     * @return
     */
    public Station calculations(Station station) {
        int[] cargoSupplied = new int[w.size(SKEY.CARGO_TYPES)];

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