/*
 * Created on Sep 6, 2004
 *
 */
package freerails.client.view;

import java.awt.*;

/**
 * Stores a list of colours to use to represent different players.
 *
 */
public class PlayerColors {

    private static final Color[] colors = new Color[]{Color.BLUE,
            Color.GREEN, Color.CYAN, Color.MAGENTA, Color.ORANGE, Color.YELLOW};// Save

    // red
    // for
    // when
    // we
    // need
    // to
    // grab
    // the
    // player's
    // attention!

    /**
     *
     * @param playerNumber
     * @return
     */

    public static Color getColor(int playerNumber) {
        return colors[playerNumber % colors.length];
    }

}
