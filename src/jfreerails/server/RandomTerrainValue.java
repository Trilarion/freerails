package jfreerails.server;

public class RandomTerrainValue {
    private int x;
    private int y;
    private int terrainType;

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