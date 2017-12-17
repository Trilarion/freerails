/*
  @author Scott Bennett
 * Date: 3rd April 2003
 * <p>
 * Class to render the city names on the game map. Names are retrieved
 * from the KEY.CITIES object.
 */
package freerails.client.renderer;

import freerails.client.common.Painter;
import freerails.world.Constants;
import freerails.world.terrain.CityModel;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.SKEY;

import java.awt.*;

/**
 * Paints the city names on the map.
 *
 * @author Scott
 */
public class CityNamesRenderer implements Painter {
    private final ReadOnlyWorld w;

    public CityNamesRenderer(ReadOnlyWorld world) {
        this.w = world;
    }

    public void paint(Graphics2D g, Rectangle newVisibleRectectangle) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", 0, 20));

        // draw city names onto map
        int size = w.size(SKEY.CITIES);
        for (int i = 0; i < size; i++) {
            CityModel tempCity = (CityModel) w.get(SKEY.CITIES, i);
            final int xpos = tempCity.getCityX() * Constants.TILE_SIZE;
            final int ypos = tempCity.getCityY() * Constants.TILE_SIZE + 10;
            Rectangle cityNameBox = new Rectangle(xpos, ypos,
                    Constants.TILE_SIZE * 8, 20);
            if (newVisibleRectectangle != null
                    && !newVisibleRectectangle.intersects(cityNameBox)) {
                continue;
            }
            g.drawString(tempCity.getCityName(), xpos, ypos);
        }
    }
}