/*
 * Created on 26-May-2003
 *
 */
package jfreerails.move;

import jfreerails.world.cargo.CargoBundle;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;


/**
 * This Move adds a cargo bundle to the cargo bundle list.
 *
 * @author Luke
 *
 */
public class AddCargoBundleMove extends AddItemToListMove {
    public AddCargoBundleMove(int i, CargoBundle item, FreerailsPrincipal p) {
        super(KEY.CARGO_BUNDLES, i, item, p);
    }
}