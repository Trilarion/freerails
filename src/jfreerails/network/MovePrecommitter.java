/*
 * Created on Sep 11, 2004
 *
 */
package jfreerails.network;

import java.util.LinkedList;
import java.util.logging.Logger;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.move.PreMove;
import jfreerails.move.PreMoveStatus;
import jfreerails.world.player.Player;
import jfreerails.world.top.World;


/**
 * The class pre-commits moves we intend to send to the server and either fully commits or undoes
 * them depending on the server's response. Note, this class does not actually send
 * or receive moves, instead you should call <code>toServer(.)</code> when a move has been sent
 * to the server and <code>fromServer(.)</code> when a Move or MoveStatus has been received from
 * the server.
 *
 * @author Luke
 *
 */
public class MovePrecommitter {
    private class PreMoveAndMove {
        final Move m;
        final PreMove pm;

        PreMoveAndMove(PreMove preMove, Move move) {
            m = move;
            pm = preMove;
        }
    }

    private static final Logger logger = Logger.getLogger(MovePrecommitter.class.getName());

    /** Whether the first move on the uncommitted list failed to go through on the last try.*/
    boolean blocked = false;

    /** List of moves that have been sent to the server and
     * executed on the local world object.
     */
    final LinkedList precomitted = new LinkedList();

    /** List of moves that have been sent to the server but not
     * executed on the local world object.
     */
    final LinkedList uncomitted = new LinkedList();
    private final World w;

    MovePrecommitter(World w) {
        this.w = w;
    }

    void fromServer(Move m) {
        rollBackPrecommittedMoves();

        MoveStatus ms = m.doMove(w, Player.AUTHORITATIVE);

        if (!ms.ok) {
            throw new IllegalStateException();
        }
    }

    /** Indicates that the server has processed a move we sent.*/
    void fromServer(MoveStatus ms) {
        precommitMoves();

        if (precomitted.size() > 0) {
            Move m = (Move)precomitted.removeFirst();

            if (!ms.ok) {
                logger.fine("Move rejected by server: " + ms.message);

                MoveStatus undoStatus = m.undoMove(w, Player.AUTHORITATIVE);

                if (!undoStatus.ok) {
                    throw new IllegalStateException();
                }
            }
        } else {
            if (!ms.ok) {
                //clear the blockage,
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

        PreMove pm = (PreMove)uncomitted.removeFirst();

        if (pms.ms.ok) {
            Move m = pm.generateMove(w);
            MoveStatus ms = m.doMove(w, Player.AUTHORITATIVE);

            if (!ms.ok) {
                throw new IllegalStateException();
            }
        } else {
            logger.fine("PreMove rejected by server: " + pms.ms.message);
        }

        precommitMoves();
    }

    void precommitMoves() {
        blocked = false;

        while (uncomitted.size() > 0 && !blocked) {
            Object first = uncomitted.getFirst();

            if (first instanceof Move) {
                Move m = (Move)first;
                MoveStatus ms = m.doMove(w, Player.AUTHORITATIVE);

                if (ms.ok) {
                    uncomitted.removeFirst();
                    precomitted.addLast(m);
                } else {
                    blocked = true;
                }
            } else if (first instanceof PreMove) {
                PreMove pm = (PreMove)first;
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

    /** Undoes each of the precommitted moves and puts them back on the
     * uncommitted list.
     */
    private void rollBackPrecommittedMoves() {
        while (precomitted.size() > 0) {
            Object last = precomitted.removeLast();
            Move move2undo;
            Object obj2add2uncomitted;

            if (last instanceof Move) {
                move2undo = (Move)last;
                obj2add2uncomitted = move2undo;
            } else if (last instanceof PreMoveAndMove) {
                PreMoveAndMove pmam = (PreMoveAndMove)last;
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
        } else {
            PreMoveAndMove pmam = (PreMoveAndMove)precomitted.getLast();

            return pmam.m;
        }
    }
}