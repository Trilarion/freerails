package jfreerails.move;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.top.World;

/**
 * Defining operations expected of ...
 *
 *
 * @author lindsal
 */

public interface Move extends FreerailsSerializable {

	MoveStatus tryDoMove(World w);

	MoveStatus tryUndoMove(World w);

	MoveStatus doMove(World w);

	MoveStatus undoMove(World w);

}
