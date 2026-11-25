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

package freerails.move;

import freerails.model.player.Player;
import freerails.model.simulation.Speed;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;

/**
 * Changes the game speed item on the world object. Only the host may change it
 * (see {@link Player#isHost()}).

 * @param player the player changing the speed
 * @param speed the simulation speed to set
 */
public record ChangeGameSpeedMove(Player player, Speed speed) implements ClientMove {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a change game speed move from the supplied values.
     *
     * @param player the player changing the speed
     * @param speed the speed
     */
    public ChangeGameSpeedMove(@NotNull Player player, @NotNull Speed speed) {
        this.player = player;
        this.speed = speed;
    }

    /**
     * @param player the player the move acts for
     * @return this move made by the supplied player
     */
    @Override
    public @NotNull ChangeGameSpeedMove withPlayer(@NotNull Player player) {
        return new ChangeGameSpeedMove(player, speed);
    }

    /**
     * Checks that the host changes the speed.
     *
     * @param world the world whose state is used or updated
     * @return a status indicating whether the move can be applied
     */
    @NotNull
    @Override
    public Status applicable(@NotNull UnmodifiableWorld world) {
        if (!player.isHost()) {
            return Status.fail("Only the host may change the game speed.");
        }
        return Status.OK;
    }

    /**
     * Applies this move to the supplied world.
     *
     * @param world the world whose state is used or updated
     */
    @Override
    public void apply(@NotNull World world) {
        Status status = applicable(world);
        if (!status.isSuccess()) {
            throw new IllegalStateException(status.getMessage());
        }
        world.setSpeed(speed);
    }

    /**
     * Returns a string representation of this object.
     *
     * @return the string
     */
    @Override
    public @NotNull String toString() {
        return "ChangeGameSpeedMove: => " + speed;
    }
}