/*
 * Created on 27-Apr-2003
 *
 */
package jfreerails.world.terrain;

import jfreerails.world.common.FreerailsSerializable;


/**This class represents the conversion of one cargo type to another one
 * a tile.
 * @author Luke
 *
 */
public class Conversion implements FreerailsSerializable {
    private final int input;
    private final int output;

    public Conversion(int in, int out) {
        input = in;
        output = out;
    }

    public int getInput() {
        return input;
    }

    public int getOutput() {
        return output;
    }
}