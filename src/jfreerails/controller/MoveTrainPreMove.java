/*
 * Created on 18-Feb-2005
 *
 */
package jfreerails.controller;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import jfreerails.move.Move;
import jfreerails.world.common.GameTime;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.common.PositionOnTrack;
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
	 * <ol type="i">
	 * 	<li>the train is moving and a new train position is due.</li>
	 * 	<li>the train is waiting for a full load and there is more cargo to add.</li>
	 * 	<li>the train is stopped but due to start moving.</li>
	 * </ol>
	 */
	public boolean canGenerateMove(ReadOnlyWorld w){
		return false;
	}
	
	public Move generateMove(ReadOnlyWorld w) {	
		//Get current position.
		
		
		return null;
	}
	
	OneTileMoveVector nextVector(ReadOnlyWorld w){
		//Find current position.
		TrainAccessor ta = new TrainAccessor(w, principal, trainID);
		KEY k = ta.getLastKEY();
		TrainMotion lastMotion = (TrainMotion)w.get(k, trainID, principal);				
		PositionOnTrack currentPosition = lastMotion.getFinalPosition();
	
		//Find targets						
		Point targetPoint = ta.getTarget();
		//Code copied from TrainPathFinder
		PositionOnTrack[] t = FlatTrackExplorer.getPossiblePositions(w,
				targetPoint);
		int[] targets = new int[t.length];

		for (int i = 0; i < t.length; i++) {
			int target = t[i].getOpposite().toInt();			
			targets[i] = target;
		}
				
		//Use path finder to decide where to go.
		
		FlatTrackExplorer tempExplorer = new FlatTrackExplorer(w, currentPosition);
		SimpleAStarPathFinder pathFinder = new SimpleAStarPathFinder();
		int next = pathFinder.findstep(currentPosition.toInt(), targets, tempExplorer);		
		PositionOnTrack nextPosition = new PositionOnTrack(next);
		//XXX should really be nextPosition.facing(), but that produces the wrong result!
		//I.e. the code somewhere else has facing and camefrom the wrong way round.
		return nextPosition.cameFrom();
	}
		
	
	SpeedAgainstTime nextSpeeds(ReadOnlyWorld w, OneTileMoveVector v, GameTime t){
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
		
		//Over estimate the time to travel distance.
		GameTime pastEnd = new GameTime(s / v1 + t1+1);		
		GameTime[] times = {t, new GameTime(t1), pastEnd};
		int[] speed = {u, v1, v1};
		SpeedAgainstTime speeds = new SpeedAgainstTime(times, speed);
		//Find the time when we have just travelled the desired distance.
		GameTime end = speeds.getTime(s);
		SpeedAgainstTime clippedSpeeds = speeds.subSection(t, end);				
		return clippedSpeeds;
	}
		
	
	TrainMotion nextMotion(ReadOnlyWorld w, OneTileMoveVector v, GameTime t){
		
		
		return null;
	}
	
	int acceleration(int wagons){
		return 1;
	}
	
	int topSpeed(int wagons){
		return 100 / wagons;
	}
	
	

}
