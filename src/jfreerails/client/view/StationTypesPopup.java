package jfreerails.client.top;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import jfreerails.client.renderer.StationRadiusRenderer;
import jfreerails.client.model.ModelRoot;
import jfreerails.client.model.StationBuildModel;


/**
 * This JPopupMenu displays the list of station types that
 * are available and builds the type that is selected.
 * @author Luke Lindsay 08-Nov-2002
 *
 */
public class StationTypesPopup extends JPopupMenu {
    Point tileToBuildStationOn;
    StationRadiusRenderer stationRadiusRenderer;
    PopupMenuListener popupMenuListener;
    private StationBuildModel stationBuildModel;

    public StationTypesPopup() {
    }

    public boolean canBuiltStationHere(Point p) {
        stationBuildModel.getStationBuildAction().putValue(StationBuildModel.StationBuildAction.STATION_POSITION_KEY,
            p);

        return stationBuildModel.canBuildStationHere();
    }

    private class StationBuildMenuItem extends JMenuItem {
        public void configurePropertiesFromAction(Action a) {
            super.configurePropertiesFromAction(a);
        }
    }

    public void setup(ModelRoot modelRoot, StationRadiusRenderer srr) {
        stationBuildModel = modelRoot.getStationBuildModel();
        stationRadiusRenderer = srr;
        this.removeAll();
        this.removePopupMenuListener(popupMenuListener);
        popupMenuListener = new PopupMenuListener() {
                    public void popupMenuCanceled(PopupMenuEvent e) {
                    }

                    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                        stationRadiusRenderer.hide();
                        stationBuildModel.getStationCancelAction()
                                         .actionPerformed(new ActionEvent(
                                StationTypesPopup.this,
                                ActionEvent.ACTION_PERFORMED, ""));
                    }

                    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                        stationRadiusRenderer.setPosition(tileToBuildStationOn.x,
                            tileToBuildStationOn.y);
                        stationBuildModel.getStationBuildAction().putValue(StationBuildModel.StationBuildAction.STATION_POSITION_KEY,
                            tileToBuildStationOn);
                    }
                };
        this.addPopupMenuListener(popupMenuListener);

        final Action[] stationChooseActions = stationBuildModel.getStationChooseActions();

        for (int i = 0; i < stationChooseActions.length; i++) {
            final StationBuildMenuItem rbMenuItem = new StationBuildMenuItem();
            final int index = i;
            rbMenuItem.configurePropertiesFromAction(stationChooseActions[i]);
            rbMenuItem.setIcon(null);
            //Show the relevant station radius when the station type's
            //menu item gets focus.
            rbMenuItem.addChangeListener(new ChangeListener() {
                    private boolean armed = false;

                    public void stateChanged(ChangeEvent e) {
                        if (rbMenuItem.isArmed() &&
                                (rbMenuItem.isArmed() != armed)) {
                            stationChooseActions[index].actionPerformed(new ActionEvent(
                                    rbMenuItem, ActionEvent.ACTION_PERFORMED, ""));
                        }

                        armed = rbMenuItem.isArmed();
                    }
                });
            rbMenuItem.addActionListener(stationBuildModel.getStationBuildAction());
            add(rbMenuItem);
        }

        stationBuildModel.getStationBuildAction().addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent e) {
                    if (e.getPropertyName().equals(StationBuildModel.StationBuildAction.STATION_RADIUS_KEY)) {
                        int newRadius = ((Integer)e.getNewValue()).intValue();
                        stationRadiusRenderer.setRadius(newRadius);
                    }

                    if (stationBuildModel.getStationBuildAction().isEnabled()) {
                        stationRadiusRenderer.show();
                    } else {
                        stationRadiusRenderer.hide();
                    }
                }
            });
    }

    public void showMenu(Component invoker, int x, int y, Point tile) {
        tileToBuildStationOn = tile;

        super.show(invoker, x, y);
    }

    public void setVisible(boolean b) {
        //If this popup is visible, we don't want the station's position to follow the mouse.
        stationBuildModel.setPositionFollowsMouse(!b);
        super.setVisible(b);
    }
}
