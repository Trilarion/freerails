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

package freerails.client.model;

import freerails.network.command.NewGameCommandToServer;
import freerails.savegames.MapCreator;
import freerails.util.ui.ActionAdapter;
import freerails.client.ModelRootImpl;
import freerails.client.ModelRootListener;
import freerails.client.view.DialogueBoxController;
import freerails.client.ModelRootProperty;
import freerails.move.ChangeGameSpeedMove;
import freerails.network.command.CommandToServer;
import freerails.model.world.WorldItem;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.game.GameSpeed;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Exposes the ServerControlInterface to client UI implementations.
 */
public class ServerControlModel implements ModelRootListener {

    private final Action loadGameAction = new LoadGameAction();
    private final Action newGameAction = new NewGameAction(null);
    private final Action saveGameAction = new SaveGameAction();
    private final SetTargetTicksPerSecondAction[] speedActions = new SetTargetTicksPerSecondAction[]{new SetTargetTicksPerSecondAction("Pause", 0, KeyEvent.VK_P), new SetTargetTicksPerSecondAction("Slow", 10, KeyEvent.VK_1), new SetTargetTicksPerSecondAction("Moderate", 30, KeyEvent.VK_2), new SetTargetTicksPerSecondAction("Fast", 70, KeyEvent.VK_3),};
    private final ActionAdapter targetTicksPerSecondActions = new ActionAdapter(speedActions, 0);
    private ModelRootImpl modelRoot;
    private ActionAdapter selectMapActions;
    private DialogueBoxController dialogueBoxController;

    /**
     * @param modelRoot
     */
    public ServerControlModel(ModelRootImpl modelRoot) {
        this.modelRoot = modelRoot;

        modelRoot.addPropertyChangeListener(this);
        setServerControlInterface();
    }

    /**
     * Returns human readable string description of {@code tickPerSecond}
     * number. Looks for {@code tickPerSecond} in
     * {@code targetTicksPerSecondActions}. If appropriate action is not
     * found returns first greater value or the greatest value.
     *
     * @param tickPerSecond int
     * @return String human readable description
     */
    public String getGameSpeedDesc(int tickPerSecond) {
        SetTargetTicksPerSecondAction action = null;

        for (SetTargetTicksPerSecondAction speedAction : speedActions) {
            action = speedAction;

            if (action.speed >= tickPerSecond) break;
        }

        return (String) action.getValue(Action.NAME);
    }

    /**
     * @return an action to load a game.
     */
    public Action getLoadGameAction() {
        return loadGameAction;
    }

    /**
     * @return an ActionAdapter representing a list of actions representing
     * valid map names.
     */
    public ActionAdapter getMapNames() {
        return selectMapActions;
    }

    /**
     * When calling this action, set the action command string to the desired
     * map name, or call the appropriate selectMapAction.
     *
     * @return an action to start a new game
     */
    public Action getNewGameAction() {
        return newGameAction;
    }

    // TODO The action produces a file selector
    /**
     * @return an action to save a game
     * dialog to save the game
     */
    public Action getSaveGameAction() {
        return saveGameAction;
    }

    /**
     * @return an action adapter to set the target ticks per second
     */
    public ActionAdapter getSetTargetTickPerSecondActions() {
        return targetTicksPerSecondActions;
    }

    /**
     * @return
     */
    private int getTargetTicksPerSecond() {
        ReadOnlyWorld world = modelRoot.getWorld();
        return ((GameSpeed) world.get(WorldItem.GameSpeed)).getSpeed();
    }

    /**
     * @param modelRootProperty
     * @param oldValue
     * @param newValue
     */
    public void propertyChange(ModelRootProperty modelRootProperty, Object oldValue, Object newValue) {
        // switch (p) {
        // case SAVED_GAMES_LIST:
        // updateLoadGameAction();
        // break;
        //
        // default:
        // break;
        // }
    }

