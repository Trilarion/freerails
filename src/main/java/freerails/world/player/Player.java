/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.world.player;

import java.io.Serializable;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

// TODO What is the difference between Player and PlayerPrincipal?

/**
 * Represents a player within the game. The player model is such that a user can
 * start a client, create a new player on the server and start playing. They can
 * disconnect from the server, which may continue running with other players
 * still active. The server can then save the list of players and be stopped and
 * restarted again, the clients can then authenticate themselves to the server
 * and continue their sessions where they left off.
 *
 * XXX the player is only authenticated when the connection is opened, and
 * subsequent exchanges are not authenticated.
 *
 * TODO implement a more complete authentication system using certificates rather than public keys.
 */
public class Player implements Serializable {

    // TODO What is the meaning of the AUTHORITATIVE principal?
    /**
     * This Principal can be granted all permissions.
     */
    public static final FreerailsPrincipal AUTHORITATIVE = new WorldPrincipal("Authoritative Server");
    private static final long serialVersionUID = 4849154251645451999L;

    /**
     * Name of the player.
     */
    private final String name;
    private FreerailsPrincipal principal;

    /**
     * Used by the client to generate a player with a particular name.
     */
    public Player(String name) {
        this.name = name;

        KeyPairGenerator kpg;

        // generate our key pair
        try {
            kpg = KeyPairGenerator.getInstance("DSA");
            kpg.initialize(1024);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Used by the server to generate a player with a particular name and public
     * key.
     */
    public Player(String name, int id) {
        this.name = name;
        principal = new PlayerPrincipal(id, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Player)) {
            return false;
        }
        return name.equals(((Player) obj).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @return
     */
    public FreerailsPrincipal getPrincipal() {
        return principal;
    }
}