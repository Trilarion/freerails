/*
 * Created on 26-May-2003
 *
 */
package jfreerails.move;

import jfreerails.world.cargo.CargoBundle;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;


/**
 * This move removes a cargo bundle from the cargo bundle list.
 * @author Luke
 *
 */
public class RemoveCargoBundleMove extends RemoveItemFromListMove {
    public RemoveCargoBundleMove(int i, CargoBundle item, FreerailsPrincipal p) {
        super(KEY.CARGO_BUNDLES, i, item, p);
    }
}