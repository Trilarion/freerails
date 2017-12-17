package freerails.client.view;

import freerails.client.common.ActionAdapter;
import freerails.client.common.ModelRootImpl;
import freerails.client.common.ModelRootListener;
import freerails.controller.Message2Server;
import freerails.controller.ModelRoot.Property;
import freerails.move.ChangeGameSpeedMove;
import freerails.network.NewGameMessage2Server;
import freerails.world.common.GameSpeed;
import freerails.world.top.ITEM;
import freerails.world.top.ReadOnlyWorld;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Enumeration;

/**
 * Exposes the ServerControlInterface to client UI implementations.
 *
 * @author rob
 * @author Luke
 * @author MystiqueAgent
 */
public class ServerControlModel implements ModelRootListener {

    private class LoadGameAction extends AbstractAction {
        private static final long serialVersionUID = 3616451215278682931L;

        public LoadGameAction() {
            putValue(NAME, "Load Game");
            putValue(MNEMONIC_KEY, 76);
        }

        public void actionPerformed(ActionEvent e) {
            dbc.showSelectSavedGame2Load();
            /*
             *
             * ImStringList files =
             * (ImStringList)modelRoot.getProperty(Property.SAVED_GAMES_LIST);
             * Object[] saves = new Object[files.size()]; for (int i = 0; i <
             * files.size(); i++) { saves[i] = files.get(i); } // Display a
             * JOptionPane that lists the existing saved games try { Object
             * showInputDialog = JOptionPane.showInputDialog(null, "Saved
             * Games:", "Select game to load", JOptionPane.INFORMATION_MESSAGE,
             * null, saves, saves[0]); String filename =
             * showInputDialog.toString(); // Load the game chosen
             * Message2Server message2 = new LoadGameMessage2Server(1,
             * filename); modelRoot.sendCommand(message2); } catch (Exception
             * exept) { // <Hack> // When no saved game is selected, or one that
             * doesnt exist, // nothing changes // </.Hack> }
             */
        }
    }

    private class NewGameAction extends AbstractAction {
        private static final long serialVersionUID = 3690758388631745337L;

        public NewGameAction(String s) {
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
                Message2Server message2 = new NewGameMessage2Server(1, mapName);
                modelRoot.sendCommand(message2);
            }

        }
    }

    private class SaveGameAction extends AbstractAction {
        private static final long serialVersionUID = 3905808578064562480L;

        public SaveGameAction() {
            putValue(NAME, "Save Game");
            putValue(MNEMONIC_KEY, 83);
        }

        public void actionPerformed(ActionEvent e) {
            dbc.showSaveGame();
            /*
             * try { // @SonnyZ // Show a JOptionPane that takes in a string
             * from a text box String filename =
             * JOptionPane.showInputDialog(null, "Saved Game Name:", "Save
             * Game", JOptionPane.QUESTION_MESSAGE, null, null,
             * modelRoot.getPrincipal().getName()).toString(); // Save the
             * current game using the string
             * modelRoot.setProperty(Property.QUICK_MESSAGE, "Saved game " +
             * filename); Message2Server message2 = new
             * SaveGameMessage2Server(1, filename + ".sav");
             *
             * modelRoot.sendCommand(message2); loadGameAction.setEnabled(true); }
             * catch (Exception except) {
             *  }
             */

        }

    }

    private class SetTargetTicksPerSecondAction extends AbstractAction {
        private static final long serialVersionUID = 3256437014978048052L;

        final int speed;

        /**
         * Same as the constructor above but it enables also to associate a
         * <code>keyEvent</code> with the action.
         *
         * @param name     action name
         * @param speed    speed
         * @param keyEvent associated key event. Use values from
         *                 <code>KeyEvent</class>.
         *                 <p>
         *                 by MystiqueAgent
         */
        public SetTargetTicksPerSecondAction(String name, int speed,
                                             int keyEvent) {
            putValue(NAME, name);
            this.speed = speed;
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyEvent, 0));
        }

        public void actionPerformed(ActionEvent e) {
            int speed2set = speed;
            if (speed == 0) { // pausing/unpausing

                speed2set = -1 * getTargetTicksPerSecond();

            }
            modelRoot.doMove(ChangeGameSpeedMove.getMove(modelRoot.getWorld(),
                    new GameSpeed(speed2set)));
        }
    }

    private final Action loadGameAction = new LoadGameAction();

    private ModelRootImpl modelRoot;

    private final Action newGameAction = new NewGameAction(null);

    private final Action saveGameAction = new SaveGameAction();

    private ActionAdapter selectMapActions;

    private final SetTargetTicksPerSecondAction[] speedActions = new SetTargetTicksPerSecondAction[]{
            new SetTargetTicksPerSecondAction("Pause", 0, KeyEvent.VK_P),
            new SetTargetTicksPerSecondAction("Slow", 10, KeyEvent.VK_1),
            new SetTargetTicksPerSecondAction("Moderate", 30, KeyEvent.VK_2),
            new SetTargetTicksPerSecondAction("Fast", 70, KeyEvent.VK_3),};

    private final ActionAdapter targetTicksPerSecondActions = new ActionAdapter(
            speedActions, 0);

    private DialogueBoxController dbc;

    public ServerControlModel(ModelRootImpl mr) {
        this.modelRoot = mr;

        mr.addPropertyChangeListener(this);
        setServerControlInterface();

    }

    /**
     * Returns human readable string description of <code>tickPerSecond</code>
     * number. Looks for <code>tickPerSecond</code> in
     * <code>targetTicksPerSecondActions</code>. If appropriate action is not
     * found returns first greater value or the greatest value.
     *
     * @param tickPerSecond int
     * @return String human readable description
     */
    public String getGameSpeedDesc(int tickPerSecond) {
        SetTargetTicksPerSecondAction action = null;

        for (SetTargetTicksPerSecondAction speedAction : speedActions) {
            action = speedAction;

            if (action.speed >= tickPerSecond)
                break;
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

    /**
     * @return an action to save a game TODO The action produces a file selector
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

    public int getTargetTicksPerSecond() {
        ReadOnlyWorld world = modelRoot.getWorld();
        return ((GameSpeed) world.get(ITEM.GAME_SPEED)).getSpeed();
    }

    public void propertyChange(Property p, Object oldValue, Object newValue) {
        // switch (p) {
        // case SAVED_GAMES_LIST:
        // updateLoadGameAction();
        // break;
        //                
        // default:
        // break;
        // }

    }

    public void setup(ModelRootImpl modelRoot, DialogueBoxController dbc) {
        this.modelRoot = modelRoot;
        this.dbc = dbc;
        modelRoot.addPropertyChangeListener(this);
    }

    public void setServerControlInterface() {
        // Check that there is a file to load..
        saveGameAction.setEnabled(true);

        Enumeration<Action> e = targetTicksPerSecondActions.getActions();
        targetTicksPerSecondActions.setPerformActionOnSetSelectedItem(false);

        while (e.hasMoreElements()) {
            e.nextElement().setEnabled(true);
        }

        String[] mapNames = NewGameMessage2Server.getMapNames();
        Action[] actions = new Action[mapNames.length];

        for (int j = 0; j < actions.length; j++) {
            actions[j] = new NewGameAction(mapNames[j]);
            actions[j].setEnabled(true);
        }

        selectMapActions = new ActionAdapter(actions);

        newGameAction.setEnabled(true);

    }

}
