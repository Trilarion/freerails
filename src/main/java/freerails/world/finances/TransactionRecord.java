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

import freerails.world.game.GameTime;

import java.io.Serializable;

/**
 * A transaction record is a transaction with a time stamp.
 */
public class TransactionRecord implements Serializable {

    private static final long serialVersionUID = 1540065347606694456L;
    private final Transaction transaction;
    private final GameTime timestamp;

    /**
     * @param transaction
     * @param timestamp
     */
    public TransactionRecord(Transaction transaction, GameTime timestamp) {
        this.transaction = transaction;
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TransactionRecord)) return false;
        final TransactionRecord transactionRecord = (TransactionRecord) obj;
        if (!transaction.equals(transactionRecord.transaction)) return false;
        return timestamp.equals(transactionRecord.timestamp);
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
