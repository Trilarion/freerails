package freerails.move;

import freerails.model.player.Player;
import freerails.model.world.World;
import org.jetbrains.annotations.NotNull;

// TODO hashcode, equals, unto, trydo...
/**
 *
 */
public class RemoveStationMove implements Move {

    private final Player player;
    private final int stationId;

    public RemoveStationMove(@NotNull Player player, int stationId) {
        this.player = player;
        this.stationId = stationId;
    }

    @Override
    public Status tryDoMove(World world, Player player) {
        return Status.OK;
    }

    @Override
    public Status tryUndoMove(World world, Player player) {
        return Status.OK;
    }

    @Override
    public Status doMove(World world, Player player) {
        world.removeStation(this.player, stationId);
        return Status.OK;
    }

    @Override
    public Status undoMove(World world, Player player) {
        return Status.OK;
    }
}
