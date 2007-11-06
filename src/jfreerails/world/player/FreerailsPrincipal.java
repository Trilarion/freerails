package jfreerails.world.player;

import java.security.Principal;

import jfreerails.world.common.FreerailsSerializable;

/**
 * This interface identifies a principal. This interface may be extended in the
 * future in order to provide faster lookups, rather than using name
 * comparisons.
 * 
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
    private Integer worldIndex = null;

    public Integer getWorldIndex() {
        return worldIndex;
    }

    public void setWorldIndex(Integer worldIndex) {
        this.worldIndex = worldIndex;
    }
    
}