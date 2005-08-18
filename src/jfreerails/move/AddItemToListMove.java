/*
 * Created on 13-Apr-2003
 *
 */
package jfreerails.move;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;

/**
 * All moves that add an item to a list should extend this class.
 * 
 * @author Luke
 * 
 */
public class AddItemToListMove implements ListMove {
	private static final long serialVersionUID = 3256721779916747824L;

	private final KEY listKey;

	private final int index;

	private final FreerailsPrincipal principal;

	private final FreerailsSerializable item;

	public int getIndex() {
		return index;
	}

	public int hashCode() {
		int result;
		result = listKey.hashCode();
		result = 29 * result + index;
		result = 29 * result + principal.hashCode();
		result = 29 * result + (item != null ? item.hashCode() : 0);

		return result;
	}

	public KEY getKey() {
		return listKey;
	}

	public AddItemToListMove(KEY key, int i, FreerailsSerializable item,
			FreerailsPrincipal p) {
		this.listKey = key;
		this.index = i;
		this.item = item;
		this.principal = p;
	}

	public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
		if (w.size(this.principal, listKey) != index) {
			return MoveStatus.moveFailed("Expected size of "
					+ listKey.toString() + " list is " + index
					+ " but actual size is " + w.size(this.principal, listKey));
		}

		return MoveStatus.MOVE_OK;
	}

	public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
		int expectListSize = index + 1;

		if (w.size(this.principal, listKey) != expectListSize) {
			return MoveStatus.moveFailed("Expected size of "
					+ listKey.toString() + " list is " + expectListSize
					+ " but actual size is " + w.size(this.principal, listKey));
		}

		return MoveStatus.MOVE_OK;
	}

	public MoveStatus doMove(World w, FreerailsPrincipal p) {
		MoveStatus ms = tryDoMove(w, p);

		if (ms.isOk()) {
			w.add(this.principal, listKey, this.item);
		}

		return ms;
	}

	public MoveStatus undoMove(World w, FreerailsPrincipal p) {
		MoveStatus ms = tryUndoMove(w, p);

		if (ms.isOk()) {
			w.removeLast(this.principal, listKey);
		}

		return ms;
	}

	public boolean equals(Object o) {
		if (o instanceof AddItemToListMove) {
			AddItemToListMove test = (AddItemToListMove) o;

			if (null == this.item) {
				if (null != test.item) {
					return false;
				}
			} else if (!this.item.equals(test.getAfter())) {
				return false;
			}

			if (this.index != test.index) {
				return false;
			}

			if (this.listKey != test.listKey) {
				return false;
			}

			return true;
		}
		return false;
	}

	public FreerailsSerializable getBefore() {
		return null;
	}

	public FreerailsSerializable getAfter() {
		return item;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer(this.getClass().getName());
		sb.append("\n list=");
		sb.append(listKey.toString());
		sb.append("\n index =");
		sb.append(index);
		sb.append("\n item =");
		sb.append(item);

		return sb.toString();
	}

	public FreerailsPrincipal getPrincipal() {
		return principal;
	}
}