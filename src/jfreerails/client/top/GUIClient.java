package jfreerails.client.top;

import java.awt.DisplayMode;
import java.io.IOException;
import java.net.InetAddress;

import javax.swing.JFrame;

import jfreerails.client.common.ScreenHandler;
import jfreerails.client.renderer.ViewLists;
import jfreerails.client.view.MapCursor;
import jfreerails.controller.ConnectionToServer;
import jfreerails.controller.InetConnection;
import jfreerails.controller.LocalConnection;
import jfreerails.controller.MoveChainFork;
import jfreerails.controller.ServerControlInterface;
import jfreerails.util.FreerailsProgressMonitor;

/**
 * This class implements a GUI-driven client to be used by human players.
 *
 * XXX How should the server be controlled from the client? (loading, saving of
 * maps etc?). Currently we will do this over the local connection only, by
 * the client having access to a ServerControlInterface object
 */
public class GUIClient extends Client {
	private MapCursor cursor = null;
	private Object mutex;
	private GUIComponentFactoryImpl gUIComponentFactory;
	protected ServerControlModel serverControls = new
	    ServerControlModel(null);

	public void setCursor(MapCursor c) {
		cursor = c;
	}

	public MapCursor getCursor() {
		return cursor;
	}

	private GUIClient(ConnectionToServer server, int mode, DisplayMode dm,
		String title, FreerailsProgressMonitor pm)
	    throws IOException {
		receiver = new ConnectionAdapter();
		moveChainFork = new MoveChainFork();
		receiver.setMoveReceiver(moveChainFork);
		receiver.setConnection(server);


		GUIComponentFactoryImpl gUIComponentFactory =
		    new GUIComponentFactoryImpl(this);
		
		ViewLists viewLists = new ViewListsImpl(receiver.world, pm);
		if (!viewLists.validate(receiver.world)) {
		    throw new IllegalArgumentException();
		}

		gUIComponentFactory.setup(viewLists);

		JFrame client = gUIComponentFactory.createClientJFrame(title);


		//We want to setup the screen handler before creating the view lists since the 
		//ViewListsImpl creates images that are compatible with the current display settings 
		//and the screen handler may change the display settings.
		ScreenHandler screenHandler= new ScreenHandler(client, mode, dm);

		moveChainFork.add(gUIComponentFactory);


		System.out.println("creating gameloop");
		GameLoop gameLoop = new GameLoop(screenHandler, receiver.getMutex());
		Thread t = new Thread(gameLoop);
		t.start();

	    }

	/**
	 * Start a client with an internet connection to a server
	 */
	public GUIClient(InetAddress server, int mode, DisplayMode dm, String
		title, FreerailsProgressMonitor pm) throws
	    IOException {
	    this(new InetConnection(server), mode, dm, title, pm);
	}

	/**
	 * sets up a connnection with a local server. Currently this is the only
	 * form of connection supported
	 */
	public GUIClient(LocalConnection server, int mode, DisplayMode dm,
	String title, FreerailsProgressMonitor pm) throws IOException {
		this((ConnectionToServer) new LocalConnection(server), mode,
		dm, title, pm);
	}

	/**
	 * Not all clients may return a valid object - access to the server controls
	 * is at the discretion of the server.
	 */
	public ServerControlModel getServerControls() {
		return serverControls;
	}

	public void setServerControls(ServerControlInterface controls) {
		serverControls.setServerControlInterface(controls);
	}
}
