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

package freerails.move;

import freerails.model.player.Player;
import freerails.model.simulation.Speed;
import freerails.util.TestUtils;
import junit.framework.TestCase;

import java.util.List;

/**
 * Tests the read-only access to the components of container moves.
 */
public class ContainerMoveTest extends TestCase {

    private static final Player PLAYER = new Player(0, "player0");

    private final Move slow = new ChangeGameSpeedMove(PLAYER, Speed.SLOW);
    private final Move moderate = new ChangeGameSpeedMove(PLAYER, Speed.MODERATE);
    private final Move fast = new ChangeGameSpeedMove(PLAYER, Speed.FAST);

    /**
     * Tests that a move that is not a container is its own only leaf.
     */
    public void testLeavesOfPlainMove() {
        assertEquals(List.of(slow), ContainerMove.leaves(slow));
    }

    /**
     * Tests that leaves are found in nested ordered and orthogonal containers, in order.
     */
    public void testLeavesOfNestedContainers() {
        Move compost = new CompostMove(List.of(moderate, fast));
        Move composite = new CompositeMove(List.of(slow, compost));
        Move outer = new CompositeMove(List.of(composite, slow));

        assertEquals(List.of(slow, moderate, fast, slow), ContainerMove.leaves(outer));
    }

    /**
     * Tests that both container types expose their components without allowing changes.
     */
    public void testComponentsAreReadOnly() {
        CompostMove compost = new CompostMove(List.of(slow, fast));
        CompositeMove composite = new CompositeMove(List.of(slow, fast));

        assertEquals(List.of(slow, fast), compost.getMoves());
        assertEquals(List.of(slow, fast), composite.getMoves());
        TestUtils.assertThrows(UnsupportedOperationException.class, () -> compost.getMoves().add(moderate));
        TestUtils.assertThrows(UnsupportedOperationException.class, () -> composite.getMoves().add(moderate));
        TestUtils.assertThrows(UnsupportedOperationException.class,
                () -> ContainerMove.leaves(compost).add(moderate));
    }
}
