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

package jfreerails.world.cargo;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.train.TransportCategory;


/** This class represents a type of cargo */
final public class CargoType implements FreerailsSerializable {
    private final int unitWeight;
    private final String name;
    private final TransportCategory category;

    public int getUnitWeight() {
        return unitWeight;
    }

    public String getName() {
        return name;
    }

    /** Returns the name, replacing any underscores with spaces. */
    public String getDisplayName() {
        return this.name.replace('_', ' ');
    }

    public CargoType(int weight, String name, TransportCategory category) {
        this.unitWeight = weight;
        this.category = category;
        this.name = name;
    }

    public String toString() {
       return "CargoType: weight=" + unitWeight + ", category=" + category +
       ", name=" + name;
    }

    public TransportCategory getCategory() {
        return category;
    }
}
