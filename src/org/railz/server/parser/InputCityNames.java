/*
 * Copyright (C) Scott Bennett
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

/**
 * @author Scott Bennett
 * Date: 31st March 2003
 *
 * Class that calls the object to input the City names and co-ords from an xml file.
 */
package org.railz.server.parser;

import java.io.*;
import java.net.URL;
import java.util.logging.*;
import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

import org.railz.world.top.World;
import org.railz.util.*;

public class InputCityNames {
    private World world;
    private static final Logger logger = Logger.getLogger("global");

    public InputCityNames(World w, URL filename) throws SAXException {
        world = w;

        InputSource is = new InputSource(filename.toString());

        DefaultHandler handler = new MapHandler(world);
        SAXParserFactory factory = SAXParserFactory.newInstance();
	factory.setValidating(true);

        logger.log(Level.INFO, "\nLoading XML " + filename);

        try {
            SAXParser saxParser = factory.newSAXParser();
	    EntityResolver er = new WorkaroundResolver
		(saxParser.getXMLReader().getEntityResolver());
	    saxParser.getXMLReader().setEntityResolver(er);
	    // can't use saxParser.parse() as this causes our EntityResolver
	    // to be ignored
	    saxParser.getXMLReader().setContentHandler(handler);
            saxParser.getXMLReader().parse(is);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        }
    }
}
