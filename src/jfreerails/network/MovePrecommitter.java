/*
 * Created on Sep 11, 2004
 *
 */
package jfreerails.network;

import java.util.LinkedList;
import java.util.logging.Logger;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
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
    private static final Logger logger = Logger.getLogger(MovePrecommitter.class.getName());
    private final World w;

    /** List of moves that have been sent to the server but not
     * executed on the local world object.
     */
    final LinkedList uncomittedMoves;

    /** List of moves that have been sent to the server and
     * executed on the local world object.
     */
    final LinkedList precomittedMoves;

    /** Whether the first move on the uncommitted list failed to go through on the last try.*/
    boolean blocked = false;

    MovePrecommitter(World w) {
        this.w = w;
        precomittedMoves = new LinkedList();
        uncomittedMoves = new LinkedList();
    }

    void toServer(Move m) {
        uncomittedMoves.addLast(m);
        precommitMoves();
    }

    void fromServer(Move m) {
        rollBackPrecommittedMoves();

        MoveStatus ms = m.doMove(w, Player.AUTHORITATIVE);

        if (!ms.ok) {
            throw new IllegalStateException();
        }
    }

    /** Undoes each of the precommitted moves and puts them back on the
     * uncommitted list.
     */
    private void rollBackPrecommittedMoves() {
        while (precomittedMoves.size() > 0) {
            Move m = (Move)precomittedMoves.removeLast();
            MoveStatus ms = m.undoMove(w, Player.AUTHORITATIVE);

            if (!ms.ok) {
                throw new IllegalStateException();
            }

            uncomittedMoves.addFirst(m);
        }
    }

    /** Indicates that the server has processed a move we sent.*/
    void fromServer(MoveStatus ms) {
        precommitMoves();

        if (precomittedMoves.size() > 0) {
            Move m = (Move)precomittedMoves.removeFirst();

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
                uncomittedMoves.removeFirst();
                precommitMoves();
            } else {
                throw new IllegalStateException();
            }
        }
    }

    void precommitMoves() {
        blocked = false;

        while (uncomittedMoves.size() > 0 && !blocked) {
            Move m = (Move)uncomittedMoves.getFirst();
            MoveStatus ms = m.doMove(w, Player.AUTHORITATIVE);

            if (ms.ok) {
                uncomittedMoves.removeFirst();
                precomittedMoves.addLast(m);
            } else {
                blocked = true;
            }
        }
    }
}