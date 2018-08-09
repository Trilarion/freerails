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
    public Status tryDoMove(World world, Player player) {
        return Status.OK;
    }

    @Override
    public Status tryUndoMove(World world, Player player) {
        return Status.moveFailed("cannot undo grow cities move");
    }

    @Override
    public Status doMove(World world, Player player) {
        CityTilePositioner cityTilePositioner = new CityTilePositioner(world);
        cityTilePositioner.growCities();
        return Status.OK;
    }

    @Override
    public Status undoMove(World world, Player player) {
        return Status.moveFailed("cannot undo grow cities move");
    }
}
