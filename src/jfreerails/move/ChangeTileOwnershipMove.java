package jfreerails.move;

import java.awt.Point;

import jfreerails.world.track.FreerailsTile;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.World;
import jfreerails.world.player.FreerailsPrincipal;

public class ChangeTileOwnershipMove implements Move {
    private final FreerailsPrincipal oldOwner;
    private final FreerailsPrincipal newOwner;
    private final Point location;

    /**
     * XXX Not to be instantiated as a standalone move.
     */
    ChangeTileOwnershipMove(ReadOnlyWorld w, Point location,
	    FreerailsPrincipal newOwner) {
	oldOwner = (FreerailsPrincipal) ((FreerailsTile) w.getTile(location.x,
		    location.y)).getOwner();
	this.newOwner = newOwner;
	this.location = new Point(location);
    }

    public FreerailsPrincipal getPrincipal() {
	return newOwner;
    }
    
    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
	FreerailsTile currentTile = w.getTile(location.x, location.y);
	if (currentTile.getOwner().equals(oldOwner))
	    return MoveStatus.MOVE_OK;

	return MoveStatus.moveFailed("Somebody acquired the tile before you!");
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
	FreerailsTile currentTile = w.getTile(location.x, location.y);
	if (currentTile.getOwner().equals(p))
	    return MoveStatus.MOVE_OK;

	return MoveStatus.moveFailed("You don't own the tile!");
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
	MoveStatus ms;
	if ((ms = tryDoMove(w, p)) == MoveStatus.MOVE_OK) {
	    FreerailsTile currentTile = w.getTile(location.x, location.y);
	    currentTile.setOwner(p);
	}
	return ms;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
	MoveStatus ms;
	if ((ms = tryUndoMove(w, p)) == MoveStatus.MOVE_OK) {
	    FreerailsTile currentTile = w.getTile(location.x, location.y);
	    currentTile.setOwner(oldOwner);
	}
	return ms;
    }

    public boolean equals(Object o) {
	if (o == null || !(o instanceof ChangeTileOwnershipMove))
	    return false;

	ChangeTileOwnershipMove m = (ChangeTileOwnershipMove) o;
	return (oldOwner.equals(m.oldOwner) &&
		newOwner.equals(m.newOwner) &&
		location.equals(m.location));
    }
    
    public int hashCode() {
	return oldOwner.hashCode() ^ newOwner.hashCode() ^
	    location.hashCode();
    }

    public String toString() {
	return "ChangeTileOwnershipMove: oldOwner=" + oldOwner + ", newOwner="
	    + newOwner + ", location" + location;
    }
}
