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

import freerails.util.Utils;

import java.io.Serial;
import java.io.Serializable;

/**
 * A CommandToClient that lets the server set a property (for example, the list
 * of saved games available) on a client.
 */
public class SetPropertyCommandToClient implements CommandToClient {

    @Serial
    private static final long serialVersionUID = 3544392521746034740L;
    private final ClientProperty key;
    private final Serializable value;

    /**
     * Creates a command that assigns a value to a client property.
     *
     * @param key property to update
     * @param value serializable value to assign
     * @throws NullPointerException if either argument is null
     */
    public SetPropertyCommandToClient(ClientProperty key, Serializable value) {
        this.key = Utils.verifyNotNull(key);
        this.value = Utils.verifyNotNull(value);
    }

    /**
     * Compares this command by property key and value.
     *
     * @param obj object to compare
     * @return {@code true} if both commands carry the same key and value
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SetPropertyCommandToClient setPropertyMessageToClient)) return false;

        if (key != setPropertyMessageToClient.key) return false;
        return value.equals(setPropertyMessageToClient.value);
    }

    /**
     * Applies the property update to a client.
     *
     * @param client client receiving the update
     * @return successful status after the property is set
     */
    @Override
    public CommandStatus execute(ClientControlInterface client) {
        client.setProperty(key, value);

        return new CommandStatus(true);
    }

    /**
     * Returns a hash code based on the property key and value.
     *
     * @return this command's hash code
     */
    @Override
    public int hashCode() {
        int result;
        result = key.hashCode();
        result = 29 * result + value.hashCode();
        return result;
    }
}