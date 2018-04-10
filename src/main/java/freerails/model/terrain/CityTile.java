package freerails.model.terrain;

import freerails.util.Vec2D;

/**
 * Stores a tile type and its location.
 */
public class CityTile {

    public final Vec2D location;
    public final TerrainType terrainType;

    public CityTile(Vec2D location, TerrainType terrainType) {
        this.location = location;
        this.terrainType = terrainType;
    }
}
