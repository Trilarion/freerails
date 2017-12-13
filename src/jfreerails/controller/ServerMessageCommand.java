/*
 * Copyright (C) 2004 Robert Tuck
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package jfreerails.controller;

import java.text.MessageFormat;
import java.io.Serializable;

import jfreerails.util.Resources;

/**
 * Used to send messages about what the server is doing to the client(s).
 * 
 * @author rtuck99@users.berlios.de
 */
public class ServerMessageCommand extends ServerCommand {
    private String messageFormatString;
    private Serializable[] objects;

    public ServerMessageCommand(String messageFormat, Serializable[]
	    formatData) {
	messageFormatString = messageFormat;
	objects = formatData;
    }

    public String getMessage() {
	return MessageFormat.format(Resources.get(messageFormatString), objects);
    }
}
