/*
 * Created on 18-Feb-2005
 *
 */
package jfreerails.controller;

import java.util.ArrayList;
import java.util.List;

import jfreerails.move.Move;
import jfreerails.world.common.GameTime;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.train.SpeedAgainstTime;
import jfreerails.world.train.TrainMotion;

/**
 * Generates moves for changes in train position and stops at stations.
 * 
 * @author Luke
 *
 */
public class MoveTrainPreMove implements PreMove {

	private static final long serialVersionUID = 3545516188269491250L;
	private static final long interval = 1000; //1 second.
	private final int trainID;
	private  final FreerailsPrincipal principal;
	private List<Move> moves = new ArrayList<Move>();
	
	public MoveTrainPreMove(int id, FreerailsPrincipal p){		
		trainID = id;
		principal = p;
	}
	
	/** Returns true if 
	 * 	the train is moving and a new train position is due.
	 * 	the train is waiting for a full load and there is more cargo to add.
	 * 	the train is stopped but due to start moving.
	 */
	public boolean canGenerateMove(ReadOnlyWorld w){
		return false;
	}
	
	public Move generateMove(ReadOnlyWorld w) {	
		//Get current position.
		
		
		return null;
	}
	
	TrainMotion nextMotion(ReadOnlyWorld w, OneTileMoveVector v, GameTime t){
		TrainAccessor ta = new TrainAccessor(w, principal, trainID);
		KEY k = ta.getLastKEY();
		TrainMotion lastMotion = (TrainMotion)w.get(k, trainID, principal);		
		int u = lastMotion.getSpeed(t);
		int s = v.getLength();
		int wagons = ta.getTrain().getNumberOfWagons();
		int a0 = acceleration(wagons);
		int v1 = topSpeed(wagons);
		int t0 = t.getTime();
		int t1 = ((v1  - u) / a0) + t0;
		GameTime[] times = {t, new GameTime(t1), GameTime.END_OF_THE_WORLD};
		int[] speed = {u, v1, v1};
		SpeedAgainstTime speeds = new SpeedAgainstTime(times, speed);
		GameTime end = speeds.getTime(s);
		
		return null;
	}
	
	int acceleration(int wagons){
		return 1;
	}
	
	int topSpeed(int wagons){
		return 100 / wagons;
	}
	
	

}
