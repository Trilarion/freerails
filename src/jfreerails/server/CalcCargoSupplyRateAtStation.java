package jfreerails.server;

import java.awt.Rectangle;
import java.util.Vector;
import java.util.logging.Logger;
import jfreerails.controller.CargoElementObject;
import jfreerails.world.station.ConvertedAtStation;
import jfreerails.world.station.DemandAtStation;
import jfreerails.world.terrain.Consumption;
import jfreerails.world.terrain.Conversion;
import jfreerails.world.terrain.Production;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.TrackRule;


/**
 * This class probes the tiles adjacent to a station for what cargo they supply, demand, and convert
 * and then returns a vector of these rates.
 *
 * @author Scott Bennett
 * Created: 9th May 2003
 */
public class CalcCargoSupplyRateAtStation {
    private static final Logger logger = Logger.getLogger(CalcCargoSupplyRateAtStation.class.getName());

    /** The threshold that demand for a cargo must exceed before the station demands the cargo. */
    private static final int PREREQUISITE_FOR_DEMAND = 16;
    private final ReadOnlyWorld w;
    private int x;
    private int y;
    private final Vector supplies;
    private final int[] demand;
    private final int[] converts;

    public CalcCargoSupplyRateAtStation(ReadOnlyWorld world, int X, int Y) {
        this.w = world;
        this.x = X;
        this.y = Y;

        supplies = new Vector();
        PopulateSuppliesVector();

        int numCargoTypes = w.size(SKEY.CARGO_TYPES);
        demand = new int[numCargoTypes];
        converts = ConvertedAtStation.emptyConversionArray(numCargoTypes);
    }

    private void PopulateSuppliesVector() {
        //fill supplies vector with 0 values for all cargo types
        //get the correct list of cargoes from the world object
        CargoElementObject tempCargoElement;

        for (int i = 0; i < w.size(SKEY.CARGO_TYPES); i++) {
            //cT = (CargoType) w.get(SKEY.CARGO_TYPES, i);
            tempCargoElement = new CargoElementObject(0, i);
            supplies.add(tempCargoElement);
        }
    }

    public Vector ScanAdjacentTiles() {
        //Find the station radius.
        FreerailsTile tile = w.getTile(this.x, this.y);
        TrackRule trackRule = tile.getTrackRule();
        int stationRadius = trackRule.getStationRadius();
        int stationDiameter = stationRadius * 2 + 1;

        Rectangle stationRadiusRect = new Rectangle(x - stationRadius,
                y - stationRadius, stationDiameter, stationDiameter);
        Rectangle mapRect = new Rectangle(0, 0, w.getMapWidth(),
                w.getMapHeight());
        Rectangle tiles2scan = stationRadiusRect.intersection(mapRect);
        logger.fine("stationRadiusRect=" + stationRadiusRect);
        logger.fine("mapRect=" + mapRect);
        logger.fine("tiles2scan=" + tiles2scan);

        //Look at the terrain type of each tile and retrieve the cargo supplied.
        //The station radius determines how many tiles each side we look at.
        for (int i = tiles2scan.x; i < (tiles2scan.x + tiles2scan.width);
                i++) {
            for (int j = tiles2scan.y; j < (tiles2scan.y + tiles2scan.height);
                    j++) {
                incrementSupplyAndDemand(i, j);
            }
        }

        //return the supplied cargo rates
        return supplies;
    }

    public DemandAtStation getDemand() {
        boolean[] demandboolean = new boolean[w.size(SKEY.CARGO_TYPES)];

        for (int i = 0; i < w.size(SKEY.CARGO_TYPES); i++) {
            if (demand[i] >= PREREQUISITE_FOR_DEMAND) {
                demandboolean[i] = true;
            }
        }

        return new DemandAtStation(demandboolean);
    }

    public ConvertedAtStation getConversion() {
        return new ConvertedAtStation(this.converts);
    }

    private void incrementSupplyAndDemand(int i, int j) {
        int tileTypeNumber = w.getTile(i, j).getTerrainTypeNumber();

        TerrainType terrainType = (TerrainType)w.get(SKEY.TERRAIN_TYPES,
                tileTypeNumber);

        //Calculate supply.
        Production[] production = terrainType.getProduction();

        //loop throught the production array and increment
        //the supply rates for the station
        for (int m = 0; m < production.length; m++) {
            int type = production[m].getCargoType();
            int rate = production[m].getRate();

            //loop through supplies vector and increment the cargo values as required
            updateSupplyRate(type, rate);
        }

        //Now calculate demand.
        Consumption[] consumption = terrainType.getConsumption();

        for (int m = 0; m < consumption.length; m++) {
            int type = consumption[m].getCargoType();
            int prerequisite = consumption[m].getPrerequisite();

            //The prerequisite is the number tiles of this type that must
            //be within the station radius before the station demands the cargo.
            demand[type] += PREREQUISITE_FOR_DEMAND / prerequisite;
        }

        Conversion[] conversion = terrainType.getConversion();

        for (int m = 0; m < conversion.length; m++) {
            int type = conversion[m].getInput();

            //Only one tile that converts the cargo type is needed for the station to demand the cargo type.
            demand[type] += PREREQUISITE_FOR_DEMAND;
            converts[type] = conversion[m].getOutput();
        }
    }

    private void updateSupplyRate(int type, int rate) {
        //loop through supplies vector and increment the cargo values as required
        for (int n = 0; n < supplies.size(); n++) {
            CargoElementObject tempElement = (CargoElementObject)supplies.elementAt(n);

            if (tempElement.getType() == type) {
                //cargo types are the same, so increment the rate in supply
                //with the rate.
                tempElement.setRate(tempElement.getRate() + rate);

                break; //no need to go through the rest if we've found a match
            }
        }
    }
}