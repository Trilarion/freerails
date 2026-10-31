/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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
import freerails.client.ModelRoot;
import freerails.util.Vector2D;
import freerails.model.terrain.TerrainTile;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.Serial;

/**
 * This JPopupMenu displays the list of station types that are available and
 * builds the type that is selected.
 */
public class StationTypesPopup extends JPopupMenu {

    @Serial
    private static final long serialVersionUID = 3258415040658093364L;
    private Vector2D tileToBuildStationOn;
    private StationRadiusRenderer stationRadiusRenderer;
    private PopupMenuListener popupMenuListener;
    private StationBuildModel stationBuildModel;
    private ModelRoot modelRoot;

    /**
     * Creates a station types popup instance.
     */
    public StationTypesPopup() {
    }

    /**
     * Returns whether this object can built station here.
     *
     * @param p the map coordinate
     * @return {@code true} if built station here; otherwise, {@code false}
     */
    public boolean canBuiltStationHere(Vector2D p) {
        stationBuildModel.getStationBuildAction().putValue(StationBuildModel.StationBuildAction.STATION_POSITION_KEY, p);
        TerrainTile tile = modelRoot.getWorld().getTile(p);
        return tile.hasTrack();
    }

    /**
     * Sets the up.
     *
     * @param modelRoot the client model that provides current game state
     * @param actionRoot the root that supplies client actions
     * @param srr the srr parameter
     */
    public void setup(ModelRoot modelRoot, ActionRoot actionRoot, StationRadiusRenderer srr) {
        this.modelRoot = modelRoot;
        stationBuildModel = actionRoot.getStationBuildModel();
        stationRadiusRenderer = srr;
        removeAll();
        removePopupMenuListener(popupMenuListener);
        popupMenuListener = new PopupMenuListener() {
            /**
             * Popup menu canceled.
             * @param e the e
             */
            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }

            /**
             * Popup menu will become invisible.
             * @param e the e
             */
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                stationRadiusRenderer.hide();
                stationBuildModel.getStationCancelAction().actionPerformed(new ActionEvent(StationTypesPopup.this, ActionEvent.ACTION_PERFORMED, ""));
            }

            /**
             * Popup menu will become visible.
             * @param e the e
             */
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                stationRadiusRenderer.setPosition(tileToBuildStationOn.x(), tileToBuildStationOn.y());
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
     * Displays the menu.
     *
     * @param invoker the invoker parameter
     * @param x the horizontal coordinate
     * @param y the vertical coordinate
     * @param tile the map coordinate
     */
    public void showMenu(Component invoker, int x, int y, Vector2D tile) {
        tileToBuildStationOn = tile;

        super.show(invoker, x, y);
    }

    /**
     * Sets the visible.
     * @param b the b
     */
    @Override
    public void setVisible(boolean b) {
        // If this popup is visible, we don't want the station's position to
        // follow the mouse.
        stationBuildModel.setPositionFollowsMouse(!b);
        super.setVisible(b);
    }

    /**
     * Provides the my change listener functionality.
     */
    private static class MyChangeListener implements ChangeListener {
        private final StationBuildMenuItem rbMenuItem;
        private final Action[] stationChooseActions;
        private final int index;
        private boolean armed;

        /**
         * Creates a my change listener instance.
         * @param rbMenuItem the rb menu item
         * @param stationChooseActions the station choose actions
         * @param index the index
         */
        private MyChangeListener(StationBuildMenuItem rbMenuItem, Action[] stationChooseActions, int index) {
            this.rbMenuItem = rbMenuItem;
            this.stationChooseActions = stationChooseActions;
            this.index = index;
            armed = false;
        }

        /**
         * State changed.
         * @param e the e
         */
        @Override
        public void stateChanged(ChangeEvent e) {
            if (rbMenuItem.isArmed() && rbMenuItem.isArmed() != armed) {
                stationChooseActions[index].actionPerformed(new ActionEvent(rbMenuItem, ActionEvent.ACTION_PERFORMED, ""));
            }

            armed = rbMenuItem.isArmed();
        }
    }

    /**
     * Provides the station build menu item functionality.
     */
    private static class StationBuildMenuItem extends JMenuItem {
        @Serial
        private static final long serialVersionUID = 3256721792751120946L;

        /**
         * Configure properties from action.
         * @param a the a
         */
        @Override
        public void configurePropertiesFromAction(Action a) {
            super.configurePropertiesFromAction(a);
        }
    }
}