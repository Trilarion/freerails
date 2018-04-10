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

package freerails.client;

import freerails.client.model.ServerControlModel;
import freerails.client.model.StationBuildModel;
import freerails.client.renderer.map.detail.DetailMapViewComponent;
import freerails.controller.BuildTrackController;
import freerails.client.view.*;
import freerails.controller.BuildMode;
import freerails.model.track.BuildTrackStrategy;
import freerails.controller.TrackMoveProducer;
import freerails.move.MoveStatus;
import freerails.util.Vec2D;
import freerails.util.Utils;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.terrain.TileTransition;
import freerails.util.ui.SoundManager;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * Handles key presses and mouse movements on the map - responsible for moving
 * the cursor etc.
 */
public class UserInputOnMapController extends KeyAdapter {

    private static final Logger logger = Logger.getLogger(UserInputOnMapController.class.getName());
    private final ModelRoot modelRoot;
    private final ActionRoot actionRoot;
    private final BuildIndustryPopupMenu buildIndustryPopupMenu = new BuildIndustryPopupMenu();
    private final MouseInputAdapter mouseInputAdapter = new CursorMouseAdapter();
    private final SoundManager soundManager = SoundManager.getInstance();
    private DetailMapViewComponent mapView;
    private StationTypesPopup stationTypesPopup;
    private TrackMoveProducer trackMoveProducer;
    private DialogueBoxController dialogueBoxController;
    private BuildTrackController buildTrackController;
    /**
     * Ignores the dragging action for efficiency I think.
     * Ignores mostly for right mouse button.
     */
    private boolean ignoreDragging = false;

    /**
     * @param modelRoot
     * @param ar
     */
    public UserInputOnMapController(ModelRoot modelRoot, ActionRoot ar) {
        this.modelRoot = modelRoot;
        actionRoot = ar;
    }

    // Inner class was here...

    /**
     * @param mv
     * @param trackBuilder
     * @param stPopup
     * @param modelRoot
     * @param dbc
     * @param buildTrackController
     */

    public void setup(DetailMapViewComponent mv, TrackMoveProducer trackBuilder, StationTypesPopup stPopup, ModelRoot modelRoot, DialogueBoxController dbc, BuildTrackController buildTrackController) {
        dialogueBoxController = dbc;
        mapView = mv;
        stationTypesPopup = stPopup;
        this.trackMoveProducer = trackBuilder;
        this.buildTrackController = buildTrackController;
        buildIndustryPopupMenu.setup(modelRoot, null, null);

        /*
         * We attempt to remove listeners before adding them to prevent them
         * being added several times.
         */
        mapView.removeMouseListener(mouseInputAdapter);
        mapView.addMouseListener(mouseInputAdapter);
        mapView.removeMouseMotionListener(mouseInputAdapter);
        mapView.addMouseMotionListener(mouseInputAdapter);
        mapView.removeKeyListener(this);
        mapView.addKeyListener(this);
    }

