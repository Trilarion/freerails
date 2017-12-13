package jfreerails.client.model;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.AbstractAction;
import java.util.Enumeration;
import jfreerails.client.common.ActionAdapter;
import jfreerails.controller.ServerControlInterface;


/**
 * Exposes the ServerControlInterface to client UI implementations
 */
public class ServerControlModel {
    private ServerControlInterface serverInterface;

    private class NewGameAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (serverInterface != null) {
                String mapName = e.getActionCommand();

                if (mapName != null) {
                    serverInterface.newGame(mapName);
                }
            }
        }

        public NewGameAction(String s) {
            if (s == null) {
                putValue(NAME, "New Game...");
            } else {
                putValue(NAME, s);
                putValue(ACTION_COMMAND_KEY, s);
            }
        }
    }

    private ActionAdapter selectMapActions;
    private Action newGameAction = new NewGameAction(null);

    private class LoadGameAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (serverInterface != null) {
                serverInterface.loadGame();
            }
        }

        public LoadGameAction() {
            putValue(NAME, "Load Game");
            putValue(MNEMONIC_KEY, new Integer(76));
        }
    }

    private Action loadGameAction = new LoadGameAction();

    private class SaveGameAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            if (serverInterface != null) {
                serverInterface.saveGame();
            }
        }

        public SaveGameAction() {
            putValue(NAME, "Save Game");
            putValue(MNEMONIC_KEY, new Integer(83));
        }
    }

    private Action saveGameAction = new SaveGameAction();

    private class SetTargetTicksPerSecondAction extends AbstractAction {
        protected int speed;

        public void actionPerformed(ActionEvent e) {
            if (serverInterface != null) {
                serverInterface.setTargetTicksPerSecond(speed);
            }
        }

        public SetTargetTicksPerSecondAction(String name, int speed) {
            putValue(NAME, name);
            this.speed = speed;
        }
    }

    private ActionAdapter targetTicksPerSecondActions = new ActionAdapter(new Action[] {
                new SetTargetTicksPerSecondAction("Pause", 0),
                new SetTargetTicksPerSecondAction("Slow", 10),
                new SetTargetTicksPerSecondAction("Moderate", 30),
                new SetTargetTicksPerSecondAction("Fast", 50),
                

            /* TODO one day we will make turbo faster :) */
            new SetTargetTicksPerSecondAction("Turbo", 50)
            }, 1);

    public void setServerControlInterface(ServerControlInterface i) {
        serverInterface = i;

        boolean enabled = (serverInterface != null);
        loadGameAction.setEnabled(enabled);
        saveGameAction.setEnabled(enabled);

        Enumeration e = targetTicksPerSecondActions.getActions();

        while (e.hasMoreElements()) {
            ((Action)e.nextElement()).setEnabled(enabled);
        }

        if (i == null) {
            selectMapActions = new ActionAdapter(new Action[0]);
        } else {
            String[] mapNames = i.getMapNames();
            Action[] actions = new Action[mapNames.length];

            for (int j = 0; j < actions.length; j++) {
                actions[j] = new NewGameAction(mapNames[j]);
                actions[j].setEnabled(enabled);
            }

            selectMapActions = new ActionAdapter(actions);
        }

        newGameAction.setEnabled(enabled);
    }

    public ServerControlModel(ServerControlInterface i) {
        setServerControlInterface(i);
    }

    /**
     * @return an action to load a game.
     * TODO The action produces a file selector dialog to load the game
     */
    public Action getLoadGameAction() {
        return loadGameAction;
    }

    /**
     * @return an action to save a game
     * TODO The action produces a file selector dialog to save the game
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
     * When calling this action, set the action command string to the desired
     * map name, or call the appropriate selectMapAction.
     * @return an action to start a new game
     */
    public Action getNewGameAction() {
        return newGameAction;
    }

    /**
     * @return an ActionAdapter representing a list of actions representing
     * valid map names
     */
    public ActionAdapter getMapNames() {
        return selectMapActions;
    }
}
