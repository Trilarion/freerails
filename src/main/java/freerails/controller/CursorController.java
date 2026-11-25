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

package freerails.controller;

import freerails.client.ModelRoot;
import freerails.client.ModelRootProperty;
import freerails.model.terrain.TileTransition;
import freerails.model.track.BuildTrackStrategy;
import freerails.move.Status;
import freerails.util.IntPoint2D;
import freerails.util.Utils;

/**
 * Moves the map cursor for keyboard input and builds track along the way.
 *
 * <p>Only building track is done with the keyboard: a move builds track from the
 * old to the new tile only if the key may build track (the number pad, not control
 * and the arrow keys) and the build mode is {@link BuildMode#BUILD_TRACK}.
 * Upgrading and removing track need a mouse drag, so a cursor move never changes
 * them by accident.
 */
public class CursorController {

    private final ModelRoot modelRoot;

    /**
     * Creates a cursor controller for the supplied client model.
     *
     * @param modelRoot the client model holding the cursor position
     */
    public CursorController(ModelRoot modelRoot) {
        this.modelRoot = Utils.verifyNotNull(modelRoot);
    }

    /**
     * Moves the cursor one tile and, if allowed, builds track along the move. The
     * result of building is shown as the cursor message.
     *
     * @param direction the direction of the move
     * @param mayBuildTrack whether the move may build track
     * @param producer the track builder, or {@code null} if there is none yet
     * @return true if the cursor moved; false if the move would leave the map
     */
    public boolean moveCursor(TileTransition direction, boolean mayBuildTrack, TrackMoveProducer producer) {
        IntPoint2D current = (IntPoint2D) modelRoot.getProperty(ModelRootProperty.CURSOR_POSITION);
        if (current == null) {
            current = IntPoint2D.ZERO;
        }
        IntPoint2D next = IntPoint2D.add(current, direction.getD());
        if (!modelRoot.getWorld().boundsContain(next)) {
            return false;
        }
        modelRoot.setProperty(ModelRootProperty.CURSOR_POSITION, next);

        if (mayBuildTrack && producer != null && producer.getTrackBuilderMode() == BuildMode.BUILD_TRACK) {
            producer.setBuildTrackStrategy(
                    (BuildTrackStrategy) modelRoot.getProperty(ModelRootProperty.BUILD_TRACK_STRATEGY));
            Status status = producer.buildTrack(current, direction);
            modelRoot.setProperty(ModelRootProperty.CURSOR_MESSAGE, status.isSuccess() ? null : status.getMessage());
        }
        return true;
    }
}
