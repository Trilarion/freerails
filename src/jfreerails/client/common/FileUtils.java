package jfreerails.client.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Utilities for persistent storage.
 *
 * Manage the following resources:
 * <ul>
 * <li>Player-specific data
 * <li>User-specific data (e.g. UI preferences)
 * <li>TODO session-specific data
 * </ul>
 * @author Rob
 */
public final class FileUtils {
    public static final String DATA_TYPE_PLAYER_SPECIFIC = "Player";

    /**
     * @param dataType one of DATA_TYPE_XXX
     * @param dataKey if dataType is PLAYER_SPECIFIC, then dataKey is the name
     * of the player. If dataType is USER_SPECIFIC, then KEY is null (seeing
     * as we should be saving in the user's home directory). If dataType is
     * SESSION_SPECIFIC then dataKey is the session ID
     * @param fileName name of the file to open
     */
    public static FileInputStream openForReading(String dataType,
        String dataKey, String fileName)
        throws FileNotFoundException, SecurityException {
        return new FileInputStream(keysToFile(dataType, dataKey, fileName));
    }

    /**
     * Opens an output stream for the file, creating any required directories
     * if necessary.
     * @param dataType one of DATA_TYPE_XXX
     * @param dataKey if dataType is PLAYER_SPECIFIC, then dataKey is the name
     * of the player. If dataType is USER_SPECIFIC, then KEY is null (seeing
     * as we should be saving in the user's home directory). If dataType is
     * SESSION_SPECIFIC then dataKey is the session ID
     * @param fileName name of the file to open
     */
    public static FileOutputStream openForWriting(String dataType,
        String dataKey, String fileName) throws IOException, SecurityException {
        File f = keysToFile(dataType, dataKey, fileName);
        File parent = f.getParentFile();
        assert parent != null;

        if (!parent.exists()) {
            if (parent.mkdirs() == false) {
                throw new IOException("Couldn't create directory " + parent);
            }
        }

        return new FileOutputStream(f);
    }

    private static File keysToFile(String dataType, String dataKey,
        String fileName) {
        /* the user's home directory is in the user.home system property */
        String path = ".jfreerails" + File.separator + "client" +
            File.separator + dataType + File.separator + dataKey +
            File.separator + fileName;
        path = System.getProperty("user.home") + File.separator + path;

        return new File(path);
    }
}