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

import freerails.controller.PreMove;
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
class MovePrecommitter {

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

    MovePrecommitter(World world) {
        this.world = world;
    }

    void fromServer(Move move) {
        rollBackPrecommittedMoves();
        MoveStatus moveStatus = move.doMove(world, Player.AUTHORITATIVE);
        if (!moveStatus.status) {
            throw new IllegalStateException(moveStatus.message);
        }
    }

    /**
     * Indicates that the server has processed a move we sent.
     */
    void fromServer(MoveStatus moveStatus) {
        precommitMoves();

        if (!precomitted.isEmpty()) {
            Move m = (Move) precomitted.removeFirst();

            if (!moveStatus.status) {
                logger.info("Move rejected by server: " + moveStatus.message);

                MoveStatus undoStatus = m.undoMove(world, Player.AUTHORITATIVE);

                if (!undoStatus.status) {
                    throw new IllegalStateException();
                }
            } else {
                logger.debug("Move accepted by server: " + m.toString());
            }
        } else {
            if (!moveStatus.status) {
                logger.debug("Clear the blockage " + moveStatus.message);

                uncomitted.removeFirst();
                precommitMoves();
            } else {
                throw new IllegalStateException();
            }
        }
    }

    Move fromServer(PreMove pm) {
        Move generatedMove = pm.generateMove(world);
        fromServer(generatedMove);

        return generatedMove;
    }

    void fromServer(PreMoveStatus pms) {
        rollBackPrecommittedMoves();

        PreMove pm = (PreMove) uncomitted.removeFirst();

        if (pms.moveStatus.status) {
            logger.debug("PreMove accepted by server: " + pms.toString());

            Move m = pm.generateMove(world);
            MoveStatus ms = m.doMove(world, Player.AUTHORITATIVE);

            if (!ms.status) {
                throw new IllegalStateException();
            }
        } else {
            logger.info("PreMove rejected by server: " + pms.moveStatus.message);
        }

        precommitMoves();
    }

    void precommitMoves() {
        blocked = false;

        while (!uncomitted.isEmpty() && !blocked) {
            Object first = uncomitted.getFirst();

            if (first instanceof Move) {
                Move m = (Move) first;
                MoveStatus ms = m.doMove(world, Player.AUTHORITATIVE);

                if (ms.status) {
                    uncomitted.removeFirst();
                    precomitted.addLast(m);
                } else {
                    blocked = true;
                }
            } else if (first instanceof PreMove) {
                PreMove pm = (PreMove) first;
                Move m = pm.generateMove(world);
                MoveStatus ms = m.doMove(world, Player.AUTHORITATIVE);

                if (ms.status) {
                    uncomitted.removeFirst();

                    Serializable pmam = new PreMoveAndMove(pm, m);
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
                move2undo = pmam.m;
                obj2add2uncomitted = pmam.pm;
            } else {
                throw new IllegalStateException();
            }

            MoveStatus ms = move2undo.undoMove(world, Player.AUTHORITATIVE);

            if (!ms.status) {
                throw new IllegalStateException(ms.message);
            }

            uncomitted.addFirst(obj2add2uncomitted);
        }
    }

    void toServer(Serializable m) {
        uncomitted.addLast(m);
        precommitMoves();
    }

    Move toServer(PreMove pm) {
        uncomitted.addLast(pm);
        precommitMoves();

        if (blocked) {
            return pm.generateMove(world);
        }
        PreMoveAndMove pmam = (PreMoveAndMove) precomitted.getLast();

        return pmam.m;
    }

    private static class PreMoveAndMove implements Serializable {
        private static final long serialVersionUID = 3256443607635342897L;

        private final Move m;

        private final PreMove pm;

        private PreMoveAndMove(PreMove preMove, Move move) {
            m = move;
            pm = preMove;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof PreMoveAndMove)) return false;

            final PreMoveAndMove preMoveAndMove = (PreMoveAndMove) obj;

            if (m != null ? !m.equals(preMoveAndMove.m) : preMoveAndMove.m != null) return false;
            return pm != null ? pm.equals(preMoveAndMove.pm) : preMoveAndMove.pm == null;
        }

        @Override
        public int hashCode() {
            int result;
            result = (m != null ? m.hashCode() : 0);
            result = 29 * result + (pm != null ? pm.hashCode() : 0);
            return result;
        }
    }
}