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

import freerails.model.player.Player;
import freerails.move.generator.MoveGenerator;
import freerails.model.world.World;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.LinkedList;

// TODO Not sure what this is good for. can it be removed?
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
    public final LinkedList<Serializable> precomitted = new LinkedList<>();
    /**
     * List of moves and premoves that have been sent to the server but not
     * executed on the local world object.
     */
    public final LinkedList<Serializable> uncomitted = new LinkedList<>();
    private final World world;
    /**
     * Whether the first move on the uncommitted list failed to go through on
     * the last try.
     */
    public boolean blocked = false;

    public MovePrecommitter(World world) {
        this.world = world;
    }

    public void fromServer(Move move) {
        rollBackPrecommittedMoves();
        Status status = move.doMove(world, Player.AUTHORITATIVE);
        if (!status.succeeds()) {
            throw new IllegalStateException(status.getMessage());
        }
    }

    /**
     * Indicates that the server has processed a move we sent.
     */
    public void fromServer(Status status) {
        precommitMoves();

        if (!precomitted.isEmpty()) {
            Move move = (Move) precomitted.removeFirst();

            if (!status.succeeds()) {
                logger.info("Move rejected by server: " + status.getMessage());

                Status undoStatus = move.undoMove(world, Player.AUTHORITATIVE);

                if (!undoStatus.succeeds()) {
                    throw new IllegalStateException();
                }
            } else {
                logger.debug("Move accepted by server: " + move.toString());
            }
        } else {
            if (!status.succeeds()) {
                logger.debug("Clear the blockage " + status.getMessage());

                uncomitted.removeFirst();
                precommitMoves();
            } else {
                throw new IllegalStateException();
            }
        }
    }

    public Move fromServer(MoveGenerator moveGenerator) {
        Move generatedMove = moveGenerator.generate(world);
        fromServer(generatedMove);

        return generatedMove;
    }

    public void fromServer(TryMoveStatus tryMoveStatus) {
        rollBackPrecommittedMoves();

        MoveGenerator moveGenerator = (MoveGenerator) uncomitted.removeFirst();

        if (tryMoveStatus.status.succeeds()) {
            logger.debug("PreMove accepted by server: " + tryMoveStatus.toString());

            Move move = moveGenerator.generate(world);
            Status status = move.doMove(world, Player.AUTHORITATIVE);

            if (!status.succeeds()) {
                throw new IllegalStateException();
            }
        } else {
            logger.info("PreMove rejected by server: " + tryMoveStatus.status.getMessage());
        }

        precommitMoves();
    }

    public void precommitMoves() {
        blocked = false;

        while (!uncomitted.isEmpty() && !blocked) {
            Object first = uncomitted.getFirst();

            if (first instanceof Move) {
                Move move = (Move) first;
                Status status = move.doMove(world, Player.AUTHORITATIVE);

                if (status.succeeds()) {
                    uncomitted.removeFirst();
                    precomitted.addLast(move);
                } else {
                    blocked = true;
                }
            } else if (first instanceof MoveGenerator) {
                MoveGenerator moveGenerator = (MoveGenerator) first;
                Move move = moveGenerator.generate(world);
                Status status = move.doMove(world, Player.AUTHORITATIVE);

                if (status.succeeds()) {
                    uncomitted.removeFirst();

                    Serializable pmam = new MoveGeneratorAndMove(moveGenerator, move);
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
            } else if (last instanceof MoveGeneratorAndMove) {
                MoveGeneratorAndMove pmam = (MoveGeneratorAndMove) last;
                move2undo = pmam.move;
                obj2add2uncomitted = pmam.moveGenerator;
            } else {
                throw new IllegalStateException();
            }

            Status status = move2undo.undoMove(world, Player.AUTHORITATIVE);

            if (!status.succeeds()) {
                throw new IllegalStateException(status.getMessage());
            }

            uncomitted.addFirst(obj2add2uncomitted);
        }
    }

    public void toServer(Serializable m) {
        uncomitted.addLast(m);
        precommitMoves();
    }

    public Move toServer(MoveGenerator moveGenerator) {
        uncomitted.addLast(moveGenerator);
        precommitMoves();

        if (blocked) {
            return moveGenerator.generate(world);
        }
        MoveGeneratorAndMove pmam = (MoveGeneratorAndMove) precomitted.getLast();

        return pmam.move;
    }

}