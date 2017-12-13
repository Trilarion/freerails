/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.railz.client.top;

import java.util.*;
import java.io.IOException;
import java.security.GeneralSecurityException;

import org.railz.client.view.GUIRoot;
import org.railz.client.renderer.ViewLists;
import org.railz.client.model.ModelRoot;
import org.railz.controller.*;
import org.railz.move.Move;
import org.railz.move.MoveStatus;
import org.railz.move.TimeTickMove;
import org.railz.util.*;
import org.railz.world.player.Player;
import org.railz.world.player.PlayerPrincipal;
import org.railz.world.top.KEY;
import org.railz.world.top.World;


/**
 * This class receives moves from the client. This class tries out moves on the
 * world if necessary, and passes them to the connection.
 */
public class ConnectionAdapter implements UntriedMoveReceiver,
    ConnectionListener {
    private NonAuthoritativeMoveExecuter moveExecuter;
    private ModelRoot modelRoot;
    private Player player;
    ConnectionToServer connection;
    private Object authMutex = new Integer(1);
    private boolean authenticated;
    private GUIClient guiClient;

    /**
     * The GameLoop providing the move execution thread for this
     * ConnectionAdapter's Move Executer
     */
    private GameLoop gameLoop;

    /**
     * we forward outbound moves from the client to this.
     */
    UncommittedMoveReceiver uncommittedReceiver;
    MoveReceiver moveReceiver;
    World world;
    private FreerailsProgressMonitor progressMonitor;
    private GUIRoot guiRoot;

    public int getNumBlockedMoves() {
	return ((NonAuthoritativeMoveExecuter.PendingQueue)
	    moveExecuter.getUncommittedMoveReceiver()).getNumBlockedMoves();
    }

    public ConnectionAdapter(ModelRoot mr, GUIRoot gr, Player
	    player, FreerailsProgressMonitor pm, GUIClient gc) {
        modelRoot = mr;
        this.player = player;
        this.progressMonitor = pm;
        guiClient = gc;
	guiRoot = gr;
    }

    /**
     * This class receives moves from the connection and passes them on to a
     * MoveReceiver.
     */
    public class WorldUpdater implements SourcedMoveReceiver {
        private MoveReceiver moveReceiver;

        /**
         * TODO get rid of this
         */
        public synchronized void undoLastMove() {
            // do nothing
        }

        public synchronized void processMove(Move move, ConnectionToServer c) {
            processMove(move);
        }

        /**
         * Processes inbound moves from the server
         */
        public synchronized void processMove(Move move) {
            if (move instanceof TimeTickMove) {
                /*
                 * flush our outgoing moves prior to receiving next tick
                 * TODO improve our buffering strategy
                 */
                connection.flush();
            }

            moveReceiver.processMove(move);
        }

        public synchronized void setMoveReceiver(MoveReceiver moveReceiver) {
            this.moveReceiver = moveReceiver;
        }
    }

    private WorldUpdater worldUpdater = new WorldUpdater();

    /**
     * Processes outbound moves to the server
     */
    public synchronized void processMove(Move move) {
        if (uncommittedReceiver != null) {
            uncommittedReceiver.processMove(move);
        }
    }

    public synchronized void undoLastMove() {
        if (uncommittedReceiver != null) {
            uncommittedReceiver.undoLastMove();
        }
    }

    public synchronized MoveStatus tryDoMove(Move move) {
	return move.tryDoMove(world, move.getPrincipal());
    }

    public synchronized MoveStatus tryUndoMove(Move move) {
	return move.tryUndoMove(world, move.getPrincipal());
    }

    private void closeConnection() {
        connection.close();
        connection.removeMoveReceiver(worldUpdater);
        modelRoot.getUserMessageLogger().println
	    (Resources.get("Connection to server closed"));
    }

    public synchronized void setConnection(ConnectionToServer c)
        throws IOException, GeneralSecurityException {
        setConnectionImpl(c);

        synchronized (authMutex) {
            if (!authenticated) {
                modelRoot.getUserMessageLogger().println
		    (Resources.get("Waiting for authentication"));

                try {
                    authMutex.wait();
                } catch (InterruptedException e) {
                    //ignore
                }

                if (!authenticated) {
                    throw new GeneralSecurityException("Server rejected " +
                        "attempt to authenticate");
                }

            }
        }
    }

    /**
     * This function may be entered from either the AWT event handler thread
     * (via a local connection when the user clicks on something), or from the
     * network connection thread, or from the initialisation thread of the
     * launcher.
     */
    private synchronized void setConnectionImpl(ConnectionToServer c)
        throws IOException, GeneralSecurityException {
        if (gameLoop != null) {
            gameLoop.stop();
        }

        if (connection != null) {
            closeConnection();
            connection.removeMoveReceiver(worldUpdater);
            connection.removeConnectionListener(this);
        }

        /* grab the lock on the WorldUpdater in order to prevent any moves from
         * the server being lost whilst we plumb it in */
        synchronized (worldUpdater) {
            connection = c;
            connection.open();

            connection.addMoveReceiver(worldUpdater);
            connection.addConnectionListener(this);
            world = connection.loadWorldFromServer();

            /* plumb in a new Move Executer */
            moveExecuter = new NonAuthoritativeMoveExecuter(world,
                    moveReceiver, modelRoot);
            worldUpdater.setMoveReceiver(moveExecuter);
            uncommittedReceiver = moveExecuter.getUncommittedMoveReceiver();
            ((NonAuthoritativeMoveExecuter.PendingQueue)uncommittedReceiver).addMoveReceiver(connection);
        }

        /* start a new game loop */
	gameLoop = new GameLoop(guiRoot.getScreenHandler(),
		moveExecuter);

        /* attempt to authenticate the player */
        modelRoot.getUserMessageLogger().println
	    (Resources.get("Attempting to authenticate player: ") +
	     player.getName());
        authenticated = false;
        connection.sendCommand(new AddPlayerCommand(player, player.sign()));

	/* send a command to set up server-specific resources */
	connection.sendCommand(new ResourceBundleManager.GetResourceCommand
		("org.railz.data.l10n.server", Locale.getDefault()));
    }

    private void playerConfirmed() {
        try {
            /* create the models */
            assert world != null;

            modelRoot.setWorld(world);
	    ViewLists viewLists = new ViewListsImpl(modelRoot,
		    guiRoot, progressMonitor);

            if (!viewLists.validate(world)) {
		/* most likely reason for failure is that the server's object
		 * set is different to what the client is expecting */
                modelRoot.getUserMessageLogger().println
		    (Resources.get("Your client is not compatible with " +
				   "the server."));
            }

            /*
             * wait until the player the client represents has been created in
             * the model (this may not occur until we process the move creating
             * the player from the server
             */
            while (!world.boundsContain(KEY.PLAYERS,
                        ((PlayerPrincipal)modelRoot.getPlayerPrincipal()).getId(),
                        modelRoot.getPlayerPrincipal())) {
                moveExecuter.update();
            }

            modelRoot.setWorld(this, viewLists);

            /* start the game loop */
            String threadName = "Railz client: " + guiClient.getTitle();
            Thread t = new Thread(gameLoop, threadName);
            t.start();
        } catch (IOException e) {
            modelRoot.getUserMessageLogger().println
		(Resources.get("There was a problem reading in the graphics "
			       + "data"));
	    System.err.println(e.getMessage());
	    e.printStackTrace();
        }
    }

    public void setMoveReceiver(MoveReceiver m) {
        //moveReceiver = new CompositeMoveSplitter(m);
        //I don't want moves split at this stage since I want to be able
        //to listen for composite moves.
        moveReceiver = m;
    }

    public void connectionClosed(ConnectionToServer c) {
        // ignore
    }

    public void connectionOpened(ConnectionToServer c) {
	// ignore
    }

    public void connectionStateChanged(ConnectionToServer c) {
        // ignore
    }

    public void processServerCommand(ConnectionToServer c, ServerCommand s) {
        if (s instanceof AddPlayerResponseCommand) {
            synchronized (authMutex) {
                authenticated = !((AddPlayerResponseCommand)s).isRejected();

                if (authenticated) {
                    modelRoot.getUserMessageLogger().println
			(Resources.get
			 ("Player was successfully authenticated"));
                    modelRoot.setPlayerPrincipal(((AddPlayerResponseCommand)s).getPrincipal());
                    playerConfirmed();
                } else {
                    modelRoot.getUserMessageLogger().println
			(Resources.get("Authentication was rejected"));
                }

                authMutex.notify();
            }
        } else if (s instanceof WorldChangedCommand) {
            try {
                setConnectionImpl(c);
            } catch (IOException e) {
                modelRoot.getUserMessageLogger().println
		    (Resources.get("Unable to open remote connection"));
                closeConnection();
            } catch (GeneralSecurityException e) {
                modelRoot.getUserMessageLogger().println
		    (Resources.get("Unable to authenticate with server: ")
		     + e.toString());
            }
        } else if (s instanceof ServerMessageCommand) {
	    modelRoot.getUserMessageLogger().println
		(Resources.get(((ServerMessageCommand) s).getMessage()));
	} else if (s instanceof
		ResourceBundleManager.GetResourceResponseCommand) {
		ResourceBundleManager.GetResourceResponseCommand response =
		    (ResourceBundleManager.GetResourceResponseCommand) s;
		if (response.isSuccessful()) {
		    Resources.setExternalResourceBundle
			(response.getResourceBundle());
		} else {
		    System.err.println("Couldn't get resource bundle from " +
			    "server");
		}
	}
    }
}
