/*
 * Created on 24-May-2003
 * 
 */
package jfreerails.move;

import jfreerails.world.cargo.CargoBundle;
import jfreerails.world.top.KEY;

/**
 * @author Luke
 * 
 */
public class ChangeCargoBundleMove extends ChangeItemInListMove {
			
	public ChangeCargoBundleMove(CargoBundle before, CargoBundle after, int bundleNumber){
		super(KEY.CARGO_BUNDLES, bundleNumber, before, after);		
	}
}
