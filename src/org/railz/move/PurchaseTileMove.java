/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.railz.move;

import java.awt.Point;

import org.railz.world.accounts.AddItemTransaction;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.common.GameTime;
import org.railz.world.player.Player;
import org.railz.world.top.ITEM;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.top.World;
import org.railz.world.track.FreerailsTile;
import org.railz.world.terrain.TerrainTileViewer;

public class PurchaseTileMove extends CompositeMove implements Move {
    private static Move[] generateMoves(ReadOnlyWorld w, Point location,
	    FreerailsPrincipal newOwner) {
	GameTime now = (GameTime) w.get(ITEM.TIME, newOwner);
	TerrainTileViewer ttv = new TerrainTileViewer(w);
	ttv.setFreerailsTile(location.x, location.y);
	long tileValue = ttv.getTerrainValue();
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
