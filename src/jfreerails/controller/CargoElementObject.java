/**
 *
 * @author Scott Bennett
 * Date: 14 May 2003
 *
 * Small data object to store the rate of supply of a cargo
 */
package jfreerails.controller;

public class CargoElementObject {
    private int rate;
    private int type;

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

    public void setType(int type) {
        this.type = type;
    }
}