package freerails.savegames;

import java.io.File;
import java.io.FilenameFilter;

/**
 * A SaveGamesManager reads and writes gzipped saved games to the working
 * directory.
 */
public class SavedGameFileFilter implements FilenameFilter {

    public boolean accept(File dir, String name) {
        return name.endsWith(".sav");
    }
}
