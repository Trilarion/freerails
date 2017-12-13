/*
 * Copyright (C) Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.railz.move;


/**
 * This {@link CompositeMove} transfers cargo from a train to a station and vice-versa.
 *
 * @author Luke Lindsay
 *
 *
 */
public class TransferCargoAtStationMove extends CompositeMove {
    private TransferCargoAtStationMove(Move[] moves) {
        super(moves);
    }

    public static TransferCargoAtStationMove generateMove(
        ChangeCargoBundleMove changeAtStation,
        ChangeCargoBundleMove changeOnTrain) {
        return new TransferCargoAtStationMove(new Move[] {
                changeAtStation, changeOnTrain});
    }

    private static Move[] getMoveArray(Move m1, Move m2, Move[] moves) {
	Move[] retMoves = new Move[2 + moves.length];
	retMoves[0] = m1;
	retMoves[1] = m2;
	for (int i = 0; i < moves.length; i++)
	    retMoves[2 + i] = moves[i];
	return retMoves;
    }

    public static TransferCargoAtStationMove generateMove(
        ChangeCargoBundleMove changeAtStation,
        ChangeCargoBundleMove changeOnTrain, AddTransactionMove[] payment) {
        return new TransferCargoAtStationMove(getMoveArray
		(changeAtStation, changeOnTrain, payment));
    }

    public ChangeCargoBundleMove getChangeAtStation() {
        return (ChangeCargoBundleMove)super.getMoves()[0];
    }

    public ChangeCargoBundleMove getChangeOnTrain() {
        return (ChangeCargoBundleMove)super.getMoves()[1];
    }

    public AddTransactionMove[] getPayment() {
        if (super.getMoves().length < 3) {
            return null;
        } else {
	    AddTransactionMove[] retMoves = 
		new AddTransactionMove[getMoves().length - 2];
	    for (int i = 0; i < retMoves.length; i++)
		retMoves[i] = (AddTransactionMove) getMoves()[i + 2];
            return retMoves;
        }
    }
}
