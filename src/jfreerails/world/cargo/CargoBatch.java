package jfreerails.world.cargo;

import jfreerails.world.common.FreerailsSerializable;

/** This class represents a cargo batch (cargo of the same batch is cargo of the same type 
 * that was produced at the same location at the same time).
 * 
 * @author Luke
 */

public class CargoBatch implements FreerailsSerializable {

	private final int cargoType;

	private final int sourceX, sourceY;
	
	private final int stationOfOrigin;

	private final long timeCreated;

	public CargoBatch(int type, int x, int y, long time, int stationOfOrigin) {
		cargoType = type;
		sourceX = x;
		sourceY = y;
		timeCreated = time;
		this.stationOfOrigin = stationOfOrigin;
	}

	public int getStationOfOrigin() {
		return stationOfOrigin;
	}

	public int getCargoType() {
		return cargoType;
	}

	public int getSourceX() {
		return sourceX;
	}

	public int getSourceY() {
		return sourceY;
	}

	public long getTimeCreated() {
		return timeCreated;
	}
	
	public boolean equals(Object o) {
		if (o instanceof CargoBatch) {
			CargoBatch test = (CargoBatch) o;
			if (test.getCargoType() == this.cargoType
				&& test.getSourceX() == this.sourceX
				&& test.sourceY == this.sourceY
				&& test.timeCreated == this.timeCreated
				&& test.stationOfOrigin == this.stationOfOrigin) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public int hashCode() {
		int result = 17;
		result = 37 * result + this.cargoType;
		result = 37 * result + this.sourceX;
		result = 37 * result + this.sourceY;
		result = 37 * result + this.stationOfOrigin;
		result = 37 * result + (int)(this.timeCreated^(this.timeCreated>>>32));
		return result;
	}

}