package jfreerails.client.view;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import jfreerails.client.common.ActionAdapter;
import jfreerails.client.common.ModelRoot;
import jfreerails.move.ChangeGameSpeedMove;
import jfreerails.network.LoadGameServerCommand;
import jfreerails.network.NewGameServerCommand;
import jfreerails.network.SaveGameServerCommand;
import jfreerails.network.ServerCommand;
import jfreerails.network.ServerControlInterface;
import jfreerails.world.common.GameSpeed;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.ReadOnlyWorld;


/**
 * Exposes the ServerControlInterface to client UI implementations.
 * @author rob
 * @author Luke
 * @author MystiqueAgent
 */
public class ServerControlModel {
   
    private ModelRoot modelRoot;

	public void setModelRoot(ModelRoot modelRoot) {
		this.modelRoot = modelRoot;
	}
    private class NewGameAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
           
                String mapName = e.getActionCommand();

                if (mapName != null) {
                    ServerCommand command = new NewGameServerCommand(1, mapName);
                    modelRoot.sendCommand(command);
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
    private final Action newGameAction = new NewGameAction(null);

    private class LoadGameAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            ServerCommand command = new LoadGameServerCommand(1, ServerControlInterface.FREERAILS_SAV);
            modelRoot.sendCommand(command);
        }

        public LoadGameAction() {
            putValue(NAME, "Load Game");
            putValue(MNEMONIC_KEY, new Integer(76));
        }
    }

    private final Action loadGameAction = new LoadGameAction();

    private class SaveGameAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            ServerCommand command = new SaveGameServerCommand(1, ServerControlInterface.FREERAILS_SAV);
            modelRoot.sendCommand(command);
            loadGameAction.setEnabled(true);
            
        }

        public SaveGameAction() {
            putValue(NAME, "Save Game");
            putValue(MNEMONIC_KEY, new Integer(83));
        }
    }

    private final Action saveGameAction = new SaveGameAction();

    private class SetTargetTicksPerSecondAction extends AbstractAction {
        final int speed;

        public void actionPerformed(ActionEvent e) {
        	 int speed2set = speed;
                if (speed == 0) { // pausing/unpausing

                    speed2set = -1 * getTargetTicksPerSecond();

                } 
                modelRoot.doMove(ChangeGameSpeedMove.getMove(modelRoot.getWorld(),
                        new GameSpeed(speed2set)));
            
        }

        public SetTargetTicksPerSecondAction(String name, int speed) {
            this(name, speed, KeyEvent.VK_UNDEFINED);
        }

        /**
         * Same as the constructor above but it enables also to associate a <code>keyEvent</code>
         * with the action.
         *
         * @param name action name
         * @param speed speed
         * @param keyEvent associated key event. Use values from <code>KeyEvent</class>.
         *
         * by MystiqueAgent
         */
        public SetTargetTicksPerSecondAction(String name, int speed,
            int keyEvent) {
            putValue(NAME, name);
            this.speed = speed;
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyEvent, 0));
        }
    }

    private final SetTargetTicksPerSecondAction[] speedActions = new SetTargetTicksPerSecondAction[] {
            new SetTargetTicksPerSecondAction("Pause", 0, KeyEvent.VK_P), 
            new SetTargetTicksPerSecondAction("Slow", 10, KeyEvent.VK_1), 
            new SetTargetTicksPerSecondAction("Moderate", 30, KeyEvent.VK_2),
            new SetTargetTicksPerSecondAction("Fast", 50, KeyEvent.VK_3), // by MystiqueAgent: added keyEvent parameter
        };
    private final ActionAdapter targetTicksPerSecondActions = new ActionAdapter(speedActions,
            0);
    public void setServerControlInterface() {
        

        boolean enabled = true;

        //Check that there is a file to load..
        boolean canLoadGame = ServerControlModel.isSaveGameAvailable();

        loadGameAction.setEnabled(enabled && canLoadGame);
        saveGameAction.setEnabled(enabled);

        Enumeration e = targetTicksPerSecondActions.getActions();
        targetTicksPerSecondActions.setPerformActionOnSetSelectedItem(false);

        while (e.hasMoreElements()) {        	
            ((Action)e.nextElement()).setEnabled(true);
        }

//        if (i == null) {
//            selectMapActions = new ActionAdapter(new Action[0]);
//        } else {
            String[] mapNames = NewGameServerCommand.getMapNames();
            Action[] actions = new Action[mapNames.length];

            for (int j = 0; j < actions.length; j++) {
                actions[j] = new NewGameAction(mapNames[j]);
                actions[j].setEnabled(true);
            }

            selectMapActions = new ActionAdapter(actions);
//        }

        newGameAction.setEnabled(true);

    }

    public ServerControlModel(ModelRoot mr) {
        setServerControlInterface();
        this.modelRoot = mr;
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
     * Returns human readable string description of <code>tickPerSecond</code> number.
     * Looks for <code>tickPerSecond</code> in <code>targetTicksPerSecondActions</code>.
     * If appropriate action is not found returns first greater value or the greatest value.
     *
     * @param tickPerSecond int
     * @return String human readable description
     */
    public String getGameSpeedDesc(int tickPerSecond) {
        SetTargetTicksPerSecondAction action = null;

        for (int i = 0; i < speedActions.length; i++) {
            action = speedActions[i];

            if (action.speed >= tickPerSecond)
                break;
        }

        return (String)action.getValue(Action.NAME);
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
     * valid map names.
     */
    public ActionAdapter getMapNames() {
        return selectMapActions;
    }

    public static boolean isSaveGameAvailable() {
        try {
            FileInputStream in = new FileInputStream(ServerControlInterface.FREERAILS_SAV);
            GZIPInputStream zipin = new GZIPInputStream(in);
            ObjectInputStream objectIn = new ObjectInputStream(zipin);
            String version_string = (String)objectIn.readObject();
    
            if (!ServerControlInterface.VERSION.equals(version_string)) {
                throw new Exception(version_string);
            }
    
            in.close();
    
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
   
        public int getTargetTicksPerSecond() {
            ReadOnlyWorld world = modelRoot.getWorld();
            return ((GameSpeed)world.get(ITEM.GAME_SPEED)).getSpeed();
        }
    
}