/**@author Scott Bennett
 * Date: 3rd April 2003
 *
 * Class to render the city names on the game map. Names are retrieved
 * from the KEY.CITIES object.
 */
package jfreerails.client.renderer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import jfreerails.client.common.Painter;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.city.CityModel;


public class CityNamesRenderer implements Painter {
    private ReadOnlyWorld w;

    public CityNamesRenderer(ReadOnlyWorld world) {
        this.w = world;
    }

    public void paint(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", 0, 20));

        //draw city names onto map
        for (int i = 0; i < w.size(KEY.CITIES); i++) {
            CityModel tempCity = (CityModel)w.get(KEY.CITIES, i);
            g.drawString(tempCity.getCityName(), tempCity.getCityX() * 30,
                tempCity.getCityY() * 30 + 10);
        }
    }
}