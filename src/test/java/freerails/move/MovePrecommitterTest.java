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

import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import junit.framework.TestCase;

/**
 * Tests move precommit queue behavior.
 */
public class MovePrecommitterTest extends TestCase {

    /**
     * Tests try-move status with no queued generator is ignored.
     */
    public void testTryMoveStatusWithEmptyQueueDoesNotThrow() {
        MovePrecommitter precommitter = new MovePrecommitter(null);

        precommitter.fromServer(TryMoveStatus.TRY_MOVE_OK);

        assertTrue(precommitter.uncomitted.isEmpty());
        assertTrue(precommitter.precomitted.isEmpty());
    }

    /**
     * Tests a rejection without queued moves does not throw.
     */
    public void testStatusFailureWithoutQueuedMovesDoesNotThrow() {
        MovePrecommitter precommitter = new MovePrecommitter(null);

        precommitter.fromServer(Status.fail("rejected"));

        assertTrue(precommitter.uncomitted.isEmpty());
        assertTrue(precommitter.precomitted.isEmpty());
    }

    /**
     * Tests a rejection after a precommitted move does not throw.
     */
    public void testStatusFailureAfterPrecommitDoesNotThrow() {
        MovePrecommitter precommitter = new MovePrecommitter(null);
        precommitter.toServer(new TestMove(Status.OK));

        precommitter.fromServer(Status.fail("rejected"));

        assertTrue(precommitter.precomitted.isEmpty());
    }

    private static final class TestMove implements Move {
        private final Status status;

        private TestMove(Status status) {
            this.status = status;
        }

        @Override
        public Status applicable(UnmodifiableWorld world) {
            return status;
        }

        @Override
        public void apply(World world) {
        }
    }
}
