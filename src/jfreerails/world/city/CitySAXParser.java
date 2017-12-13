/**
 * @author Scott Bennett
 * Date: 31st March 2003
 *
 * Class to parse an xml file that contains city names and co-ords.
 * Upon reading in the data, its stored in KEY.CITIES.
 */
package jfreerails.world.city;

import java.util.Vector;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import jfreerails.world.top.World;
import jfreerails.world.top.KEY;
import jfreerails.world.player.Player;


public class CitySAXParser extends DefaultHandler {
    private Vector cities;
    private World world;

    public CitySAXParser(World w) throws SAXException {
        world = w;
        cities = new Vector();
    }

    public void endDocument() throws SAXException {
        for (int i = 0; i < cities.size(); i++) {
            CityModel tempCity = (CityModel)cities.elementAt(i);
            world.add(KEY.CITIES,
                new CityModel(tempCity.getCityName(), tempCity.getCityX(),
                    tempCity.getCityY()), Player.AUTHORITATIVE);
        }
    }

    public void startElement(String namespaceURI, String sName, String qName,
        Attributes attrs) throws SAXException {
        String eName = sName; //element name

        String cityName = null;
        int x = 0;
        int y = 0;

        if (eName.equals("")) {
            eName = qName;
        }

        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                String aName = attrs.getLocalName(i); //Attr name

                if (aName.equals("")) {
                    aName = attrs.getQName(i);
                }

                //put values in CityModel obj
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

            //end for loop
        }

        //end if
    }
    //end startElement method
}