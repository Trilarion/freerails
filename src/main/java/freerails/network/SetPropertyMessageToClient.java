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

package freerails.network;

import freerails.controller.ClientControlInterface;
import freerails.controller.ClientProperty;
import freerails.controller.MessageStatus;
import freerails.util.Utils;

import java.io.Serializable;

/**
 * A MessageToClient that lets the server set a property (for example, the list
 * of saved games available) on a client.
 */
public class SetPropertyMessageToClient implements MessageToClient {

    private static final long serialVersionUID = 3544392521746034740L;
    private final int id;
    private final ClientProperty key;
    private final Serializable value;

    /**
     * @param id
     * @param key
     * @param value
     */
    public SetPropertyMessageToClient(int id, ClientProperty key, Serializable value) {
        this.id = id;
        this.key = Utils.verifyNotNull(key);
        this.value = Utils.verifyNotNull(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SetPropertyMessageToClient)) return false;

        final SetPropertyMessageToClient setPropertyMessageToClient = (SetPropertyMessageToClient) obj;

        if (id != setPropertyMessageToClient.id) return false;
        if (key != setPropertyMessageToClient.key) return false;
        return value.equals(setPropertyMessageToClient.value);
    }

    public MessageStatus execute(ClientControlInterface client) {
        client.setProperty(key, value);

        return new MessageStatus(id, true);
    }

    @Override
    public int hashCode() {
        int result;
        result = id;
        result = 29 * result + key.hashCode();
        result = 29 * result + value.hashCode();
        return result;
    }
}