/**
 * @author Scott Bennett
 *
 * Date: 12th April 2003
 *
 * Class to find the nearest city and return that name, so that a train station
 * can be named appropriately.
 */
package jfreerails.controller;

import jfreerails.world.city.CityModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;


public class CalcNearestCity {
    private int x;
    private int y;
    private ReadOnlyWorld w;

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

        if (w.size(KEY.CITIES) > 0) {
            tempCity = (CityModel)w.get(KEY.CITIES, 0);
            cityDistance = getDistance(tempCity.getCityX(), tempCity.getCityY());
            cityName = tempCity.getCityName();

            for (int i = 1; i < w.size(KEY.CITIES); i++) {
                tempCity = (CityModel)w.get(KEY.CITIES, i);
                tempDistance = getDistance(tempCity.getCityX(),
                        tempCity.getCityY());

                if (tempDistance < cityDistance) {
                    cityDistance = tempDistance;
                    cityName = tempCity.getCityName();
                }
            }

            return cityName;
        }

        return null;
    }

    public double getDistance(int cityX, int cityY) {
        double distance = 0;
        double a = (this.x - cityX) * (this.x - cityX);
        double b = (this.y - cityY) * (this.y - cityY);

        distance = Math.sqrt(a + b);

        return distance;
    }
}