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

package freerails.io.savegame;

import freerails.model.world.World;
import freerails.network.LogOnCredentials;
import freerails.server.SavedGame;
import freerails.util.IntPoint2D;
import freerails.util.TestUtils;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

/** Tests save-game file location and persistence behavior. */
public class FullSaveGameManagerTest extends TestCase {

    /**
     * Tests saving, listing, and loading games from the configured directory.
     *
     * @throws Exception if the save-game round trip fails
     */
    public void testSaveListAndLoadUseConfiguredDirectory() throws Exception {
        Path parentDirectory = Files.createTempDirectory("freerails-savegame-test");
        File saveGameDirectory = parentDirectory.resolve("FreeRails User Data").toFile();
        FullSaveGameManager manager = new FullSaveGameManager(saveGameDirectory);
        String saveGameName = "round-trip.sav";

        try {
            SavedGame savedGame = createSavedGame();
            manager.saveGame(saveGameName, savedGame);

            assertTrue(saveGameDirectory.isDirectory());
            assertTrue(new File(saveGameDirectory, saveGameName).isFile());
            assertEquals(saveGameName, manager.getSaveGameNames()[0]);

            SavedGame loadedGame = manager.loadGame(saveGameName);
            assertEquals(savedGame.credentials(), loadedGame.credentials());
            assertEquals(WorldGenerator.TEST_PLAYER.getName(), loadedGame.world().getPlayer(0).getName());
        } finally {
            FileUtils.deleteDirectory(parentDirectory.toFile());
        }
    }

    /**
     * Tests that save-game filenames cannot resolve outside their configured directory.
     *
     * @throws Exception if the invalid save-game path is accepted
     */
    public void testRejectsSaveGamePaths() throws Exception {
        Path parentDirectory = Files.createTempDirectory("freerails-savegame-path-test");
        Path saveGameDirectory = parentDirectory.resolve("FreeRails User Data");
        FullSaveGameManager manager = new FullSaveGameManager(saveGameDirectory.toFile());

        try {
            TestUtils.assertThrows(IOException.class, () -> manager.saveGame(
                    "../outside.sav", createSavedGame()));
            assertFalse(Files.exists(parentDirectory.resolve("outside.sav")));
            assertFalse(Files.exists(saveGameDirectory));
        } finally {
            FileUtils.deleteDirectory(parentDirectory.toFile());
        }
    }

    /**
     * Tests that saving replaces an existing save game and leaves no temporary files.
     *
     * @throws Exception if the save-game round trip fails
     */
    public void testSaveOverwritesExistingSaveGame() throws Exception {
        Path saveGameDirectory = Files.createTempDirectory("freerails-savegame-overwrite-test");
        FullSaveGameManager manager = new FullSaveGameManager(saveGameDirectory.toFile());

        try {
            manager.saveGame("game.sav", createSavedGame());
            manager.saveGame("game.sav", createSavedGame());

            assertNotNull(manager.loadGame("game.sav"));
            assertEquals(List.of("game.sav"), listFileNames(saveGameDirectory));
        } finally {
            FileUtils.deleteDirectory(saveGameDirectory.toFile());
        }
    }

    /**
     * Tests that a save that fails while writing keeps the previous save game intact.
     *
     * @throws Exception if the previous save game was damaged
     */
    public void testFailedSaveKeepsPreviousSaveGame() throws Exception {
        Path saveGameDirectory = Files.createTempDirectory("freerails-savegame-failure-test");
        FullSaveGameManager manager = new FullSaveGameManager(saveGameDirectory.toFile());

        try {
            manager.saveGame("game.sav", createSavedGame());
            byte[] previous = Files.readAllBytes(saveGameDirectory.resolve("game.sav"));

            World failingWorld = new UnserializableWorld();
            failingWorld.addPlayer(WorldGenerator.TEST_PLAYER);
            SavedGame failingGame = new SavedGame(failingWorld, List.of(credentials()));
            TestUtils.assertThrows(NotSerializableException.class,
                    () -> manager.saveGame("game.sav", failingGame));

            assertTrue(Arrays.equals(previous, Files.readAllBytes(saveGameDirectory.resolve("game.sav"))));
            assertEquals(List.of("game.sav"), listFileNames(saveGameDirectory));
        } finally {
            FileUtils.deleteDirectory(saveGameDirectory.toFile());
        }
    }

