package jfreerails.client.view;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.event.MouseInputAdapter;
import jfreerails.client.renderer.StationRadiusRenderer;


/**
 * This class implements a cursor which can be used to place a station on the
 * map.
 * Mode of operation:
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
 * TODO scroll the area when the mouse hovers at the edge of the map.
 */
public class StationPlacementCursor extends MouseInputAdapter {
    private boolean buildEnabled;
    private MapViewJComponent mapView;
    private StationRadiusRenderer stationRadiusRenderer;
    private StationBuildModel stationBuildModel;
    private float scale;

    public void mouseMoved(MouseEvent e) {
        if (stationBuildModel.isPositionFollowsMouse()) {
            Point p = e.getPoint();
            Point mapCoord = new Point((int)(p.x / scale), (int)(p.y / scale));
            stationBuildModel.getStationBuildAction().putValue(StationBuildModel.StationBuildAction.STATION_POSITION_KEY,
                mapCoord);
        }
    }

    public void mouseClicked(MouseEvent e) {
        int button = e.getButton();

        if (button == MouseEvent.BUTTON1) {
            /* attempt to build */
            stationBuildModel.getStationBuildAction().actionPerformed(new ActionEvent(
                    this, ActionEvent.ACTION_PERFORMED, ""));
        } else if (button == MouseEvent.BUTTON3) {
            /* cancel the build */
            stationBuildModel.getStationCancelAction().actionPerformed(new ActionEvent(
                    this, ActionEvent.ACTION_PERFORMED, ""));
        }
    }

    public void mouseEntered(MouseEvent e) {
        stationRadiusRenderer.show();
    }

    public void mouseExited(MouseEvent e) {
        stationRadiusRenderer.hide();
    }

    private PropertyChangeListener buildActionListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                if (e.getPropertyName().equals(StationBuildModel.StationBuildAction.STATION_POSITION_KEY)) {
                    /* update the renderer pos */
                    Point p = (Point)e.getNewValue();
                    stationRadiusRenderer.setPosition(p.x, p.y);

                    if (stationBuildModel.canBuildStationHere()) {
                        stationRadiusRenderer.setBorderColor(StationRadiusRenderer.COLOR_OK);
                    } else {
                        stationRadiusRenderer.setBorderColor(StationRadiusRenderer.COLOR_CANNOT_BUILD);
                    }
                } else if (e.getPropertyName().equals(StationBuildModel.StationBuildAction.STATION_RADIUS_KEY)) {
                    Integer radius = (Integer)e.getNewValue();
                    stationRadiusRenderer.setRadius(radius.intValue());
                }

                boolean enabled = stationBuildModel.getStationBuildAction()
                                                   .isEnabled();

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

    public StationPlacementCursor(ModelRoot mr, StationRadiusRenderer srr,
        MapViewJComponent mapView) {
        scale = mapView.getScale();
        this.mapView = mapView;
        stationBuildModel = mr.getStationBuildModel();
        stationRadiusRenderer = srr;
        buildEnabled = stationBuildModel.getStationBuildAction().isEnabled();

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
}