    @Override
    public void keyPressed(KeyEvent e) {

        int keyCode = e.getKeyCode();
        if (isIgnoreKeyEvents()) {
            if (keyCode == KeyEvent.VK_ESCAPE) {
                setIgnoreKeyEvents(false);
            } else {
                return;
            }
        }
        Vec2D cursorPosition = getCursorPosition();

        switch (keyCode) {

            case KeyEvent.VK_NUMPAD1:
                moveCursorOneTile(TileTransition.SOUTH_WEST);
                break;

            case KeyEvent.VK_NUMPAD2:
                moveCursorOneTile(TileTransition.SOUTH);
                break;

            case KeyEvent.VK_DOWN:
                if (e.getModifiers() == 2) moveCursorOneTile(TileTransition.SOUTH);
                break;

            case KeyEvent.VK_NUMPAD3:
                moveCursorOneTile(TileTransition.SOUTH_EAST);
                break;

            case KeyEvent.VK_NUMPAD4:
                moveCursorOneTile(TileTransition.WEST);
                break;

            case KeyEvent.VK_LEFT:
                if (e.getModifiers() == 2) moveCursorOneTile(TileTransition.WEST);
                break;

            case KeyEvent.VK_NUMPAD6:
                moveCursorOneTile(TileTransition.EAST);
                break;

            case KeyEvent.VK_RIGHT:
                if (e.getModifiers() == 2) moveCursorOneTile(TileTransition.EAST);
                break;

            case KeyEvent.VK_NUMPAD7:
                moveCursorOneTile(TileTransition.NORTH_WEST);
                break;

            case KeyEvent.VK_NUMPAD8:
                moveCursorOneTile(TileTransition.NORTH);
                break;

            case KeyEvent.VK_UP:
                if (e.getModifiers() == 2) moveCursorOneTile(TileTransition.NORTH);
                break;

            case KeyEvent.VK_NUMPAD9:
                moveCursorOneTile(TileTransition.NORTH_EAST);
                break;

            case KeyEvent.VK_F8: {
                // Check whether we can built a station here before proceeding.
                if (stationTypesPopup.canBuiltStationHere(cursorPosition)) {
                    float scale = mapView.getScale();
                    Dimension tileSize = new Dimension((int) scale, (int) scale);
                    int x = cursorPosition.x * tileSize.width;
                    int y = cursorPosition.y * tileSize.height;
                    stationTypesPopup.showMenu(mapView, x, y, cursorPosition);
                } else {
                    modelRoot.setProperty(ModelRootProperty.QUICK_MESSAGE, "Can't build station here!");
                }
                break;
            }

            case KeyEvent.VK_BACK_SPACE:
                logger.info("Undo building track currently not implemented.");
                break;

            case KeyEvent.VK_I: {
                dialogueBoxController.showStationOrTerrainInfo(cursorPosition);
                break;
            }

            case KeyEvent.VK_C: {
                mapView.centerOnTile(cursorPosition);
                break;
            }

            case KeyEvent.VK_B: {
                float scale = mapView.getScale();
                Dimension tileSize = new Dimension((int) scale, (int) scale);
                int x = cursorPosition.x * tileSize.width;
                int y = cursorPosition.y * tileSize.height;
                buildIndustryPopupMenu.setCursorLocation(cursorPosition);
                buildIndustryPopupMenu.show(mapView, x, y);

                break;
            }
            case KeyEvent.VK_ESCAPE: {
                cancelProposedBuild();
                break;
            }

            case KeyEvent.VK_X: {
                // modelRoot.setProperty(Property.QUICK_MESSAGE, keyCode + " was
                // pressed!");
                dialogueBoxController.showExitDialog();
                break;
            }

            case KeyEvent.VK_S: {
                if (e.getModifiers() == 2) {

                    ServerControlModel cont = actionRoot.getServerControls();
                    // String name = JOptionPane.showInputDialog(null, "Saved Game
                    // Name:","Save
                    // Game",JOptionPane.QUESTION_MESSAGE,null,null,modelRoot.getPrincipal().getName()).toString();
                    // modelRoot.setProperty(Property.QUICK_MESSAGE, name);
                    cont.getSaveGameAction().actionPerformed(null);
                }
                break;
            }

            case KeyEvent.VK_L: {
                if (e.getModifiers() == 2) {
                    ServerControlModel cont = actionRoot.getServerControls();
                    cont.getLoadGameAction().actionPerformed(null);
                }
                break;
            }

            case KeyEvent.VK_M: {
                dialogueBoxController.showBrokerScreen();
                break;
            }

            case KeyEvent.VK_F12: {
                System.out.println("Disable keyboard input!");
                setIgnoreKeyEvents(true);
                break;
            }
        } // End switch
    }

