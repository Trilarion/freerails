/**
 * @author Scott Bennett
 * Created: 9th May 2003
 *
 * This class probes the tiles adjacent to a station for what cargo they supply
 * and then returns a vector of these cargo rates
 */
package jfreerails.server;

import java.util.Vector;
import jfreerails.controller.CargoElementObject;
import jfreerails.world.cargo.CargoType;
import jfreerails.world.station.ConvertedAtStation;
import jfreerails.world.station.DemandAtStation;
import jfreerails.world.terrain.Consumption;
import jfreerails.world.terrain.Conversion;
import jfreerails.world.terrain.Production;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.TrackRule;


public class CalcCargoSupplyRateAtStation {
    /** The threshold that demand for a cargo must exceed before the station demands the cargo */
    private static final int PREREQUISITE_FOR_DEMAND = 16;
    private ReadOnlyWorld w;
    private int x;
    private int y;
    Vector supplies;
    private int[] demand;
    private int[] converts;

    public CalcCargoSupplyRateAtStation(ReadOnlyWorld world, int X, int Y) {
        this.w = world;
        this.x = X;
        this.y = Y;

        if (x < 2) {
            x = 2;
        }

        if (y < 2) {
            y = 2;
        }

        supplies = new Vector();
        PopulateSuppliesVector();

        int numCargoTypes = w.size(KEY.CARGO_TYPES);
        demand = new int[numCargoTypes];
        converts = ConvertedAtStation.emptyConversionArray(numCargoTypes);
    }

    public void PopulateSuppliesVector() {
        //fill supplies vector with 0 values for all cargo types
        //get the correct list of cargoes from the world object
        CargoElementObject tempCargoElement;

        //CargoType cT;
        int type;

        for (int i = 0; i < w.size(KEY.CARGO_TYPES); i++) {
            //cT = (CargoType) w.get(KEY.CARGO_TYPES, i);
            tempCargoElement = new CargoElementObject(0, i);
            supplies.add(tempCargoElement);
        }
    }

    public Vector ScanAdjacentTiles() {
        //Find the station radius.
        FreerailsTile tile = w.getTile(this.x, this.y);
        TrackRule trackRule = tile.getTrackRule();
        int stationRadius = trackRule.getStationRadius();

        //Look at the terrain type of each tile and retrieve the cargo supplied.
        //The station radius determines how many tiles each side we look at. 		
        for (int i = x - stationRadius; i <= (x + stationRadius); i++) {
            for (int j = y - stationRadius; j <= (y + stationRadius); j++) {
                incrementSupplyAndDemand(i, j);
            }
        }

        //return the supplied cargo rates
        return supplies;
    }

    public DemandAtStation getDemand() {
        boolean[] demandboolean = new boolean[w.size(KEY.CARGO_TYPES)];

        for (int i = 0; i < w.size(KEY.CARGO_TYPES); i++) {
            if (demand[i] >= PREREQUISITE_FOR_DEMAND) {
                demandboolean[i] = true;

                CargoType ct = (CargoType)w.get(KEY.CARGO_TYPES, i);
            }
        }

        return new DemandAtStation(demandboolean);
    }

    public ConvertedAtStation getConversion() {
        return new ConvertedAtStation(this.converts);
    }

    private void incrementSupplyAndDemand(int i, int j) {
        int tileTypeNumber = w.getTile(i, j).getTerrainTypeNumber();

        TerrainType terrainType = (TerrainType)w.get(KEY.TERRAIN_TYPES,
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