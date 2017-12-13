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
import org.railz.world.train.*;

class EngineTypesHandler {
    private World world;

    private String name;
    private long price;
    private int speed;
    private long maintenance;
    private int annualFuelConsumption;
    private int fuelType;
    private int waterCapacity;

    public EngineTypesHandler(World w) {
	world = w;
    }

    public void handleEngineType(Attributes meta) throws SAXException {
	name = meta.getValue("id");
	price = Long.parseLong(meta.getValue("price"));
	speed = Integer.parseInt(meta.getValue("maxSpeed"));
	maintenance = Long.parseLong(meta.getValue("maintenance"));
	waterCapacity = Integer.parseInt(meta.getValue("waterCapacity"));
	annualFuelConsumption =
	    Integer.parseInt(meta.getValue("annualFuelConsumption"));
	String ft = meta.getValue("fuelType");
	if (ft.equals("Coal"))
	    fuelType = EngineType.FUEL_TYPE_COAL;
	else if (ft.equals("Diesel"))
	    fuelType = EngineType.FUEL_TYPE_DIESEL;
	else 
	    fuelType = EngineType.FUEL_TYPE_ELECTRIC;
    }

    public void startElement(String ns, String name, String qname, Attributes
	    attrs) throws SAXException {
	if ("EngineType".equals(name))
	    handleEngineType(attrs);
    }

    public void endElement(String ns, String name, String qname) throws
	SAXException {
	    if ("EngineType".equals(name)) {
		EngineType et = new EngineType(this.name, price, speed,
			maintenance, annualFuelConsumption, fuelType,
			waterCapacity);
		world.add(KEY.ENGINE_TYPES, et, Player.AUTHORITATIVE);
	    }
	}
}

