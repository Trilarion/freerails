
package jfreerails.launcher;

/** Exposes the methods on the Launcher that the launcher panels may call.
 * 
 * @author Luke
 *
 */

public interface LauncherInterface {
	  void setInfoText(String text);

	  void setNextEnabled(boolean enabled);
}