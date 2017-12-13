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
package org.railz.server;

import javax.swing.*;
import javax.swing.event.*;

import org.railz.controller.*;
import org.railz.server.scenario.*;
import org.railz.util.*;
import org.railz.world.player.*;
import org.railz.world.top.*;
/**
 * Provides access to all installed scenarios.
 * TODO run-time discovery of scenarios.
 */
public final class ScenarioManager {
    private final ServerCommandReceiver commandReceiver;
    private final ReadOnlyWorld world;
    private final Scenario scenario;

    private static final Scenario[] scenarios = new Scenario[] {
	new SandBox(),
	new CashDash()
    };

    private static ListModel listModel = new ListModel() {
	public void addListDataListener(ListDataListener l) {
	    // ignore
	}
	
	public void removeListDataListener(ListDataListener l) {
	    // ignore
	}

	public int getSize() {
	    return scenarios.length;
	}

	public Object getElementAt(int index) {
	    return Resources.get(scenarios[index].getName());
	}
    };

    public static Scenario[] getScenarios() {
	return scenarios;
    }

    public static Scenario getScenario(String scenarioName) {
	for (int i = 0; i < scenarios.length; i++) {
	    if (scenarios[i].getName().equals(scenarioName))
		return scenarios[i];
	}
	return null;
    }

    public static ListModel getScenarioListModel() {
	return listModel;
    }

    public ScenarioManager(ReadOnlyWorld w, Scenario s,
	    ServerCommandReceiver r) {
	world = w;
	scenario = s;
	commandReceiver = r;
    }

    public void checkVictory() {
	NonNullElements i = new NonNullElements(KEY.PLAYERS, world,
		Player.AUTHORITATIVE);
	while (i.next()) {
	    FreerailsPrincipal p = ((Player) i.getElement()).getPrincipal();
	    int outcome = scenario.getVictoryState(world, p);
	    if (outcome != Scenario.UNDECIDED) {
		commandReceiver.sendCommand
		    (new VictoryCommand(p, outcome));
	    }
	} 
    }
}
