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

package freerails.client;

import freerails.move.*;
import freerails.util.Utils;
import freerails.model.world.UnmodifiableWorld;

import freerails.util.ui.SoundManager;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Inspects incoming moves and generates a user message if appropriate. It is also used to trigger sounds.
 */
public class UserMessageGenerator implements MoveReceiver {

    private final DecimalFormat formatter = new DecimalFormat("#,###,###");
    private final SoundManager soundManager = SoundManager.getInstance();
    private ModelRoot modelRoot;
    private ActionRoot actionRoot;

    /**
     * Creates a user message generator instance.
     *
     * @param modelRoot the client model that provides current game state
     * @param actionRoot the root that supplies client actions
     */
    public UserMessageGenerator(ModelRoot modelRoot, ActionRoot actionRoot) {
        this.actionRoot = Utils.verifyNotNull(actionRoot);
        this.modelRoot = Utils.verifyNotNull(modelRoot);
    }

    /**
     * Performs the process operation.
     *
     * @param move the move parameter
     */
    @Override
    public void process(Move move) {
        // TODO then it could also be a splitMoveReceiver in MoveChainFork
        if (move instanceof CompositeMove) {
            List<Move> moves = ((CompositeMove) move).getMoves();

            for (Move move1 : moves) {
                process(move1);
            }
        }

        // TODO there was a WorldDiffMove before which triggered trainArrives (must be re-implemented) somehow
        // trainArrives(move);

        if (move instanceof ChangeGameSpeedMove) {
            logSpeed();
        }
    }

    /**
     * Performs the log speed operation.
     */
    public void logSpeed() {
        UnmodifiableWorld world = modelRoot.getWorld();
        int gameSpeed = world.getSpeed().getTicksPerSecond();

        if (gameSpeed <= 0) {
            modelRoot.setProperty(ModelRootProperty.PERMANENT_MESSAGE, "Game is paused.");

            /*
             * Also hide any other message. It looks silly if it says "Game is
             * paused." and "Game speed: fast" on screen at the same time!
             */
            modelRoot.setProperty(ModelRootProperty.QUICK_MESSAGE, "");
        } else {
            modelRoot.setProperty(ModelRootProperty.PERMANENT_MESSAGE, null);

            String gameSpeedDesc = actionRoot.getServerControls().getGameSpeedDesc(gameSpeed);
            modelRoot.setProperty(ModelRootProperty.QUICK_MESSAGE, "Game speed: " + gameSpeedDesc);
        }
    }
}