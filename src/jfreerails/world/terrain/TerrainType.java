package jfreerails.world.terrain;

import experimental.FreerailsSerializable;

public interface TerrainType extends FreerailsSerializable {
    
    String getTerrainTypeName();

    int getRGB();
}
