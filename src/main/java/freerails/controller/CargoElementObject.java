package freerails.controller;

/**
 * Small data object to store the rate of supply of a cargo.
 *
 */
public class CargoElementObject {
    private final int type;
    private int rate;

    /**
     *
     * @param rate
     * @param type
     */
    public CargoElementObject(int rate, int type) {
        this.rate = rate;
        this.type = type;
    }

    /**
     *
     * @return
     */
    public int getRate() {
        return rate;
    }

    /**
     *
     * @param rate
     */
    public void setRate(int rate) {
        this.rate = rate;
    }

    /**
     *
     * @return
     */
    public int getType() {
        return type;
    }
}