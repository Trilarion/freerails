package jfreerails.controller;

import java.util.NoSuchElementException;

import jfreerails.world.terrain.CityModel;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;


/**
 *
 *
 * Class to find the nearest city and return that name, so that a station
 * can be named appropriately.
 * Date: 12th April 2003
 * @author Scott Bennett
 */
public class CalcNearestCity {
    private final int x;
    private final int y;
    private final ReadOnlyWorld w;

    public CalcNearestCity(ReadOnlyWorld world, int x, int y) {
        this.w = world;
        this.x = x;
        this.y = y;
    }

    public String findNearestCity() {
        double cityDistance;
        String cityName = null;
        double tempDistance;
        CityModel tempCity;

        if (w.size(SKEY.CITIES) > 0) {
            tempCity = (CityModel)w.get(SKEY.CITIES, 0);
            cityDistance = getDistance(tempCity.getCityX(), tempCity.getCityY());
            cityName = tempCity.getCityName();

            for (int i = 1; i < w.size(SKEY.CITIES); i++) {
                tempCity = (CityModel)w.get(SKEY.CITIES, i);
                tempDistance = getDistance(tempCity.getCityX(),
                        tempCity.getCityY());

                if (tempDistance < cityDistance) {
                    cityDistance = tempDistance;
                    cityName = tempCity.getCityName();
                }
            }

            return cityName;
        }

        throw new NoSuchElementException();
    }

    private double getDistance(int cityX, int cityY) {
        double distance = 0;
        double a = (this.x - cityX) * (this.x - cityX);
        double b = (this.y - cityY) * (this.y - cityY);

        distance = Math.sqrt(a + b);

        return distance;
    }
}