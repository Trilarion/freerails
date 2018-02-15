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
package freerails.move;

import freerails.move.mapupdatemove.ChangeTrackPieceMove;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.world.SharedKey;
import freerails.model.WorldConstants;
import freerails.model.finances.ItemTransaction;
import freerails.model.finances.Money;
import freerails.model.finances.Transaction;
import freerails.model.finances.TransactionCategory;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.track.NullTrackType;
import freerails.model.track.TrackCategories;
import freerails.model.track.TrackPiece;
import freerails.model.track.TrackRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculates the cost of a series of track moves. The motivation for
 * separating this code from the code that generates track moves is that the
 * transactions will be generated by the server whereas the track moves will be
 * generated by a client.
 */
public class TrackMoveTransactionsGenerator {
    private final FreerailsPrincipal principal;
    /*
     * Note, trackAdded and trackRemoved cannot be combined, since it may cost
     * more to added a unit of track than is refunded when you removed it.
     */
    private final List<Transaction> transactions = new ArrayList<>();
    private final ReadOnlyWorld world;
    /**
     * Number of each of the track types added.
     */
    private int[] trackAdded;
    private long fixedCostsStations = 0;
    private long fixedCostsBridges = 0;
    /**
     * Number of each of the track types removed.
     */
    private int[] trackRemoved;

    /**
     * @param p the Principal on behalf of which this object generates
     *          transactions for
     */
    public TrackMoveTransactionsGenerator(ReadOnlyWorld world, FreerailsPrincipal p) {
        this.world = world;
        principal = p;
    }

    /**
     * @param move
     * @return
     */
    public CompositeMove addTransactions(Move move) {
        int numberOfTrackTypes = world.size(SharedKey.TrackRules);
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
            moves[i + 1] = new AddTransactionMove(principal, transaction, true);
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
        TrackRule newTrackRule = newTrackPiece.getTrackRule();
        final int ruleAfter = newTrackPiece.getTrackTypeID();
        TrackPiece oldTrackPiece = move.getOldTrackPiece();
        final int ruleBefore = oldTrackPiece.getTrackTypeID();

        final int oldLength = oldTrackPiece.getTrackConfiguration().getLength();
        final int newLength = newTrackPiece.getTrackConfiguration().getLength();

        if (ruleAfter != ruleBefore) {
            TrackCategories category = newTrackRule.getCategory();
            switch (category) {
                case station: {
                    // TODO Money arithmetics
                    fixedCostsStations -= newTrackRule.getFixedCost().amount;
                    break;
                }
                case bridge: {
                    // TODO Money arithmetics
                    fixedCostsBridges -= newTrackRule.getFixedCost().amount;
                    break;
                }
                default: {}
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

        if (ruleAfter != NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER) {
            trackAdded[ruleAfter] += newLength;
        }

        if (ruleBefore != NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER) {
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
                TrackRule rule = (TrackRule) world.get(SharedKey.TrackRules, i);
                Money price = rule.getPrice();
                // TODO Money arithmetics
                Money total = new Money(-price.amount * numberAdded / WorldConstants.LENGTH_OF_STRAIGHT_TRACK_PIECE);
                Transaction transaction = new ItemTransaction(TransactionCategory.TRACK, i, numberAdded, total);
                transactions.add(transaction);
            }

            int numberRemoved = trackRemoved[i];

            if (0 != numberRemoved) {
                TrackRule rule = (TrackRule) world.get(SharedKey.TrackRules, i);
                Money m = rule.getPrice();
                // TODO Money arithmetics
                Money total = new Money((m.amount * numberRemoved) / WorldConstants.LENGTH_OF_STRAIGHT_TRACK_PIECE);

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