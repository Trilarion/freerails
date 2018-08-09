package freerails.move;

import freerails.model.player.Player;
import freerails.model.station.Station;
import freerails.model.world.World;
import org.jetbrains.annotations.NotNull;

// TODO equals, hashcode, try, undo ..

/**
 *
 */
public class ChangeStationMove implements Move {

    private final Player player;
    private final Station station;

    public ChangeStationMove(@NotNull Player player, @NotNull Station station) {
        this.player = player;
        this.station = station;
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
        world.removeStation(this.player, station.getId());
        world.addStation(this.player, station);
        return Status.OK;
    }

    @Override
    public Status undoMove(World world, Player player) {
        return Status.OK;
    }
}
