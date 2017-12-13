package jfreerails.world.terrain;

import java.io.ObjectStreamException;

import jfreerails.world.common.FreerailsSerializable;

public interface TerrainType extends FreerailsSerializable {
    String getTerrainTypeName();

    String getTerrainCategory();

    int getRGB();

    Production[] getProduction();

    Consumption[] getConsumption();

    Conversion[] getConversion();

    String getDisplayName();

    long getBaseValue();

    static final TerrainType NULL = (new TerrainType() {
            public Production[] getProduction() {
                return new Production[0];
            }

            public Consumption[] getConsumption() {
                return new Consumption[0];
            }

            public Conversion[] getConversion() {
                return new Conversion[0];
            }

            public String getTerrainTypeName() {
                return null;
            }

            public String getTerrainCategory() {
                return "TerrainType NULL";
            }

            public int getRGB() {
                return 0;
            }

            public int getRightOfWay() {
                return 0;
            }

            public String getDisplayName() {
                return "";
            }

            private Object readResolve() throws ObjectStreamException {
                return NULL;
            }

	    public long getBaseValue() {
		return 0;
	    }
        });
}
