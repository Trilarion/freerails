/*
 * MarkAsCrashSiteMove.java
 *
 * Created on February 8, 2005, 9:38 PM
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
public class MarkAsCrashSiteMove extends ChangeItemInListMove {

	private static final long serialVersionUID = 3545795468467975477L;

	/** Creates a new instance of MarkAsCrashSiteMove */
	private MarkAsCrashSiteMove(int index, FreerailsSerializable before,
			FreerailsSerializable after, FreerailsPrincipal p) {
		super(KEY.TRAIN_POSITIONS, index, before, after, p);
	}

	public static MarkAsCrashSiteMove generateMove(int index, ReadOnlyWorld w,
			FreerailsPrincipal p) {
		TrainPositionOnMap pos = (TrainPositionOnMap) w.get(
				KEY.TRAIN_POSITIONS, index, p);
		TrainPositionOnMap newPos = TrainPositionOnMap.createInstance(pos
				.getXPoints(), pos.getYPoints());
		newPos.setCrashSite(true);

		return new MarkAsCrashSiteMove(index, pos, newPos, p);
	}
}
