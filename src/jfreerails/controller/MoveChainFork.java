package jfreerails.controller;

import java.util.ArrayList;

import jfreerails.move.Move;
import jfreerails.move.MoveStatus;

/**
 * @version 	1.0
 * 
 */
final public class MoveChainFork implements MoveReceiver {
	
	private final ArrayList moveReceivers = new ArrayList(); 
	
	private final MoveReceiver primary;
	
	public MoveChainFork(MoveReceiver primaryReceiver){
		this.primary=primaryReceiver;		
	}
	
	public void add(MoveReceiver moveReceiver){
		moveReceivers.add(moveReceiver);	
	}

	/*
	 * @see MoveReceiver#processMove(Move)
	 */
	public MoveStatus processMove(Move move) {
		MoveStatus ms=primary.processMove(move);
		for(int i=0;i<moveReceivers.size();i++){
				MoveReceiver m = (MoveReceiver)moveReceivers.get(i);
				m.processMove(move);
		}
		return ms;
	}

}
