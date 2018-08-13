package freerails.model.terrain;

import freerails.model.world.UnmodifiableWorld;
import freerails.util.Vec2D;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;

/**
 *
 */
public final class CityUtils {

    private CityUtils() {
    }

    /**
     * Finds the nearest city and returns that name, so that a station can be
     * named appropriately.
     *
     * @return
     */
    public static String findNearestCity(@NotNull UnmodifiableWorld world, @NotNull Vec2D location) {
        double closestDistance = Double.MAX_VALUE;
        String cityName = null;

        for (City city: world.getCities()) {
            Vec2D delta = Vec2D.subtract(location, city.getLocation());
            double distance = delta.norm();
            if (distance < closestDistance) {
                closestDistance = distance;
                cityName = city.getName();
            }
        }

        if (cityName != null) {
            return cityName;
        } else {
            throw new NoSuchElementException();
        }
    }
}
