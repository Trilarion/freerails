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

package freerails.move;

import freerails.model.player.Player;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Advances the time within the world object.
 */
public class AdvanceClockMove implements Move {

    private static final long serialVersionUID = 3257290240212153393L;
    private final Player player;

    public AdvanceClockMove(@NotNull Player player) {
        this.player = player;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AdvanceClockMove)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public Status applicable(@NotNull UnmodifiableWorld world) {
        if (!player.equals(Player.AUTHORITATIVE)) {
            return Status.fail("Only authoritative player shall advance the clock.");
        }
        return Status.OK;
    }

    @Override
    public void apply(World world) {
        world.getClock().advanceTime();
    }
}