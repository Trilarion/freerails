package jfreerails.controller;

import java.util.ArrayList;

import jfreerails.move.CompositeMove;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;

/**
 * @version 	1.0
 * 
 * A central point at which a client may register to receive moves which have
 * been committed.
 */
final public class MoveChainFork implements MoveReceiver {
	
	private final ArrayList moveReceivers = new ArrayList(); 
	
	private MoveReceiver primary;
	
	private static MoveChainFork moveChainFork = null;
	
	private MoveChainFork(){
	    // do nothing
	}

	public void setPrimaryReceiver(MoveReceiver primaryReceiver) {
	    primary = primaryReceiver;
	}
	
	public static void init() {
	    moveChainFork = new MoveChainFork();
	}
	
	public static MoveChainFork getMoveChainFork() {
	    return moveChainFork;
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
	public MoveStatus processMove(Move move) {
	    MoveStatus ms;
	    if (primary != null) {
		ms=primary.processMove(move);
	    } else {
		ms = MoveStatus.MOVE_OK;
	    }
		
		for(int i=0;i<moveReceivers.size();i++){
				MoveReceiver m = (MoveReceiver)moveReceivers.get(i);
				m.processMove(move);
		}
	    if (move instanceof CompositeMove) {
		Move[] moves = ((CompositeMove) move).getMoves();
		for (int i = 0; i < moves.length; i++) {
		    processMove(moves[i]);
		}
	    }
		return ms;
	}
}
