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
 * ChangeTrackPieceCompositeMove.java
 *
 */
package freerails.move.mapupdatemove;

import freerails.model.finance.TransactionUtils;
import freerails.model.station.Station;
import freerails.model.train.Train;
import freerails.model.train.schedule.Schedule;
import freerails.model.train.schedule.UnmodifiableSchedule;
import freerails.model.world.*;
import freerails.move.*;
import freerails.util.Vec2D;
import freerails.model.game.Rules;
import freerails.model.player.Player;
import freerails.model.terrain.TileTransition;
import freerails.model.track.*;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This Move changes adds, removes, or upgrades the track between two tiles.
 */
public final class ChangeTrackPieceCompositeMove extends CompositeMove implements TrackMove {

    private static final long serialVersionUID = 3616443518780978743L;
    private final Rectangle rectangle;
    private final Player player;

    public ChangeTrackPieceCompositeMove(TrackMove a, TrackMove b, Player player) {
        super(Arrays.asList(a, b));
        rectangle = a.getUpdatedTiles().union(b.getUpdatedTiles());
        this.player = player;
    }

    /**
     * @return
     */
    @Override
    public Rectangle getUpdatedTiles() {
        return rectangle;
    }

    @Override
    public Status compositeTest(World world) {
        // must connect to existing track
        Rules rules = world.getRules();

        if (rules.mustStayConnectedToExistingTrack()) {
            if (TransactionUtils.hasAnyTrackBeenBuilt(world, player)) {
                try {
                    ChangeTrackPieceMove a = (ChangeTrackPieceMove) super.getMove(0);
                    ChangeTrackPieceMove b = (ChangeTrackPieceMove) super.getMove(1);

                    if (a.trackPieceBefore == null && b.trackPieceBefore == null) {
                        return Status.fail("Must connect to existing track");
                    }
                } catch (ClassCastException e) {
                    // It was not the type of move we expected.
                    // We end up here when we are removing a station.
                    // TODO and what if it must indeed connect to other tracks???
                    return Status.OK;
                }
            }
        }
        return Status.OK;
    }

    @Override
    public void apply(@NotNull World world) {
        super.apply(world);
    }
}