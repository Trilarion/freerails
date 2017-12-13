/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

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
