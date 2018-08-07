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
    public MoveStatus tryDoMove(World world, Player player) {
        return MoveStatus.MOVE_OK;
    }

    @Override
    public MoveStatus tryUndoMove(World world, Player player) {
        return MoveStatus.MOVE_OK;
    }

    @Override
    public MoveStatus doMove(World world, Player player) {
        world.removeStation(this.player, stationId);
        return MoveStatus.MOVE_OK;
    }

    @Override
    public MoveStatus undoMove(World world, Player player) {
        return MoveStatus.MOVE_OK;
    }
}