    /**
     * @param modelRoot
     * @param dbc
     */
    public void setup(ModelRootImpl modelRoot, DialogueBoxController dbc) {
        this.modelRoot = modelRoot;
        this.dialogueBoxController = dbc;
        modelRoot.addPropertyChangeListener(this);
    }

    /**
     *
     */
    private void setServerControlInterface() {
        // Check that there is a file to load..
        saveGameAction.setEnabled(true);

        targetTicksPerSecondActions.setPerformActionOnSetSelectedItem(false);

        for (Action action: targetTicksPerSecondActions.getActions()) {
            action.setEnabled(true);
        }

        String[] mapNames = MapCreator.getAvailableMapNames();
        Action[] actions = new Action[mapNames.length];

        for (int j = 0; j < actions.length; j++) {
            actions[j] = new NewGameAction(mapNames[j]);
            actions[j].setEnabled(true);
        }

        selectMapActions = new ActionAdapter(actions);

        newGameAction.setEnabled(true);
    }

    private class LoadGameAction extends AbstractAction {
        private static final long serialVersionUID = 3616451215278682931L;

        private LoadGameAction() {
            putValue(NAME, "Load Game");
            putValue(MNEMONIC_KEY, 76);
        }

        public void actionPerformed(ActionEvent e) {
            dialogueBoxController.showSelectSavedGame2Load();
        }
    }

    private class NewGameAction extends AbstractAction {
        private static final long serialVersionUID = 3690758388631745337L;

        private NewGameAction(String s) {
            if (s == null) {
                putValue(NAME, "New Game...");
            } else {
                putValue(NAME, s);
                putValue(ACTION_COMMAND_KEY, s);
            }
        }

        public void actionPerformed(ActionEvent e) {

            String mapName = e.getActionCommand();

            if (mapName != null) {
                CommandToServer message2 = new NewGameCommandToServer(1, mapName);
                modelRoot.sendCommand(message2);
            }
        }
    }

    private class SaveGameAction extends AbstractAction {
        private static final long serialVersionUID = 3905808578064562480L;

        private SaveGameAction() {
            putValue(NAME, "Save Game");
            putValue(MNEMONIC_KEY, 83);
        }

        public void actionPerformed(ActionEvent e) {
            dialogueBoxController.showSaveGame();
            /*
             * try { // @SonnyZ // Show a JOptionPane that takes in a string
             * from a text box String filename =
             * JOptionPane.showInputDialog(null, "Saved Game Name:", "Save
             * Game", JOptionPane.QUESTION_MESSAGE, null, null,
             * modelRoot.getPrincipal().getName()).toString(); // Save the
             * current game using the string
             * modelRoot.setProperty(Property.QUICK_MESSAGE, "Saved game " +
             * filename); CommandToServer message2 = new
             * SaveGameCommandToServer(1, filename + ".sav");
             *
             * modelRoot.sendCommand(message2); loadGameAction.setEnabled(true); }
             * catch (Exception except) {
             *  }
             */
        }
    }

    private class SetTargetTicksPerSecondAction extends AbstractAction {
        private static final long serialVersionUID = 3256437014978048052L;

        private final int speed;

        /**
         * Same as the constructor above but it enables also to associate a
         * {@code keyEvent} with the action.
         *
         * @param name     action name
         * @param speed    speed
         * @param keyEvent associated key event. Use values from
         *                 <code>KeyEvent</class>.
         *                 <p>
         *                 by MystiqueAgent
         */
        private SetTargetTicksPerSecondAction(String name, int speed, int keyEvent) {
            putValue(NAME, name);
            this.speed = speed;
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyEvent, 0));
        }

        public void actionPerformed(ActionEvent e) {
            int speed2set = speed;
            if (speed == 0) { // pausing/unpausing

                // TODO this is not nice, do the conversion of values for the display not like this
                speed2set = -1 * getTargetTicksPerSecond();
            }
            modelRoot.doMove(new ChangeGameSpeedMove((GameSpeed) modelRoot.getWorld().get(WorldItem.GameSpeed), new GameSpeed(speed2set)));
        }
    }

}
