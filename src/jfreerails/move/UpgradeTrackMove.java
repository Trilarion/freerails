/*
 * Created on 21-Jul-2003
 *
 */
package jfreerails.move;

import java.awt.Point;
import java.awt.Rectangle;

import jfreerails.world.track.TrackPiece;

/**
 * This CompositeMove changes the track type at a point
 * on the map and charges the players account for the cost 
 * of the change.
 *  
 * @author Luke Lindsay
 *
 */
public class UpgradeTrackMove extends CompositeMove implements TrackMove {
	
	public UpgradeTrackMove(ChangeTrackPieceMove trackMove){		
		super(new Move[]{trackMove});
	}
	
	public static UpgradeTrackMove generateMove(TrackPiece before, TrackPiece after, Point p){
		ChangeTrackPieceMove m = new ChangeTrackPieceMove(before, after, p);				
		return new UpgradeTrackMove(m);
	}

	public Rectangle getUpdatedTiles() {
		ChangeTrackPieceMove m = (ChangeTrackPieceMove)this.getMove(0);
		return m.getUpdatedTiles();
	}

}
