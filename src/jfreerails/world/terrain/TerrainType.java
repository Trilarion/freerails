package jfreerails.world.terrain;

import java.io.ObjectStreamException;

import jfreerails.world.common.FreerailsSerializable;

public interface TerrainType extends FreerailsSerializable {

	String getTerrainTypeName();
	String getTerrainCategory();
	int getRGB();

	static final TerrainType NULL = (new TerrainType() {

		public String getTerrainTypeName() {
				// TODO Auto-generated method stub
	return null;
		}

		public String getTerrainCategory() {
			return null;
		}
		public int getRGB() {
			// TODO Auto-generated method stub
			return 0;
		}

		private Object readResolve() throws ObjectStreamException {
			return NULL;
		}
	});

}
