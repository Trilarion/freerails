package jfreerails.client.view;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.ObjectInputStream;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import jfreerails.client.common.ActionAdapter;
import jfreerails.client.common.ModelRootImpl;
import jfreerails.controller.ModelRoot.Property;
import jfreerails.move.ChangeGameSpeedMove;
import jfreerails.network.LoadGameMessage2Server;
import jfreerails.network.Message2Server;
import jfreerails.network.NewGameMessage2Server;
import jfreerails.network.SaveGameMessage2Server;
import jfreerails.network.ServerControlInterface;
import jfreerails.world.common.GameSpeed;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.ReadOnlyWorld;

/**
 * Exposes the ServerControlInterface to client UI implementations.
 * 
 * @author rob
 * @author Luke
 * @author MystiqueAgent
 */

class SavFileFilter implements FilenameFilter {
	public boolean accept(File dir, String name) {
		return (name.endsWith(".sav"));
	}
}

public class ServerControlModel {

	private ModelRootImpl modelRoot;		

	public void setModelRoot(ModelRootImpl modelRoot) {
		this.modelRoot = modelRoot;
	}

	private class NewGameAction extends AbstractAction {
		private static final long serialVersionUID = 3690758388631745337L;

		public void actionPerformed(ActionEvent e) {

			String mapName = e.getActionCommand();

			if (mapName != null) {
				Message2Server message2 = new NewGameMessage2Server(1, mapName);
				modelRoot.sendCommand(message2);
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
		private static final long serialVersionUID = 3616451215278682931L;

		public void actionPerformed(ActionEvent e) {
			java.io.File dir = new File("./");
			FilenameFilter filter = new SavFileFilter();
			String[] files = dir.list(filter);
			Object[] saves = new Object[files.length + 1];
			for (int i = 0; i < files.length; i++) {
				saves[i] = files[i];
			}
			// Display a JOptionPane that lists the existing saved games
			try {
				String filename = (JOptionPane.showInputDialog(null,
						"Saved Games:", "Select game to load",
						JOptionPane.INFORMATION_MESSAGE, null, saves, saves[0]))
						.toString();
				// Load the game chosen
				Message2Server message2 = new LoadGameMessage2Server(1,
						filename);
				modelRoot.sendCommand(message2);
			} catch (Exception exept) {
				// <Hack>
				// When no saved game is selected, or one that doesnt exist,
				// nothing changes
				// </Hack>
			}
		}

		public LoadGameAction() {
			putValue(NAME, "Load Game");
			putValue(MNEMONIC_KEY, new Integer(76));
		}
	}

	private final Action loadGameAction = new LoadGameAction();

	private class SaveGameAction extends AbstractAction {
		private static final long serialVersionUID = 3905808578064562480L;

		public void actionPerformed(ActionEvent e) {
			try {
				// @SonnyZ
				// Show a JOptionPane that takes in a string from a text box
				String filename = JOptionPane.showInputDialog(null,
						"Saved Game Name:", "Save Game",
						JOptionPane.QUESTION_MESSAGE, null, null,
						modelRoot.getPrincipal().getName()).toString();
				// Save the current game using the string
				modelRoot.setProperty(Property.QUICK_MESSAGE, "Saved game "
						+ filename);
				Message2Server message2 = new SaveGameMessage2Server(1,
						filename + ".sav");

				modelRoot.sendCommand(message2);
				loadGameAction.setEnabled(true);
			} catch (Exception except) {

			}
		}

		public SaveGameAction() {
			putValue(NAME, "Save Game");
			putValue(MNEMONIC_KEY, new Integer(83));
		}
	}

	private final Action saveGameAction = new SaveGameAction();

	private class SetTargetTicksPerSecondAction extends AbstractAction {
		private static final long serialVersionUID = 3256437014978048052L;

		final int speed;

		public void actionPerformed(ActionEvent e) {
			int speed2set = speed;
			if (speed == 0) { // pausing/unpausing

				speed2set = -1 * getTargetTicksPerSecond();

			}
			modelRoot.doMove(ChangeGameSpeedMove.getMove(modelRoot.getWorld(),
					new GameSpeed(speed2set)));
		}

        /**
         * Same as the constructor above but it enables also to associate a
         * <code>keyEvent</code> with the action.
         *
         * @param name
         *            action name
         * @param speed
         *            speed
         * @param keyEvent
         *            associated key event. Use values from
         *            <code>KeyEvent</class>.
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
			new SetTargetTicksPerSecondAction("Fast", 70, KeyEvent.VK_3),
	};

	private final ActionAdapter targetTicksPerSecondActions = new ActionAdapter(
			speedActions, 0);

	public void setServerControlInterface() {

		boolean enabled = true;

		// Check that there is a file to load..
		boolean canLoadGame = ServerControlModel.isSaveGameAvailable();

		loadGameAction.setEnabled(enabled && canLoadGame);
		saveGameAction.setEnabled(enabled);

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

	public ServerControlModel(ModelRootImpl mr) {
		setServerControlInterface();
		this.modelRoot = mr;
	}

	/**
	 * @return an action to load a game. TODO The action produces a file
	 *         selector dialog to load the game
	 */
	public Action getLoadGameAction() {
		return loadGameAction;
	}

	/**
	 * @return an action to save a game TODO The action produces a file selector
	 *         dialog to save the game
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
	 * Returns human readable string description of <code>tickPerSecond</code>
	 * number. Looks for <code>tickPerSecond</code> in
	 * <code>targetTicksPerSecondActions</code>. If appropriate action is not
	 * found returns first greater value or the greatest value.
	 * 
	 * @param tickPerSecond
	 *            int
	 * @return String human readable description
	 */
	public String getGameSpeedDesc(int tickPerSecond) {
		SetTargetTicksPerSecondAction action = null;

		for (int i = 0; i < speedActions.length; i++) {
			action = speedActions[i];

			if (action.speed >= tickPerSecond)
				break;
		}

		return (String) action.getValue(Action.NAME);
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
	 * @return an ActionAdapter representing a list of actions representing
	 *         valid map names.
	 */
	public ActionAdapter getMapNames() {
		return selectMapActions;
	}

	public static boolean isSaveGameAvailable() {
		try {
			FileInputStream in = new FileInputStream(
					ServerControlInterface.FREERAILS_SAV);
			GZIPInputStream zipin = new GZIPInputStream(in);
			ObjectInputStream objectIn = new ObjectInputStream(zipin);
			String version_string = (String) objectIn.readObject();

			if (!ServerControlInterface.VERSION.equals(version_string)) {
				throw new Exception(version_string);
			}

			in.close();

			return true;
		} catch (Exception e) {
			return true;
		}
	}

	public int getTargetTicksPerSecond() {
		ReadOnlyWorld world = modelRoot.getWorld();
		return ((GameSpeed) world.get(ITEM.GAME_SPEED)).getSpeed();
	}
}