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

import org.railz.world.player.Player;
import org.railz.world.top.KEY;
import org.railz.world.top.World;
import org.railz.world.train.EngineType;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

class EngineTypesHandler {
    private World world;
    
    private String name;
    private long price;
    private long maintenance;
    private int annualFuelConsumption;
    private int fuelType;
    private int waterCapacity;
    private int mass;
    private int powerOutput;
    private int maxTractiveForce;
    private boolean available;
    private float dragCoeff;
    private float frictionCoeff;
    
    public EngineTypesHandler(World w) {
	world = w;
    }
    
    public void handleEngineType(Attributes meta) throws SAXException {
	name = meta.getValue("id");
	price = Long.parseLong(meta.getValue("price"));
	maintenance = Long.parseLong(meta.getValue("maintenance"));
	waterCapacity = Integer.parseInt(meta.getValue("waterCapacity"));
	mass = Integer.parseInt(meta.getValue("mass"));
	powerOutput = Integer.parseInt(meta.getValue("powerOutput"));
	annualFuelConsumption = Integer.parseInt(meta.getValue("annualFuelConsumption"));
	String ft = meta.getValue("fuelType");
	maxTractiveForce = Integer.parseInt(meta.getValue("maxTractiveForce"));
	dragCoeff = Float.parseFloat(meta.getValue("dragCoeff"));
	frictionCoeff = Float.parseFloat(meta.getValue("frictionCoeff"));
	if (ft.equals("Coal"))
	    fuelType = EngineType.FUEL_TYPE_COAL;
	else if (ft.equals("Diesel"))
	    fuelType = EngineType.FUEL_TYPE_DIESEL;
	else
	    fuelType = EngineType.FUEL_TYPE_ELECTRIC;
	available = "true".equals(meta.getValue("available"));
    }
    
    public void startElement(String ns, String name, String qname, Attributes attrs)
	    throws SAXException {
	
	if ("EngineType".equals(name))
	    handleEngineType(attrs);
    }
    
    public void endElement(String ns, String name, String qname) throws SAXException {
	if ("EngineType".equals(name)) {
	    
	    EngineType et = new EngineType(this.name, price, maintenance, annualFuelConsumption,
		    fuelType, waterCapacity, mass, powerOutput, maxTractiveForce, frictionCoeff,
		    dragCoeff, available);
	    world.add(KEY.ENGINE_TYPES, et, Player.AUTHORITATIVE);
	}
    }
}
