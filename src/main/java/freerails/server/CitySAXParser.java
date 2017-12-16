package freerails.server;

import freerails.world.terrain.CityModel;
import freerails.world.top.SKEY;
import freerails.world.top.World;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Vector;

/**
 * Class to parse an xml file that contains city names and co-ords. Upon reading
 * in the data, its stored in KEY.CITIES.
 *
 * @author Scott Bennett Date: 31st March 2003
 */
public class CitySAXParser extends DefaultHandler {
    private final Vector<CityModel> cities;

    private final World world;

    public CitySAXParser(World w) throws SAXException {
        world = w;
        cities = new Vector<CityModel>();
    }

    @Override
    public void endDocument() throws SAXException {
        for (int i = 0; i < cities.size(); i++) {
            CityModel tempCity = cities.elementAt(i);
            world.add(SKEY.CITIES, new CityModel(tempCity.getCityName(),
                    tempCity.getCityX(), tempCity.getCityY()));
        }
    }

    @Override
    public void startElement(String namespaceURI, String sName, String qName,
                             Attributes attrs) throws SAXException {

        String cityName = null;
        int x = 0;
        int y = 0;

        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                String aName = attrs.getLocalName(i); // Attr name

                if (aName.equals("")) {
                    aName = attrs.getQName(i);
                }

                // put values in CityModel obj
                if (aName.equals("name")) {
                    cityName = attrs.getValue(i);
                }

                if (aName.equals("x")) {
                    x = Integer.parseInt(attrs.getValue(i));
                }

                if (aName.equals("y")) {
                    y = Integer.parseInt(attrs.getValue(i));

                    CityModel city = new CityModel(cityName, x, y);
                    cities.addElement(city);
                }
            }

            // end for loop
        }

        // end if
    }
    // end startElement method
}