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
import java.util.Objects;

// TODO reason is not read anywhere
/**
 * An instance of this class is returned to the client (the server) when a
 * CommandToServer (CommandToClient) is executed by the server (the client).
 *
 * @see CommandToClient
 * @see CommandToServer
 */
public class CommandStatus implements Serializable {

    @Serial
    private static final long serialVersionUID = 3257285842216103987L;
    private final String reason;
    private final boolean successful;

    /**
     * Creates a command result with an optional explanation.
     *
     * @param successful whether the command completed successfully
     * @param reason explanation associated with the result, if any
     */
    public CommandStatus(boolean successful, String reason) {
        this.reason = reason;
        this.successful = successful;
    }

    /**
     * Creates a command result without an explanation.
     *
     * @param successful whether the command completed successfully
     */
    public CommandStatus(boolean successful) {
        reason = null;
        this.successful = successful;
    }

    /**
     * Compares this result with another result.
     *
     * @param obj object to compare
     * @return {@code true} if the success flag and reason match
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CommandStatus commandStatus)) return false;

        if (successful != commandStatus.successful) return false;
        return Objects.equals(reason, commandStatus.reason);
    }

    /**
     * Returns a hash code consistent with {@link #equals(Object)}.
     *
     * @return this result's hash code
     */
    @Override
    public int hashCode() {
        int result;
        result = reason != null ? reason.hashCode() : 0;
        result = 29 * result + (successful ? 1 : 0);
        return result;
    }

    /**
     * True if the command was successfully executed.
     */
    public boolean isSuccessful() {
        return successful;
    }
}