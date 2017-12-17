package freerails.world.player;

import freerails.world.common.FreerailsSerializable;

import java.security.Principal;

/**
 * This interface identifies a principal. This interface may be extended in the
 * future in order to provide faster lookups, rather than using name
 * comparisons.
 * <p>
 * A principal represents an entity which can view or alter the game world. A
 * principal usually corresponds to a player's identity, but may also represent
 * an authorititative server, or a another game entity such as a corporation.
 * All entities which may own game world objects must be represented by a
 * principal.
 *
 * @author rob
 */
public abstract class FreerailsPrincipal implements Principal,
        FreerailsSerializable {
    private final int worldIndex;

    public FreerailsPrincipal(int worldIndex) {
        this.worldIndex = worldIndex;
    }

    /**
     * returns -1 if it's not a player
     *
     * @return the index in the world structures
     */
    public int getWorldIndex() {
        return worldIndex;
    }

}