/*
 * Created on 26-May-2003
 *
 */
package jfreerails.move;

import jfreerails.world.cargo.ImmutableCargoBundle;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;

/**
 * This move removes a cargo bundle from the cargo bundle list.
 * 
 * @author Luke
 * 
 */
public class RemoveCargoBundleMove extends RemoveItemFromListMove {
	private static final long serialVersionUID = 3762247522239723316L;

	public RemoveCargoBundleMove(int i, ImmutableCargoBundle item,
			FreerailsPrincipal p) {
		super(KEY.CARGO_BUNDLES, i, item, p);
	}
}