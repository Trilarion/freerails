/*
 * Created on 22-Jul-2005
 *
 */
package freerails.world.accounts;

import freerails.world.common.FreerailsSerializable;
import freerails.world.common.GameTime;

/**
 *
 * @author jkeller1
 */
public class TransactionAndTimeStamp implements FreerailsSerializable {

    private static final long serialVersionUID = 1540065347606694456L;

    private final Transaction t;

    private final GameTime timeStamp;

    /**
     *
     * @param t
     * @param stamp
     */
    public TransactionAndTimeStamp(Transaction t, GameTime stamp) {
        this.t = t;
        timeStamp = stamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TransactionAndTimeStamp))
            return false;

        final TransactionAndTimeStamp transactionAndTimeStamp = (TransactionAndTimeStamp) o;

        if (!t.equals(transactionAndTimeStamp.t))
            return false;
        return timeStamp.equals(transactionAndTimeStamp.timeStamp);
    }

    @Override
    public int hashCode() {
        int result;
        result = t.hashCode();
        result = 29 * result + timeStamp.hashCode();
        return result;
    }

    /**
     *
     * @return
     */
    public Transaction getT() {
        return t;
    }

    /**
     *
     * @return
     */
    public GameTime getTimeStamp() {
        return timeStamp;
    }
}
