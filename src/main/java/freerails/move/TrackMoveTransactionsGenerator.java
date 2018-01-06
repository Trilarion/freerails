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

import freerails.util.ImList;
import freerails.world.ReadOnlyWorld;
import freerails.world.SKEY;
import freerails.world.finances.ItemTransaction;
import freerails.world.finances.Money;
import freerails.world.finances.Transaction;
import freerails.world.finances.TransactionCategory;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.track.*;

import java.util.ArrayList;

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
    private final ArrayList<Transaction> transactions = new ArrayList<>();
    private final ReadOnlyWorld w;
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
     * @param world
     * @param p     the Principal on behalf of which this object generates
     *              transactions for
     */
    public TrackMoveTransactionsGenerator(ReadOnlyWorld world,
                                          FreerailsPrincipal p) {
        w = world;
        principal = p;
    }

    /**
     * @param move
     * @return
     */
    public CompositeMove addTransactions(Move move) {
        int numberOfTrackTypes = w.size(SKEY.TRACK_RULES);
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
            Transaction t = transactions.get(i);
            moves[i + 1] = new AddTransactionMove(principal, t, true);
        }

        return new CompositeMove(moves);
    }

    private void unpackMove(Move move) {
        if (move instanceof ChangeTrackPieceMove) {
            ChangeTrackPieceMove tm = (ChangeTrackPieceMove) move;
            processMove(tm);
        } else if (move instanceof CompositeMove) {
            CompositeMove cm = (CompositeMove) move;
            cm.getMoves();

            ImList<Move> moves = cm.getMoves();

            for (int i = 0; i < moves.size(); i++) {
                unpackMove(moves.get(i));
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
                    fixedCostsStations -= newTrackRule.getFixedCost().getAmount();
                    break;
                }
                case bridge: {
                    fixedCostsBridges -= newTrackRule.getFixedCost().getAmount();
                    break;
                }
                default: {
                    // Do nothing.
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
                TrackRule rule = (TrackRule) w.get(SKEY.TRACK_RULES, i);
                Money m = rule.getPrice();
                Money total = new Money(-m.getAmount() * numberAdded
                        / TrackConfiguration.LENGTH_OF_STRAIGHT_TRACK_PIECE);
                Transaction t = new ItemTransaction(TransactionCategory.TRACK, i, numberAdded,
                        total);
                transactions.add(t);
            }

            int numberRemoved = trackRemoved[i];

            if (0 != numberRemoved) {
                TrackRule rule = (TrackRule) w.get(SKEY.TRACK_RULES, i);
                Money m = rule.getPrice();

                Money total = new Money((m.getAmount() * numberRemoved)
                        / TrackConfiguration.LENGTH_OF_STRAIGHT_TRACK_PIECE);

                // You only get half the money back.
                total = new Money(total.getAmount() / 2);

                Transaction t = new ItemTransaction(TransactionCategory.TRACK, i,
                        -numberRemoved, total);
                transactions.add(t);
            }
        }
        if (0 != fixedCostsStations) {
            Transaction t = new ItemTransaction(TransactionCategory.STATIONS, -1, -1, new Money(
                    fixedCostsStations));
            transactions.add(t);
        }
        if (0 != fixedCostsBridges) {
            Transaction t = new ItemTransaction(TransactionCategory.BRIDGES, -1, -1, new Money(
                    fixedCostsBridges));
            transactions.add(t);
        }
    }
}