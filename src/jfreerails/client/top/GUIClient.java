package jfreerails.client.top;

import java.io.IOException;
import javax.swing.JFrame;

import jfreerails.client.common.ScreenHandler;
import jfreerails.client.renderer.ViewLists;
import jfreerails.client.view.MapCursor;
import jfreerails.controller.ServerControlInterface;
import jfreerails.controller.ConnectionToServer;
import jfreerails.controller.LocalConnection;
import jfreerails.controller.MoveChainFork;

/**
 * This class implements a GUI-driven client to be used by human players.
 *
 * XXX How should the server be controlled from the client? (loading, saving of
 * maps etc?). Currently we will do this over the local connection only, by
 * the client having access to a ServerControlInterface object
 */
public class GUIClient extends Client {
	private MapCursor cursor = null;
	protected ServerControlInterface serverControls;

	public void setCursor(MapCursor c) {
		cursor = c;
	}

	public MapCursor getCursor() {
		return cursor;
	}

	/**
	 * sets up a connnection with a local server. Currently this is the only
	 * form of connection supported
	 */
	public GUIClient(
		LocalConnection server,
		boolean fullscreen,
		boolean nogameloop)
		throws IOException {
		receiver = new ConnectionAdapter();
		ConnectionToServer connection = new LocalConnection(server);
		receiver.setConnection(connection);
		moveChainFork = new MoveChainFork();
		receiver.setMoveReceiver(moveChainFork);
		ViewLists viewLists = new ViewListsImpl(receiver.world);
		if (!viewLists.validate(receiver.world)) {
			throw new IllegalArgumentException();
		}
		GUIComponentFactoryImpl gUIComponentFactory =
			new GUIComponentFactoryImpl();
		gUIComponentFactory.setup(viewLists, this);
		moveChainFork.add(gUIComponentFactory);
		JFrame client = gUIComponentFactory.createClientJFrame();

		if (nogameloop) {
			client.setSize(740, 500);
			client.show();
		} else {
			ScreenHandler screenHandler = new ScreenHandler(client, fullscreen);
			System.out.println("creating gameloop");
			GameLoop gameLoop = new GameLoop(screenHandler, server.getMutex());
			Thread t = new Thread(gameLoop);
			t.start();
		}
	}

	/**
	 * Not all clients may return a valid object - access to the server controls
	 * is at the discretion of the server.
	 */
	public ServerControlInterface getServerControls() {
		return serverControls;
	}

	public void setServerControls(ServerControlInterface controls) {
		serverControls = controls;
	}

}
