/*
 * Created on 18-Feb-2005
 *
 */
package jfreerails.controller;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.common.PositionOnTrack;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.train.ImmutableSchedule;
import jfreerails.world.train.PathOnTiles;
import jfreerails.world.train.SpeedAgainstTime;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainMotion;

/**
 * @author Luke
 *
 */
public class AddTrainPreMove {
	
	private final int engineTypeId;
	private final int[] wagons;
	private final Point point;
	private final FreerailsPrincipal principal;
	private final ImmutableSchedule schedule;
	
	
    public AddTrainPreMove(int e, int[] wags, Point p,
        FreerailsPrincipal fp, ImmutableSchedule s) { 
    	engineTypeId = e;
    	wagons = wags;
    	point = p;
    	principal = fp;
    	schedule = s;
    }
    
    PathOnTiles initPositionStep1(ReadOnlyWorld w){
    	PositionOnTrack[] pp = FlatTrackExplorer.getPossiblePositions(w, point);
    	FlatTrackExplorer fte = new FlatTrackExplorer(w, pp[0]);
    		    	
    	List<OneTileMoveVector> vectors = new ArrayList<OneTileMoveVector>();
    	int length = calTrainLength();
    	int distanceTravelled = 0;
    	PositionOnTrack p = new PositionOnTrack();
    	while(distanceTravelled < length){
    		fte.nextEdge();
    		fte.moveForward();
    		p.setValuesFromInt(fte.getPosition());
    		OneTileMoveVector v = p.cameFrom();
    		distanceTravelled += v.getLength();
    		vectors.add(v);
    		
    	}
    	return new PathOnTiles(point, vectors);
    }

	private int calTrainLength() {
		TrainModel train = new TrainModel(engineTypeId, wagons, 0);
		int length = train.getLength();
		return length;
	}
    
    TrainMotion initPositionStep2(PathOnTiles path){
    	TrainMotion tm = new TrainMotion(path, calTrainLength(), SpeedAgainstTime.STOPPED);
    	return tm;
    }
    
    
        

}
