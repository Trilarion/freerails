/*
 * Created on 04-Mar-2005
 *
 */
package jfreerails.world.train;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.GameTime;

/**
 * @author Luke
 *
 */
public final class TrainStatus implements FreerailsSerializable {

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrainStatus)) return false;

        final TrainStatus trainStatus = (TrainStatus) o;

        if (!dontUpdateUntil.equals(trainStatus.dontUpdateUntil)) return false;
        if (!status.equals(trainStatus.status)) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = dontUpdateUntil.hashCode();
        result = 29 * result + status.hashCode();
        return result;
    }

    private static final long serialVersionUID = 3545233652419081264L;

	public enum Status {STOPPED_AT_SIGNAL, WAITING_FOR_FULL_LOAD, LOADING_AND_UNLOADING, MOVING};
	
	private final GameTime dontUpdateUntil;
	
	private final Status status;
	
	public TrainStatus(GameTime t, Status s){
		dontUpdateUntil = t;
		status = s;
	}
	
	public TrainStatus(Status s){
		dontUpdateUntil = GameTime.BIG_BANG;
		status = s;
	}

	public GameTime getDontUpdateUntil() {
		return dontUpdateUntil;
	}
	public Status getStatus() {
		return status;
	}
}
