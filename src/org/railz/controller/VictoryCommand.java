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
package org.railz.controller;

import org.railz.world.player.*;
/**
 * Sent by the server to indicate that a player has won or lost.
 */
public class VictoryCommand extends ServerCommand {
    private FreerailsPrincipal player;
    private int outcome;

    public VictoryCommand(FreerailsPrincipal player, int outcome) {
	this.player = player;
	this.outcome = outcome;
    }

    /**
     * @return one of Scenario.VICTORY or SCENARIO.DEFEAT
     */
    public int getOutcome() {
	return outcome;
    }

    /**
     * @return the player that has won or lost.
     */
    public FreerailsPrincipal getPlayer() {
	return player;
    }
}
