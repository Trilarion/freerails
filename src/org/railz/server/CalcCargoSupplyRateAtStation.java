/*
 * Copyright (C) 2003 Scott Bennett
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/**
 * @author Scott Bennett
 * Created: 9th May 2003
 *
 * This class probes the tiles adjacent to a station for what cargo they supply
 * and then returns a vector of these cargo rates
 */
package org.railz.server;

import java.util.Vector;
import org.railz.world.cargo.CargoType;
import org.railz.world.station.ConvertedAtStation;
import org.railz.world.station.DemandAtStation;
import org.railz.world.building.*;
import org.railz.world.player.*;
import org.railz.world.terrain.TerrainType;
import org.railz.world.top.*;
import org.railz.world.track.FreerailsTile;
import org.railz.world.track.TrackRule;

class CalcCargoSupplyRateAtStation {
    /**
     * The threshold that demand for a cargo must exceed before the station
     * demands the cargo
     */
    private static final int PREREQUISITE_FOR_DEMAND = 16;

    private ReadOnlyWorld w;
    private int x;
    private int y;
    private int[] supplies;
    private int[] demand;
    private int[] converts;

    private static int maxStationRadius = -1;

    CalcCargoSupplyRateAtStation(ReadOnlyWorld world, int X, int Y) {
        w = world;
        x = X;
        y = Y;

	if (maxStationRadius == -1) {
	    // initialize maxStationRadius if not already done
	    NonNullElements i = new NonNullElements(KEY.BUILDING_TYPES, world,
		    Player.AUTHORITATIVE);
	    while (i.next()) {
		BuildingType bt = (BuildingType) i.getElement();
		if (bt.getStationRadius() > maxStationRadius)
		    maxStationRadius = bt.getStationRadius();
	    }
	}

        int numCargoTypes = w.size(KEY.CARGO_TYPES);
        supplies = new int[numCargoTypes];
        demand = new int[numCargoTypes];
        converts = ConvertedAtStation.emptyConversionArray(numCargoTypes);
    }

    int[] scanAdjacentTiles() {
        //Find the station radius.
        FreerailsTile tile = w.getTile(this.x, this.y);
	BuildingTile bTile = tile.getBuildingTile();
	if (bTile == null)
	    throw new IllegalStateException();
	BuildingType bType = (BuildingType) w.get(KEY.BUILDING_TYPES,
		bTile.getType(), Player.AUTHORITATIVE);
        int stationRadius = bType.getStationRadius();

	// xmin/max, ymin/max - boundaries of our station radius
	int xmin = x < stationRadius ? 0 : x - stationRadius;
	int xmax = x + stationRadius;
	if (xmax >= w.getMapWidth())
	    xmax = w.getMapWidth();
	int ymin = y < stationRadius ? 0 : y - stationRadius;
	int ymax = y + stationRadius;
	if (ymax >= w.getMapHeight())
	    ymax = w.getMapHeight();
	// Build a cache of demand which could be within sufficient distance
	// to be competing for resources
	int[][] competingStations = new int[stationRadius * 2 + 1][stationRadius
	    * 2 + 1];
	// xxmin/max, yymin/max - bounds of where to find competing stations
	int xxmin = x - stationRadius - maxStationRadius;
	int xxmax = x + stationRadius + maxStationRadius;
	int yymin = y - stationRadius - maxStationRadius;
	int yymax = y + stationRadius + maxStationRadius;
	xxmin = xxmin < 0 ? 0 : xxmin;
	xxmax = xxmax > w.getMapWidth() ? w.getMapWidth() : xxmax;
	yymin = yymin < 0 ? 0 : yymin;
	yymax = yymax > w.getMapHeight() ? w.getMapHeight() : yymax;
	
	for (int xx = xxmin; xx <= xxmax; xx++) {
	    for (int yy = yymin; yy <= yymax; yy++) {
		FreerailsTile ft = w.getTile(xx, yy);
		BuildingTile bt = ft.getBuildingTile();
		if (bt == null)
		    continue;

		bType = (BuildingType) w.get(KEY.BUILDING_TYPES,
			bt.getType(), Player.AUTHORITATIVE);

		if (bType.getStationRadius() > 0) {
		    // xxxmin/max, yyymin/max - bounds of competing station
		    // radius
		    int xxxmin = xx - bType.getStationRadius();
		    int yyymin = yy - bType.getStationRadius();
		    int xxxmax = xx + bType.getStationRadius();
		    int yyymax = yy + bType.getStationRadius();
		    xxxmin = xxxmin < xmin ? xmin : xxxmin;
		    xxxmax = xxxmax > xmax ? xmax : xxxmax;
		    yyymin = yyymin < ymin ? ymin : yyymin;
		    yyymax = yyymax > ymax ? ymax : yyymax;
		    for (int yyy = yyymin; yyy <= yyymax; yyy++) {
			for (int xxx = xxxmin; xxx <= xxxmax; xxx++) {
			    competingStations[xxx - xmin][yyy - ymin]++;
			}
		    }
		}
	    }
	}

        //Look at the terrain type of each tile and retrieve the cargo supplied.
        //The station radius determines how many tiles each side we look at. 		
        for (int i = xmin; i <= xmax; i++) {
            for (int j = ymin; j <= ymax; j++) {
                incrementSupplyAndDemand(i, j,
		       	competingStations[i - xmin][j - ymin]);
            }
        }

        //return the supplied cargo rates
        return supplies;
    }

    DemandAtStation getDemand() {
        boolean[] demandboolean = new boolean[w.size(KEY.CARGO_TYPES)];

        for (int i = 0; i < w.size(KEY.CARGO_TYPES); i++) {
            if (demand[i] >= PREREQUISITE_FOR_DEMAND) {
                demandboolean[i] = true;
            }
        }

        return new DemandAtStation(demandboolean);
    }

    ConvertedAtStation getConversion() {
        return new ConvertedAtStation(this.converts);
    }

    private void incrementSupplyAndDemand(int i, int j, int competingStations) {
	BuildingTile bTile = w.getTile(i, j).getBuildingTile();
	if (bTile == null)
	    return;

        int tileTypeNumber = bTile.getType();

        BuildingType buildingType = (BuildingType)w.get(KEY.BUILDING_TYPES,
                tileTypeNumber, Player.AUTHORITATIVE);

        //Calculate supply.
        Production[] production = buildingType.getProduction();

        //loop throught the production array and increment 
        //the supply rates for the station
        for (int m = 0; m < production.length; m++) {
            int type = production[m].getCargoType();
            int rate = production[m].getRate();
	    supplies[type] += rate / competingStations;
        }

        //Now calculate demand.
        Consumption[] consumption = buildingType.getConsumption();

        for (int m = 0; m < consumption.length; m++) {
            int type = consumption[m].getCargoType();
            int prerequisite = consumption[m].getPrerequisite();

            //The prerequisite is the number tiles of this type that must 
            //be within the station radius before the station demands the cargo.			
            demand[type] += PREREQUISITE_FOR_DEMAND / prerequisite;
        }

        Conversion[] conversion = buildingType.getConversion();

        for (int m = 0; m < conversion.length; m++) {
            int type = conversion[m].getInput();

            //Only one tile that converts the cargo type is needed for the station to demand the cargo type.				
            demand[type] += PREREQUISITE_FOR_DEMAND;
            converts[type] = conversion[m].getOutput();
        }
    }
}
