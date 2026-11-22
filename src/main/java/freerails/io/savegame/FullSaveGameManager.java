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

/*
 *
 */
package freerails.io.savegame;

import freerails.UserData;
import freerails.server.SaveGamesManager;
import freerails.server.SavedGame;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import java.io.*;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Loads and saves games (world and player credentials) in the configured save-game directory.
 */
public class FullSaveGameManager implements SaveGamesManager {

    // TODO meaningful version string that actually changes if the savegame structure changes
    /** Format version stored with each save-game file. */
    public static final String VERSION = "1";
    private static final TaggedLogger logger = Logger.tag("FullSaveGameManager");
    private final File saveGameDirectory;

    /**
     * Creates a manager that stores save games in the user's FreeRails data folder.
     */
    public FullSaveGameManager() {
        this(UserData.FREERAILS_USER_FOLDER);
    }

    /**
     * Creates a manager for the specified save-game directory.
     *
     * @param saveGameDirectory directory used for save-game files
     */
    FullSaveGameManager(File saveGameDirectory) {
        this.saveGameDirectory = saveGameDirectory;
    }

    /**
     * Returns the names of save-game files in the configured directory.
     *
     * @return the available save-game names
     */
    @Override
    public String[] getSaveGameNames() {
        FilenameFilter filter = new SavedGameFileFilter();
        if (!saveGameDirectory.exists()) {
            return new String[0];
        }
        String[] saveGameNames = saveGameDirectory.list(filter);
        if (saveGameNames == null) {
            throw new IllegalStateException("Could not list save games in " + saveGameDirectory);
        }
        return saveGameNames;
    }

    /**
     * Serializes a saved game to a compressed save-game file.
     *
     * @param path the save-game filename
     * @param savedGame the world and player credentials to save
     * @throws IOException if the operation cannot complete because of an input/output error
     */
    @Override
    public void saveGame(String path, SavedGame savedGame) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("Saving game..  " + path);

        File saveGameFile = saveGameFile(path);
        if (!saveGameDirectory.isDirectory() && !saveGameDirectory.mkdirs()) {
            throw new IOException("Could not create save-game directory: " + saveGameDirectory);
        }
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                new GZIPOutputStream(new FileOutputStream(saveGameFile)))) {
            objectOutputStream.writeObject(VERSION);
            objectOutputStream.writeObject(savedGame);
        }

        long finishTime = System.currentTimeMillis();
        logger.info("done, " + (finishTime - startTime));
    }

    /**
     * Loads and decompresses a saved game from a save-game file.
     *
     * @param path the path of the save-game file
     * @return the loaded world and player credentials
     * @throws IOException if the operation cannot complete because of an input/output error
     */
    @Override
    public SavedGame loadGame(String path) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("Loading game..  " + path);

        try (ObjectInputStream objectInputStream = new ObjectInputStream(
                new GZIPInputStream(new FileInputStream(saveGameFile(path))))) {
            String version = (String) objectInputStream.readObject();

            if (!VERSION.equals(version)) {
                throw new IOException("Unknown save game version: " + version);
            }

            if (!(objectInputStream.readObject() instanceof SavedGame savedGame)) {
                throw new IOException("Save game does not contain a saved game: " + path);
            }

            long finishTime = System.currentTimeMillis();
            logger.info("done, " + (finishTime - startTime));

            return savedGame;
        } catch (ClassNotFoundException | InvalidClassException e) {
            throw new IOException(e);
        }
    }

    /**
     * Resolves a save-game filename inside the configured directory.
     *
     * @param name save-game filename
     * @return the save-game file
     * @throws IOException if the value is not a filename
     */
    private File saveGameFile(String name) throws IOException {
        try {
            Path fileName = Path.of(name);
            if (name.isEmpty() || fileName.isAbsolute() || fileName.getNameCount() != 1
                    || ".".equals(name) || "..".equals(name)) {
                throw new IOException("Save-game name must be a filename: " + name);
            }
            return saveGameDirectory.toPath().resolve(fileName).toFile();
        } catch (InvalidPathException e) {
            throw new IOException("Invalid save-game name: " + name, e);
        }
    }

}