/*
 * Created on 04-Mar-2005
 *
 */
package jfreerails.controller;

import jfreerails.world.cargo.ImmutableCargoBundle;
import jfreerails.world.common.GameTime;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.train.ImmutableSchedule;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainMotion;
import jfreerails.world.train.TrainStatus;

/**
 * Provides convenience methods to access the properties of a train from the world object.
 * 
 * @author Luke
 *
 */
public class TrainAccessor {
	
	private final ReadOnlyWorld w;
	private final FreerailsPrincipal p;
	private final int id;

	public TrainAccessor(final ReadOnlyWorld w, final FreerailsPrincipal p,
			final int id) {		
		this.w = w;
		this.p = p;
		this.id = id;
	}	

	public int getId() {
		return id;
	}
		
		
	public TrainMotion findCurrentMotion(GameTime time) {
		int t = time.getTime();
		TrainMotion motionA, motionB;
		motionA = (TrainMotion)w.get(KEY.TRAIN_MOTION1, id, p);
		motionB = (TrainMotion)w.get(KEY.TRAIN_MOTION2, id, p);
		TrainMotion first, second;		
		int startA = motionA.getStart().getTime();
		int startB = motionB.getStart().getTime();
		if(startA < startB){
			first = motionA;
			second = motionB;
		}else{
			first = motionB;
			second = motionA;
		}
		int start = first.getStart().getTime();
		int end = second.getEnd().getTime();
		
		if(t > end) throw new IllegalArgumentException();
		if(t < start) throw new IllegalArgumentException();
		
		int secondStart = second.getStart().getTime();
		
		TrainMotion currentMotion = secondStart > t ? first : second;
		return currentMotion;
	}
	
	public KEY getFirstKEY(){
		TrainMotion motionA, motionB;
		motionA = (TrainMotion)w.get(KEY.TRAIN_MOTION1, id, p);
		motionB = (TrainMotion)w.get(KEY.TRAIN_MOTION2, id, p);		
		int startA = motionA.getStart().getTime();
		int startB = motionB.getStart().getTime();
		if(startA < startB){
			return KEY.TRAIN_MOTION1;
		}
		return KEY.TRAIN_MOTION2;		
	}
	
	public KEY getLastKEY(){
		TrainMotion motionA, motionB;
		motionA = (TrainMotion)w.get(KEY.TRAIN_MOTION1, id, p);
		motionB = (TrainMotion)w.get(KEY.TRAIN_MOTION2, id, p);		
		int startA = motionA.getStart().getTime();
		int startB = motionB.getStart().getTime();
		if(startA < startB){
			return KEY.TRAIN_MOTION2;
		}
		return KEY.TRAIN_MOTION1;		
	}
	
	public TrainModel getTrain(){
		return (TrainModel)w.get(KEY.TRAINS, id, p);
	}
	
	public ImmutableSchedule getSchedule(){
		TrainModel train = getTrain();
		return (ImmutableSchedule)w.get(KEY.TRAIN_SCHEDULES, train.getScheduleID(), p);
	}
	
	public ImmutableCargoBundle getCargoBundle(){
		TrainModel train = getTrain();
		return (ImmutableCargoBundle)w.get(KEY.CARGO_BUNDLES, train.getCargoBundleID(), p);
	}
	
	public TrainStatus getStatus(){
		return (TrainStatus)w.get(KEY.TRAIN_STATUS, id, p);
	}
	
}
