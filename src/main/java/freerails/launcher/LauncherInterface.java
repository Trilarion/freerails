package freerails.launcher;

/**
 * Exposes the methods on the Launcher that the launcher panels may call.
 * 
 * @author Luke
 * 
 */

public interface LauncherInterface {

    public static final String PROPERTIES_FILENAME = "freerails.properties";

    public static final String SERVER_IP_ADDRESS_PROPERTY = "freerails.server.ip.address";

    public static final String PLAYER_NAME_PROPERTY = "freerails.player.name";

    public static final String SERVER_PORT_PROPERTY = "freerails.server.port";

    public static final String CLIENT_DISPLAY_PROPERTY = "freerails.client.display";

    public static final String CLIENT_FULLSCREEN_PROPERTY = "freerails.client.fullscreen";

    public enum MSG_TYPE {
        INFO, WARNING, ERROR
    };

    void setInfoText(String text, MSG_TYPE status);

    void setNextEnabled(boolean enabled);

    void hideErrorMessages();

    void hideAllMessages();

    void setProperty(String key, String value);

    String getProperty(String key);

    void saveProps();

}