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
package org.railz.server.scenario;

import java.awt.*;
import java.text.*;
import javax.swing.*;

import org.railz.move.*;
import org.railz.server.*;
import org.railz.util.*;
import org.railz.util.Resources.ResourceKey;
import org.railz.world.accounts.*;
import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.top.*;

/**
 * An open ended game.
 * TODO customisable start year and game speed.
 */
public class SandBox implements Scenario {
    private long initialFunds = 1000000;

    private static SandBoxControlPanel 
	controlPanel = new SandBoxControlPanel();
   
    private static class SandBoxControlPanel extends JPanel {
	private JSlider cashSlider = new JSlider(250, 10000, 1000);
	private JLabel label = new JLabel
	    (Resources.get("Initial funds / $1000"));

	public SandBoxControlPanel() {
	    super(new BorderLayout());
	    cashSlider.setMinorTickSpacing(250);
	    cashSlider.setMajorTickSpacing(1000);
	    cashSlider.setSnapToTicks(true);
	    cashSlider.setPaintTicks(true);
	    cashSlider.setPaintLabels(true);
	    add(label, BorderLayout.NORTH);
	    add(cashSlider, BorderLayout.CENTER);
	}

	public void setEnabled(boolean enabled) {
	    cashSlider.setEnabled(enabled);
	}
    };

    public String getName() {
	return "SandBox";
    }

    public ResourceKey getDescription() {
	return Resources.getResourceKey
	    ("An open-ended scenario with no time-limit. Players "
	    + "start with a pre-determined amount of cash.");
    }

    public int getVictoryState(ReadOnlyWorld w, FreerailsPrincipal p) {
	return UNDECIDED;
    }

    public Move getSetupMoves(ReadOnlyWorld w, FreerailsPrincipal p) {
	GameTime now = (GameTime) w.get(ITEM.TIME, Player.AUTHORITATIVE);
	InitialDeposit r = new InitialDeposit(now, initialFunds);
	Move m = new AddTransactionMove(0, r, false, p);
	return m;
    }

    public JPanel getControlPanel() {
	return controlPanel;
    }

    public void applyControlPanelSettings() {
	initialFunds = controlPanel.cashSlider.getValue() * 1000;
    }

    public int getTicksPerDay() {
	return 30;
    }

    public int getStartYear() {
	return 1840;
    }
}
