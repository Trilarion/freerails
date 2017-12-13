/*
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

package jfreerails.world.track;

import jfreerails.world.common.FreerailsSerializable;

final public class TrackRuleProperties implements FreerailsSerializable {
    public String getTypeName() {
        return typeName;
    }

    public int getRuleNumber() {
        return number;
    }

    public boolean isStation() {
        return isStation;
    }

    private final int rGBvalue;
    private final int number; //This rule's position in the track rule list.
    private final boolean enableDoubleTrack;
    private final String typeName;
    private final boolean isStation;
    private final int stationRadius;
    private final long price;
    private final long maintenanceCost;

    public long getMaintenanceCost() {
        return maintenanceCost;
    }

    public TrackRuleProperties(int rgb, boolean doubleTrack, String name,
        int n, boolean station, int radius, int price, int maintenance) {
        stationRadius = radius;
        rGBvalue = rgb;
        enableDoubleTrack = doubleTrack;
        typeName = name;
        number = n;
        isStation = station;
        this.price = price;
        this.maintenanceCost = maintenance;
    }

    public long getPrice() {
        return price;
    }

    public int getStationRadius() {
        return stationRadius;
    }

    public boolean equals(Object o) {
        if (o instanceof TrackRuleProperties) {
            TrackRuleProperties test = (TrackRuleProperties)o;

            if (rGBvalue == test.getRGBvalue() && number == test.getNumber() &&
                    enableDoubleTrack == test.isEnableDoubleTrack() &&
                    typeName.equals(test.getTypeName()) &&
                    isStation == test.isStation() &&
                    stationRadius == test.stationRadius) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isEnableDoubleTrack() {
        return enableDoubleTrack;
    }

    public int getNumber() {
        return number;
    }

    public int getRGBvalue() {
        return rGBvalue;
    }
}
