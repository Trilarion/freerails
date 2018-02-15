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

import freerails.move.listmove.AddItemToListMove;
import freerails.util.Vector2D;
import freerails.model.world.PlayerKey;
import freerails.model.station.Station;
import freerails.model.MapFixtureFactory;

/**
 *
 */
public class CompositeMoveTest extends AbstractMoveTestCase {

    private final Station station1 = new Station(new Vector2D(1, 1), "station1", 10, 0);
    private final Station station2 = new Station(new Vector2D(2, 3), "station2", 10, 0);
    private final Station station3 = new Station(new Vector2D(3, 3), "station3", 10, 0);
    private final Station station4 = new Station(new Vector2D(4, 4), "station4", 10, 0);

    /**
     *
     */
    public void testMove() {
        Move[] moves = new Move[4];
        moves[0] = new AddItemToListMove(PlayerKey.Stations, 0, station1, MapFixtureFactory.TEST_PRINCIPAL);
        moves[1] = new AddItemToListMove(PlayerKey.Stations, 1, station2, MapFixtureFactory.TEST_PRINCIPAL);
        moves[2] = new AddItemToListMove(PlayerKey.Stations, 2, station3, MapFixtureFactory.TEST_PRINCIPAL);
        moves[3] = new AddItemToListMove(PlayerKey.Stations, 3, station4, MapFixtureFactory.TEST_PRINCIPAL);
        Move compositeMove = new CompositeMove(moves);
        assertSurvivesSerialisation(compositeMove);
        assertTryMoveIsOk(compositeMove);
        assertEquals("The stations should not have been add yet.", 0,
                getWorld().size(MapFixtureFactory.TEST_PRINCIPAL, PlayerKey.Stations));
        assertDoMoveIsOk(compositeMove);
        assertEquals("The stations should have been add now.", 4, getWorld()
                .size(MapFixtureFactory.TEST_PRINCIPAL, PlayerKey.Stations));
        assertTryUndoMoveIsOk(compositeMove);
        assertUndoMoveIsOk(compositeMove);
        assertOkButNotRepeatable(compositeMove);
    }
}