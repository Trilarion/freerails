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

/*
 * DisplayModeWithName.java
 *
 */
package freerails.controller;

/**
 * Stores a DisplayModeWithName and provides a customised implementation of toString that can be used in menus.
 */
public class DisplayModeWithName {

    /**
     *
     */
    public final java.awt.DisplayMode displayMode;

    /**
     * @param displayMode
     */
    public DisplayModeWithName(java.awt.DisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    @Override
    public String toString() {
        return displayMode.getWidth() + "x" + displayMode.getHeight() + ' ' + displayMode.getBitDepth() + " bit " + displayMode.getRefreshRate() + "Hz";
    }

    @Override
    public int hashCode() {
        return displayMode.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DisplayModeWithName) {
            DisplayModeWithName other = (DisplayModeWithName) obj;

            return other.displayMode.equals(displayMode);
        }
        return false;
    }
}