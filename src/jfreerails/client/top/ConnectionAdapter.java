package jfreerails.client.top;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;
import jfreerails.client.common.ModelRoot;
import jfreerails.client.renderer.ViewLists;
import jfreerails.controller.AddPlayerCommand;
import jfreerails.controller.AddPlayerResponseCommand;
import jfreerails.controller.ConnectionListener;
import jfreerails.controller.ConnectionToServer;
import jfreerails.controller.MoveReceiver;
import jfreerails.controller.ServerCommand;
import jfreerails.controller.UntriedMoveReceiver;
import jfreerails.controller.WorldChangedCommand;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.move.TimeTickMove;
import jfreerails.util.FreerailsProgressMonitor;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.player.PlayerPrincipal;
import jfreerails.world.top.World;


/**
 * This class receives moves from the client. This class tries out moves on the
 * world if necessary, and passes them to the connection.
 * @author rob
 */
public class ConnectionAdapter implements UntriedMoveReceiver,
    ConnectionListener {
    private static final Logger logger = Logger.getLogger(ConnectionAdapter.class.getName());
    private NonAuthoritativeMoveExecuter moveExecuter;
    private final ModelRoot modelRoot;
    private final Player player;
    private ConnectionToServer connection;
    private final Object authMutex = new Integer(1);
    private boolean authenticated;
    private final GUIClient guiClient;

    /**
     * The GameLoop providing the move execution thread for this
     * ConnectionAdapter's Move Executer.
     */
    private GameLoop gameLoop;

    /**
     * we forward outbound moves from the client to this.
     */
    private MoveReceiver uncommittedReceiver;
    private MoveReceiver moveReceiver;
    private World world;
    private final FreerailsProgressMonitor progressMonitor;

    public ConnectionAdapter(Player player, FreerailsProgressMonitor pm,
        GUIClient gc) {
        modelRoot = gc.getModelRoot();
        this.player = player;
        this.progressMonitor = pm;
        guiClient = gc;
    }

    /**
     * This class receives moves from the connection and passes them on to a
     * MoveReceiver.
     */
    public class WorldUpdater implements MoveReceiver {
        private MoveReceiver moveReceiver;

        /**
         * Processes inbound moves from the server.
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

    private final WorldUpdater worldUpdater = new WorldUpdater();

    /**
     * Processes outbound moves to the server.
     */
    public synchronized void processMove(Move move) {
        if (uncommittedReceiver != null) {
            uncommittedReceiver.processMove(move);
        }
    }

    public synchronized MoveStatus tryDoMove(Move move) {
        /* TODO
         * return move.tryDoMove(world, move.getPrincipal());
         */
        return move.tryDoMove(world, Player.AUTHORITATIVE);
    }

    private void closeConnection() {
        connection.close();
        connection.removeMoveReceiver(worldUpdater);
        modelRoot.setProperty(ModelRoot.QUICK_MESSAGE,
            "Connection to server closed");
    }

    public synchronized void setConnection(ConnectionToServer c)
        throws IOException, GeneralSecurityException {
        setConnectionImpl(c);

        synchronized (authMutex) {
            if (!authenticated) {
                logger.fine("Waiting for authentication");

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
            connection.removeConnectionListener();
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
        gameLoop = new GameLoop(guiClient.getScreenHandler(), moveExecuter);

        /* attempt to authenticate the player */
        modelRoot.setProperty(ModelRoot.QUICK_MESSAGE,
            "Attempting to " + "authenticate " + player.getName() +
            " with server");
        authenticated = false;
        connection.sendCommand(new AddPlayerCommand(player, player.sign()));
    }

    private void playerConfirmed(PlayerPrincipal principal) {
        try {
            /* create the models */
            assert world != null;

            /*
             * wait until the player the client represents has been created in
             * the model (this may not occur until we process the move creating
             * the player from the server
            */
            int playerID = principal.getId();

            while (world.getNumberOfPlayers() <= playerID) {
                logger.fine("Size of players list is " +
                    world.getNumberOfPlayers());
                moveExecuter.update();
            }

            assert world.isPlayer(principal);

            ViewLists viewLists = new ViewListsImpl(world, progressMonitor);

            if (!viewLists.validate(world)) {
                modelRoot.setProperty(ModelRoot.QUICK_MESSAGE,
                    "Couldn't validate " + "viewLists!");
            }

            guiClient.setup(world, this, viewLists, principal);

            /* start the game loop */
            String threadName = "JFreerails client: " + guiClient.getTitle();
            Thread t = new Thread(gameLoop, threadName);
            t.start();
        } catch (IOException e) {
            modelRoot.setProperty(ModelRoot.QUICK_MESSAGE,
                "Couldn't set up " + "view Lists");
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

    public void connectionStateChanged(ConnectionToServer c) {
        // ignore
    }

    public void processServerCommand(ConnectionToServer c, ServerCommand s) {
        if (s instanceof AddPlayerResponseCommand) {
            synchronized (authMutex) {
                authenticated = !((AddPlayerResponseCommand)s).isRejected();

                if (authenticated) {
                    logger.fine("Player was authenticated");

                    FreerailsPrincipal principal = ((AddPlayerResponseCommand)s).getPrincipal();
                    playerConfirmed((PlayerPrincipal)principal);
                } else {
                    logger.fine("Authentication was rejected");
                }

                authMutex.notify();
            }
        } else if (s instanceof WorldChangedCommand) {
            try {
                setConnectionImpl(c);
            } catch (IOException e) {
                modelRoot.setProperty(ModelRoot.QUICK_MESSAGE,
                    "Unable to open" + " remote connection");
                closeConnection();
            } catch (GeneralSecurityException e) {
                modelRoot.setProperty(ModelRoot.QUICK_MESSAGE,
                    "Unable to " + "authenticate with server: " + e.toString());
            }
        }
    }
}