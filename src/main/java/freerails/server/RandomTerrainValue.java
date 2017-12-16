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

    public RandomTerrainValue(int x, int y, int tt) {
        this.x = x;
        this.y = y;
        this.terrainType = tt;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getType() {
        return terrainType;
    }
}