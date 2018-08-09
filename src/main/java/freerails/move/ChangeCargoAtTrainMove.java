package freerails.move;

import freerails.model.cargo.CargoBatchBundle;
import freerails.model.player.Player;
import freerails.model.train.Train;
import freerails.model.world.World;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class ChangeCargoAtTrainMove implements Move {

    private final Player player;
    private final int trainId;
    private final CargoBatchBundle cargoBatchBundle;

    public ChangeCargoAtTrainMove(@NotNull Player player, int trainId, @NotNull CargoBatchBundle cargoBatchBundle) {
        this.player = player;
        this.trainId = trainId;
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
        Train train = world.getTrain(this.player, trainId);
        train.setCargoBatchBundle(cargoBatchBundle);
        return Status.OK;
    }

    @Override
    public Status undoMove(World world, Player player) {
        return Status.OK;
    }
}
