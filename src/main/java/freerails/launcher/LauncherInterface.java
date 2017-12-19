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

package freerails.launcher;

/**
 * Exposes the methods on the Launcher that the launcher panels may call.
 */

public interface LauncherInterface {

    /**
     *
     */
    String PROPERTIES_FILENAME = "freerails.properties";

    /**
     *
     */
    String SERVER_IP_ADDRESS_PROPERTY = "freerails.server.ip.address";

    /**
     *
     */
    String PLAYER_NAME_PROPERTY = "freerails.player.name";

    /**
     *
     */
    String SERVER_PORT_PROPERTY = "freerails.server.port";

    /**
     *
     */
    String CLIENT_DISPLAY_PROPERTY = "freerails.client.display";

    /**
     *
     */
    String CLIENT_FULLSCREEN_PROPERTY = "freerails.client.fullscreen";

    /**
     * @param text
     * @param status
     */
    void setInfoText(String text, MSG_TYPE status);

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
    void saveProps();

    /**
     *
     */
    enum MSG_TYPE {

        /**
         *
         */
        INFO,

        /**
         *
         */
        WARNING,

        /**
         *
         */
        ERROR
    }

}