
package jfreerails.launcher;

/** Exposes the methods on the Launcher that the launcher panels may call.
 *
 * @author Luke
 *
 */

public interface LauncherInterface {
    
    public static final int INFO = 0;
    public static final int WARNING = 1;
    public static final int ERROR = 2;
    
    
    void setInfoText(String text, int status);
    
    void setNextEnabled(boolean enabled);
    
    void hideErrorMessages();
    
}