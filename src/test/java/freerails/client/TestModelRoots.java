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
import freerails.model.player.Player;
import freerails.model.world.World;

/**
 * Creates model roots for tests that apply moves directly to a local world and ignore server
 * commands.
 */
public final class TestModelRoots {

    private TestModelRoots() {
    }

    /**
     * Creates a model root with fresh client properties for a world and player.
     *
     * @param world the world that moves are applied to
     * @param player the local player
     * @return the model root
     */
    public static ModelRootImpl create(World world, Player player) {
        return new ModelRootImpl(
                new ClientProperties(), world, player, new TestMoveReceiver(world), message -> {
                }, new MoveChainFork());
    }
}
