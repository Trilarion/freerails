/*
 * Created on 21-Jul-2003
 *
 */
package jfreerails.move;

import java.awt.Point;
import java.awt.Rectangle;

import jfreerails.world.accounts.Bill;
import jfreerails.world.common.Money;
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
	
	public UpgradeTrackMove(ChangeTrackPieceMove trackMove, AddTransactionMove transaction){		
		super(new Move[]{trackMove, transaction});
	}
	
	public static UpgradeTrackMove generateMove(TrackPiece before, TrackPiece after, Point p){
		ChangeTrackPieceMove m = new ChangeTrackPieceMove(before, after, p);	
		Money oldPieceCost = before.getTrackRule().getPrice();
		Money newPieceCost = after.getTrackRule().getPrice();
		Money cost = new Money(oldPieceCost.getAmount() - newPieceCost.getAmount());		
		return new UpgradeTrackMove(m, new AddTransactionMove(0, new Bill(cost), true));
	}

	public Rectangle getUpdatedTiles() {
		ChangeTrackPieceMove m = (ChangeTrackPieceMove)this.getMove(0);
		return m.getUpdatedTiles();
	}

}
