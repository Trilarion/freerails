package jfreerails.world.track;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.Money;


final public class TrackRuleProperties implements FreerailsSerializable {
    public String getTypeName() {
        return typeName;
    }

    public boolean isDoubleTrackEnabled() {
        return false;
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
    private final Money price;
    private final Money maintenanceCost;

    public Money getMaintenanceCost() {
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
        this.price = new Money(price);
        this.maintenanceCost = new Money(maintenance);
    }

    public Money getPrice() {
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