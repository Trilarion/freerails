package freerails.server;

import freerails.world.terrain.TerrainType;

import java.awt.*;

/**
 * Stores a tile type and its location.
 */
class CityTile {

    final Point p;
    final TerrainType type;

    public CityTile(final Point p, final TerrainType type) {
        this.p = p;
        this.type = type;
    }
}
