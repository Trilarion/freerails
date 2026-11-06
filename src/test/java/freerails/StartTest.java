/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

package freerails;

import org.junit.Test;

import java.net.InetSocketAddress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** Tests parsing of application roles from command-line arguments. */
public class StartTest {

    /** Checks that an omitted role and {@code --both} select both application roles. */
    @Test
    public void defaultsToBothRoles() {
        assertEquals(Start.Role.BOTH, Start.parseRole(new String[0]));
        assertEquals(Start.Role.BOTH, Start.parseRole(new String[]{"--both"}));
    }

    /** Checks that the client-only and server-only arguments select their respective roles. */
    @Test
    public void parsesSingleRoleArguments() {
        assertEquals(Start.Role.CLIENT, Start.parseRole(new String[]{"--client"}));
        assertEquals(Start.Role.SERVER, Start.parseRole(new String[]{"--server"}));
    }

    /** Checks that the default and explicit role arguments are supported. */
    @Test
    public void parsesSupportedRoleArguments() {
        assertEquals(Start.Role.BOTH, Start.parseRole(new String[0]));
        assertEquals(Start.Role.BOTH, Start.parseRole(new String[]{"--both"}));
        assertEquals(Start.Role.CLIENT, Start.parseRole(new String[]{"--client"}));
        assertEquals(Start.Role.SERVER, Start.parseRole(new String[]{"--server"}));
    }

    /** Keeps ordinary hosted games local and exposes the standalone server to remote clients. */
    @Test
    public void selectsServerBindAddressForRole() {
        InetSocketAddress localGame = Start.serverSocketAddress(Start.Role.BOTH, 0);
        InetSocketAddress standaloneServer = Start.serverSocketAddress(Start.Role.SERVER, 0);

        assertTrue(localGame.getAddress().isLoopbackAddress());
        assertTrue(standaloneServer.getAddress().isAnyLocalAddress());
    }

    /** Checks that an unsupported argument is rejected. */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsUnknownArguments() {
        Start.parseRole(new String[]{"--unknown"});
    }

    /** Checks that multiple role arguments are rejected. */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsConflictingRoles() {
        Start.parseRole(new String[]{"--client", "--server"});
    }

    /** Checks that the removed JavaFX selection option is rejected. */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsRemovedUiSelection() {
        Start.parseRole(new String[]{"--javafx"});
    }
}
