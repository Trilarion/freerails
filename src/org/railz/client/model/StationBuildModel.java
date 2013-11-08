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

package org.railz.client.model;

import java.awt.Point;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import java.util.Vector;

import org.railz.client.renderer.*;
import org.railz.controller.StationBuilder;
import org.railz.world.building.*;
import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.top.KEY;
import org.railz.world.track.TrackRule;

/**
 * This class provides the UI model for building a station.
 * The mode of operation is as follows:
 * <ol>
 * <li>Select a station to build by calling ActionPerformed() on the choose
 * Action.
 * <li>Set the position to build.
 * <li>call actionPerformed on the build Action
 * <li> alternatively, call actionPerformed on the cancel Action
 * </ol>
 */
public class StationBuildModel {
    /**
     * Vector of StationBuildAction.
     * Actions which represent stations which can be built
     */
    private Vector stationChooseActions = new Vector();
    
    /**
     * Whether the station's position can should change when the mouse moves.
     */
    private boolean positionFollowsMouse = true;

    private StationBuildAction stationBuildAction = new StationBuildAction();

    private StationCancelAction stationCancelAction = new StationCancelAction();
    
    private ReadOnlyWorld world;

    private StationBuilder stationBuilder;
 
    public StationBuildModel(StationBuilder sb, ReadOnlyWorld
	    world, ViewLists vl) {
    	
		stationBuilder = sb;
		this.world = world;
		TileRendererList tileRendererList =
		    vl.getBuildingViewList();
		for (int i = 0; i < world.size(KEY.BUILDING_TYPES); i++) {
		    BuildingType buildingType =
			(BuildingType)world.get(KEY.BUILDING_TYPES, i);
		    if (buildingType.getCategory() == BuildingType.CATEGORY_STATION) {
			TileRenderer renderer =
			    tileRendererList.getTileViewWithNumber(i);
			StationChooseAction action = new StationChooseAction(i);
			String stationType = buildingType.getName();
			action.putValue(Action.SHORT_DESCRIPTION, stationType + " @ $" +
				buildingType.getBaseValue());
			action.putValue(Action.NAME, "Build " + stationType);
			action.putValue(Action.SMALL_ICON, new
				ImageIcon(renderer.getDefaultIcon()));
			stationChooseActions.add(action);	
		    }
		}
    }

    public Action[] getStationChooseActions() {
	return (Action[]) stationChooseActions.toArray(new Action[0]);
    }

    private class StationChooseAction extends AbstractAction {
	private int actionId;

	public StationChooseAction(int actionId) {
	    this.actionId = actionId;
	}

	public void actionPerformed(
		java.awt.event.ActionEvent actionEvent) {
	    stationBuilder.setStationType(actionId);
	    BuildingType buildingType = (BuildingType)
		world.get(KEY.BUILDING_TYPES, actionId);
	    //Show the relevant station radius when the station type's menu item
	    //gets focus.
	    stationBuildAction.putValue(StationBuildAction.STATION_RADIUS_KEY,
		    new Integer(buildingType.getStationRadius()));
	    stationBuildAction.setEnabled(true);    
	}
    }

    private class StationCancelAction extends AbstractAction {
	public void actionPerformed(ActionEvent e) {
	    stationBuildAction.setEnabled(false);
	}
    }

    /**
     * This action builds the station.
     */
    public class StationBuildAction extends AbstractAction {
	/**
	 * This key can be used to set the position where the station is to be
	 * built as a Point object. 
	 */
	public final static String STATION_POSITION_KEY =
	    "STATION_POSITION_KEY";

	/**
	 * This key can be used to retrieve the radius of the currently selected
	 * station as an Integer value. Don't bother writing to it!
	 */
	public final static String STATION_RADIUS_KEY = "STATION_RADIUS_KEY";

	StationBuildAction () {
	    setEnabled(false);
	}

	public void actionPerformed(ActionEvent e) {
	    stationBuilder.buildStation((Point)
		    stationBuildAction.getValue(
			StationBuildAction.STATION_POSITION_KEY));
	    setEnabled(false);		
	}
    }

    public boolean canBuildStationHere() {
	Point p = (Point) stationBuildAction.getValue(
		    StationBuildAction.STATION_POSITION_KEY);
	return stationBuilder.canBuiltStationHere(p);
    }

    public Action getStationCancelAction() {
	return stationCancelAction;
    }

    public StationBuildAction getStationBuildAction() {
	return stationBuildAction;
    }
		
	public boolean isPositionFollowsMouse() {
		return positionFollowsMouse;
	}

	public void setPositionFollowsMouse(boolean positionFollowsMouse) {
		this.positionFollowsMouse = positionFollowsMouse;
	}

}

