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

import freerails.client.model.StationBuildModel;
import freerails.client.renderer.StationRadiusRenderer;
import freerails.controller.ModelRoot;
import freerails.util.Vector2D;
import freerails.world.terrain.FullTerrainTile;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * This JPopupMenu displays the list of station types that are available and
 * builds the type that is selected.
 */
public class StationTypesPopup extends JPopupMenu {

    private static final long serialVersionUID = 3258415040658093364L;
    private Vector2D tileToBuildStationOn;
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
    public boolean canBuiltStationHere(Vector2D p) {
        stationBuildModel.getStationBuildAction().putValue(StationBuildModel.StationBuildAction.STATION_POSITION_KEY, p);
        FullTerrainTile tile = (FullTerrainTile) modelRoot.getWorld().getTile(p);
        return tile.hasTrack();
    }

    /**
     * @param modelRoot
     * @param actionRoot
     * @param srr
     */
    public void setup(ModelRoot modelRoot, ActionRoot actionRoot, StationRadiusRenderer srr) {
        this.modelRoot = modelRoot;
        stationBuildModel = actionRoot.getStationBuildModel();
        stationRadiusRenderer = srr;
        removeAll();
        removePopupMenuListener(popupMenuListener);
        popupMenuListener = new PopupMenuListener() {
            public void popupMenuCanceled(PopupMenuEvent e) {
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                stationRadiusRenderer.hide();
                stationBuildModel.getStationCancelAction().actionPerformed(new ActionEvent(StationTypesPopup.this, ActionEvent.ACTION_PERFORMED, ""));
            }

            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                stationRadiusRenderer.setPosition(tileToBuildStationOn.x, tileToBuildStationOn.y);
                stationBuildModel.getStationBuildAction().putValue(StationBuildModel.StationBuildAction.STATION_POSITION_KEY, tileToBuildStationOn);
            }
        };
        addPopupMenuListener(popupMenuListener);

        final Action[] stationChooseActions = stationBuildModel.getStationChooseActions();

        for (int i = 0; i < stationChooseActions.length; i++) {
            final StationBuildMenuItem rbMenuItem = new StationBuildMenuItem();
            rbMenuItem.configurePropertiesFromAction(stationChooseActions[i]);
            rbMenuItem.setIcon(null);
            // Show the relevant station radius when the station type's
            // menu item gets focus.
            rbMenuItem.addChangeListener(new MyChangeListener(rbMenuItem, stationChooseActions, i));
            rbMenuItem.addActionListener(stationBuildModel.getStationBuildAction());
            add(rbMenuItem);
        }

        stationBuildModel.getStationBuildAction().addPropertyChangeListener(evt -> {
            if (evt.getPropertyName().equals(StationBuildModel.StationBuildAction.STATION_RADIUS_KEY)) {
                int newRadius = (Integer) evt.getNewValue();
                stationRadiusRenderer.setRadius(newRadius);
            }

            if (stationBuildModel.getStationBuildAction().isEnabled()) {
                stationRadiusRenderer.show();
            } else {
                stationRadiusRenderer.hide();
            }
        });
    }

    /**
     * @param invoker
     * @param x
     * @param y
     * @param tile
     */
    public void showMenu(Component invoker, int x, int y, Vector2D tile) {
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

    private static class MyChangeListener implements ChangeListener {
        private final StationBuildMenuItem rbMenuItem;
        private final Action[] stationChooseActions;
        private final int index;
        private boolean armed;

        private MyChangeListener(StationBuildMenuItem rbMenuItem, Action[] stationChooseActions, int index) {
            this.rbMenuItem = rbMenuItem;
            this.stationChooseActions = stationChooseActions;
            this.index = index;
            armed = false;
        }

        public void stateChanged(ChangeEvent e) {
            if (rbMenuItem.isArmed() && (rbMenuItem.isArmed() != armed)) {
                stationChooseActions[index].actionPerformed(new ActionEvent(rbMenuItem, ActionEvent.ACTION_PERFORMED, ""));
            }

            armed = rbMenuItem.isArmed();
        }
    }

    private static class StationBuildMenuItem extends JMenuItem {
        private static final long serialVersionUID = 3256721792751120946L;

        @Override
        public void configurePropertiesFromAction(Action a) {
            super.configurePropertiesFromAction(a);
        }
    }
}