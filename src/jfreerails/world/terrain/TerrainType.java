package jfreerails.world.terrain;

import jfreerails.misc.FreerailsSerializable;

public interface TerrainType extends FreerailsSerializable {
    
    String getTerrainTypeName();

    int getRGB();
}
