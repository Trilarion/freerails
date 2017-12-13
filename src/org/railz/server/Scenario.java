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

import org.railz.move.*;
import org.railz.util.*;
import org.railz.util.Resources.*;
import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.top.*;
/**
 * Implemented by classes representing game scenarios.
 */
public interface Scenario extends FreerailsSerializable {
    public static final int UNDECIDED = 0;
    public static final int VICTORY = 1;
    public static final int DEFEAT = 2;

    /**
     * @return a resource key to the name of this scenario.
     */
    public String getName();
    
    /**
     * @return a resource key to a textual description of the conditions which
     * must be met in order to be victorious
     */
    public ResourceKey getDescription();

    /**
     * @return VICTORY, DEFEAT or UNDECIDED
     */
    public int getVictoryState(ReadOnlyWorld w, FreerailsPrincipal p);

    /**
     * @return a series of Moves to be executed when a new player joins the
     * game. This determines initial bank balance, overdraft, pre-existing
     * assets etc.
     */
    public Move getSetupMoves(ReadOnlyWorld w, FreerailsPrincipal p);

    /** 
     * @return a control panel that can be used to customize starting
     * conditions. The setEnabled() method must be overridden to disable all
     * contained components.
     */
    public JPanel getControlPanel();

    /**
     * Used to submit the control panel settings.
     */
    public void applyControlPanelSettings();

    /**
     * @return number of TimeTicks per "day"
     */
    public int getTicksPerDay();

    /**
     * @return the start year for the scenario
     */
    public int getStartYear();
}
