/*
 * Copyright (C) 2004 Robert Tuck
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

package org.railz.server.parser;

import org.xml.sax.*;

import org.railz.world.cargo.*;
import org.railz.world.player.*;
import org.railz.world.top.*;
import org.railz.world.train.*;

class WagonTypesHandler {
    private World world;

    public WagonTypesHandler(World w) {
	world = w;
    }

    public void startElement(String ns, String name, String qname, Attributes
	    attrs) throws SAXException {
	if ("WagonType".equals(name)) {
	    String id = attrs.getValue("id");
	    String categoryString = attrs.getValue("category");
	    TransportCategory category = TransportCategory.MAIL;
	    if ("mail".equals(categoryString)) {
		category = TransportCategory.MAIL;
	    } else if ("passenger".equals(categoryString)) {
		category = TransportCategory.PASSENGER;
	    } else if ("fast_freight".equals(categoryString)) {
		category = TransportCategory.FAST_FREIGHT;
	    } else if ("slow_freight".equals(categoryString)) {
		category = TransportCategory.SLOW_FREIGHT;
	    } else if ("bulk_freight".equals(categoryString)) {
		category = TransportCategory.BULK_FREIGHT;
	    } 
	    int capacity = Integer.parseInt(attrs.getValue("capacity"));
	    int unladenMass = Integer.parseInt(attrs.getValue("unladenMass"));
	    String cargoTypeString = attrs.getValue("cargoType");
	    NonNullElements i = new NonNullElements(KEY.CARGO_TYPES, world,
		    Player.AUTHORITATIVE);
	    int cargoTypeIndex = 0;
	    while (i.next()) {
		CargoType ct = (CargoType) i.getElement();
		if (ct.getName().equals(cargoTypeString)) {
		    cargoTypeIndex = i.getIndex();
		    break;
		}
	    }

	    WagonType wt = new WagonType(id, category, capacity,
		    cargoTypeIndex, unladenMass);
	    world.add(KEY.WAGON_TYPES, wt, Player.AUTHORITATIVE);
	}
    }

    public void endElement(String ns, String name, String qname) throws
	SAXException {
	}
}
