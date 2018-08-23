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

import freerails.model.game.Speed;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Changes the game speed item on the world object.
 */
public class ChangeGameSpeedMove implements Move {

    private static final long serialVersionUID = 3545794368956086071L;
    private final Speed speed;

    public ChangeGameSpeedMove(@NotNull Speed speed) {
        this.speed = speed;
    }

    @NotNull
    @Override
    public Status applicable(@NotNull UnmodifiableWorld world) {
        return Status.OK;
    }

    @Override
    public void apply(@NotNull World world) {
        world.setSpeed(speed);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ChangeGameSpeedMove)) return false;

        final ChangeGameSpeedMove other = (ChangeGameSpeedMove) obj;

        return speed.equals(other.speed);
    }

    @Override
    public int hashCode() {
        return speed.hashCode();
    }

    /**
     * @return
     */
    public Speed getSpeed() {
        return speed;
    }

    @Override
    public String toString() {
        return "ChangeGameSpeedMove: => " + speed;
    }
}