package jfreerails.move;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.ImList;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldDifferences;
import jfreerails.world.track.FreerailsTile;

/**
 * A move that makes a number of changes to the map.
 * 
 * @author Luke
 */
public class MapDiffMove implements Move, MapUpdateMove {
	private static final long serialVersionUID = 3905245632406239544L;

	private final ImList<Diff> diffs;

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof MapDiffMove))
			return false;

		final MapDiffMove mapDiffMove = (MapDiffMove) o;

		if (h != mapDiffMove.h)
			return false;
		if (w != mapDiffMove.w)
			return false;
		if (x != mapDiffMove.x)
			return false;
		if (y != mapDiffMove.y)
			return false;
		if (!diffs.equals(mapDiffMove.diffs))
			return false;

		return true;
	}

	public int hashCode() {
		int result;
		result = diffs.hashCode();
		result = 29 * result + x;
		result = 29 * result + y;
		result = 29 * result + w;
		result = 29 * result + h;
		return result;
	}

	private final int x, y, w, h;

	public MapDiffMove(ReadOnlyWorld world, WorldDifferences worldDiffs) {

		Iterator<Point> it = worldDiffs.getMapDifferences();
		ArrayList<Diff> diffsArrayList = new ArrayList<Diff>();
		while (it.hasNext()) {
			Point p = it.next();
			FreerailsTile oldTile = (FreerailsTile) world.getTile(p.x, p.y);
			FreerailsTile newTile = (FreerailsTile) worldDiffs
					.getTile(p.x, p.y);
			diffsArrayList.add(new Diff(oldTile, newTile, p));
		}
		diffs = new ImList<Diff>(diffsArrayList);

		x = 0;
		y = 0;
		w = world.getMapWidth();
		h = world.getMapHeight();
	}

	public MoveStatus tryDoMove(World world, FreerailsPrincipal p) {
		return tryMove(world, false);
	}

	private MoveStatus tryMove(World world, boolean undo) {
		for (int i = 0; i < diffs.size(); i++) {
			Diff diff = diffs.get(i);
			FreerailsTile actual = (FreerailsTile) world
					.getTile(diff.x, diff.y);
			FreerailsTile expected = undo ? diff.after : diff.before;
			if (!actual.equals(expected)) {
				return MoveStatus.moveFailed("expected =" + expected
						+ ", actual = " + actual);
			}
		}

		return MoveStatus.MOVE_OK;
	}

	private void doMove(World world, boolean undo) {
		for (int i = 0; i < diffs.size(); i++) {
			Diff diff = diffs.get(i);
			FreerailsTile tile = undo ? diff.before : diff.after;
			world.setTile(diff.x, diff.y, tile);
		}
	}

	public MoveStatus tryUndoMove(World world, FreerailsPrincipal p) {
		return tryMove(world, true);
	}

	public MoveStatus doMove(World world, FreerailsPrincipal p) {
		MoveStatus ms = tryMove(world, false);

		if (ms.isOk()) {
			doMove(world, false);
		}

		return ms;
	}

	public MoveStatus undoMove(World world, FreerailsPrincipal p) {
		MoveStatus ms = tryMove(world, true);

		if (ms.isOk()) {
			doMove(world, true);
		}

		return ms;
	}

	public Rectangle getUpdatedTiles() {
		return new Rectangle(x, y, w, h);
	}

	static class Diff implements FreerailsSerializable {
		private static final long serialVersionUID = -5935670372745313360L;

		final int x, y;

		final FreerailsTile before, after;

		Diff(FreerailsTile before, FreerailsTile after, Point p) {
			this.after = after;
			this.before = before;
			this.x = p.x;
			this.y = p.y;
		}

		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof Diff))
				return false;

			final Diff diff = (Diff) o;

			if (x != diff.x)
				return false;
			if (y != diff.y)
				return false;
			if (!after.equals(diff.after))
				return false;
			if (!before.equals(diff.before))
				return false;

			return true;
		}

		public int hashCode() {
			int result;
			result = x;
			result = 29 * result + y;
			result = 29 * result + before.hashCode();
			result = 29 * result + after.hashCode();
			return result;
		}
	}
}