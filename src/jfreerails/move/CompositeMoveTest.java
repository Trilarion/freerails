/*
 * Created on 31-May-2003
 *
 */
package jfreerails.move;

import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;


/**
 * @author Luke
 *
 */
public class CompositeMoveTest extends AbstractMoveTestCase {
    StationModel station1 = new StationModel(1, 1, "station1", 10, 0);
    StationModel station2 = new StationModel(2, 3, "station2", 10, 0);
    StationModel station3 = new StationModel(3, 3, "station3", 10, 0);
    StationModel station4 = new StationModel(4, 4, "station4", 10, 0);

    public void testMove() {
        Move[] moves = new Move[4];
        moves[0] = new AddItemToListMove(KEY.STATIONS, 0, station1);
        moves[1] = new AddItemToListMove(KEY.STATIONS, 1, station2);
        moves[2] = new AddItemToListMove(KEY.STATIONS, 2, station3);
        moves[3] = new AddItemToListMove(KEY.STATIONS, 3, station4);

        Move compositeMove = new CompositeMove(moves);
        assertEqualsSurvivesSerialisation(compositeMove);
        assertTryMoveIsOk(compositeMove);
        assertEquals("The stations should not have been add yet.", 0,
            world.size(KEY.STATIONS));
        assertDoMoveIsOk(compositeMove);
        assertEquals("The stations should have been add now.", 4,
            world.size(KEY.STATIONS));
        assertTryUndoMoveIsOk(compositeMove);
        assertUndoMoveIsOk(compositeMove);

        assertOkButNotRepeatable(compositeMove);
    }
}