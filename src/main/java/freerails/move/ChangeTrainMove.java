package freerails.move;

import freerails.model.player.Player;
import freerails.model.train.Train;
import freerails.model.world.World;
import org.jetbrains.annotations.NotNull;

// TODO no undo, equals, hashcode
/**
 *
 */
public class ChangeTrainMove implements Move {

    private final Player player;
    private final Train train;

    public ChangeTrainMove(@NotNull Player player, @NotNull Train train) {
        this.player = player;
        this.train = train;
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
        world.removeTrain(player, train.getId());
        world.addTrain(player, train);
        return MoveStatus.MOVE_OK;
    }

    @Override
    public MoveStatus undoMove(World world, Player player) {
        return MoveStatus.MOVE_OK;
    }
}
