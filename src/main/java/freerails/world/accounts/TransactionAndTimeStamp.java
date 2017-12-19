package freerails.world.accounts;

import freerails.world.FreerailsSerializable;
import freerails.world.common.GameTime;

/**
 *
 */
public class TransactionAndTimeStamp implements FreerailsSerializable {

    private static final long serialVersionUID = 1540065347606694456L;
    private final Transaction transaction;
    private final GameTime timestamp;

    /**
     *
     * @param transaction
     * @param time
     */
    public TransactionAndTimeStamp(Transaction transaction, GameTime time) {
        this.transaction = transaction;
        timestamp = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TransactionAndTimeStamp))
            return false;

        final TransactionAndTimeStamp transactionAndTimeStamp = (TransactionAndTimeStamp) o;

        if (!transaction.equals(transactionAndTimeStamp.transaction))
            return false;
        return timestamp.equals(transactionAndTimeStamp.timestamp);
    }

    @Override
    public int hashCode() {
        int result;
        result = transaction.hashCode();
        result = 29 * result + timestamp.hashCode();
        return result;
    }

    /**
     *
     * @return
     */
    public Transaction getTransaction() {
        return transaction;
    }

    /**
     *
     * @return
     */
    public GameTime getTimestamp() {
        return timestamp;
    }
}
