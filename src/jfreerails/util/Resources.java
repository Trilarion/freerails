package jfreerails.util;
 
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility class for obtaining localised resource strings.
 */
public final class Resources {
    private static ResourceBundle bundle;
   
   static {
       try {
	   bundle = ResourceBundle.getBundle
	       ("jfreerails.data.l10n.jfreerails");
       } catch (MissingResourceException e) {
	   bundle = null;
       }
   }

    /**
     * Provided for especially long localised texts.
     * @return a localised string corresponding to the specified key and object
     * @param defaultString The string to be localised. This will be returned
     * as the default if no localised string is found
     * @param key The key to be searched for in the resource bundles. This can
     * be much shorter than the default string
     */
    public static String get(String key, String defaultString) {
	try {
	    if (bundle != null) {
		return bundle.getString(key);
	    }
	} catch (MissingResourceException e) {
	    // ignore
	}
	return defaultString;
    }

    /**
     * @return a localised string corresponding to the specified key and object
     * @param defaultString The string to be localised. This will be returned
     * as the default if no localised string is found
     */
    public static String get(String defaultString) {
	return get(defaultString, defaultString);
    }
}
