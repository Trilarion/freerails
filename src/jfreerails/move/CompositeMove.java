/*
 * Created on 26-May-2003
 * 
 */
package jfreerails.move;

import jfreerails.world.top.World;

/**
 * @author Luke
 * 
 */
public class CompositeMove implements Move {
	
	private final Move[] moves; 
	
	public CompositeMove(Move[] moves){
		this.moves=moves;
	}

	public MoveStatus tryDoMove(World w) {
		//Since whether a move later in the list goes through could
		//depend on whether an ealier move has been executed, we need
		//actually execute moves, then undo them to test whether the 
		//array of moves can be excuted ok.
		MoveStatus ms  = doMove(w);
		
		if(ms.ok){		
			//We just wanted to see if we could do them so we undo them again.
			undoMoves(w,  moves.length -1);
		}
		//If its not ok, then doMove would have undone the moves so we don't need to undo them.
		return ms;
	}

	public MoveStatus tryUndoMove(World w) {
		MoveStatus ms = undoMove(w);
		if(ms.isOk()){
			undoMoves(w, 0);
		}		
		return ms;
	}

	public MoveStatus doMove(World w) {
		MoveStatus ms=null;
		for(int i = 0 ; i < moves.length ; i++){
			
			ms = moves[i].doMove(w);
			if(!ms.ok){
				//Undo any moves we have already done.
				undoMoves(w, i-1);
				return ms;	
			}
		}
		return ms;
	}

	public MoveStatus undoMove(World w) {
		MoveStatus ms=null;
		for (int i = moves.length -1; i >=0 ; i-- ){
			ms = moves[i].undoMove(w);
			if(!ms.ok){
				redoMoves(w, i);		
			}
		}
		return ms;
	}
	
	private void undoMoves(World w, int number){
		for(int i = number ; i >= 0 ; i --){
			moves[i].undoMove(w);
		}		
	}
	
	private void redoMoves(World w, int number){
		for (int i = moves.length -1; i >=0 ; i-- ){
			moves[i].undoMove(w);			
		}
	}		
}
