/*
 * Created on 26-May-2003
 *
 */
package freerails.move;

import freerails.world.cargo.ImmutableCargoBundle;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.KEY;

/**
 * This Move adds a cargo bundle to the cargo bundle list.
 *
 */
public class AddCargoBundleMove extends AddItemToListMove {
    private static final long serialVersionUID = 3257288049795674934L;

    /**
     *
     * @param i
     * @param item
     * @param p
     */
    public AddCargoBundleMove(int i, ImmutableCargoBundle item,
                              FreerailsPrincipal p) {
        super(KEY.CARGO_BUNDLES, i, item, p);
    }
}