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

import org.railz.world.player.*;
import org.railz.world.top.*;
import org.railz.world.common.*;
import org.railz.world.train.*;

class EconomyHandler {
    private Economy economy;
    private World world;
    
    public EconomyHandler(World w) {
	world = w;
    }

    public void startElement(String ns, String name, String qname, Attributes
	    attrs) throws SAXException {
	if ("Economy".equals(name)) {
	    int incomeTaxRate =
		Integer.parseInt(attrs.getValue("incomeTaxRate"));
	    float baseRate = Float.parseFloat(attrs.getValue("baseRate"));
	    economy = new Economy(incomeTaxRate, baseRate);
	} else if ("FuelPrice".equals(name)) {
	    String fuelType = attrs.getValue("fuelType");
	    int p = Integer.parseInt(attrs.getValue("unitPrice"));
	    if ("Coal".equals(fuelType)) 
		economy = economy.setFuelUnitPrice(EngineType.FUEL_TYPE_COAL,
			p);
	    else if ("Diesel".equals(fuelType))
		economy =
		    economy.setFuelUnitPrice(EngineType.FUEL_TYPE_DIESEL, p);
	    else if ("Electric".equals(fuelType))
		economy =
		    economy.setFuelUnitPrice(EngineType.FUEL_TYPE_ELECTRIC,
			    p);
	}
    }

    public void endElement(String ns, String name, String qname) throws
	SAXException {
	    if ("Economy".equals(name))
		world.set(ITEM.ECONOMY, economy, Player.AUTHORITATIVE);
	}
}
