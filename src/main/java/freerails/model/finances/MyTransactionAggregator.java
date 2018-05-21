package freerails.model.finances;

import freerails.model.game.GameTime;
import freerails.model.player.Player;
import freerails.model.world.UnmodifiableWorld;

/**
 *
 */
public class MyTransactionAggregator extends TransactionAggregator {
    private final GameTime[] totalTimeInterval;

    public MyTransactionAggregator(UnmodifiableWorld world, Player player, GameTime[] totalTimeInterval) {
        super(world, player);
        this.totalTimeInterval = totalTimeInterval;
    }

    @Override
    protected boolean condition(int transactionID) {
        int transactionTicks = world.getTransactionTimeStamp(player, transactionID).getTicks();

        int from = totalTimeInterval[0].getTicks();
        int to = totalTimeInterval[1].getTicks();
        return transactionTicks >= from && transactionTicks <= to;
    }
}
