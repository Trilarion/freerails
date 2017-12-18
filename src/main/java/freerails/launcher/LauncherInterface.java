package freerails.launcher;

/**
 * Exposes the methods on the Launcher that the launcher panels may call.
 *
 * @author Luke
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
     *
     * @param text
     * @param status
     */
    void setInfoText(String text, MSG_TYPE status);

    /**
     *
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
     *
     * @param key
     * @param value
     */
    void setProperty(String key, String value);

    /**
     *
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