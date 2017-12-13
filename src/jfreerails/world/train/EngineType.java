/*
 * Copyright (C) Luke Lindsay
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

package jfreerails.world.train;

import jfreerails.world.common.FreerailsSerializable;

/**
 * This class represents an engine type, for example 'Grass Hopper'.  It
 * encapsulates the properties that are common to all engines of the same
 * type.
 *
 * @author Luke
 */
final public class EngineType implements FreerailsSerializable {
    private final String engineTypeName;
    private final int powerAtDrawbar;
    private final long price;
    private final long maintenance;
    private final int maxSpeed; //speed in mph

    /**
     * @return annual maintenance expense
     */
    public long getMaintenance() {
        return maintenance;
    }

    public String getEngineTypeName() {
        return engineTypeName;
    }

    public void setAvailable(boolean b) {
    }

    public int getPowerAtDrawbar() {
        return powerAtDrawbar;
    }

    public long getPrice() {
        return price;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public void getRatedTrainSpeedAtGrade(int speed, int grade) {
    }

    public EngineType(String name, int power, long m, int speed) {
        engineTypeName = name;
        powerAtDrawbar = power;
        price = m;
        this.maxSpeed = speed;
        this.maintenance = 0;
    }

    public EngineType(String name, int power, long m, int speed,
        long maintenance) {
        engineTypeName = name;
        powerAtDrawbar = power;
        price = m;
        this.maxSpeed = speed;
        this.maintenance = maintenance;
    }
}
