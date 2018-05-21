package freerails.move;

import freerails.model.player.Player;
import freerails.model.terrain.CityTilePositioner;
import freerails.model.world.World;

/**
 * Grows the cities (at the end of the year). This move cannot be undone
 * and it should always be possible to perform it.
 */
public class GrowCitiesMove implements Move {

    @Override
    public MoveStatus tryDoMove(World world, Player player) {
        return MoveStatus.MOVE_OK;
    }

    @Override
    public MoveStatus tryUndoMove(World world, Player player) {
        return MoveStatus.moveFailed("cannot undo grow cities move");
    }

    @Override
    public MoveStatus doMove(World world, Player player) {
        CityTilePositioner cityTilePositioner = new CityTilePositioner(world);
        cityTilePositioner.growCities();
        return MoveStatus.MOVE_OK;
    }

    @Override
    public MoveStatus undoMove(World world, Player player) {
        return MoveStatus.moveFailed("cannot undo grow cities move");
    }
}
