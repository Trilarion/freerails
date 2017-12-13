/**
 * The tabbed panel that sits in the lower right hand corner of the screen
 */

/*
 * $Id: TrainsJTabPane.java,v 1.3 2004/03/09 08:53:01 rtuck99 Exp $
 */

package jfreerails.client.view;

import java.awt.Point;

import javax.swing.JTabbedPane;

import jfreerails.client.model.CursorEventListener;
import jfreerails.client.model.CursorEvent;
import jfreerails.client.model.ModelRoot;
import jfreerails.client.renderer.ViewLists;
import jfreerails.world.top.ReadOnlyWorld;

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
    
    public void setup(ModelRoot modelRoot) {	
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
	stationInfoPanel.setup(modelRoot);
	trainSchedulePanel.setup(world, vl, modelRoot);
 	buildJPane.setup(vl, modelRoot);
        modelRoot.getCursor().addCursorEventListener(this);
        
	stationInfoPanel.display();
        	
    }

    private void updateTerrainInfo(CursorEvent e) {
        
	Point p = e.newPosition;
	terrainInfoPanel.setTerrainType(world.getTile(p.x,
		    p.y).getTerrainTypeNumber());
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

