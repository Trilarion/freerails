package jfreerails.controller;

import jfreerails.move.Move;
import jfreerails.move.MoveStatus;

/**
 * @version 	1.0
 * @author
 */
final public class MoveChainFork implements MoveReceiver {
	
	private final MoveReceiver primary, secondary;
	
	public MoveChainFork(MoveReceiver a, MoveReceiver b){
		this.primary=a;
		this.secondary=b;	
	}

	/*
	 * @see MoveReceiver#processMove(Move)
	 */
	public MoveStatus processMove(Move move) {
		MoveStatus ms=primary.processMove(move);
		secondary.processMove(move);
		return ms;
	}

}
