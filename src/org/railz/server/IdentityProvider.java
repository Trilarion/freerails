/*
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

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.railz.config.LogManager;
import org.railz.controller.ConnectionToServer;
import org.railz.controller.MoveChainFork;
import org.railz.controller.MoveReceiver;
import org.railz.controller.SourcedMoveReceiver;
import org.railz.move.AddPlayerMove;
import org.railz.move.Move;
import org.railz.move.MoveStatus;
import org.railz.move.RejectedMove;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.player.Player;
import org.railz.world.top.KEY;
import org.railz.world.top.NonNullElements;
import org.railz.world.top.World;
import org.railz.world.top.WorldView;

/**
 * Provides a method by which a Principal may be obtained
 */
class IdentityProvider {
    private static final String CLASS_NAME = IdentityProvider.class.getName();
    private static final Logger logger = LogManager.getLogger(CLASS_NAME);
    
    private StatGatherer statGatherer;
    
    /**
     * submits a move to the queue, sleeps until confirmation that a move has
     * been implemented (un)successfully received
     */
    private class MoveConfirmer implements MoveReceiver {
	boolean confirmed = false;
	MoveStatus result = MoveStatus.moveFailed("not executed!");
	SourcedMoveReceiver executer;
	private Move move;
	private MoveChainFork moveChainFork;
	
	public MoveConfirmer(SourcedMoveReceiver me, MoveChainFork mcf) {
	    executer = me;
	    moveChainFork = mcf;
	    mcf.add(this);
	}
	
	public synchronized void processMove(Move m) {
	    if (m.equals(move)) {
		confirmed = true;
		result = MoveStatus.MOVE_OK;
		notify();
	    } else if (m instanceof RejectedMove) {
		RejectedMove rm = (RejectedMove) m;
		
		if (move.equals(rm.getAttemptedMove())) {
		    confirmed = true;
		    result = rm.getMoveStatus();
		    notify();
		}
	    }
	}
	
	public synchronized MoveStatus confirmMove(Move m, ConnectionToServer c) {
	    move = m;
	    executer.processMove(m, c);
	    
	    while (!confirmed) {
		try {
		    wait();
		} catch (InterruptedException e) {
		    // ignore
		}
	    }
	    
	    moveChainFork.remove(this);
	    
	    return result;
	}
    }
    
    /**
     * A HashMap in which the keys are instances of ConnectionToServer and the
     * values are FreerailsPrincipals.
     */
    private HashMap principals = new HashMap();
    private ServerGameEngine serverGameEngine;
    private Scenario scenario;
    
    public IdentityProvider(ServerGameEngine s, Scenario scenario, StatGatherer sg) {
	serverGameEngine = s;
	this.scenario = scenario;
	statGatherer = sg;
    }
    
    /**
     * TODO deprecate strings in favour of certificates XXX currently we only
     * support a single player per connection.
     * 
     * @param player
     *            the player trying to connect
     * @return true if the connection was successfully identified with the
     *         specified player
     */
    public synchronized boolean addConnection(ConnectionToServer c, Player player, byte[] signature) {
	logger.log(Level.INFO, "Authenticating player " + player.getName());
	
	/* determine whether this identity already exists */
	NonNullElements i = new NonNullElements(KEY.PLAYERS, serverGameEngine.getWorld(),
		Player.AUTHORITATIVE);
	
	while (i.next()) {
	    Player p = (Player) i.getElement();
	    
	    if (p.getName().equals(player.getName())) {
		/* this player already exists */
		/* is this identity already connected ? */
		if (principals.containsValue(p)) {
		    logger.log(Level.INFO, "Player " + p.getName() + " is already" + " connected");
		    
		    return false;
		}
		
		/*
		 * is this player the same as the one which previously connected
		 * under the same name?
		 */
		logger.log(Level.FINE, "Verifying player " + p + " with " + player);
		
		if (!p.verify(player, signature)) {
		    logger.log(Level.WARNING, "Couldn't verify signature of player " + p.getName());
		    
		    return false;
		}
		
		principals.put(c, p);
		
		/* set the connection world */
		c.setWorld(new WorldView(serverGameEngine.getWorld(), p.getPrincipal()));
		
		return true;
	    }
	}
	
	/* this player does not already exist */
	logger.log(Level.INFO,
		"Adding player " + player.getName() + " to " + serverGameEngine.getWorld());
	
	MoveConfirmer mc = new MoveConfirmer(serverGameEngine.getMoveExecuter(),
		serverGameEngine.getMoveChainFork());
	AddPlayerMove m = new AddPlayerMove(serverGameEngine.getWorld(), player);
	MoveStatus ms = mc.confirmMove(m, null);
	
	assert ms == MoveStatus.MOVE_OK;
	
	/*
	 * get the newly created player-with-principal
	 */
	World w = serverGameEngine.getWorld();
	assert (w != null);
	player = (Player) w.get(KEY.PLAYERS, w.size(KEY.PLAYERS, Player.AUTHORITATIVE) - 1,
		Player.AUTHORITATIVE);
	
	principals.put(c, player);
	
	/* Perform any moves necessary for adding a new player */
	serverGameEngine.getMoveExecuter().processMove(
		statGatherer.generateNewPlayerMove(player.getPrincipal()), c);
	serverGameEngine.getMoveExecuter().processMove(
		scenario.getSetupMoves(serverGameEngine.getWorld(), player.getPrincipal()), c);
	
	/* set the connection world */
	c.setWorld(new WorldView(serverGameEngine.getWorld(), player.getPrincipal()));
	return true;
    }
    
    /**
     * Dissociate all players with this connection
     */
    public synchronized void removeConnection(ConnectionToServer c) {
	principals.remove(c);
	c.setWorld(null);
    }
    
    public synchronized FreerailsPrincipal getPrincipal(ConnectionToServer c) {
	Player p;
	
	if (c == null) {
	    /* No connection implies the principal is the server itself */
	    return Player.AUTHORITATIVE;
	}
	
	if ((p = (Player) principals.get(c)) == null) {
	    return Player.NOBODY;
	}
	
	return p.getPrincipal();
    }
    
    public synchronized Player getPlayer(FreerailsPrincipal p) {
	NonNullElements i = new NonNullElements(KEY.PLAYERS, serverGameEngine.getWorld(),
		Player.AUTHORITATIVE);
	
	while (i.next()) {
	    if (((Player) i.getElement()).getPrincipal().equals(p)) {
		return (Player) i.getElement();
	    }
	}
	
	return null;
    }
}
