/*
 * Copyright (C) 2004 Robert Tuck
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
package org.railz.world.player;

import java.io.*;
import java.util.*;

import org.railz.world.common.*;
/**
 * Describes a statistic about the player.
 */
public class Statistic implements FreerailsSerializable {
    /** ArrayList of DataPoint */
    private ArrayList dataPoints;

    private String description;
    private String name;
    private String yUnit;

    public static class DataPoint implements FreerailsSerializable {
	/** x-value */
	public final GameTime time;

	/** y-value */
	public final int y;

	DataPoint(GameTime x, int y) {
	    time = x;
	    this.y = y;
	}

	public boolean equals(Object o) {
	    if (o == null || !(o instanceof DataPoint))
		return false;

	    DataPoint dp = (DataPoint) o;
	    return time.equals(dp.time) && y == dp.y;
	}

	public int hashCode() {
	    return y;
	}
    }

    /**
     * @return an ArrayList of DataPoint
     */
    public ArrayList getData() {
	return dataPoints;
    }

    /**
     * @return a resource key to a description of the statistic
     */
    public String getDescription() {
	return description;
    }

    /**
     * Add a data point
     */
    public void addDataPoint(GameTime x, int y) {
	dataPoints.add(new DataPoint(x, y));
    }

    /**
     * Remove a data point
     */
    public void removeDataPoint() {
	dataPoints.remove(dataPoints.size() - 1);
    }

    /**
     * @return a resource key to the name of the statistic
     */
    public String getName() {
	return name;
    }

    /**
     * @return a resource key to the y unit name
     */
    public String getYUnit() {
	return yUnit;
    }

    public Statistic(String name, String description, String yUnit) {
	dataPoints = new ArrayList();
	this.description = description;
	this.name = name;
	this.yUnit = yUnit;
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
	oos.writeUnshared(dataPoints);
	oos.writeObject(description);
	oos.writeObject(name);
	oos.writeObject(yUnit);
    }

    private void readObject(ObjectInputStream ois) throws IOException,
    ClassNotFoundException {
	dataPoints = (ArrayList) ois.readUnshared();
	description = (String) ois.readObject();
	name = (String) ois.readObject();
	yUnit = (String) ois.readObject();
    }
}
