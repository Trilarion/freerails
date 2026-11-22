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
import freerails.util.TestUtils;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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

    private static SavedGame createSavedGame() {
        World world = WorldGenerator.defaultWorld();
        world.addPlayer(WorldGenerator.TEST_PLAYER);
        return new SavedGame(world, List.of(new LogOnCredentials(WorldGenerator.TEST_PLAYER.getName(), "secret")));
    }
}
