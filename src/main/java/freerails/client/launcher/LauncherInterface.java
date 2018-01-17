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

package freerails.client.launcher;

/**
 * Exposes the methods on the Launcher that the launcher panels may call.
 */

public interface LauncherInterface {

    /**
     * @param text
     * @param status
     */
    void setInfoText(String text, InfoMessageType status);

    /**
     * @param enabled
     */
    void setNextEnabled(boolean enabled);

    /**
     *
     */
    void hideErrorMessages();

    /**
     *
     */
    void hideAllMessages();

    /**
     * @param key
     * @param value
     */
    void setProperty(String key, String value);

    /**
     * @param key
     * @return
     */
    String getProperty(String key);

    /**
     *
     */
    void saveProperties();

}