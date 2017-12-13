package jfreerails.world.train;

import jfreerails.world.common.FreerailsSerializable;


/** This class represents an engine type, for example 'Grass Hopper'.  It encapsulates
 * the properties that are common to all engines of the same type.
 *
 * @author Luke
 *
 */
final public class EngineType implements FreerailsSerializable {
    private final String engineTypeName;
    private final int powerAtDrawbar;
    private final long price;
    private final long maintenance;
    private final int maxSpeed; //speed in mph

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
