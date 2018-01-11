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

import freerails.client.common.SoundManager;
import freerails.client.renderer.BuildTrackController;
import freerails.client.view.*;
import freerails.controller.BuildMode;
import freerails.controller.BuildTrackStrategy;
import freerails.controller.ModelRoot;
import freerails.controller.ModelRoot.Property;
import freerails.controller.TrackMoveProducer;
import freerails.move.MoveStatus;
import freerails.util.Point2D;
import freerails.world.ReadOnlyWorld;
import freerails.world.terrain.TileTransition;
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
    private final SoundManager soundManager = SoundManager.getSoundManager();
    private MapViewJComponent mapView;
    private StationTypesPopup stationTypesPopup;
    private TrackMoveProducer trackBuilder;
    private DialogueBoxController dialogueBoxController;
    private BuildTrackController buildTrack;
    /**
     * Ignores the dragging action for efficiency I think.
     * Ignores mostly for right mouse button.
     */
    private boolean ignoreDragging = false;

    /**
     * @param mr
     * @param ar
     */
    public UserInputOnMapController(ModelRoot mr, ActionRoot ar) {
        modelRoot = mr;
        actionRoot = ar;
    }

    // Inner class was here...

    /**
     * @param mv
     * @param trackBuilder
     * @param stPopup
     * @param mr
     * @param dbc
     * @param buildTrack
     */

    public void setup(MapViewJComponent mv, TrackMoveProducer trackBuilder, StationTypesPopup stPopup, ModelRoot mr, DialogueBoxController dbc, BuildTrackController buildTrack) {
        dialogueBoxController = dbc;
        mapView = mv;
        stationTypesPopup = stPopup;
        this.trackBuilder = trackBuilder;
        this.buildTrack = buildTrack;
        buildIndustryPopupMenu.setup(mr, null, null);

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
        Point2D cursorPosition = getCursorPosition();

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
                if (stationTypesPopup.canBuiltStationHere(cursorPosition.toPoint())) {
                    float scale = mapView.getScale();
                    Dimension tileSize = new Dimension((int) scale, (int) scale);
                    int x = cursorPosition.x * tileSize.width;
                    int y = cursorPosition.y * tileSize.height;
                    stationTypesPopup.showMenu(mapView, x, y, cursorPosition.toPoint());
                } else {
                    modelRoot.setProperty(Property.QUICK_MESSAGE, "Can't" + " build station here!");
                }
                break;
            }

            case KeyEvent.VK_BACK_SPACE:
                logger.info("Undo building track currently not implemented.");

                // TODO Investigate
                // MoveStatus ms = trackBuilder.undoLastTrackMove();
                // if (!ms.isOk()) {
                // setCursorMessage(ms.message);
                // }
                break;

            case KeyEvent.VK_I: {
                dialogueBoxController.showStationOrTerrainInfo(cursorPosition.x, cursorPosition.y);
                break;
            }

            case KeyEvent.VK_C: {
                mapView.centerOnTile(cursorPosition.toPoint());
                break;
            }

            case KeyEvent.VK_B: {
                float scale = mapView.getScale();
                Dimension tileSize = new Dimension((int) scale, (int) scale);
                int x = cursorPosition.x * tileSize.width;
                int y = cursorPosition.y * tileSize.height;
                buildIndustryPopupMenu.setCursorLocation(cursorPosition.toPoint());
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
                // FIXME broker screen bug maybe?
                // if the screen is not clicked after the broker screen is closed
                // and 'M' is pressed
                // again, the broker screen will never show up again.
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

    private void cursorOneTileMove(Point2D oldPosition, TileTransition vector) {
        boolean b = (modelRoot.getProperty(ModelRoot.Property.CURSOR_MODE) == ModelRoot.Value.BUILD_TRACK_CURSOR_MODE);

        if (null != trackBuilder && b) {
            trackBuilder.setBuildTrackStrategy(getBts());
            MoveStatus ms = trackBuilder.buildTrack(oldPosition, vector);

            if (ms.ok) {
                setCursorMessage("");
                playAppropriateSound();
            } else {
                setCursorMessage(ms.message);
            }

        } else {
            logger.warn("No track builder available!");
        }
    }

    private void moveCursorJump(Point2D tryThisPoint) {
        setCursorMessage("");

        if (legalRectangleContains(tryThisPoint)) {
            setCursorPosition(tryThisPoint);
        } else {
            setCursorMessage("Illegal cursor position!");
        }
    }

    private void moveCursorOneTile(TileTransition v) {
        setCursorMessage(null);

        Point2D cursorMapPosition = getCursorPosition();
        Point2D tryThisPoint = new Point2D(cursorMapPosition.x + v.getDx(), cursorMapPosition.y + v.getDy());

        /* Move the cursor. */
        if (legalRectangleContains(tryThisPoint)) {
            setCursorPosition(tryThisPoint);
            cursorOneTileMove(cursorMapPosition, v);
        } else {
            setCursorMessage("Illegal cursor position!");
        }
    }

    private void cancelProposedBuild() {
        ignoreDragging = true;
        buildTrack.hide();
        StationBuildModel sbm = actionRoot.getStationBuildModel();
        sbm.getStationCancelAction().actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
        setIgnoreKeyEvents(false);
    }

    private void playAppropriateSound() {
        switch (trackBuilder.getTrackBuilderMode()) {
            case BUILD_TRACK:
            case UPGRADE_TRACK:
                soundManager.playSound(ClientConfig.SOUND_BUILD_TRACK, 0);
                break;
            case REMOVE_TRACK:
                soundManager.playSound(ClientConfig.SOUND_REMOVE_TRACK, 0);
                break;
            default:
                // do nothing
        }
    }

    private BuildTrackStrategy getBts() {
        BuildTrackStrategy bts = (BuildTrackStrategy) modelRoot.getProperty(ModelRoot.Property.BUILD_TRACK_STRATEGY);
        if (null == bts) throw new NullPointerException();
        return bts;
    }

    /**
     * Checks whether specified point is in legal rectangle.
     *
     * @param tryThisPoint Point2D
     * @return boolean
     */
    private boolean legalRectangleContains(Point2D tryThisPoint) {
        ReadOnlyWorld world = modelRoot.getWorld();
        int width = world.getMapWidth();
        int height = world.getMapHeight();
        Rectangle legalRectangle = new Rectangle(0, 0, width, height);

        return legalRectangle.contains(tryThisPoint.toPoint());
    }

    private Point2D getCursorPosition() {
        Point2D point = (Point2D) modelRoot.getProperty(ModelRoot.Property.CURSOR_POSITION);
        // Check for null
        point = null == point ? new Point2D() : point;
        return point;
    }

    private void setCursorPosition(Point2D p) {
        // Make a defensive copy.
        modelRoot.setProperty(Property.CURSOR_POSITION, p);
    }

    private void setCursorMessage(String s) {
        modelRoot.setProperty(Property.CURSOR_MESSAGE, s);
    }

    private boolean isIgnoreKeyEvents() {
        return (Boolean) modelRoot.getProperty(Property.IGNORE_KEY_EVENTS);
    }

    private void setIgnoreKeyEvents(boolean ignoreKeyEvents) {
        modelRoot.setProperty(Property.IGNORE_KEY_EVENTS, ignoreKeyEvents);
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
                moveCursorJump(new Point2D(e.getX() / tileSize.width, e.getY() / tileSize.height));

                mapView.requestFocus();
                pressedInside = true;

                /*
                 * Fix for bug [ 972866 ] Build track by dragging - only when
                 * build track selected
                 */
                boolean isBuildTrackModeSet = trackBuilder.getTrackBuilderMode() == BuildMode.BUILD_TRACK;

                if (isBuildTrackModeSet) {
                    buildTrack.show();
                }

            } else if (SwingUtilities.isRightMouseButton(e)) {
                // Cancel building track.
                buildTrack.hide();
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
                if (pressedInside && buildTrack.isBuilding() && buildTrack.isBuildTrackSuccessful()) {

                    // Fix for bug [ 997088 ]
                    // Is current position different from original position?
                    float scale = mapView.getScale();
                    Dimension tileSize = new Dimension((int) scale, (int) scale);
                    int tileX = e.getX() / tileSize.width;
                    int tileY = e.getY() / tileSize.height;

                    if (getCursorPosition().x != tileX || getCursorPosition().y != tileY) {
                        // copy WorldDifferences from buildTrack to World
                        Point2D newPosition = buildTrack.updateWorld(trackBuilder);
                        setCursorPosition(newPosition);
                    }
                }

                pressedInside = false;
                buildTrack.hide();
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {

            // Called a lot for a small area, not just every square... efficiency questions?
            logger.debug("Mouse dragged");

            BuildMode trackBuilderMode = trackBuilder.getTrackBuilderMode();
            /*
             * Fix for bug [ 972866 ] Build track by dragging - only when build
             * track selected Fix for bug [1537413 ] Exception when building
             * station.
             */
            // TODO pull these next bits out into method
            boolean trackBuildingOn = (trackBuilderMode == BuildMode.BUILD_TRACK) || (trackBuilderMode == BuildMode.REMOVE_TRACK) || (trackBuilderMode == BuildMode.UPGRADE_TRACK);
            trackBuildingOn = trackBuildingOn && (modelRoot.getProperty(ModelRoot.Property.CURSOR_MODE) == ModelRoot.Value.BUILD_TRACK_CURSOR_MODE);

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

                Point2D to = new Point2D(tileX, tileY);
                buildTrack.setProposedTrack(to, trackBuilder);
                mapView.requestFocus();
            }

        }

    }

}