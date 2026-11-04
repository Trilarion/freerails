/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

import freerails.move.generator.MoveGenerator;
import freerails.model.world.World;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import java.io.Serializable;
import java.util.LinkedList;

// TODO Not sure what this is good for. can it be removed?
// TODO is important because of the undo mechanism, can be abolished at some point
/**
 * The class pre-commits moves we intend to send to the server and either fully
 * commits or undoes them depending on the server's response. Note, this class
 * does not actually send or receive moves, instead you should call
 * {@code toServer(.)} when a move has been sent to the server and
 * {@code fromServer(.)} when a Move or MoveStatus has been received from
 * the server.
 */
public class MovePrecommitter {

    private static final TaggedLogger logger = Logger.tag("MovePrecommitter");
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

    /**
     * Creates a move precommitter from the supplied values.
     *
     * @param world the world whose state is used or updated
     */
    public MovePrecommitter(World world) {
        this.world = world;
    }

    /**
     * Performs the from server operation.
     *
     * @param move the move
     */
    public void fromServer(Move move) {
        move.apply(world);
    }

    /**
     * Indicates that the server has processed a move we sent.
     */
    public void fromServer(Status status) {
        precommitMoves();

        // TODO in precomitted there can be a move generator instead of a move
        if (!precomitted.isEmpty()) {
            precomitted.removeFirst();
            // Move move = (Move) precomitted.removeFirst();

            if (!status.isSuccess()) {
                logger.info("Move rejected by server: " + status.getMessage());
                // TODO there was an undo here
            } else {
                // logger.debug("Move accepted by server: " + move.toString());
            }
        } else {
            if (!status.isSuccess()) {
                logger.debug("Clear the blockage " + status.getMessage());

                uncomitted.removeFirst();
                precommitMoves();
            } else {
                // throw new IllegalStateException();
            }
        }
    }

    /**
     * Generates a move from the current world and passes it through server-move precommit handling.
     *
     * @param moveGenerator the move generator
     * @return the from server
     */
    public Move fromServer(MoveGenerator moveGenerator) {
        Move generatedMove = moveGenerator.generate(world);
        fromServer(generatedMove);

        return generatedMove;
    }

    /**
     * Performs the from server operation.
     *
     * @param tryMoveStatus the try move status
     */
    public void fromServer(TryMoveStatus tryMoveStatus) {

        // TODO somehow uncommitted is empty
        if (1==1) {
            return;
        }
        MoveGenerator moveGenerator = (MoveGenerator) uncomitted.removeFirst();

        if (tryMoveStatus.status.isSuccess()) {
            logger.debug("PreMove accepted by server: " + tryMoveStatus.toString());

            Move move = moveGenerator.generate(world);
            Status status = move.applicable(world);
            move.apply(world);

            if (!status.isSuccess()) {
                throw new IllegalStateException();
            }
        } else {
            logger.info("PreMove rejected by server: " + tryMoveStatus.status.getMessage());
        }

        precommitMoves();
    }

    /**
     * Performs the precommit moves operation.
     */
    public void precommitMoves() {
        blocked = false;

        while (!uncomitted.isEmpty() && !blocked) {
            Object first = uncomitted.getFirst();

            if (first instanceof Move move) {
                Status status = move.applicable(world);
                move.apply(world);

                if (status.isSuccess()) {
                    uncomitted.removeFirst();
                    precomitted.addLast(move);
                } else {
                    blocked = true;
                }
            } else if (first instanceof MoveGenerator moveGenerator) {
                Move move = moveGenerator.generate(world);
                Status status = move.applicable(world);
                move.apply(world);

                if (status.isSuccess()) {
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
     * Returns the server.
     *
     * @param m the m
     */
    public void toServer(Serializable m) {
        uncomitted.addLast(m);
        precommitMoves();
    }

    /**
     * Returns the server.
     *
     * @param moveGenerator the move generator
     * @return the server
     */
    public Move toServer(MoveGenerator moveGenerator) {
        uncomitted.addLast(moveGenerator);
        precommitMoves();

        if (blocked) {
            return moveGenerator.generate(world);
        }
        MoveGeneratorAndMove pmam = (MoveGeneratorAndMove) precomitted.getLast();

        return pmam.move();
    }

}