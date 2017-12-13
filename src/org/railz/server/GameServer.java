/*
 * Copyright (C) Robert Tuck
 * Copyright (C) Luke Lindsay
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

package org.railz.server;

import java.io.*;
import java.util.Vector;

import org.railz.controller.ServerControlInterface;
import org.railz.util.FreerailsProgressMonitor;

/**
 * This implements a game server. A game server may host a number of independent
 * games all being played simultaneously.
 *
 * @author lindsal
 * @author rtuck99@users.sourceforge.net
 */
public class GameServer {
    /**
     * The set of games which this server is serving. Vector of
     * ServerGameController.
     */
    private Vector gameControllers = new Vector();

    /**
     * starts the server and creates a new ServerGameEngine running initialised
     * from a new map, accepting connections on the default port.
     */
    public ServerControlInterface getNewGame(String mapName,
        FreerailsProgressMonitor pm, int port, Scenario scenario) {
	ServerGameEngine gameEngine = new ServerGameEngine(mapName, pm,
		scenario);
        ServerGameController sgc = new ServerGameController(gameEngine, port);
        gameControllers.add(sgc);

        return sgc;
    }

    /**
     * Load a saved game
     * @param port port number on which to accept incoming connections, or 0 for
     * no network connections.
     */
    public ServerControlInterface getSavedGame(FreerailsProgressMonitor pm,
        int port, File filename) {
	ServerGameEngine gameEngine;
	try {
	    gameEngine = ServerGameEngine.loadGame(filename);
	} catch (IOException e) {
	    pm.setMessage ("There was a problem loading the game: " +
		    e.getMessage());
	    return null;
	}
        ServerGameController sgc = new ServerGameController(gameEngine, port);
        gameControllers.add(sgc);

        return sgc;
    }

    /**
     * @return a list of possible map names that could be used to start a game
     */
    public static String[] getMapNames() {
        return WorldFactory.getMapNames();
    }
}
