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

package freerails.client.top;

import freerails.client.renderer.StationRadiusRenderer;
import freerails.client.view.ActionRoot;
import freerails.client.view.StationBuildModel;
import freerails.controller.ModelRoot;
import freerails.world.terrain.FullTerrainTile;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * This JPopupMenu displays the list of station types that are available and
 * builds the type that is selected.
 */
public class StationTypesPopup extends JPopupMenu {
    private static final long serialVersionUID = 3258415040658093364L;

    private Point tileToBuildStationOn;

    private StationRadiusRenderer stationRadiusRenderer;

    private PopupMenuListener popupMenuListener;

    private StationBuildModel stationBuildModel;

    private ModelRoot modelRoot;

    /**
     *
     */
    public StationTypesPopup() {
    }

    /**
     * @param p
     * @return
     */
    public boolean canBuiltStationHere(Point p) {
        stationBuildModel.getStationBuildAction().putValue(
                StationBuildModel.StationBuildAction.STATION_POSITION_KEY, p);

        FullTerrainTile tile = (FullTerrainTile) modelRoot.getWorld().getTile(p.x,
                p.y);
        return tile.hasTrack();
    }

    /**
     * @param mr
     * @param actionRoot
     * @param srr
     */
    public void setup(ModelRoot mr, ActionRoot actionRoot,
                      StationRadiusRenderer srr) {
        modelRoot = mr;
        stationBuildModel = actionRoot.getStationBuildModel();
        stationRadiusRenderer = srr;
        this.removeAll();
        this.removePopupMenuListener(popupMenuListener);
        popupMenuListener = new PopupMenuListener() {
            public void popupMenuCanceled(PopupMenuEvent e) {
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                stationRadiusRenderer.hide();
                stationBuildModel.getStationCancelAction().actionPerformed(
                        new ActionEvent(StationTypesPopup.this,
                                ActionEvent.ACTION_PERFORMED, ""));
            }

            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                stationRadiusRenderer.setPosition(tileToBuildStationOn.x,
                        tileToBuildStationOn.y);
                stationBuildModel
                        .getStationBuildAction()
                        .putValue(
                                StationBuildModel.StationBuildAction.STATION_POSITION_KEY,
                                tileToBuildStationOn);
            }
        };
        this.addPopupMenuListener(popupMenuListener);

        final Action[] stationChooseActions = stationBuildModel
                .getStationChooseActions();

        for (int i = 0; i < stationChooseActions.length; i++) {
            final StationBuildMenuItem rbMenuItem = new StationBuildMenuItem();
            final int index = i;
            rbMenuItem.configurePropertiesFromAction(stationChooseActions[i]);
            rbMenuItem.setIcon(null);
            // Show the relevant station radius when the station type's
            // menu item gets focus.
            rbMenuItem.addChangeListener(new ChangeListener() {
                private boolean armed = false;

                public void stateChanged(ChangeEvent e) {
                    if (rbMenuItem.isArmed() && (rbMenuItem.isArmed() != armed)) {
                        stationChooseActions[index]
                                .actionPerformed(new ActionEvent(rbMenuItem,
                                        ActionEvent.ACTION_PERFORMED, ""));
                    }

                    armed = rbMenuItem.isArmed();
                }
            });
            rbMenuItem.addActionListener(stationBuildModel
                    .getStationBuildAction());
            add(rbMenuItem);
        }

        stationBuildModel.getStationBuildAction().addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent e) {
                        if (e
                                .getPropertyName()
                                .equals(
                                        StationBuildModel.StationBuildAction.STATION_RADIUS_KEY)) {
                            int newRadius = (Integer) e.getNewValue();
                            stationRadiusRenderer.setRadius(newRadius);
                        }

                        if (stationBuildModel.getStationBuildAction()
                                .isEnabled()) {
                            stationRadiusRenderer.show();
                        } else {
                            stationRadiusRenderer.hide();
                        }
                    }
                });
    }

    /**
     * @param invoker
     * @param x
     * @param y
     * @param tile
     */
    public void showMenu(Component invoker, int x, int y, Point tile) {
        tileToBuildStationOn = tile;

        super.show(invoker, x, y);
    }

    @Override
    public void setVisible(boolean b) {
        // If this popup is visible, we don't want the station's position to
        // follow the mouse.
        stationBuildModel.setPositionFollowsMouse(!b);
        super.setVisible(b);
    }

    private class StationBuildMenuItem extends JMenuItem {
        private static final long serialVersionUID = 3256721792751120946L;

        @Override
        public void configurePropertiesFromAction(Action a) {
            super.configurePropertiesFromAction(a);
        }
    }
}