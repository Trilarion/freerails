package freerails.controller;

import freerails.world.common.ImList;
import freerails.world.station.ConvertedAtStation;
import freerails.world.station.Demand4Cargo;
import freerails.world.station.StationModel;
import freerails.world.station.SupplyAtStation;
import freerails.world.terrain.Consumption;
import freerails.world.terrain.Conversion;
import freerails.world.terrain.Production;
import freerails.world.terrain.TerrainType;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.SKEY;
import freerails.world.track.FreerailsTile;
import freerails.world.track.TrackRule;
import org.apache.log4j.Logger;

import java.awt.*;
import java.util.Vector;

/**
 * This class probes the tiles adjacent to a station for what cargo they supply,
 * demand, and convert and then returns a vector of these rates.
 *
 * @author Scott Bennett
 * @author Luke Created: 9th May 2003
 */
public class CalcCargoSupplyRateAtStation {
    private static final Logger logger = Logger
            .getLogger(CalcCargoSupplyRateAtStation.class.getName());

    /**
     * The threshold that demand for a cargo must exceed before the station
     * demands the cargo.
     */
    private static final int PREREQUISITE_FOR_DEMAND = 16;

    private final int[] converts;

    private final int[] demand;

    private final Vector<CargoElementObject> supplies;

    private final ReadOnlyWorld w;

    private int x;

    private int y;

    private int stationRadius;

    /**
     * Call this constructor if the station does not exist yet.
     *
     * @param trackRuleNo the station type.
     */
    public CalcCargoSupplyRateAtStation(ReadOnlyWorld world, int X, int Y,
                                        int trackRuleNo) {
        this.w = world;
        this.x = X;
        this.y = Y;

        TrackRule trackRule = (TrackRule) w.get(SKEY.TRACK_RULES, trackRuleNo);
        stationRadius = trackRule.getStationRadius();

        supplies = new Vector<CargoElementObject>();
        populateSuppliesVector();

        int numCargoTypes = w.size(SKEY.CARGO_TYPES);
        demand = new int[numCargoTypes];
        converts = ConvertedAtStation.emptyConversionArray(numCargoTypes);
    }

    /**
     * Call this constructor if the station already exists.
     */
    public CalcCargoSupplyRateAtStation(ReadOnlyWorld world, int X, int Y) {
        this(world, X, Y, findTrackRule(X, Y, world));
    }

    public ConvertedAtStation getConversion() {
        return new ConvertedAtStation(this.converts);
    }

    public Demand4Cargo getDemand() {
        boolean[] demandboolean = new boolean[w.size(SKEY.CARGO_TYPES)];

        for (int i = 0; i < w.size(SKEY.CARGO_TYPES); i++) {
            if (demand[i] >= PREREQUISITE_FOR_DEMAND) {
                demandboolean[i] = true;
            }
        }

        return new Demand4Cargo(demandboolean);
    }

    private void incrementSupplyAndDemand(int i, int j) {
        int tileTypeNumber = ((FreerailsTile) w.getTile(i, j))
                .getTerrainTypeID();

        TerrainType terrainType = (TerrainType) w.get(SKEY.TERRAIN_TYPES,
                tileTypeNumber);

        // Calculate supply.
        ImList<Production> production = terrainType.getProduction();

        // loop throught the production array and increment
        // the supply rates for the station
        for (int m = 0; m < production.size(); m++) {
            int type = production.get(m).getCargoType();
            int rate = production.get(m).getRate();

            // loop through supplies vector and increment the cargo values as
            // required
            updateSupplyRate(type, rate);
        }

        // Now calculate demand.
        ImList<Consumption> consumption = terrainType.getConsumption();

        for (int m = 0; m < consumption.size(); m++) {
            int type = consumption.get(m).getCargoType();
            int prerequisite = consumption.get(m).getPrerequisite();

            // The prerequisite is the number tiles of this type that must
            // be within the station radius before the station demands the
            // cargo.
            demand[type] += PREREQUISITE_FOR_DEMAND / prerequisite;
        }

        ImList<Conversion> conversion = terrainType.getConversion();

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

    public Vector<CargoElementObject> scanAdjacentTiles() {
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

    private static int findTrackRule(int xx, int yy, ReadOnlyWorld w) {
        FreerailsTile tile = (FreerailsTile) w.getTile(xx, yy);
        int ruleNumber = tile.getTrackPiece().getTrackTypeID();

        return ruleNumber;
    }

    private void updateSupplyRate(int type, int rate) {
        // loop through supplies vector and increment the cargo values as
        // required
        for (int n = 0; n < supplies.size(); n++) {
            CargoElementObject tempElement = supplies.elementAt(n);

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
     * @param station A StationModel object to be processed
     */
    public StationModel calculations(StationModel station) {
        int[] cargoSupplied = new int[w.size(SKEY.CARGO_TYPES)];

        Vector<CargoElementObject> supply = scanAdjacentTiles();

        // grab the supply rates from the vector
        for (int i = 0; i < supply.size(); i++) {
            cargoSupplied[i] = supply.get(i).getRate();
        }

        // set the supply rates for the current station
        SupplyAtStation supplyAtStation = new SupplyAtStation(cargoSupplied);
        station = new StationModel(station, supplyAtStation);
        station = new StationModel(station, getDemand());
        station = new StationModel(station, getConversion());

        return station;
    }
}