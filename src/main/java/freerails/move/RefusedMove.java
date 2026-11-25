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

import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.Objects;

/**
 * The result of a move generator whose action is not allowed in the current
 * world. It is never applicable, so it carries the refusal reason back to the
 * player like any other failed move.
 *
 * @param reason why the action is not allowed
 */
public record RefusedMove(String reason) implements Move {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a refused move.
     *
     * @param reason why the action is not allowed
     */
    public RefusedMove {
        Objects.requireNonNull(reason);
    }

    /**
     * @param world the world whose state is used
     * @return always a failure with the refusal reason
     */
    @Override
    public @NotNull Status applicable(@NotNull UnmodifiableWorld world) {
        return Status.fail(reason);
    }

    /**
     * Never succeeds.
     *
     * @param world the world whose state would be updated
     * @throws IllegalStateException always
     */
    @Override
    public void apply(@NotNull World world) {
        throw new IllegalStateException(reason);
    }
}
