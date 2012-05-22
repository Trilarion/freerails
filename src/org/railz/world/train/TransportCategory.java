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

package org.railz.world.train;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;

import org.railz.world.common.FreerailsSerializable;

/**
 * This class provides an enumeration of the various classifications of
 * freight/rolling stock that are available.
 */
public final class TransportCategory implements FreerailsSerializable {
    /**
     * Enumerations of "transport category"
     */
    public static final TransportCategory MAIL = new TransportCategory(0);
    public static final TransportCategory PASSENGER = new TransportCategory(1);
    public static final TransportCategory FAST_FREIGHT = new TransportCategory(2);
    public static final TransportCategory SLOW_FREIGHT = new TransportCategory(3);
    public static final TransportCategory BULK_FREIGHT = new TransportCategory(4);
    public static final TransportCategory ENGINE = new TransportCategory(5);
    
    public int getCategory() {
	return category;
    }
    
    private int category;
    
    private TransportCategory(int category) {
	this.category = category;
    }
    
    @Override
    public boolean equals(Object obj) {
	if (obj != null && obj instanceof TransportCategory) {
	    
	    if (((TransportCategory) obj).getCategory() == this.getCategory()) {
		return true;
	    }
	}
	// TODO Auto-generated method stub
	// return super.equals(obj);
	return false;
    }
    
    public final String toString() {
	switch (category) {
	case 0:
	    return "Mail";
	case 1:
	    return "Passengers";
	case 2:
	    return "Fast Freight";
	case 3:
	    return "Slow Freight";
	case 4:
	    return "Bulk Freight";
	case 5:
	    return "Engine";
	default:
	    throw new IllegalStateException("Illegal transport category " + category);
	}
    }
    
    public Object writeReplace() throws ObjectStreamException {
	switch (category) {
	case 0:
	    return MAIL;
	case 1:
	    return PASSENGER;
	case 2:
	    return FAST_FREIGHT;
	case 3:
	    return SLOW_FREIGHT;
	case 4:
	    return BULK_FREIGHT;
	case 5:
	    return ENGINE;
	default:
	    throw new InvalidObjectException("Illegal transport category " + category);
	}
    }
    
    public static TransportCategory parseString(String s) {
	String t = s.trim();
	if (t.equals("Mail"))
	    return MAIL;
	else if (t.equals("Passengers"))
	    return PASSENGER;
	else if (t.equals("Fast Freight"))
	    return FAST_FREIGHT;
	else if (t.equals("Slow Freight"))
	    return SLOW_FREIGHT;
	else if (t.equals("Bulk Freight"))
	    return BULK_FREIGHT;
	else if (t.equals("Engine"))
	    return ENGINE;
	
	throw new IllegalArgumentException("Illegal transport category " + t);
    }
}
