/**
 * @author Scott Bennett
 * Date 31st March 2003
 *
 * Class for a city. Simply storing the city name and x & y co-ords.
 * Possible potential for expansion?? Initial size of city, growth rate etc.???
 */
package jfreerails.world.terrain;

import jfreerails.world.common.FreerailsSerializable;


/** A city.
 * @author Luke
 */
public class CityModel implements FreerailsSerializable {
    private final String name;
    private final int x;
    private final int y;

    public CityModel(String s, int xx, int yy) {
        name = s;
        x = xx;
        y = yy;
    }

    public String getCityName() {
        return name;
    }

    public int getCityX() {
        return x;
    }

    public int getCityY() {
        return y;
    }
}