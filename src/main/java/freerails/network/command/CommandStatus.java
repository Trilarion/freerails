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

package freerails.network.command;

import java.io.Serial;
import java.io.Serializable;

/**
 * An instance of this class is returned to the client (the server) when a
 * CommandToServer (CommandToClient) is executed by the server (the client).
 *
 * @see CommandToClient
 * @see CommandToServer
 */
public record CommandStatus(boolean successful) implements Serializable {

    @Serial
    private static final long serialVersionUID = 3257285842216103987L;

    /**
     * Creates a command result.
     *
     * @param successful whether the command completed successfully
     */
    public CommandStatus {
    }

    /**
     * Compares this result with another result.
     *
     * @param obj object to compare
     * @return {@code true} if the success flag matches
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CommandStatus commandStatus)) return false;

        return successful == commandStatus.successful;
    }

    /**
     * Returns a hash code consistent with {@link #equals(Object)}.
     *
     * @return this result's hash code
     */
    @Override
    public int hashCode() {
        return Boolean.hashCode(successful);
    }

    /**
     * True if the command was successfully executed.
     */
    @Override
    public boolean successful() {
        return successful;
    }
}