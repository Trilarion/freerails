/*
 * Created on 28-Mar-2003
 *
 */
package jfreerails.world.station;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.ImInts;

/**
 * This class represents the blue print for what an engine shop is producing.
 * 
 * @author Luke
 * 
 */
public class PlannedTrain implements FreerailsSerializable {
	private static final long serialVersionUID = 3545515106038592057L;

	private final int engineType;

	private final ImInts wagonTypes;

	public PlannedTrain(int e, int[] wagons) {
		engineType = e;
		wagonTypes = new ImInts(wagons);
	}

	public int hashCode() {
		return engineType;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof PlannedTrain))
			return false;

		final PlannedTrain productionAtEngineShop = (PlannedTrain) o;

		if (engineType != productionAtEngineShop.engineType)
			return false;
		if (!wagonTypes.equals(productionAtEngineShop.wagonTypes))
			return false;

		return true;
	}

	public int getEngineType() {
		return engineType;
	}

	public ImInts getWagonTypes() {
		return wagonTypes;
	}

	public String toString() {
		return "engine type: " + this.engineType + ", with "
				+ wagonTypes.size() + "wagons";
	}
}