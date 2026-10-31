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

/**
 * Exposes the methods on the Launcher that the launcher panels may call.
 */

interface LauncherInterface {

    /**
     * Sets the info text.
     *
     * @param text the text parameter
     * @param status the status parameter
     */
    void setInfoText(String text, InfoMessageType status);

    /**
     * Sets the next enabled.
     *
     * @param enabled whether the control is enabled
     */
    void setNextEnabled(boolean enabled);

    /**
     * Hides the error messages.
     */
    void hideErrorMessages();

    /**
     * Hides the all messages.
     */
    void hideAllMessages();
}