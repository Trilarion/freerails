package freerails.move;

import freerails.model.player.Player;
import freerails.model.train.Train;
import freerails.model.world.World;
import org.jetbrains.annotations.NotNull;

// TODO hashcode, equals
/**
 *
 */
public class AddTrainMove implements Move {

    private final Player player;
    private final Train train;

    public AddTrainMove(@NotNull Player player, @NotNull Train train) {
        this.player = player;
        this.train = train;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AddTrainMove)) return false;
        final AddTrainMove other = (AddTrainMove) o;

        return player.equals(other.player) && train.equals(other.train);
    }

    @Override
    public int hashCode() {
        int result = player.hashCode();
        result = 29 * result + train.hashCode();
        return result;
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
        world.addTrain(this.player, train);
        return Status.OK;
    }

    @Override
    public Status undoMove(World world, Player player) {
        world.removeTrain(this.player, train.getId());
        return Status.OK;
    }
}