    private void cursorOneTileMove(Vec2D oldPosition, TileTransition vector) {
        boolean b = (modelRoot.getProperty(ModelRootProperty.CURSOR_MODE) == ModelRootValue.BUILD_TRACK_CURSOR_MODE);

        if (null != trackMoveProducer && b) {
            trackMoveProducer.setBuildTrackStrategy(getBts());
            MoveStatus moveStatus = trackMoveProducer.buildTrack(oldPosition, vector);

            if (moveStatus.succeeds()) {
                setCursorMessage("");
                playAppropriateSound();
            } else {
                setCursorMessage(moveStatus.getMessage());
            }
        } else {
            logger.warn("No track builder available!");
        }
    }

    private void moveCursorJump(Vec2D tryThisPoint) {
        setCursorMessage("");

        if (legalRectangleContains(tryThisPoint)) {
            setCursorPosition(tryThisPoint);
        } else {
            setCursorMessage("Illegal cursor position!");
        }
    }

    private void moveCursorOneTile(TileTransition v) {
        setCursorMessage(null);

        Vec2D cursorMapPosition = getCursorPosition();
        Vec2D tryThisPoint = Vec2D.add(cursorMapPosition, v.getD());

        // Move the cursor.
        if (legalRectangleContains(tryThisPoint)) {
            setCursorPosition(tryThisPoint);
            cursorOneTileMove(cursorMapPosition, v);
        } else {
            setCursorMessage("Illegal cursor position!");
        }
    }

    private void cancelProposedBuild() {
        ignoreDragging = true;
        buildTrackController.hide();
        StationBuildModel stationBuildModel = actionRoot.getStationBuildModel();
        stationBuildModel.getStationCancelAction().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
        setIgnoreKeyEvents(false);
    }

    private void playAppropriateSound() {
        switch (trackMoveProducer.getTrackBuilderMode()) {
            case BUILD_TRACK:
            case UPGRADE_TRACK:
                soundManager.playSound(ClientConfig.SOUND_BUILD_TRACK, 0);
                break;
            case REMOVE_TRACK:
                soundManager.playSound(ClientConfig.SOUND_REMOVE_TRACK, 0);
                break;
            default:
        }
    }

    private BuildTrackStrategy getBts() {
        BuildTrackStrategy bts = (BuildTrackStrategy) modelRoot.getProperty(ModelRootProperty.BUILD_TRACK_STRATEGY);
        return Utils.verifyNotNull(bts);
    }

    /**
     * Checks whether specified point is in legal rectangle.
     *
     * @param tryThisPoint Point2D
     * @return boolean
     */
    private boolean legalRectangleContains(Vec2D tryThisPoint) {
        ReadOnlyWorld world = modelRoot.getWorld();
        Vec2D mapSize = world.getMapSize();
        Rectangle legalRectangle = new Rectangle(0, 0, mapSize.x, mapSize.y);
        return legalRectangle.contains(Vec2D.toPoint(tryThisPoint));
    }

    private Vec2D getCursorPosition() {
        Vec2D point = (Vec2D) modelRoot.getProperty(ModelRootProperty.CURSOR_POSITION);
        // Check for null
        point = null == point ? new Vec2D() : point;
        return point;
    }

    private void setCursorPosition(Vec2D location) {
        // Make a defensive copy.
        modelRoot.setProperty(ModelRootProperty.CURSOR_POSITION, location);
    }

    private void setCursorMessage(String message) {
        modelRoot.setProperty(ModelRootProperty.CURSOR_MESSAGE, message);
    }

    private boolean isIgnoreKeyEvents() {
        return (Boolean) modelRoot.getProperty(ModelRootProperty.IGNORE_KEY_EVENTS);
    }

    private void setIgnoreKeyEvents(boolean ignoreKeyEvents) {
        modelRoot.setProperty(ModelRootProperty.IGNORE_KEY_EVENTS, ignoreKeyEvents);
    }

    private class CursorMouseAdapter extends MouseInputAdapter {

        private boolean pressedInside = false;

