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
