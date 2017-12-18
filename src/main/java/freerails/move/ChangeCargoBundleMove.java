/*
 * Created on 24-May-2003
 *
 */
package freerails.move;

import freerails.world.cargo.ImmutableCargoBundle;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.KEY;

/**
 * This {@link Move} changes a cargo bundle (cargo bundles are used to represent
 * the cargo carried by trains and the cargo waiting at stations).
 *
 * @author Luke
 */
public class ChangeCargoBundleMove extends ChangeItemInListMove {
    private static final long serialVersionUID = 3258126960072143408L;

    /**
     *
     * @param before
     * @param after
     * @param bundleNumber
     * @param p
     */
    public ChangeCargoBundleMove(ImmutableCargoBundle before,
                                 ImmutableCargoBundle after, int bundleNumber, FreerailsPrincipal p) {
        super(KEY.CARGO_BUNDLES, bundleNumber, before, after, p);
    }
}