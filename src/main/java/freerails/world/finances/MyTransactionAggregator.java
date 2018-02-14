package freerails.world.finances;

import freerails.world.game.GameTime;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.world.ReadOnlyWorld;

/**
 *
 */
public class MyTransactionAggregator extends TransactionAggregator {
    private final GameTime[] totalTimeInterval;

    public MyTransactionAggregator(ReadOnlyWorld world, FreerailsPrincipal principal, GameTime[] totalTimeInterval) {
        super(world, principal);
        this.totalTimeInterval = totalTimeInterval;
    }

    @Override
    protected boolean condition(int transactionID) {
        int transactionTicks = world.getTransactionTimeStamp(principal, transactionID).getTicks();

        int from = totalTimeInterval[0].getTicks();
        int to = totalTimeInterval[1].getTicks();
        return transactionTicks >= from && transactionTicks <= to;
    }
}
