package jfreerails.client.top;

import java.io.IOException;

import javax.swing.JFrame;

import jfreerails.client.common.ScreenHandler;
import jfreerails.client.renderer.ViewLists;
import jfreerails.client.view.MapCursor;
import jfreerails.controller.ConnectionToServer;
import jfreerails.controller.LocalConnection;
import jfreerails.controller.MoveChainFork;
import jfreerails.controller.ServerControlInterface;

/**
 * This class implements a GUI-driven client to be used by human players.
 *
 * XXX How should the server be controlled from the client? (loading, saving of
 * maps etc?). Currently we will do this over the local connection only, by
 * the client having access to a ServerControlInterface object
 */
public class GUIClient extends Client  {
	private MapCursor cursor = null;
	protected ServerControlInterface serverControls;	
	private Object mutex;
	private GUIComponentFactoryImpl gUIComponentFactory = new GUIComponentFactoryImpl();

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
	public GUIClient(LocalConnection server) throws IOException {
		mutex = server.getMutex();
		receiver = new ConnectionAdapter();
		ConnectionToServer connection = new LocalConnection(server);
		receiver.setConnection(connection);
		moveChainFork = new MoveChainFork();
		receiver.setMoveReceiver(moveChainFork);												
		moveChainFork.add(gUIComponentFactory);					
	}
	
	public JFrame getClientJFrame(){
		return gUIComponentFactory.createClientJFrame();
	}	

	public void setViewLists(ViewLists viewLists) {
		if (!viewLists.validate(receiver.world)) {
			throw new IllegalArgumentException();
		}		
		gUIComponentFactory.setup(viewLists, this);
	}

	public void start(ScreenHandler screenHandler) {
		System.out.println("creating gameloop");
		GameLoop gameLoop = new GameLoop(screenHandler, mutex);
		Thread t = new Thread(gameLoop);
		t.start();
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
