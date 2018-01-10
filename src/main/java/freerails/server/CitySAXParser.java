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

package freerails.server;

import freerails.world.SKEY;
import freerails.world.World;
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
     * @param w
     * @throws SAXException
     */
    public CitySAXParser(World w) {
        world = w;
        cities = new ArrayList();
    }

    @Override
    public void endDocument() {
        for (int i = 0; i < cities.size(); i++) {
            City tempCity = cities.get(i);
            world.add(SKEY.CITIES, new City(tempCity.getName(),
                    tempCity.getX(), tempCity.getY()));
        }
    }

    @Override
    public void startElement(String namespaceURI, String sName, String qName,
                             Attributes attrs) {

        String cityName = null;
        int x = 0;
        int y;

        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                String aName = attrs.getLocalName(i); // Attr name

                if (aName.isEmpty()) {
                    aName = attrs.getQName(i);
                }

                // put values in City obj
                if (aName.equals("name")) {
                    cityName = attrs.getValue(i);
                }

                if (aName.equals("x")) {
                    x = Integer.parseInt(attrs.getValue(i));
                }

                if (aName.equals("y")) {
                    y = Integer.parseInt(attrs.getValue(i));

                    City city = new City(cityName, x, y);
                    cities.add(city);
                }
            }
        }
    }
}