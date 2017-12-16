package freerails.controller;

/**
 * Small data object to store the rate of supply of a cargo.
 *
 * @author Scott Bennett Date: 14 May 2003
 */
public class CargoElementObject {
    private int rate;

    private final int type;

    public CargoElementObject(int rate, int type) {
        this.rate = rate;
        this.type = type;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public int getType() {
        return type;
    }
}