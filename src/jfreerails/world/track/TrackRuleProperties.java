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
