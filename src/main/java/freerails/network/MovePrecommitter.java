/*
 * Created on Sep 11, 2004
 *
 */
package freerails.network;

import freerails.controller.PreMove;
import freerails.controller.PreMoveStatus;
import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.world.FreerailsSerializable;
import freerails.world.player.Player;
import freerails.world.top.World;
import org.apache.log4j.Logger;

import java.util.LinkedList;

/**
 * The class pre-commits moves we intend to send to the server and either fully
 * commits or undoes them depending on the server's response. Note, this class
 * does not actually send or receive moves, instead you should call
 * {@code toServer(.)} when a move has been sent to the server and
 * {@code fromServer(.)} when a Move or MoveStatus has been received from
 * the server.
 *
 */
public class MovePrecommitter {
    private static final Logger logger = Logger
            .getLogger(MovePrecommitter.class.getName());
    /**
     * List of moves and premoves that have been sent to the server and executed
     * on the local world object.
     */
    final LinkedList<FreerailsSerializable> precomitted = new LinkedList<>();
    /**
     * List of moves and premoves that have been sent to the server but not
     * executed on the local world object.
     */
    final LinkedList<FreerailsSerializable> uncomitted = new LinkedList<>();
    private final World w;
    /**
     * Whether the first move on the uncommitted list failed to go through on
     * the last try.
     */
    boolean blocked = false;

    MovePrecommitter(World w) {
        this.w = w;
    }

    void fromServer(Move m) {
        rollBackPrecommittedMoves();

        MoveStatus ms = m.doMove(w, Player.AUTHORITATIVE);

        if (!ms.ok) {
            throw new IllegalStateException(ms.message);
        }
    }

    /**
     * Indicates that the server has processed a move we sent.
     */
    void fromServer(MoveStatus ms) {
        precommitMoves();

        if (precomitted.size() > 0) {
            Move m = (Move) precomitted.removeFirst();

            if (!ms.ok) {
                logger.info("Move rejected by server: " + ms.message);

                MoveStatus undoStatus = m.undoMove(w, Player.AUTHORITATIVE);

                if (!undoStatus.ok) {
                    throw new IllegalStateException();
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Move accepted by server: " + m.toString());
                }
            }
        } else {
            if (!ms.ok) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Clear the blockage " + ms.message);
                }
                uncomitted.removeFirst();
                precommitMoves();
            } else {
                throw new IllegalStateException();
            }
        }
    }

    Move fromServer(PreMove pm) {
        Move generatedMove = pm.generateMove(w);
        fromServer(generatedMove);

        return generatedMove;
    }

    void fromServer(PreMoveStatus pms) {
        rollBackPrecommittedMoves();

        PreMove pm = (PreMove) uncomitted.removeFirst();

        if (pms.ms.ok) {
            if (logger.isDebugEnabled()) {
                logger.debug("PreMove accepted by server: " + pms.toString());
            }
            Move m = pm.generateMove(w);
            MoveStatus ms = m.doMove(w, Player.AUTHORITATIVE);

            if (!ms.ok) {
                throw new IllegalStateException();
            }
        } else {
            logger.info("PreMove rejected by server: " + pms.ms.message);
        }

        precommitMoves();
    }

    void precommitMoves() {
        blocked = false;

        while (uncomitted.size() > 0 && !blocked) {
            Object first = uncomitted.getFirst();

            if (first instanceof Move) {
                Move m = (Move) first;
                MoveStatus ms = m.doMove(w, Player.AUTHORITATIVE);

                if (ms.ok) {
                    uncomitted.removeFirst();
                    precomitted.addLast(m);
                } else {
                    blocked = true;
                }
            } else if (first instanceof PreMove) {
                PreMove pm = (PreMove) first;
                Move m = pm.generateMove(w);
                MoveStatus ms = m.doMove(w, Player.AUTHORITATIVE);

                if (ms.ok) {
                    uncomitted.removeFirst();

                    PreMoveAndMove pmam = new PreMoveAndMove(pm, m);
                    precomitted.addLast(pmam);
                } else {
                    blocked = true;
                }
            }
        }
    }

    /**
     * Undoes each of the precommitted moves and puts them back on the
     * uncommitted list.
     */
    private void rollBackPrecommittedMoves() {
        while (precomitted.size() > 0) {
            Object last = precomitted.removeLast();
            Move move2undo;
            FreerailsSerializable obj2add2uncomitted;

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

            MoveStatus ms = move2undo.undoMove(w, Player.AUTHORITATIVE);

            if (!ms.ok) {
                throw new IllegalStateException(ms.message);
            }

            uncomitted.addFirst(obj2add2uncomitted);
        }
    }

    void toServer(Move m) {
        uncomitted.addLast(m);
        precommitMoves();
    }

    Move toServer(PreMove pm) {
        uncomitted.addLast(pm);
        precommitMoves();

        if (blocked) {
            return pm.generateMove(w);
        }
        PreMoveAndMove pmam = (PreMoveAndMove) precomitted.getLast();

        return pmam.m;
    }

    private static class PreMoveAndMove implements FreerailsSerializable {
        private static final long serialVersionUID = 3256443607635342897L;

        final Move m;

        final PreMove pm;

        PreMoveAndMove(PreMove preMove, Move move) {
            m = move;
            pm = preMove;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof PreMoveAndMove))
                return false;

            final PreMoveAndMove preMoveAndMove = (PreMoveAndMove) o;

            if (m != null ? !m.equals(preMoveAndMove.m)
                    : preMoveAndMove.m != null)
                return false;
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