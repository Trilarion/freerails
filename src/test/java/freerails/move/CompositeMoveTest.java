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

import freerails.model.cargo.CargoBatchBundle;
import freerails.util.Vec2D;
import freerails.model.station.Station;
import freerails.util.WorldGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class CompositeMoveTest extends AbstractMoveTestCase {

    private final Station station1 = new Station(0, new Vec2D(1, 1), "station1", 10, new CargoBatchBundle());
    private final Station station2 = new Station(1, new Vec2D(2, 3), "station2", 10, new CargoBatchBundle());
    private final Station station3 = new Station(2, new Vec2D(3, 3), "station3", 10, new CargoBatchBundle());
    private final Station station4 = new Station(3, new Vec2D(4, 4), "station4", 10, new CargoBatchBundle());

    /**
     *
     */
    public void testMove() {
        List<Move> moves = new ArrayList<>(4);
        moves.add(new AddStationMove(WorldGenerator.TEST_PLAYER, station1));
        moves.add(new AddStationMove(WorldGenerator.TEST_PLAYER, station2));
        moves.add(new AddStationMove(WorldGenerator.TEST_PLAYER, station3));
        moves.add(new AddStationMove(WorldGenerator.TEST_PLAYER, station4));
        Move compositeMove = new CompositeMove(moves);
        assertSurvivesSerialisation(compositeMove);
        assertTryMoveIsOk(compositeMove);
        assertEquals("The stations should not have been add yet.", 0, getWorld().getStations(WorldGenerator.TEST_PLAYER).size());
        assertDoMoveIsOk(compositeMove);
        assertEquals("The stations should have been add now.", 4, getWorld().getStations(WorldGenerator.TEST_PLAYER).size());
        assertTryUndoMoveIsOk(compositeMove);
        assertUndoMoveIsOk(compositeMove);
        assertOkButNotRepeatable(compositeMove);
    }
}