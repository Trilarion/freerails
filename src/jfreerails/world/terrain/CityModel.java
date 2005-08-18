/**
 * @author Scott Bennett
 * Date 31st March 2003
 *
 * Class for a city. Simply storing the city name and x & y co-ords.
 * Possible potential for expansion?? Initial size of city, growth rate etc.???
 */
package jfreerails.world.terrain;

import jfreerails.world.common.FreerailsSerializable;

/**
 * A city.
 * 
 * @author Luke
 */
public class CityModel implements FreerailsSerializable {
	private static final long serialVersionUID = 3256720697500709428L;

	private final String name;

	private final int x;

	private final int y;

	public CityModel(String s, int xx, int yy) {
		name = s;
		x = xx;
		y = yy;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof CityModel))
			return false;

		final CityModel cityModel = (CityModel) o;

		if (x != cityModel.x)
			return false;
		if (y != cityModel.y)
			return false;
		if (!name.equals(cityModel.name))
			return false;

		return true;
	}

	public int hashCode() {
		int result;
		result = name.hashCode();
		result = 29 * result + x;
		result = 29 * result + y;
		return result;
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

	@Override
	public String toString() {		
		return name+" "+x+", "+y;
	}
}