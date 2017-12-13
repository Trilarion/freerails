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

/**
 * The tabbed panel that sits in the lower right hand corner of the screen
 */

/*
 * $Id: TrainsJTabPane.java,v 1.5 2005/01/28 22:51:13 rtuck99 Exp $
 */

package org.railz.client.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Point;
import java.util.*;
import javax.swing.JTabbedPane;
import javax.swing.event.*;

import org.railz.client.common.*;
import org.railz.client.common.ActionAdapter.MappedButtonModel; 
import org.railz.client.model.*;
import org.railz.client.renderer.ViewLists;
import org.railz.world.top.ReadOnlyWorld;

public class TrainsJTabPane extends JTabbedPane implements CursorEventListener {
    private TerrainInfoJPanel terrainInfoPanel;
    private StationInfoJPanel stationInfoPanel;
    private TrainDialogueJPanel trainSchedulePanel;
    private ReadOnlyWorld world;
    private BuildJPane buildJPane;
    private MappedButtonModel viewModeButtonModel;
    private MappedButtonModel buildModeButtonModel;

    public TrainsJTabPane() {
	/* set up trainsJTabbedPane */
        /*
	 * XXX Don't use SCROLL_TAB_LAYOUT as tooltips don't work (JDK bug)
	 * setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
	 */
        
	buildJPane = new BuildJPane();
    }
    
    public void setup(ModelRoot modelRoot, GUIRoot gr) {	
	world = modelRoot.getWorld();
	ViewLists vl = modelRoot.getViewLists();

	if (trainSchedulePanel != null) {
	    // we've already been initialised
	    removeChangeListener(tabListener);

	    remove(trainSchedulePanel);
	    remove(terrainInfoPanel);
	    remove(stationInfoPanel);
	    remove(buildJPane);
	    viewModeButtonModel.removeChangeListener(viewModeListener);
	    modelRoot.getCursor().removeCursorEventListener(this);
	}

	trainSchedulePanel = new TrainDialogueJPanel(modelRoot, gr);
	terrainInfoPanel = new TerrainInfoJPanel(modelRoot, gr);
	stationInfoPanel = new StationInfoJPanel(gr);
	
	addTab(null, vl.getImageIcon("terrain_info"), terrainInfoPanel, 
		"Terrain Info");
	addTab(null, vl.getImageIcon("station_info"), stationInfoPanel,
		"Station Info");
	addTab(null, vl.getImageIcon("schedule"), trainSchedulePanel,
		"Train Schedule");
 	addTab(null, vl.getImageIcon("build"), buildJPane, "Build");

	stationInfoPanel.setup(modelRoot);
        
	stationInfoPanel.display();
        TrackBuildModel tbm = modelRoot.getTrackBuildModel();
	ActionAdapter aa = tbm.getBuildModeActionAdapter();
	Enumeration e = aa.getButtonModels();
	while (e.hasMoreElements()) {
	    MappedButtonModel mbm = (MappedButtonModel) e.nextElement();
	    if ("View Mode".equals(mbm.actionName)) {
		viewModeButtonModel = mbm;
		viewModeButtonModel.addChangeListener(viewModeListener);
	    } else if ("Build Track".equals(mbm.actionName)) {
		buildModeButtonModel = mbm;
	    }
	}
	addChangeListener(tabListener);

 	buildJPane.setup(vl, modelRoot);
        modelRoot.getCursor().addCursorEventListener(this);
    }

    /** Sets the track build mode whenever the View or Build tabs are clicked
     * */
    private ChangeListener tabListener = new ChangeListener() {
	public void stateChanged(ChangeEvent e) {
	    if (getSelectedComponent() == terrainInfoPanel &&
		    ! viewModeButtonModel.isSelected()) {
		viewModeButtonModel.setSelected(true);
	    } else if (getSelectedComponent() == buildJPane) {
		buildModeButtonModel.setSelected(true);
	    }
	}
    };

    /** Changes the tab to the view tab whenever the view mode is selected */
    private ChangeListener viewModeListener = new ChangeListener() {
	public void stateChanged(ChangeEvent e) {
	    if (viewModeButtonModel.isSelected()) {
		if (getSelectedComponent() != terrainInfoPanel)
		    setSelectedComponent(terrainInfoPanel);
	    } else {
		if (getSelectedComponent() != buildJPane)
		    setSelectedComponent(buildJPane);
	    }
	}
    };

    private void updateTerrainInfo(CursorEvent e) {
	Point p = e.newPosition;
	terrainInfoPanel.setTerrainLocation(p);
    }
    
    /**
     * Implements {CursorEventListener#cursorOneTileMove}
     */
    public void cursorOneTileMove(CursorEvent e) {
        updateTerrainInfo(e);
    }

    /**
     * Implements {CursorEventListener#cursorJumped}
     */
    public void cursorJumped(CursorEvent e) {
        updateTerrainInfo(e);
    }

    /**
     * Implements {CursorEventListener#cursorKeyPressed}
     */
    public void cursorKeyPressed(CursorEvent e) {
	// do nothing
    }
}

