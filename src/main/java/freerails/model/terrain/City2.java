package freerails.model.terrain;

import freerails.model.Identifiable;
import freerails.util.Vec2D;

/**
 * Simply stores the name and x and y coordinates of a city.
 */
public class City2 extends Identifiable {

    private final String name;
    private final Vec2D location;

    public City2(int id, String name, Vec2D location) {
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
}
