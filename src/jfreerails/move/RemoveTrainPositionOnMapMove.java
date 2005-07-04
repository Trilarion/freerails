/*
 * RemoveTrainPositionOnMapMove.java
 *
 * Created on February 8, 2005, 2:04 PM
 */

package jfreerails.move;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.train.TrainPositionOnMap;

/**
 * 
 * @author cphillips
 */
public class RemoveTrainPositionOnMapMove extends RemoveItemFromListMove {

	private static final long serialVersionUID = 3258415044869043509L;

	private RemoveTrainPositionOnMapMove(int index,
			FreerailsSerializable position, FreerailsPrincipal p) {
		super(KEY.TRAIN_POSITIONS, index, position, p);
	}

	public static Move generateMove(int index, FreerailsPrincipal p,
			ReadOnlyWorld world) {

		FreerailsSerializable position = (TrainPositionOnMap) world.get(
				KEY.TRAIN_POSITIONS, index, p);

		return new RemoveTrainPositionOnMapMove(index, position, p);
	}

}
