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

import junit.framework.TestCase;

import javax.swing.AbstractButton;
import java.awt.Component;

/**
 * Tests launcher mode visibility.
 */
public class LauncherPanelTest extends TestCase {

    /** Checks that the server-only option is omitted when it is unavailable. */
    public void testServerOnlyOptionIsOmittedWhenNotAllowed() {
        LauncherPanel panel = new LauncherPanel(null, false);

        for (Component component : panel.getComponents()) {
            if (component instanceof AbstractButton button) {
                assertFalse("Server only option should not be shown",
                        "Server only".equals(button.getText()));
            }
        }
    }

    /** Checks that the server-only option is present when it is available. */
    public void testServerOnlyOptionIsShownWhenAllowed() {
        LauncherPanel panel = new LauncherPanel(null, true);

        for (Component component : panel.getComponents()) {
            if (component instanceof AbstractButton button && "Server only".equals(button.getText())) {
                return;
            }
        }
        fail("Server only option should be shown when allowed");
    }
}
