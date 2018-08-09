package freerails.move;

import freerails.model.cargo.CargoBatchBundle;
import freerails.model.player.Player;
import freerails.model.station.Station;
import freerails.model.world.World;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class ChangeCargoAtStationMove implements Move {

    private final Player player;
    private final int stationId;
    private final CargoBatchBundle cargoBatchBundle;

    public ChangeCargoAtStationMove(@NotNull Player player, int stationId, @NotNull CargoBatchBundle cargoBatchBundle) {
        this.player = player;
        this.stationId = stationId;
        this.cargoBatchBundle = cargoBatchBundle;
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
        Station station = world.getStation(this.player, stationId);
        station.setCargoBatchBundle(cargoBatchBundle);
        return Status.OK;
    }

    @Override
    public Status undoMove(World world, Player player) {
        return Status.OK;
    }
}
