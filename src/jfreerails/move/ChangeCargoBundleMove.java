/*
 * Created on 24-May-2003
 *
 */
package jfreerails.move;

import jfreerails.world.cargo.CargoBundle;
import jfreerails.world.top.KEY;


/**
 * This {@link Move} changes a cargo bundle (cargo bundles are used to represent
 * the cargo carried by trains and the cargo waiting at stations).
 * @author Luke
 *
 */
public class ChangeCargoBundleMove extends ChangeItemInListMove {
    public ChangeCargoBundleMove(CargoBundle before, CargoBundle after,
        int bundleNumber) {
        super(KEY.CARGO_BUNDLES, bundleNumber, before, after);
    }
}