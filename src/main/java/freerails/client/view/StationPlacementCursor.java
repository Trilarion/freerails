/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.client.view;

import freerails.client.ActionRoot;
import freerails.client.model.StationBuildModel;
import freerails.client.renderer.map.detail.StationRadiusRenderer;
import freerails.client.renderer.map.detail.DetailMapViewComponent;

import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

// TODO scroll the area when the mouse hovers at the edge of the map.
/**
 * Implements a cursor which can be used to place a station on the
 * map. Mode of operation:
 * <ol>
 * <li>User selects a station to place, which sets the current cursor to the
 * station placement cursor.
 * <li>User highlights desired build location with the mouse, boundary of the
 * station radius is highlighted. If the station cannot be built, the boundary
 * highlights in red.
 * <li>User places station with the left mouse button.
 * <li>User may cancel placement by using the right mouse button
 * <li>Cursor fires the actionPerformed causing the station to be built.
 * </ol>
 * When the StationBuildAction is no longer enabled, the owner reverts to the
 * regular cursor type.
 */
public class StationPlacementCursor extends MouseInputAdapter {

    private final DetailMapViewComponent mapView;
    private final float scale;
    private final StationBuildModel stationBuildModel;
    private final StationRadiusRenderer stationRadiusRenderer;
    private boolean buildEnabled;
    private final PropertyChangeListener buildActionListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(StationBuildModel.StationBuildAction.STATION_POSITION_KEY)) {
                // update the renderer pos
                Point p = (Point) evt.getNewValue();
                stationRadiusRenderer.setPosition(p.x, p.y);

                if (stationBuildModel.canBuildStationHere()) {
                    stationRadiusRenderer.setBorderColor(StationRadiusRenderer.COLOR_OK);
                } else {
                    stationRadiusRenderer.setBorderColor(StationRadiusRenderer.COLOR_CANNOT_BUILD);
                }
            } else if (evt.getPropertyName().equals(StationBuildModel.StationBuildAction.STATION_RADIUS_KEY)) {
                Integer radius = (Integer) evt.getNewValue();
                stationRadiusRenderer.setRadius(radius);
            }

            boolean enabled = stationBuildModel.getStationBuildAction().isEnabled();

            if (buildEnabled != enabled) {
                if (enabled) {
                    mapView.addMouseListener(StationPlacementCursor.this);
                    mapView.addMouseMotionListener(StationPlacementCursor.this);
                    stationRadiusRenderer.show();
                } else {
                    stationRadiusRenderer.hide();
                    mapView.removeMouseListener(StationPlacementCursor.this);
                    mapView.removeMouseMotionListener(StationPlacementCursor.this);
                }

                buildEnabled = enabled;
            }
        }
    };

    private StationPlacementCursor(ActionRoot actionRoot, StationRadiusRenderer stationRadiusRenderer, DetailMapViewComponent mapView) {
        scale = mapView.getScale();
        this.mapView = mapView;
        stationBuildModel = actionRoot.getStationBuildModel();
        this.stationRadiusRenderer = stationRadiusRenderer;
        buildEnabled = stationBuildModel.getStationBuildAction().isEnabled();
    }

    /**
     * @param actionRoot
     * @param srr
     * @param mapView
     */
    public static void wireUp(ActionRoot actionRoot, StationRadiusRenderer srr, DetailMapViewComponent mapView) {
        StationPlacementCursor spc = new StationPlacementCursor(actionRoot, srr, mapView);
        spc.init();
    }

    private void init() {
        if (buildEnabled) {
            mapView.addMouseListener(this);
            mapView.addMouseMotionListener(this);
            stationRadiusRenderer.show();
        } else {
            stationRadiusRenderer.hide();
            mapView.removeMouseListener(this);
            mapView.removeMouseMotionListener(this);
        }
        stationBuildModel.getStationBuildAction().addPropertyChangeListener(buildActionListener);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int button = e.getButton();

        if (button == MouseEvent.BUTTON1) {
            // attempt to build
            stationBuildModel.getStationBuildAction().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
        } else if (button == MouseEvent.BUTTON3) {
            // cancel the build
            stationBuildModel.getStationCancelAction().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        stationRadiusRenderer.show();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        stationRadiusRenderer.hide();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (stationBuildModel.isPositionFollowsMouse()) {
            Point p = e.getPoint();
            Point mapCoord = new Point((int) (p.x / scale), (int) (p.y / scale));
            stationBuildModel.getStationBuildAction().putValue(StationBuildModel.StationBuildAction.STATION_POSITION_KEY, mapCoord);
        }
    }

}