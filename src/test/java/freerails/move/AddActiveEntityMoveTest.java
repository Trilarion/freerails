/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
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
package freerails.move;

import freerails.world.Activity;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.WorldImplTest;

/**
 *
 */
public class AddActiveEntityMoveTest extends AbstractMoveTestCase {

    /**
     *
     */
    @Override
    public void testMove() {
        FreerailsPrincipal p = getPrincipal();
        Activity a = new WorldImplTest.TestActivity(50);
        AddActiveEntityMove move = new AddActiveEntityMove(a, 0, p);
        assertSurvivesSerialisation(move);
        assertOkButNotRepeatable(move);
        AddActiveEntityMove move2 = new AddActiveEntityMove(a, 2, p);
        assertTryMoveFails(move2);
    }

}
