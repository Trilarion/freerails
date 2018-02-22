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

package freerails.network.command;

import java.io.Serializable;

/**
 * An instance of this class is returned to the client (the server) when a
 * CommandToServer (CommandToClient) is executed by the server (the client).
 *
 * @see CommandToClient
 * @see CommandToServer
 */
public class CommandStatus implements Serializable {

    private static final long serialVersionUID = 3257285842216103987L;
    private final int id;
    private final String reason;
    private final boolean successful;

    /**
     * @param id
     * @param successful
     * @param reason
     */
    public CommandStatus(int id, boolean successful, String reason) {
        this.id = id;
        this.reason = reason;
        this.successful = successful;
    }

    /**
     * @param id
     * @param successful
     */
    public CommandStatus(int id, boolean successful) {
        this.id = id;
        reason = null;
        this.successful = successful;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CommandStatus)) return false;

        final CommandStatus commandStatus = (CommandStatus) obj;

        if (id != commandStatus.id) return false;
        if (successful != commandStatus.successful) return false;
        return reason != null ? reason.equals(commandStatus.reason) : commandStatus.reason == null;
    }

    @Override
    public int hashCode() {
        int result;
        result = id;
        result = 29 * result + (reason != null ? reason.hashCode() : 0);
        result = 29 * result + (successful ? 1 : 0);
        return result;
    }

    /**
     * Returns the id of the command whose success this object stores.
     */
    public int getId() {
        return id;
    }

    /**
     * True if the command was successfully executed.
     */
    public boolean isSuccessful() {
        return successful;
    }
}