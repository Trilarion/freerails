package freerails.model.terrain;

import freerails.util.Vector2D;

/**
 * Stores a tile type and its location.
 */
public class CityTile {

    public final Vector2D location;
    public final TerrainType terrainType;

    public CityTile(Vector2D location, TerrainType terrainType) {
        this.location = location;
        this.terrainType = terrainType;
    }
}
