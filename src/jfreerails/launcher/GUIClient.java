/*
 * Created on Sep 11, 2004
 *
 */
package jfreerails.launcher;

import java.awt.DisplayMode;
import java.io.IOException;
import java.io.Serializable;

import javax.swing.JFrame;

import jfreerails.client.common.ModelRootImpl;
import jfreerails.client.renderer.ViewLists;
import jfreerails.client.top.GUIComponentFactoryImpl;
import jfreerails.client.top.GameLoop;
import jfreerails.client.top.ViewListsImpl;
import jfreerails.client.view.ActionRoot;
import jfreerails.controller.ModelRoot;
import jfreerails.controller.ReportBugTextGenerator;
import jfreerails.controller.ScreenHandler;
import jfreerails.controller.ModelRoot.Property;
import jfreerails.network.FreerailsClient;
import jfreerails.network.FreerailsGameServer;
import jfreerails.network.SavedGamesManager;
import jfreerails.server.SavedGamesManagerImpl;
import jfreerails.server.ServerGameModelImpl;
import jfreerails.util.FreerailsProgressMonitor;
import jfreerails.util.GameModel;
import jfreerails.world.common.GameSpeed;
import jfreerails.world.common.GameTime;
import jfreerails.world.player.Player;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.World;

/**
 * A swing freerails client.
 * 
 * @author Luke
 * 
 */
public class GUIClient extends FreerailsClient implements
		FreerailsProgressMonitor {

	public static void main(String[] args) {
		try {
			GUIClient client = new GUIClient("Test", null,
					ScreenHandler.WINDOWED_MODE, null);
			client.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final ActionRoot actionRoot;

	private final GUIComponentFactoryImpl factory;

	private final ModelRootImpl modelRoot;

	private final FreerailsProgressMonitor monitor;

	private final String name;

	private final ScreenHandler screenHandler;

	private ViewLists vl;

	public GUIClient(String name, FreerailsProgressMonitor fm, int screenMode,
			DisplayMode dm) throws IOException {
		this.name = name;
		this.monitor = null == fm ? this : fm;
		// Set up model root and action root.
		modelRoot = new ModelRootImpl();
		modelRoot.setMoveFork(this.getMoveFork());
		modelRoot.setMoveReceiver(this);
		modelRoot.setServerCommandReceiver(this);
		actionRoot = new ActionRoot(modelRoot);

		// Create GUI components
		factory = new GUIComponentFactoryImpl(modelRoot, actionRoot);

		JFrame createClientJFrame = factory.createClientJFrame(name);

		screenHandler = new ScreenHandler(createClientJFrame, screenMode, dm);

	}

	protected void clientUpdates() {
		if (factory.isSetup()) {
			factory.getBuildTrackController().update();
//			Update sub tick time.
			long currentTime = System.currentTimeMillis();
			long lastTick = getLastTickTime();
			double dt = currentTime - lastTick;
			ReadOnlyWorld world2 = modelRoot.getWorld();
			GameSpeed gameSpeed = (GameSpeed) world2.get(ITEM.GAME_SPEED);
			GameTime currentGameTime  = world2.currentTime();
			double ticks = currentGameTime.getTicks();
			if(!gameSpeed.isPaused()){			
				double milliSecondsPerTick = 1000/ gameSpeed.getSpeed();			
				double subTicks = dt / milliSecondsPerTick;			
				subTicks = Math.min(dt, 1d);				
				ticks += subTicks;		
			}
			modelRoot.setProperty(Property.TIME, new Double(ticks));
		}
		
	}

	public void finished() {
		// TODO Auto-generated method stub
	}

	public ScreenHandler getScreenHandler() {
		return screenHandler;
	}

	protected void newWorld(World w) {
		try {
			if (null == vl || !vl.validate(w)) {
				try {
					vl = new ViewListsImpl(w, monitor);
					monitor.finished();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// Should be a smarter way of doing this..
			for (int player = 0; player < w.getNumberOfPlayers(); player++) {
				Player p = w.getPlayer(player);

				if (p.getName().equals(this.name)) {
					modelRoot.setup(w, p.getPrincipal());
				}
			}

			modelRoot.setProperty(ModelRoot.Property.SERVER, connection2Server
					.getServerDetails());
			actionRoot.setup(modelRoot, vl);

			factory.setup(vl, w);
		} catch (Exception e) {
			ReportBugTextGenerator.unexpectedException(e);	
		}
	}

	public void nextStep(int max) {
		// TODO Auto-generated method stub
	}

	public void setMessage(String s) {
		System.out.println(s);
	}

	public void setValue(int i) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setProperty(ClientProperty propertyName, Serializable value) {		
		super.setProperty(propertyName, value);
		switch (propertyName) {
		case SAVED_GAMES:
			modelRoot.setProperty(Property.SAVED_GAMES_LIST, value);
			break;

		default:
			break;
		}
	}

	void start() {
		// Set up world.
		SavedGamesManager gamesManager = new SavedGamesManagerImpl();
		FreerailsGameServer server = new FreerailsGameServer(gamesManager);
		String mapName = gamesManager.getNewMapNames()[0];

		ServerGameModelImpl serverGameModel = new ServerGameModelImpl();
		server.setServerGameModel(serverGameModel);

		this.connect(server, name, "password");

		server.newGame(mapName);

		while (null == this.getWorld()) {
			this.update();
			server.update();
		}

		GameModel[] models = new GameModel[] { this, server };

		// Start the game loop
		GameLoop gameLoop = new GameLoop(screenHandler, models);
		screenHandler.apply();

		Thread t = new Thread(gameLoop);
		t.start();
	}
}