/*
 * Created on 18-Feb-2005
 *
 */
package jfreerails.controller;

import java.util.ArrayList;
import java.util.List;

import jfreerails.move.Move;
import jfreerails.world.common.GameTime;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ReadOnlyWorld;

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

}
