/*
 * Created on Sep 11, 2004
 *
 */
package jfreerails.network.specifics;

import java.util.LinkedList;
import java.util.logging.Logger;

import jfreerails.controller.PreMove;
import jfreerails.controller.PreMoveStatus;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.player.Player;
import jfreerails.world.top.World;

/**
 * The class pre-commits moves we intend to send to the server and either fully
 * commits or undoes them depending on the server's response. Note, this class
 * does not actually send or receive moves, instead you should call
 * <code>toServer(.)</code> when a move has been sent to the server and
 * <code>fromServer(.)</code> when a Move or MoveStatus has been received from
 * the server.
 * 
 * @author Luke
 * 
 */
public class MovePrecommitter {
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
			if (pm != null ? !pm.equals(preMoveAndMove.pm)
					: preMoveAndMove.pm != null)
				return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result;
			result = (m != null ? m.hashCode() : 0);
			result = 29 * result + (pm != null ? pm.hashCode() : 0);
			return result;
		}
	}

	private static final Logger logger = Logger
			.getLogger(MovePrecommitter.class.getName());

	/**
	 * Whether the first move on the uncommitted list failed to go through on
	 * the last try.
	 */
	boolean blocked = false;

	/**
	 * List of moves and premoves that have been sent to the server and executed
	 * on the local world object.
	 */
	final LinkedList<FreerailsSerializable> precomitted = new LinkedList<FreerailsSerializable>();

	/**
	 * List of moves and premoves that have been sent to the server but not
	 * executed on the local world object.
	 */
	final LinkedList<FreerailsSerializable> uncomitted = new LinkedList<FreerailsSerializable>();

	private final World w;

	MovePrecommitter(World w) {
		this.w = w;
	}

	void fromServer(Move m) {
		logger.finest("Move from server: " + m.toString());
		rollBackPrecommittedMoves();

		MoveStatus ms = m.doMove(w, Player.AUTHORITATIVE);

		if (!ms.ok) {
			throw new IllegalStateException(ms.message);
		}
	}

	/** Indicates that the server has processed a move we sent. */
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
				logger.finest("Move accepted by server: " + m.toString());
			}
		} else {
			if (!ms.ok) {
				logger.fine("Clear the blockage " + ms.message);
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
			logger.finest("PreMove accepted by server: " + pms.toString());
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
}