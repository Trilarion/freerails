package jfreerails.controller;

import java.util.ArrayList;

import jfreerails.move.Move;

/**
 * @version 	1.0
 * 
 * A central point at which a client may register to receive moves which have
 * been committed.
 */
final public class MoveChainFork implements MoveReceiver {
	
	private final ArrayList moveReceivers = new ArrayList(); 
	
	private MoveReceiver primary;
	
	public MoveChainFork(){
	    // do nothing
	}

	public void setPrimaryReceiver(MoveReceiver primaryReceiver) {
	    primary = primaryReceiver;
	}
	
	public void remove(MoveReceiver moveReceiver) {
	    System.out.println("MoveReceiver.remove(" + moveReceiver + ")");
	    moveReceivers.remove(moveReceiver);
	}
	
	public void add(MoveReceiver moveReceiver){
	    System.out.println("MoveReceiver.add(" + moveReceiver + ")");
		moveReceivers.add(moveReceiver);	
	}

	/*
	 * @see MoveReceiver#processMove(Move)
	 */
	public void processMove(Move move) {
	    if (primary != null) {
		primary.processMove(move);
	    }
		
	    for(int i=0;i<moveReceivers.size();i++){
		MoveReceiver m = (MoveReceiver)moveReceivers.get(i);
		m.processMove(move);
	    }
	}
}
