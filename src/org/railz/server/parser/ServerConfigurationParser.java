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

import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.railz.util.*;

/**
 * Parses the serverConfiguration.xml file, which stores configuration
 * information for the server (maps, scenarios, etc)
 */
public class ServerConfigurationParser extends DefaultHandler {
    private ArrayList mapNames = new ArrayList();
    private static final Logger logger = Logger.getLogger("global");
    
    public String[] getMapNames() {
	return (String []) mapNames.toArray(new String[mapNames.size()]);
    }

    /** Create the class and start parsing */
    public ServerConfigurationParser(ModdableResourceFinder mrf) {
	try {
	    SAXParserFactory spf = SAXParserFactory.newInstance();
	    SAXParser sp = spf.newSAXParser();
	    sp.parse(mrf.getURLForReading("serverConfiguration.xml").toString(),
		    this);
	} catch (ParserConfigurationException e) {
	    logger.log(Level.SEVERE, "ServerConfigurationParser() caught " +
		    "ParserConfigurationException", e);
	} catch (SAXException e) {
	    logger.log(Level.SEVERE, "ServerConfigurationParser() caught " +
		    "SAXException", e);
	} catch (IOException e) {
	    logger.log(Level.SEVERE, "ServerConfigurationParser() caught " + 
		    "IOException", e);
	}
    }

    public void endElement(String uri, String localName, String qName) throws
	SAXException {
	    // do nothing
	}

    public void startElement(String uri, String localName, String qName,
	    Attributes attributes) {
	if (qName.equals("Map")) {
	    mapNames.add(attributes.getValue("name"));
	}
    }
}
