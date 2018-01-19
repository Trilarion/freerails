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
package freerails.network;

import freerails.move.PreMove;
import freerails.controller.PreMoveStatus;
import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.world.World;
import freerails.world.player.Player;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * The class pre-commits moves we intend to send to the server and either fully
 * commits or undoes them depending on the server's response. Note, this class
 * does not actually send or receive moves, instead you should call
 * {@code toServer(.)} when a move has been sent to the server and
 * {@code fromServer(.)} when a Move or MoveStatus has been received from
 * the server.
 */
public class MovePrecommitter {

    private static final Logger logger = Logger.getLogger(MovePrecommitter.class.getName());
    /**
     * List of moves and premoves that have been sent to the server and executed
     * on the local world object.
     */
    final LinkedList<Serializable> precomitted = new LinkedList<>();
    /**
     * List of moves and premoves that have been sent to the server but not
     * executed on the local world object.
     */
    final LinkedList<Serializable> uncomitted = new LinkedList<>();
    private final World world;
    /**
     * Whether the first move on the uncommitted list failed to go through on
     * the last try.
     */
    boolean blocked = false;

    public MovePrecommitter(World world) {
        this.world = world;
    }

    public void fromServer(Move move) {
        rollBackPrecommittedMoves();
        MoveStatus moveStatus = move.doMove(world, Player.AUTHORITATIVE);
        if (!moveStatus.succeeds()) {
            throw new IllegalStateException(moveStatus.getMessage());
        }
    }

    /**
     * Indicates that the server has processed a move we sent.
     */
    public void fromServer(MoveStatus moveStatus) {
        precommitMoves();

        if (!precomitted.isEmpty()) {
            Move move = (Move) precomitted.removeFirst();

            if (!moveStatus.succeeds()) {
                logger.info("Move rejected by server: " + moveStatus.getMessage());

                MoveStatus undoStatus = move.undoMove(world, Player.AUTHORITATIVE);

                if (!undoStatus.succeeds()) {
                    throw new IllegalStateException();
                }
            } else {
                logger.debug("Move accepted by server: " + move.toString());
            }
        } else {
            if (!moveStatus.succeeds()) {
                logger.debug("Clear the blockage " + moveStatus.getMessage());

                uncomitted.removeFirst();
                precommitMoves();
            } else {
                throw new IllegalStateException();
            }
        }
    }

    public Move fromServer(PreMove preMove) {
        Move generatedMove = preMove.generateMove(world);
        fromServer(generatedMove);

        return generatedMove;
    }

    public void fromServer(PreMoveStatus preMoveStatus) {
        rollBackPrecommittedMoves();

        PreMove preMove = (PreMove) uncomitted.removeFirst();

        if (preMoveStatus.moveStatus.succeeds()) {
            logger.debug("PreMove accepted by server: " + preMoveStatus.toString());

            Move move = preMove.generateMove(world);
            MoveStatus moveStatus = move.doMove(world, Player.AUTHORITATIVE);

            if (!moveStatus.succeeds()) {
                throw new IllegalStateException();
            }
        } else {
            logger.info("PreMove rejected by server: " + preMoveStatus.moveStatus.getMessage());
        }

        precommitMoves();
    }

    void precommitMoves() {
        blocked = false;

        while (!uncomitted.isEmpty() && !blocked) {
            Object first = uncomitted.getFirst();

            if (first instanceof Move) {
                Move move = (Move) first;
                MoveStatus moveStatus = move.doMove(world, Player.AUTHORITATIVE);

                if (moveStatus.succeeds()) {
                    uncomitted.removeFirst();
                    precomitted.addLast(move);
                } else {
                    blocked = true;
                }
            } else if (first instanceof PreMove) {
                PreMove preMove = (PreMove) first;
                Move move = preMove.generateMove(world);
                MoveStatus moveStatus = move.doMove(world, Player.AUTHORITATIVE);

                if (moveStatus.succeeds()) {
                    uncomitted.removeFirst();

                    Serializable pmam = new PreMoveAndMove(preMove, move);
                    precomitted.addLast(pmam);
                } else {
                    blocked = true;
                }
            }
        }
    }

    /**
     * Undoes each of the pre-committed moves and puts them back on the
     * uncommitted list.
     */
    private void rollBackPrecommittedMoves() {
        while (!precomitted.isEmpty()) {
            Object last = precomitted.removeLast();
            Move move2undo;
            Serializable obj2add2uncomitted;

            if (last instanceof Move) {
                move2undo = (Move) last;
                obj2add2uncomitted = move2undo;
            } else if (last instanceof PreMoveAndMove) {
                PreMoveAndMove pmam = (PreMoveAndMove) last;
                move2undo = pmam.move;
                obj2add2uncomitted = pmam.preMove;
            } else {
                throw new IllegalStateException();
            }

            MoveStatus moveStatus = move2undo.undoMove(world, Player.AUTHORITATIVE);

            if (!moveStatus.succeeds()) {
                throw new IllegalStateException(moveStatus.getMessage());
            }

            uncomitted.addFirst(obj2add2uncomitted);
        }
    }

    public void toServer(Serializable m) {
        uncomitted.addLast(m);
        precommitMoves();
    }

    public Move toServer(PreMove preMove) {
        uncomitted.addLast(preMove);
        precommitMoves();

        if (blocked) {
            return preMove.generateMove(world);
        }
        PreMoveAndMove pmam = (PreMoveAndMove) precomitted.getLast();

        return pmam.move;
    }

    private static class PreMoveAndMove implements Serializable {

        private static final long serialVersionUID = 3256443607635342897L;
        private final Move move;
        private final PreMove preMove;

        private PreMoveAndMove(PreMove preMove, Move move) {
            this.move = move;
            this.preMove = preMove;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof PreMoveAndMove)) return false;

            final PreMoveAndMove preMoveAndMove = (PreMoveAndMove) obj;

            if (move != null ? !move.equals(preMoveAndMove.move) : preMoveAndMove.move != null) return false;
            return preMove != null ? preMove.equals(preMoveAndMove.preMove) : preMoveAndMove.preMove == null;
        }

        @Override
        public int hashCode() {
            int result;
            result = (move != null ? move.hashCode() : 0);
            result = 29 * result + (preMove != null ? preMove.hashCode() : 0);
            return result;
        }
    }
}