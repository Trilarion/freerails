/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.savegames;

import freerails.util.Vector2D;
import freerails.world.SKEY;
import freerails.world.world.World;
import freerails.world.terrain.City;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to parse an xml file that contains city names and coordinates. Upon reading
 * in the data, its stored in KEY.CITIES.
 */
public class CitySAXParser extends DefaultHandler {

    private final List<City> cities;
    private final World world;

    /**
     * @param world
     * @throws SAXException
     */
    public CitySAXParser(World world) {
        this.world = world;
        cities = new ArrayList<>();
    }

    @Override
    public void endDocument() {
        for (City city : cities) {
            // TODO why the copying here?
            world.add(SKEY.CITIES, new City(city.getName(), city.getLocation()));
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {

        // TODO are these initializations really needed
        String cityName = null;
        int x=0, y;

        // TODO when can attributes be null
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                String attributeName = attributes.getLocalName(i); // Attr name

                if (attributeName.isEmpty()) {
                    attributeName = attributes.getQName(i);
                }

                // put values in City obj
                if (attributeName.equals("name")) {
                    cityName = attributes.getValue(i);
                }

                if (attributeName.equals("x")) {
                    x = Integer.parseInt(attributes.getValue(i));
                }

                if (attributeName.equals("y")) {
                    y = Integer.parseInt(attributes.getValue(i));

                    // TODO is it clear that y always comes last?
                    City city = new City(cityName, new Vector2D(x, y));
                    cities.add(city);
                }
            }
        }
    }
}