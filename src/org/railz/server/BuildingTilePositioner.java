/*
 * Copyright (C) Robert Tuck
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

package org.railz.server;

import java.awt.Point;

import java.util.ArrayList;
import org.railz.world.city.*;
import org.railz.world.terrain.TerrainType;
import org.railz.world.building.*;
import org.railz.world.player.*;
import org.railz.world.top.*;
import org.railz.world.track.FreerailsTile;

class BuildingTilePositioner {
    private double[] probabilities;
    /** City consists of 7 x 7 grid */
    private static final int CITY_RADIUS = 3;

    public BuildingTilePositioner(World w) {
	probabilities = new double[w.size(KEY.BUILDING_TYPES,
		Player.AUTHORITATIVE)];
	processCities(w);
	processCountryside(w);
    }

    private void processCities(World w) {
	NonNullElements i = new NonNullElements(KEY.CITIES, w,
		Player.AUTHORITATIVE);
	while (i.next()) {
	    CityModel cm = (CityModel) i.getElement();
	    processCity(cm, w);
	}
    }

    private void processCity(CityModel cm, World w) {
	Point cityCentre = new Point(cm.getCityX(), cm.getCityY());
	int xmin = cm.getCityX() - CITY_RADIUS;
	int xmax = cm.getCityX() + CITY_RADIUS;
	int ymin = cm.getCityY() - CITY_RADIUS;
	int ymax = cm.getCityY() + CITY_RADIUS;
	xmin = xmin < 0 ? 0 : xmin;
	xmax = xmax >= w.getMapWidth() ? w.getMapWidth() - 1 : xmax;
	ymin = ymin < 0 ? 0 : ymin;
	ymax = ymax >= w.getMapHeight() ? w.getMapHeight() - 1 : ymax;
	Point p = new Point();
	for (int x = xmin; x <= xmax; x++) {
	    for (int y = ymin; y <= ymax; y++) {
		p.setLocation(x, y);
		processCityTile(w, p, cityCentre);
	    }
	}
    }

    private void processRuralTile(World w, Point p) {
	double total = 0;
	for (int i = 0; i < probabilities.length; i++) {
	    BuildingType bt = (BuildingType) w.get(KEY.BUILDING_TYPES, i,
		    Player.AUTHORITATIVE);
	    if (! bt.canBuildHere(w, p)) {
		probabilities[i] = total;
		continue;
	    }
	    total += bt.distributionProbability(w, p);
	    probabilities[i] = total;
	}
	double max = probabilities[probabilities.length - 1];
	max = max > 1 ? max : 1;
	double r = Math.random() * max;
	if (r >= total)
	    return;

	int i = 0;
	while (probabilities[i] < r)
	    i++;
	placeBuilding(w, p, i);
    }

    private void processCityTile(World w, Point p, Point cityCentre) {
	double total = 0;
	for (int i = 0; i < probabilities.length; i++) {
	    BuildingType bt = (BuildingType) w.get(KEY.BUILDING_TYPES, i,
		    Player.AUTHORITATIVE);
	    if (! bt.canBuildHere(w, p)) {
		probabilities[i] = total;
		continue;
	    }
	    total += bt.distributionProbability(w, p, cityCentre);
	    probabilities[i] = total;
	}
	double max = probabilities[probabilities.length - 1];
	max = max > 1 ? max : 1;
	double r = Math.random() * max;
	if (r >= total)
	    return;

	int i = 0;
	while (probabilities[i] < r)
	    i++;
	placeBuilding(w, p, i);
    }

    private void placeBuilding(World w, Point p, int buildingType) {
	FreerailsTile ft = w.getTile(p);
	if (ft.getBuildingTile() != null)
	    return;

	BuildingTile bt = new BuildingTile(buildingType);
	ft = new FreerailsTile(ft, bt);
	w.setTile(p.x, p.y, ft);
    }

    private void processCountryside(World w) {
	int xmax = w.getMapWidth();
	int ymax = w.getMapHeight();
	Point p = new Point();
	for (int x = 0; x < xmax; x++) {
	    for (int y = 0; y < ymax; y++) {
		p.setLocation(x, y);
		processRuralTile(w, p);
	    }
	}
    }
}

