package freerails.server;

/**
 * Stores a location and terrain type.
 *
 * @author Scott?
 */
public class RandomTerrainValue {
    private final int x;

    private final int y;

    private final int terrainType;

    /**
     *
     * @param x
     * @param y
     * @param tt
     */
    public RandomTerrainValue(int x, int y, int tt) {
        this.x = x;
        this.y = y;
        this.terrainType = tt;
    }

    /**
     *
     * @return
     */
    public int getX() {
        return x;
    }

    /**
     *
     * @return
     */
    public int getY() {
        return y;
    }

    /**
     *
     * @return
     */
    public int getType() {
        return terrainType;
    }
}