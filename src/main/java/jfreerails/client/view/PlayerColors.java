/*
 * Created on Sep 6, 2004
 *
 */
package jfreerails.client.view;

import java.awt.Color;

/**
 * Stores a list of colours to use to represent different players.
 * 
 * @author Luke
 * 
 */
public class PlayerColors {

    private static final Color[] colors = new Color[] { Color.BLUE,
            Color.GREEN, Color.CYAN, Color.MAGENTA, Color.ORANGE, Color.YELLOW };// Save

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

    public static Color getColor(int playerNumber) {
        return colors[playerNumber % colors.length];
    }

}
