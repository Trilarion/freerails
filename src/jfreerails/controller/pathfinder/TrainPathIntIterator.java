package jfreerails.controller.pathfinder;

import jfreerails.util.IntIterator;

/**
 * 30-Nov-2002
 * @author Luke Lindsay
 *
 */
public class TrainPathIntIterator implements IntIterator {
	
	public FlatTrackExplorer trackExplorer;
	
	public TrainPathIntIterator(FlatTrackExplorer t){
		trackExplorer = t;		
	}
	
	public boolean hasNextInt(){
		return trackExplorer.hasNextBranch();
	}
	
	public int nextInt(){
		trackExplorer.nextBranch();
		trackExplorer.moveForward();
		return trackExplorer.getPosition();
	}		
}
