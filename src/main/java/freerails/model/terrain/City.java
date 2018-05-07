package freerails.model.terrain;

import freerails.model.Identifiable;
import freerails.util.Vec2D;

// TODO Possible potential for expansion?? Initial size of city, growth rate etc.??? incorporate CityModel?
/**
 * Simply stores the name and x and y coordinates of a city.
 */
public class City extends Identifiable {

    private final String name;
    private final Vec2D location;

    public City(int id, String name, Vec2D location) {
        super(id);
        this.name = name;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public Vec2D getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return String.format("%s %s", name, location);
    }
}
