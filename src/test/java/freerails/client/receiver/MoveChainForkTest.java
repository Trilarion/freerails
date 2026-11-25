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

package freerails.client.receiver;

import freerails.model.player.Player;
import freerails.model.simulation.Speed;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import freerails.move.*;
import freerails.move.mapupdatemove.MapUpdateMove;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests how the move chain fork passes moves to its receivers and map listeners.
 */
public class MoveChainForkTest extends TestCase {

    private static final Player PLAYER = new Player(0, "player0");

    /**
     * Tests that complete receivers get the container and split receivers get the components of
     * ordered and orthogonal containers.
     */
    public void testSplitsBothContainerTypes() {
        Move slow = new ChangeGameSpeedMove(PLAYER, Speed.SLOW);
        Move fast = new ChangeGameSpeedMove(PLAYER, Speed.FAST);
        Move pause = new ChangeGameSpeedMove(PLAYER, Speed.PAUSE);
        Move move = new CompositeMove(List.of(slow, new CompostMove(List.of(fast, pause))));

        MoveChainFork fork = new MoveChainFork();
        List<Move> complete = new ArrayList<>();
        List<Move> split = new ArrayList<>();
        fork.addCompleteMoveReceiver(complete::add);
        fork.addSplitMoveReceiver(split::add);
        fork.process(move);

        assertEquals(List.of(move), complete);
        assertEquals(List.of(slow, fast, pause), split);
    }

    /**
     * Tests that a removed complete receiver no longer gets moves.
     */
    public void testRemovedCompleteReceiverGetsNoMoves() {
        MoveChainFork fork = new MoveChainFork();
        List<Move> complete = new ArrayList<>();
        MoveReceiver receiver = complete::add;
        fork.addCompleteMoveReceiver(receiver);
        fork.removeCompleteMoveReceiver(receiver);
        fork.process(new ChangeGameSpeedMove(PLAYER, Speed.FAST));

        assertTrue(complete.isEmpty());
    }

    /**
     * Tests that map listeners hear about a map update inside an orthogonal container, such as the
     * tile change of a new industry.
     */
    public void testNotifiesMapListenersOfMapUpdatesInOrthogonalContainer() {
        Rectangle tiles = new Rectangle(3, 4, 1, 1);
        Move move = new CompostMove(List.of(new TestMapUpdateMove(tiles), new ChangeGameSpeedMove(PLAYER, Speed.FAST)));

        MoveChainFork fork = new MoveChainFork();
        List<Rectangle> changed = new ArrayList<>();
        fork.addMapListener(changed::add);
        fork.process(move);

        assertEquals(List.of(tiles), changed);
    }

    /**
     * A map update that changes nothing and reports a fixed rectangle.
     *
     * @param tiles the rectangle reported as updated
     */
    private record TestMapUpdateMove(Rectangle tiles) implements MapUpdateMove {

        /**
         * Reports that the move can always be applied.
         *
         * @param world the world to check against
         * @return {@link Status#OK}
         */
        @Override
        public @NotNull Status applicable(@NotNull UnmodifiableWorld world) {
            return Status.OK;
        }

        /**
         * Does nothing.
         *
         * @param world the world to change
         */
        @Override
        public void apply(@NotNull World world) {
        }

        /**
         * Returns the rectangle supplied at construction.
         *
         * @return the updated tiles
         */
        @Override
        public Rectangle getUpdatedTiles() {
            return tiles;
        }
    }
}