        @Override
        public void mousePressed(MouseEvent e) {
            logger.debug("Mouse pressed");
            if (SwingUtilities.isLeftMouseButton(e)) {

                ignoreDragging = false;

                Dimension tileSize = new Dimension((int) mapView.getScale(), (int) mapView.getScale());

                // only jump - no track building
                moveCursorJump(new Vec2D(e.getX() / tileSize.width, e.getY() / tileSize.height));

                mapView.requestFocus();
                pressedInside = true;

                /*
                 * Fix for bug [ 972866 ] Build track by dragging - only when
                 * build track selected
                 */
                boolean isBuildTrackModeSet = trackMoveProducer.getTrackBuilderMode() == BuildMode.BUILD_TRACK;

                if (isBuildTrackModeSet) {
                    buildTrackController.show();
                }
            } else if (SwingUtilities.isRightMouseButton(e)) {
                // Cancel building track.
                buildTrackController.hide();
                ignoreDragging = true;
                setIgnoreKeyEvents(false);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {

            logger.debug("Mouse released");
            if (SwingUtilities.isLeftMouseButton(e)) {

                ignoreDragging = false;
                setIgnoreKeyEvents(false);

                // build a railroad from x,y to current cursor position
                if (pressedInside && buildTrackController.isBuilding() && buildTrackController.isBuildTrackSuccessful()) {

                    // Fix for bug [ 997088 ]
                    // Is current position different from original position?
                    float scale = mapView.getScale();
                    Dimension tileSize = new Dimension((int) scale, (int) scale);
                    int tileX = e.getX() / tileSize.width;
                    int tileY = e.getY() / tileSize.height;

                    if (getCursorPosition().x != tileX || getCursorPosition().y != tileY) {
                        // copy WorldDifferences from buildTrack to World
                        Vec2D newPosition = buildTrackController.updateWorld(trackMoveProducer);
                        setCursorPosition(newPosition);
                    }
                }

                pressedInside = false;
                buildTrackController.hide();
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {

            // Called a lot for a small area, not just every square... efficiency questions?
            logger.debug("Mouse dragged");

            BuildMode trackBuilderMode = trackMoveProducer.getTrackBuilderMode();
            /*
             * Fix for bug [ 972866 ] Build track by dragging - only when build
             * track selected Fix for bug [1537413 ] Exception when building
             * station.
             */
            // TODO pull these next bits out into method
            boolean trackBuildingOn = (trackBuilderMode == BuildMode.BUILD_TRACK) || (trackBuilderMode == BuildMode.REMOVE_TRACK) || (trackBuilderMode == BuildMode.UPGRADE_TRACK);
            trackBuildingOn = trackBuildingOn && (modelRoot.getProperty(ModelRootProperty.CURSOR_MODE) == ModelRootValue.BUILD_TRACK_CURSOR_MODE);

            if (SwingUtilities.isLeftMouseButton(e) && pressedInside && trackBuildingOn && !ignoreDragging) {

                setIgnoreKeyEvents(true);

                int x = e.getX();
                int y = e.getY();

                float scale = mapView.getScale();
                Dimension tileSize = new Dimension((int) scale, (int) scale);
                int tileX = x / tileSize.width;
                int tileY = y / tileSize.height;

                /*
                 * See the javadoc for JComponent.setAutoscrolls(boolean
                 * autoscrolls)
                 */
                assert mapView.getAutoscrolls();

                // Scroll view if necessary.
                if (!mapView.getVisibleRect().contains(x, y)) {
                    /*
                     * Making the rectangle we scroll to 2 tiles wide and
                     * centered on x, y means that we scroll at least one tile.
                     * This stops painfully slow scrolling in full screen mode
                     * when the mouse cannot be dragged far from the viewport
                     * since it hits the screen edge.
                     */
                    Rectangle r = new Rectangle(x - tileSize.width, y - tileSize.height, 2 * tileSize.width, 2 * tileSize.height);
                    mapView.scrollRectToVisible(r);
                }

                Vec2D destination = new Vec2D(tileX, tileY);
                buildTrackController.setProposedTrack(destination, trackMoveProducer);
                mapView.requestFocus();
            }
        }
    }

}