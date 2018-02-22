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

package freerails.network;

import freerails.network.gameserver.FreerailsGameServer;
import freerails.savegames.TestSaveGamesManager;
import junit.framework.TestCase;

/**
 * Test for FreerailsGameServer - tests logging on.
 */
public class FreerailsGameServerTest extends TestCase {

    private FreerailsGameServer server;

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        server = new FreerailsGameServer(new TestSaveGamesManager());
    }

    /**
     *
     */
    public void testLogon() {
        LogOnResponse response;

        // Test 1
        LogOnCredentials request1 = new LogOnCredentials("Name", "password");
        response = server.logon(request1);
        assertTrue("Simple case, should go through.", response.isSuccess());
        assertEquals("1st logon is player 0", 0, response.getId());

        // Test 2
        LogOnCredentials request2 = new LogOnCredentials("Name2", "password2");
        response = server.logon(request2);
        assertTrue("Simple case, should go through.", response.isSuccess());
        assertEquals("2nd logon is player 1", 1, response.getId());

        // Test 3: When player is already logged on.
        LogOnCredentials request3 = new LogOnCredentials("Name", "password");
        response = server.logon(request3);
        assertFalse("Player is already logged on.", response.isSuccess());

        // Test 4: When new log-ons are not allowed.
        server.setNewPlayersAllowed(false);

        LogOnCredentials request4 = new LogOnCredentials("Name4", "password4");
        response = server.logon(request4);
        assertFalse("New logons are not allowed.", response.isSuccess());

        // Test 5: When the player has logged off, then tries to log on.
        server.logoff(0);

        response = server.logon(request1);
        assertTrue("Player 0 has logged off, so should succeed.", response
                .isSuccess());
        assertEquals("Should keep the same player id", 0, response
                .getId());

        /*
         * Test 6: When the player has logged off, then tries to log on with
         * wrong password.
         */
        server.logoff(0);

        LogOnCredentials request6 = new LogOnCredentials("Name", "batman");
        response = server.logon(request6);
        assertFalse("Player 0 has logged off, but the password is wrong.", response.isSuccess());
    }
}