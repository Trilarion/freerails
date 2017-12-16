/*
 * Created on 26-May-2003
 *
 */
package freerails.move;

import freerails.world.cargo.ImmutableCargoBundle;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.KEY;

/**
 * This move removes a cargo bundle from the cargo bundle list.
 *
 * @author Luke
 */
public class RemoveCargoBundleMove extends RemoveItemFromListMove {
    private static final long serialVersionUID = 3762247522239723316L;

    public RemoveCargoBundleMove(int i, ImmutableCargoBundle item,
                                 FreerailsPrincipal p) {
        super(KEY.CARGO_BUNDLES, i, item, p);
    }
}