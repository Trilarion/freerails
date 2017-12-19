/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.world.finances;

import freerails.world.common.GameTime;

import java.io.Serializable;

/**
 *
 */
public class TransactionAndTimeStamp implements Serializable {

    private static final long serialVersionUID = 1540065347606694456L;
    private final Transaction transaction;
    private final GameTime timestamp;

    /**
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
     * @return
     */
    public Transaction getTransaction() {
        return transaction;
    }

    /**
     * @return
     */
    public GameTime getTimestamp() {
        return timestamp;
    }
}
