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
 * $Id: TrainsJTabPane.java,v 1.2 2004/10/25 20:38:18 rtuck99 Exp $
 */

package org.railz.client.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Point;

import javax.swing.JTabbedPane;

import org.railz.client.model.CursorEventListener;
import org.railz.client.model.CursorEvent;
import org.railz.client.model.ModelRoot;
import org.railz.client.renderer.ViewLists;
import org.railz.world.top.ReadOnlyWorld;

public class TrainsJTabPane extends JTabbedPane implements CursorEventListener {
    private TerrainInfoJPanel terrainInfoPanel;
    private StationInfoJPanel stationInfoPanel;
    private TrainDialogueJPanel trainSchedulePanel;
    private ReadOnlyWorld world;
    private BuildJPane buildJPane;

    public TrainsJTabPane() {
	/* set up trainsJTabbedPane */
        /*
	 * XXX Don't use SCROLL_TAB_LAYOUT as tooltips don't work (JDK bug)
	 * setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
	 */
        
	terrainInfoPanel = new TerrainInfoJPanel();
	stationInfoPanel = new StationInfoJPanel();
	trainSchedulePanel = new TrainDialogueJPanel();
	buildJPane = new BuildJPane();
    }
    
    public void setup(ModelRoot modelRoot, GUIRoot gr) {	
	world = modelRoot.getWorld();
	ViewLists vl = modelRoot.getViewLists();
	
	addTab(null, vl.getImageIcon("terrain_info"), terrainInfoPanel, 
		"Terrain Info");
	addTab(null, vl.getImageIcon("station_info"), stationInfoPanel,
		"Station Info");
	addTab(null, vl.getImageIcon("schedule"), trainSchedulePanel,
		"Train Schedule");
 	addTab(null, vl.getImageIcon("build"), buildJPane, "Build");

	terrainInfoPanel.setup(world, vl);
	stationInfoPanel.setup(modelRoot, gr);
	trainSchedulePanel.setup(modelRoot, gr);
 	buildJPane.setup(vl, modelRoot);
        modelRoot.getCursor().addCursorEventListener(this);
        
	stationInfoPanel.display();
        	
    }

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

