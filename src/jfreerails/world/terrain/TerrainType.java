package jfreerails.world.terrain;

import jfreerails.world.common.FreerailsSerializable;

public interface TerrainType extends FreerailsSerializable {
    
    String getTerrainTypeName();
	String getTerrainCategory();
    int getRGB();
}
