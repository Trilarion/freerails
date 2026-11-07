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

package freerails.client.launcher;

import freerails.util.TestUtils;
import junit.framework.TestCase;

/**
 * Tests JavaFX launcher input validation.
 */
public class JavaFxLauncherTest extends TestCase {

    /** Checks that both endpoints of the supported port range are accepted. */
    public void testParsePortAcceptsRangeBoundaries() {
        assertEquals(0, JavaFxLauncher.parsePort("0"));
        assertEquals(65535, JavaFxLauncher.parsePort("65535"));
    }

    /** Checks that malformed and out-of-range ports are rejected with a useful message. */
    public void testParsePortRejectsInvalidValues() {
        assertInvalidPort(null);
        assertInvalidPort("");
        assertInvalidPort("not a port");
        assertInvalidPort("-1");
        assertInvalidPort("65536");
    }

    /**
     * Checks that an input value is rejected as an invalid port.
     *
     * @param value the value to parse
     */
    private void assertInvalidPort(String value) {
        IllegalArgumentException exception = TestUtils.assertThrows(IllegalArgumentException.class,
                () -> JavaFxLauncher.parsePort(value));
        assertEquals("A valid port value is between 0 and 65535.", exception.getMessage());
    }
}
