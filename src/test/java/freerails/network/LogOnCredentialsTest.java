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

package freerails.network;

import junit.framework.TestCase;

/**
 * Tests log on credentials behavior.
 */
public class LogOnCredentialsTest extends TestCase {

    /**
     * Tests string representation omits password behavior.
     */
    public void testStringRepresentationOmitsPassword() {
        LogOnCredentials credentials = new LogOnCredentials("player", "secret");

        assertEquals("player", credentials.toString());
        assertFalse(credentials.toString().contains("secret"));
        assertEquals(credentials, new LogOnCredentials("player", "secret"));
        assertFalse(credentials.equals(new LogOnCredentials("player", "different")));
    }
}
