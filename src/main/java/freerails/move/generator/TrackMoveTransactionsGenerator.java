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

/*
 *
 */
package freerails.move.generator;

import freerails.model.track.TrackType;
import freerails.move.AddTransactionMove;
import freerails.move.CompositeMove;
import freerails.move.Move;
import freerails.move.mapupdatemove.ChangeTrackPieceMove;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.ModelConstants;
import freerails.model.finances.ItemTransaction;
import freerails.model.finances.Money;
import freerails.model.finances.Transaction;
import freerails.model.finances.TransactionCategory;
import freerails.model.player.Player;
import freerails.model.track.TrackCategory;
import freerails.model.track.TrackPiece;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculates the cost of a series of track moves. The motivation for
 * separating this code from the code that generates track moves is that the
 * transactions will be generated by the server whereas the track moves will be
 * generated by a client.
 */
public class TrackMoveTransactionsGenerator {

    private final Player player;
    /*
     * Note, trackAdded and trackRemoved cannot be combined, since it may cost
     * more to added a unit of track than is refunded when you removed it.
     */
    private final List<Transaction> transactions = new ArrayList<>();
    private final UnmodifiableWorld world;
    /**
     * Number of each of the track types added.
     */
    private int[] trackAdded;
    // TODO use Money?
    private long fixedCostsStations = 0;
    private long fixedCostsBridges = 0;
    /**
     * Number of each of the track types removed.
     */
    private int[] trackRemoved;

    /**
     * @param player the Player on behalf of which this object generates
     *          transactions for
     */
    public TrackMoveTransactionsGenerator(UnmodifiableWorld world, Player player) {
        this.world = world;
        this.player = player;
    }

    /**
     * @param move
     * @return
     */
    public CompositeMove addTransactions(Move move) {
        int numberOfTrackTypes = world.getTrackTypes().size();
        trackAdded = new int[numberOfTrackTypes];
        trackRemoved = new int[numberOfTrackTypes];
        fixedCostsStations = 0;
        fixedCostsBridges = 0;
        unpackMove(move);
        generateTransactions();

        int numberOfMoves = 1 + transactions.size();
        Move[] moves = new Move[numberOfMoves];
        moves[0] = move;

        for (int i = 0; i < transactions.size(); i++) {
            Transaction transaction = transactions.get(i);
            moves[i + 1] = new AddTransactionMove(player, transaction, true);
        }

        return new CompositeMove(moves);
    }

    private void unpackMove(Move move) {
        if (move instanceof ChangeTrackPieceMove) {
            ChangeTrackPieceMove tm = (ChangeTrackPieceMove) move;
            processMove(tm);
        } else if (move instanceof CompositeMove) {
            CompositeMove cm = (CompositeMove) move;
            List<Move> moves = cm.getMoves();

            for (Move move1 : moves) {
                unpackMove(move1);
            }
        }
    }

    private void processMove(ChangeTrackPieceMove move) {
        TrackPiece newTrackPiece = move.getNewTrackPiece();
        TrackPiece oldTrackPiece = move.getOldTrackPiece();

        if (oldTrackPiece != null && newTrackPiece != null) {
            int oldLength = oldTrackPiece.getTrackConfiguration().getLength();
            int newLength = newTrackPiece.getTrackConfiguration().getLength();

            // TODO instead of comparing ids, use equal (which does the some)
            int ruleBefore = oldTrackPiece.getTrackType().getId();
            int ruleAfter = newTrackPiece.getTrackType().getId();

            TrackType newTrackRule = newTrackPiece.getTrackType();

            if (ruleAfter != ruleBefore) {
                TrackCategory category = newTrackRule.getCategory();
                switch (category) {
                    case STATION: {
                        // TODO Money arithmetic
                        // TODO was getFixedCost(), now not anymore, was is meaning of fixed cost?
                        fixedCostsStations -= newTrackRule.getYearlyMaintenance().amount;
                        break;
                    }
                    case BRIDGE: {
                        // TODO Money arithmetic
                        // TODO was getFixedCost(), now not anymore, was is meaning of fixed cost?
                        fixedCostsBridges -= newTrackRule.getYearlyMaintenance().amount;
                        break;
                    }
                    default: {
                    }
                }
            }

            if (ruleAfter == ruleBefore) {
                if (oldLength < newLength) {
                    trackAdded[ruleAfter] += (newLength - oldLength);
                } else if (oldLength > newLength) {
                    trackRemoved[ruleAfter] += (oldLength - newLength);
                }

                return;
            }
        }

        if (oldTrackPiece == null) {
            int ruleAfter = newTrackPiece.getTrackType().getId();
            int newLength = newTrackPiece.getTrackConfiguration().getLength();
            // TODO trackAdded should be a Map
            trackAdded[ruleAfter] += newLength;
        }

        if (newTrackPiece == null) {
            int ruleBefore = oldTrackPiece.getTrackType().getId();
            int oldLength = oldTrackPiece.getTrackConfiguration().getLength();
            // TODO trackRemoved should be a Map (id, int)
            trackRemoved[ruleBefore] += oldLength;
        }
    }

    private void generateTransactions() {
        transactions.clear();

        // For each track type, generate a transaction if any pieces of the type
        // have been added or removed.
        for (int i = 0; i < trackAdded.length; i++) {
            int numberAdded = trackAdded[i];

            if (0 != numberAdded) {
                Money price = world.getTrackType(i).getPurchasingPrice();
                // TODO Money arithmetic
                Money total = new Money(-price.amount * numberAdded / ModelConstants.LENGTH_OF_STRAIGHT_TRACK_PIECE);
                Transaction transaction = new ItemTransaction(TransactionCategory.TRACK, i, numberAdded, total);
                transactions.add(transaction);
            }

            int numberRemoved = trackRemoved[i];

            if (0 != numberRemoved) {
                Money price = world.getTrackType(i).getPurchasingPrice();
                // TODO Money arithmetic
                Money total = new Money((price.amount * numberRemoved) / ModelConstants.LENGTH_OF_STRAIGHT_TRACK_PIECE);

                // You only get half the money back.
                total = Money.divide(total, 2);

                Transaction transaction = new ItemTransaction(TransactionCategory.TRACK, i, -numberRemoved, total);
                transactions.add(transaction);
            }
        }
        if (0 != fixedCostsStations) {
            Transaction transaction = new ItemTransaction(TransactionCategory.STATIONS, -1, -1, new Money(fixedCostsStations));
            transactions.add(transaction);
        }
        if (0 != fixedCostsBridges) {
            Transaction transaction = new ItemTransaction(TransactionCategory.BRIDGES, -1, -1, new Money(fixedCostsBridges));
            transactions.add(transaction);
        }
    }
}