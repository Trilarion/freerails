package jfreerails.world.terrain;

import jfreerails.lib.FreerailsSerializable;

public interface TerrainType extends FreerailsSerializable {
    
    String getTerrainTypeName();

    int getRGB();
}
