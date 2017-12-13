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

import java.text.*;
import java.util.*;
import org.xml.sax.*;

import org.railz.server.scripting.*;
import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.top.*;

class ScriptingEventHandler {
    private World world;
    private HashMap attributes;
    private String eventName;
    private GameTime startTime;
    private GameTime endTime;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",
	    Locale.US);
    private GameCalendar gameCalendar;

    public ScriptingEventHandler(World w) {
	world = w;
	gameCalendar = (GameCalendar) world.get(ITEM.CALENDAR,
		Player.AUTHORITATIVE);
    }

    public void startElement(String sName, String qName, Attributes attrs)
	throws SAXException {
	if ("ScriptingEvent".equals(qName)) {
	    attributes = new HashMap();
	    eventName=attrs.getValue("name");
	    Date startDate, endDate;
	    try {
		startDate = dateFormat.parse(attrs.getValue("startTime"));
		endDate = dateFormat.parse(attrs.getValue("endTime"));
	    } catch (ParseException e) {
		throw new SAXParseException
		    ("Couldn't parse date: " + e.getMessage(), null, e);
	    }
	    GregorianCalendar gc = new GregorianCalendar();
	    gc.setTime(startDate);
	    startTime = gameCalendar.getTimeFromCalendar(gc);
	    gc.setTime(endDate);
	    endTime = gameCalendar.getTimeFromCalendar(gc);
	} else if ("Param".equals(qName)) {
	    String name = attrs.getValue("name");
	    String value = attrs.getValue("value");
	    attributes.put(name, value);
	}
    }

    public void endElement(String sName, String qName) throws
	SAXParseException {
	if ("ScriptingEvent".equals(qName)) {
		ScriptingEvent se = ScriptingEvent.createEvent
		   (eventName, world, startTime, endTime, attributes);
		world.add(KEY.SCRIPTING_EVENTS, se, Player.AUTHORITATIVE);
	}
    }
}
