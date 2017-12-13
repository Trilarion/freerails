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
import java.beans.*;
import java.io.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;

import org.railz.move.*;
import org.railz.server.*;
import org.railz.util.*;
import org.railz.util.Resources.ResourceKey;
import org.railz.world.accounts.*;
import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.player.Statistic.DataPoint;
import org.railz.world.top.*;

/**
 * Victory is achieved by acquiring a preset amount of assets within a limited
 * time period.
 * TODO customisable start year and game speed.
 */
public class CashDash implements Scenario {
    private int startYear = 1840;
    private int endYear;

    private long initialFunds = 1000000;
    private long assetsRequired = 10000000;

    private transient CashDashControlPanel controlPanel
       	= new CashDashControlPanel();
   
    /**
     * Provides a slider which controls the amount of starting cash and amount
     * of time available to finish.
     * TODO allow customisation of start year
     */ 
    private class CashDashControlPanel extends JPanel {
	private final long minTarget = 2500000;
	private final long maxTarget = 100000000;

	private final int minYears = 10;
	private final int maxYears = 100;
	
	private final int minCash = 250;
	private final int maxCash = 10000;

	private JSlider cashSlider = new JSlider(minCash, maxCash, 1000);
	private JLabel label = new JLabel
	    (Resources.get("Initial funds / $1000"));

	private void setValues() {
	    int value = cashSlider.getValue();
	    initialFunds = value * 1000;
	    endYear = startYear + minYears + (maxYears - minYears) * (value -
		    minCash) / (maxCash - minCash);
	    assetsRequired = minTarget + (maxTarget - minTarget) * (value -
		    minCash) / (maxCash - minCash);
	}

	private ChangeListener changeListener = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
		    long oldValue = initialFunds;
		    setValues();
		    CashDashControlPanel.this.firePropertyChange
			("cash", new Long(oldValue), new Long(initialFunds));
		}
	};
	
	public CashDashControlPanel() {
	    super(new BorderLayout());
	    cashSlider.setMinorTickSpacing(250);
	    cashSlider.setMajorTickSpacing(1000);
	    cashSlider.setSnapToTicks(true);
	    cashSlider.setPaintTicks(true);
	    cashSlider.setPaintLabels(true);
	    add(label, BorderLayout.NORTH);
	    add(cashSlider, BorderLayout.CENTER);
	    cashSlider.addChangeListener(changeListener);
	    setValues();
	}

	public void setEnabled(boolean enabled) {
	    cashSlider.setEnabled(enabled);
	}
    };

    public String getName() {
	return "Cash Dash";
    }

    public ResourceKey getDescription() {
	return Resources.getResourceKey
	    ("To achieve victory, you must accumulate total assets of " +
	    "at least ${0} before the year {1, number, ####} in order to win.",
	    new Serializable[] {new Long(assetsRequired),
	    new Integer(endYear)});
    }

    public int getVictoryState(ReadOnlyWorld w, FreerailsPrincipal p) {
	NonNullElements i = new NonNullElements(KEY.STATISTICS, w, p);
	GameCalendar cal = (GameCalendar) w.get(ITEM.CALENDAR,
		Player.AUTHORITATIVE);
	GameTime endTime = cal.getTimeFromCalendar(new GregorianCalendar
		(endYear, 0, 1));
	while (i.next()) {
	    Statistic s = (Statistic) i.getElement(); 
	    if (s.getName().equals("Total Assets")) {
		ArrayList l = s.getData();
		if (((DataPoint) l.get(l.size() - 1)).y >=
		       	assetsRequired / 1000)
		    return VICTORY;
		else if (((GameTime) w.get(ITEM.TIME, Player.AUTHORITATIVE))
			.getTime() < endTime.getTime()) {
		    return UNDECIDED;
		}
		return DEFEAT;
	    }
	}
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
	controlPanel.setValues();
    }

    public int getTicksPerDay() {
	return 30;
    }

    public int getStartYear() {
	return startYear;
    }
}

