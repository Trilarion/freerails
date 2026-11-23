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
import freerails.Version;
import freerails.server.SaveGamesManager;
import freerails.server.SavedGame;
import freerails.util.SerializationFilter;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import java.io.*;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipException;

/**
 * Loads and saves games (world and player credentials) in the configured save-game directory.
 *
 * <p>A save-game file is a gzipped Java object stream containing, in order: the {@link #MAGIC}
 * number, the {@link #FORMAT_VERSION}, the build version that wrote the file (informational only),
 * and the {@link SavedGame}.
 */
public class FullSaveGameManager implements SaveGamesManager {

    /** Identifies FreeRails save-game files ("FRSV"). */
    static final int MAGIC = 0x46525356;
    /**
     * Version of the save-game structure. Bump it whenever {@code SaveGameFormatTest} reports that
     * the structure of the serialized classes changed, then regenerate the test's golden file.
     */
    public static final int FORMAT_VERSION = 2;
    /** Deserialization limits and class allowlist for save-game files. */
    private static final SerializationFilter INPUT_FILTER =
            new SerializationFilter(1_000, 50_000_000, 1024L * 1024 * 1024, 50_000_000);
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
     * Serializes a saved game to a compressed save-game file. The file is written to a temporary
     * file first and then moved into place, so an existing save game is never left half-written.
     *
     * @param path the save-game filename
     * @param savedGame the world and player credentials to save
     * @throws IOException if the operation cannot complete because of an input/output error
     */
    @Override
    public void saveGame(String path, SavedGame savedGame) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("Saving game..  " + path);

        Path saveGameFile = saveGameFile(path).toPath();
        if (!saveGameDirectory.isDirectory() && !saveGameDirectory.mkdirs()) {
            throw new IOException("Could not create save-game directory: " + saveGameDirectory);
        }
        Path temporaryFile = Files.createTempFile(saveGameDirectory.toPath(), path, ".tmp");
        try {
            try (OutputStream fileOutputStream = Files.newOutputStream(temporaryFile);
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                         new GZIPOutputStream(fileOutputStream))) {
                objectOutputStream.writeInt(MAGIC);
                objectOutputStream.writeInt(FORMAT_VERSION);
                objectOutputStream.writeUTF(buildVersion());
                objectOutputStream.writeObject(savedGame);
            }
            moveIntoPlace(temporaryFile, saveGameFile);
        } finally {
            Files.deleteIfExists(temporaryFile);
        }

        long finishTime = System.currentTimeMillis();
        logger.info("done, " + (finishTime - startTime));
    }

    /**
     * Loads and decompresses a saved game from a save-game file.
     *
     * @param path the path of the save-game file
     * @return the loaded world and player credentials
     * @throws IOException if the file is not a readable save game of the current format
     */
    @Override
    public SavedGame loadGame(String path) throws IOException {
        long startTime = System.currentTimeMillis();
        logger.info("Loading game..  " + path);

        try (InputStream fileInputStream = new FileInputStream(saveGameFile(path));
             ObjectInputStream objectInputStream = new ObjectInputStream(new GZIPInputStream(fileInputStream))) {
            objectInputStream.setObjectInputFilter(INPUT_FILTER);

            if (objectInputStream.readInt() != MAGIC) {
                throw new IOException("Not a FreeRails save game: " + path);
            }
            int formatVersion = objectInputStream.readInt();
            String writtenBy = objectInputStream.readUTF();
            if (formatVersion != FORMAT_VERSION) {
                throw new IOException("Save game " + path + " has format " + formatVersion
                        + " (written by build " + writtenBy + "), but this build ("
                        + buildVersion() + ") only supports format " + FORMAT_VERSION);
            }

            if (!(objectInputStream.readObject() instanceof SavedGame savedGame)) {
                throw new IOException("Save game does not contain a saved game: " + path);
            }

            long finishTime = System.currentTimeMillis();
            logger.info("done, written by build " + writtenBy + ", " + (finishTime - startTime));

            return savedGame;
        } catch (ClassNotFoundException | ObjectStreamException | EOFException | ZipException
                | ClassCastException e) {
            throw new IOException("Could not read save game " + path + ": " + e.getMessage(), e);
        }
    }

    /**
     * Returns a description of the build writing or reading save games.
     *
     * @return the version and git revision of this build
     */
    private static String buildVersion() {
        return Version.VERSION + " (" + Version.GIT_REVISION + ")";
    }

    /**
     * Moves a completely written temporary file over the target save-game file.
     *
     * @param source the temporary file
     * @param target the save-game file
     * @throws IOException if the file cannot be moved
     */
    private static void moveIntoPlace(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
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
