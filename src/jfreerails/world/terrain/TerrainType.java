package jfreerails.world.terrain;

import jfreerails.world.misc.FreerailsSerializable;

public interface TerrainType extends FreerailsSerializable {
    
    String getTerrainTypeName();

    int getRGB();
}
