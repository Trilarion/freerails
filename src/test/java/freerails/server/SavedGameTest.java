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

package freerails.server;

import freerails.model.world.World;
import freerails.network.LogOnCredentials;
import freerails.util.TestUtils;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

import java.util.List;

/**
 * Tests the validation of {@link SavedGame}.
 */
public class SavedGameTest extends TestCase {

    /** Tests that matching credentials are accepted. */
    public void testAcceptsOneCredentialPerPlayer() {
        World world = worldWithTestPlayer();
        LogOnCredentials credentials = new LogOnCredentials(WorldGenerator.TEST_PLAYER.getName(), "secret");

        SavedGame savedGame = new SavedGame(world, List.of(credentials));

        assertEquals(List.of(credentials), savedGame.credentials());
    }

    /** Tests that the number of credentials must match the number of players. */
    public void testRejectsCredentialCountMismatch() {
        World world = worldWithTestPlayer();

        TestUtils.assertThrows(IllegalArgumentException.class, () -> new SavedGame(world, List.of()));
    }

    /** Tests that each username must match the name of the player with the same id. */
    public void testRejectsUsernameMismatch() {
        World world = worldWithTestPlayer();
        List<LogOnCredentials> credentials = List.of(new LogOnCredentials("someone else", "secret"));

        TestUtils.assertThrows(IllegalArgumentException.class, () -> new SavedGame(world, credentials));
    }

    private static World worldWithTestPlayer() {
        World world = WorldGenerator.defaultWorld();
        world.addPlayer(WorldGenerator.TEST_PLAYER);
        return world;
    }
}