    /**
     * Tests that files without a matching header are rejected with an {@link IOException}.
     *
     * @throws Exception if an invalid file is accepted
     */
    public void testRejectsInvalidSaveGameFiles() throws Exception {
        Path saveGameDirectory = Files.createTempDirectory("freerails-savegame-invalid-test");
        FullSaveGameManager manager = new FullSaveGameManager(saveGameDirectory.toFile());

        try {
            Files.writeString(saveGameDirectory.resolve("plain.sav"), "not a save game");
            writeGzippedObjectStream(saveGameDirectory.resolve("magic.sav"), out -> {
                out.writeInt(0x12345678);
                out.writeInt(FullSaveGameManager.FORMAT_VERSION);
                out.writeUTF("test");
            });
            writeGzippedObjectStream(saveGameDirectory.resolve("format.sav"), out -> {
                out.writeInt(FullSaveGameManager.MAGIC);
                out.writeInt(FullSaveGameManager.FORMAT_VERSION + 1);
                out.writeUTF("test");
            });
            writeGzippedObjectStream(saveGameDirectory.resolve("truncated.sav"), out ->
                    out.writeInt(FullSaveGameManager.MAGIC));
            writeGzippedObjectStream(saveGameDirectory.resolve("legacy.sav"), out -> {
                out.writeObject("1");
                out.writeObject(createSavedGame());
            });
            writeGzippedObjectStream(saveGameDirectory.resolve("payload.sav"), out -> {
                out.writeInt(FullSaveGameManager.MAGIC);
                out.writeInt(FullSaveGameManager.FORMAT_VERSION);
                out.writeUTF("test");
                out.writeObject("not a saved game");
            });

            for (String name : List.of("plain.sav", "magic.sav", "format.sav", "truncated.sav",
                    "legacy.sav", "payload.sav")) {
                TestUtils.assertThrows(IOException.class, () -> manager.loadGame(name));
            }
        } finally {
            FileUtils.deleteDirectory(saveGameDirectory.toFile());
        }
    }

    /**
     * Tests that save games containing classes outside the allowlist are rejected.
     *
     * @throws Exception if the disallowed class is deserialized
     */
    public void testRejectsDisallowedClasses() throws Exception {
        Path saveGameDirectory = Files.createTempDirectory("freerails-savegame-filter-test");
        FullSaveGameManager manager = new FullSaveGameManager(saveGameDirectory.toFile());

        try {
            writeGzippedObjectStream(saveGameDirectory.resolve("evil.sav"), out -> {
                out.writeInt(FullSaveGameManager.MAGIC);
                out.writeInt(FullSaveGameManager.FORMAT_VERSION);
                out.writeUTF("test");
                out.writeObject(new File("evil"));
            });

            IOException e = TestUtils.assertThrows(IOException.class, () -> manager.loadGame("evil.sav"));
            assertTrue(e.getCause() instanceof InvalidClassException);
        } finally {
            FileUtils.deleteDirectory(saveGameDirectory.toFile());
        }
    }

    private static List<String> listFileNames(Path directory) throws IOException {
        try (Stream<Path> files = Files.list(directory)) {
            return files.map(file -> file.getFileName().toString()).sorted().toList();
        }
    }

    private static void writeGzippedObjectStream(Path file, ObjectStreamWriter writer) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(Files.newOutputStream(file)))) {
            writer.write(out);
        }
    }

    private static LogOnCredentials credentials() {
        return new LogOnCredentials(WorldGenerator.TEST_PLAYER.getName(), "secret");
    }

    private static SavedGame createSavedGame() {
        World world = WorldGenerator.defaultWorld();
        world.addPlayer(WorldGenerator.TEST_PLAYER);
        return new SavedGame(world, List.of(credentials()));
    }

    /** Writes the contents of a test save-game file. */
    @FunctionalInterface
    private interface ObjectStreamWriter {
        void write(ObjectOutputStream out) throws IOException;
    }

    /** World whose serialization fails after part of the stream has been written. */
    private static final class UnserializableWorld extends World {

        @Serial
        private static final long serialVersionUID = 1L;
        @SuppressWarnings({"unused", "serial"})
        private final Object unserializable = new Object();

        UnserializableWorld() {
            super(WorldGenerator.minimalWorldBuilder(new IntPoint2D(20, 20)));
        }
    }
}
