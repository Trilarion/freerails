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

package freerails.client;

import freerails.client.receiver.MoveChainFork;
import freerails.client.receiver.TestMoveReceiver;
import freerails.model.MapFixtureFactory2;
import freerails.model.player.Player;
import freerails.model.track.BuildTrackStrategy;
import freerails.model.world.World;
import freerails.network.command.CommandToServer;
import freerails.network.command.ServerCommandReceiver;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests that a model root is complete from construction on.
 */
public class ModelRootImplTest extends TestCase {

    private World world;
    private Player player;

    /**
     * Creates a world with one player.
     */
    @Override
    protected void setUp() {
        world = MapFixtureFactory2.getCopy();
        player = world.getPlayer(0);
    }

    /**
     * Tests that the constructor exposes its parts and sets the default build track strategy.
     */
    public void testConstructionSetsWorldDependentState() {
        ClientProperties properties = new ClientProperties();
        ModelRootImpl modelRoot = new ModelRootImpl(
                properties, world, player, new TestMoveReceiver(world), message -> {
                }, new MoveChainFork());

        assertSame(world, modelRoot.getWorld());
        assertSame(player, modelRoot.getPlayer());
        assertSame(properties, modelRoot.getProperties());
        assertTrue(properties.getProperty(ModelRootProperty.BUILD_TRACK_STRATEGY) instanceof BuildTrackStrategy);
    }

    /**
     * Tests that commands go to the server command receiver.
     */
    public void testSendsCommandsToReceiver() {
        List<CommandToServer> sent = new ArrayList<>();
        ServerCommandReceiver receiver = sent::add;
        ModelRootImpl modelRoot = new ModelRootImpl(
                new ClientProperties(), world, player, new TestMoveReceiver(world), receiver, new MoveChainFork());
        CommandToServer command = model -> null;

        modelRoot.sendCommand(command);

        assertEquals(List.of(command), sent);
    }

    /**
     * Tests that the constructor rejects every missing part.
     */
    public void testRejectsMissingParts() {
        ClientProperties properties = new ClientProperties();
        TestMoveReceiver moveReceiver = new TestMoveReceiver(world);
        ServerCommandReceiver commandReceiver = message -> {
        };
        MoveChainFork fork = new MoveChainFork();

        assertRejected(() -> new ModelRootImpl(null, world, player, moveReceiver, commandReceiver, fork));
        assertRejected(() -> new ModelRootImpl(properties, null, player, moveReceiver, commandReceiver, fork));
        assertRejected(() -> new ModelRootImpl(properties, world, null, moveReceiver, commandReceiver, fork));
        assertRejected(() -> new ModelRootImpl(properties, world, player, null, commandReceiver, fork));
        assertRejected(() -> new ModelRootImpl(properties, world, player, moveReceiver, null, fork));
        assertRejected(() -> new ModelRootImpl(properties, world, player, moveReceiver, commandReceiver, null));
    }

    /**
     * Asserts that creating a model root throws a {@link NullPointerException}.
     *
     * @param construction creates the model root
     */
    private static void assertRejected(Runnable construction) {
        try {
            construction.run();
            fail("Expected a NullPointerException");
        } catch (NullPointerException expected) {
            // expected
        }
    }
}
