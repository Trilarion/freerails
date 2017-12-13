package jfreerails.world.top;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.player.FreerailsPrincipal;

/**
 * Identifies a unique object within a table in the Game World
 */
public class ObjectKey implements FreerailsSerializable {
    public final int index;
    public final FreerailsPrincipal principal;
    public final KEY key;

    public ObjectKey(KEY k, FreerailsPrincipal p, int i) {
	key = k;
	principal = p;
	index = i;
    }
    
    public boolean equals(Object o) {
	if (o == null || ! (o instanceof ObjectKey))
	    return false;

	ObjectKey ok = (ObjectKey) o;
	return (ok.index == index &&
		((ok.key == null) ? (key == null) : (ok.key.equals(key))) &&
		((ok.principal == null) ? (principal == null) :
		 ok.principal.equals(principal)));
    }

    public int hashcode() {
	/* TODO should be good enough for most purposes but could be improved */
	return index;
    }
}
