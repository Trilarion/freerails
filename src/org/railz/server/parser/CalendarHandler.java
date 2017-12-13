/*
 * Copyright (C) 2005 Robert Tuck
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

import java.text.*;
import java.util.*;
import org.xml.sax.*;

import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.top.*;

class CalendarHandler {
    private World world;
    private GameCalendar gameCalendar;

    public CalendarHandler(World w) {
	world = w;
	gameCalendar = (GameCalendar) world.get(ITEM.CALENDAR,
		Player.AUTHORITATIVE);
    }

    public void startElement(String sName, String qName, Attributes attrs)
	throws SAXException {
	if ("Calendar".equals(qName)) {
	    int year = Integer.parseInt(attrs.getValue("startYear"));
	    int ticksPerDay = Integer.parseInt(attrs.getValue("ticksPerDay"));
	    GameCalendar gc = new GameCalendar
		(ticksPerDay, year, gameCalendar.getTicksPerSecond());
	    world.set(ITEM.CALENDAR, gc, Player.AUTHORITATIVE);
	}
    }

    public void endElement(String sName, String qName) throws
	SAXParseException {
    }
}

