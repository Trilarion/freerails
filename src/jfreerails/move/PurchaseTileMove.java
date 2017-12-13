package jfreerails.move;

import java.awt.Point;

import jfreerails.world.accounts.AddItemTransaction;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.common.GameTime;
import jfreerails.world.player.Player;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.World;
import jfreerails.world.track.FreerailsTile;

public class PurchaseTileMove extends CompositeMove implements Move {
    private static Move[] generateMoves(ReadOnlyWorld w, Point location,
	    FreerailsPrincipal newOwner) {
	GameTime now = (GameTime) w.get(ITEM.TIME, newOwner);
	long tileValue = w.getTile(location.x, location.y).getTerrainValue(w,
		location.x, location.y);
	AddItemTransaction t = new AddItemTransaction(now,
		AddItemTransaction.LAND, 0, 1, - tileValue);
	return new Move[] {
	    new ChangeTileOwnershipMove(w, location, newOwner),
	    new AddTransactionMove(0, t, true, newOwner)
	};
    }
    
    public PurchaseTileMove(ReadOnlyWorld w, Point location,
	    FreerailsPrincipal newOwner) {
	super(generateMoves(w, location, newOwner));
    }
}
