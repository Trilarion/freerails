/*
 * Created on Apr 17, 2004
 */
package jfreerails.network;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.move.PreMove;
import jfreerails.move.PreMoveStatus;
import jfreerails.world.accounts.BondTransaction;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.top.World;


/**
 * When executed by a thread, this class does the following: reads and executes moves and commands received from connected clients; sends moves and commands to
 * connected clients.
 *
 * @see InetConnectionAccepter
 * @see Connection2Client
 *
 * @author Luke
 *
 */
public class FreerailsGameServer implements ServerControlInterface,
    NewGameServer, Runnable {
    /** Used as a property name for property change events.*/
    public static final String CONNECTED_PLAYERS = "CONNECTED_PLAYERS";
    private static final Logger logger = Logger.getLogger(FreerailsGameServer.class.getName());

    public static FreerailsGameServer startServer(
        SavedGamesManager gamesManager) {
        FreerailsGameServer server = new FreerailsGameServer(gamesManager);
        Thread t = new Thread(server);
        t.start();

        try {
            /* Wait for the server to start before returning. */
            synchronized (server.status) {
                server.status.wait();
            }

            return server;
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }
    }

    private HashMap acceptedConnections = new HashMap();
    private int commandID = 0;

    /**
     * ID of the last SetWorldClientCommand sent out. Used to keep track of
     * which clients have updated their world object to the current version.
     */
    private int confirmationID = Integer.MIN_VALUE; /*Don't default 0 to avoid mistaken confirmations.*/

    /**
     * The players who have cofirmed that they have received the last copy of
     * the world object sent.
     */
    private HashSet confirmedPlayers = new HashSet();

    /* Contains the usernames of the players who are currently logged on.*/
    private HashSet currentlyLoggedOn = new HashSet();
    private HashMap id2username = new HashMap();
    private boolean newPlayersAllowed = true;
    private ArrayList players = new ArrayList();
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private final SavedGamesManager savedGamesManager;
    private ServerGameModel serverGameModel = new SimpleServerGameModel();
    private final SynchronizedFlag status = new SynchronizedFlag(false);
    private HashMap username2password = new HashMap();

    public FreerailsGameServer(SavedGamesManager gamesManager) {
        this.savedGamesManager = gamesManager;
    }

    public synchronized void addConnection(Connection2Client connection) {
        String[] before = getPlayerNames();
        logger.fine("Adding connection..");
        logger.fine("Waiting for logon details..");

        try {
            LogOnRequest request = (LogOnRequest)connection.waitForObjectFromClient();
            logger.fine("Trying to logon player: " + request.getUsername());

            LogOnResponse response = this.logon(request);
            connection.writeToClient(response);
            connection.flush();

            if (response.isSuccessful()) {
                logger.fine("Logon successful");

                synchronized (acceptedConnections) {
                    acceptedConnections.put(new Integer(
                            response.getPlayerNumber()), connection);
                }

                /* Just send to the new client. */
                ClientCommand setMaps = new SetPropertyClientCommand(getNextClientCommandId(),
                        ClientControlInterface.MAPS_AVAILABLE,
                        savedGamesManager.getNewMapNames());
                ClientCommand setSaveGames = new SetPropertyClientCommand(getNextClientCommandId(),
                        ClientControlInterface.SAVED_GAMES,
                        savedGamesManager.getSaveGameNames());
                connection.writeToClient(setMaps);
                connection.writeToClient(setSaveGames);

                //no need to flush since it is done in
                // sendListOfConnectedPlayers2Clients()

                /* If there is a game in progress, we need to
                 * send the client a copy of the world object.
                 */
                if (null != serverGameModel && null != getWorld()) {
                    SetWorldClientCommand command = new SetWorldClientCommand(confirmationID,
                            getWorld());
                    connection.writeToClient(command);
                }

                /* Send to all clients. */
                sendListOfConnectedPlayers2Clients();

                String[] after = getPlayerNames();
                propertyChangeSupport.firePropertyChange("CONNECTED_PLAYERS",
                    before, after);
            } else {
                connection.disconnect();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public synchronized int countOpenConnections() {
        Iterator it = acceptedConnections.keySet().iterator();
        int numConnections = 0;

        while (it.hasNext()) {
            Connection2Client connection = (Connection2Client)acceptedConnections.get(it.next());

            if (connection.isOpen()) {
                numConnections++;
            }
        }

        return numConnections;
    }

    World getCopyOfWorld() {
        return this.getWorld().defensiveCopy();
    }

    private int getNextClientCommandId() {
        return commandID++;
    }

    public String[] getPlayerNames() {
        String[] playerNames = new String[players.size()];

        for (int i = 0; i < players.size(); i++) {
            playerNames[i] = (String)players.get(i);
        }

        return playerNames;
    }

    private World getWorld() {
        return serverGameModel.getWorld();
    }

    boolean isConfirmed(int player) {
        logger.fine("confirmedPlayers.size()=" + confirmedPlayers.size());

        boolean isConfirmed = confirmedPlayers.contains(new Integer(player));

        return isConfirmed;
    }

    public boolean isNewPlayersAllowed() {
        return newPlayersAllowed;
    }

    public void loadgame(String saveGameName) {
        logger.info("load game " + saveGameName);

        ServerGameModel loadedGame;

        try {
            loadedGame = (ServerGameModel)savedGamesManager.loadGame(saveGameName);
            setServerGameModel(loadedGame);
            sendWorldUpdatedCommand();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void logoff(int player) {
        String username = (String)players.get(player);
        currentlyLoggedOn.remove(username);
    }

    public LogOnResponse logon(LogOnRequest lor) {
        String username = lor.getUsername();
        boolean isReturningPlayer = username2password.containsKey(username);

        if (!this.newPlayersAllowed && !isReturningPlayer) {
            return LogOnResponse.rejected("New logons not allowed.");
        }

        if (currentlyLoggedOn.contains(username)) {
            return LogOnResponse.rejected("Already logged on.");
        }

        int id;

        if (isReturningPlayer) {
            Integer idInteger = (Integer)id2username.get(username);
            id = idInteger.intValue();

            /* Check the password. */
            String correctPassword = (String)username2password.get(username);

            if (!correctPassword.equals(lor.getPassword())) {
                return LogOnResponse.rejected("Incorrect password.");
            }
        } else {
            id = players.size();
            players.add(username);
            username2password.put(username, lor.getPassword());
            id2username.put(username, new Integer(id));
        }

        currentlyLoggedOn.add(username);

        return LogOnResponse.accepted(id);
    }

    public void newGame(String mapName) {
        this.newPlayersAllowed = false;
        confirmedPlayers.clear();

        try {
            World world = (World)savedGamesManager.newMap(mapName);

            /* Add players to world. */
            for (int i = 0; i < players.size(); i++) {
                String name = (String)players.get(i);
                Player p = new Player(name, null, i); //public key set to null!
                int index = world.addPlayer(p);

                world.addTransaction(BondTransaction.issueBond(5),
                    p.getPrincipal());
                world.addTransaction(BondTransaction.issueBond(5),
                    p.getPrincipal());
                assert i == index;
            }

            serverGameModel.setWorld(world);
            setServerGameModel(serverGameModel);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        sendWorldUpdatedCommand();

        logger.fine("newGame");
    }

    private void removeConnection(Integer id) throws IOException {
        String[] before = getPlayerNames();
        Connection2Client connection = (Connection2Client)acceptedConnections.get(id);

        /* Fix for bug 1047439        Shutting down remote client crashes server
         * We get an IllegalStateException if we try to disconnect a
         * connection that is not open.
         */
        if (connection.isOpen()) {
            connection.disconnect();
        }

        String userName = (String)players.get(id.intValue());
        this.currentlyLoggedOn.remove(userName);

        String[] after = getPlayerNames();
        propertyChangeSupport.firePropertyChange("CONNECTED_PLAYERS", before,
            after);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    public void run() {
        status.open();
        status.close();
    }

    public void savegame(String saveGameName) {
        System.err.println("save game as " + saveGameName);

        try {
            savedGamesManager.saveGame(serverGameModel, saveGameName);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void send2All(FreerailsSerializable message) {
        send2AllExcept(null, message);
    }

    /** Sends the specified message to all connections except the specified one.*/
    private void send2AllExcept(Connection2Client dontSend2,
        FreerailsSerializable message) {
        Iterator it = acceptedConnections.keySet().iterator();

        while (it.hasNext()) {
            Integer id = (Integer)it.next();
            Connection2Client connection = (Connection2Client)acceptedConnections.get(id);

            if (dontSend2 != connection) {
                try {
                    connection.writeToClient(message);
                    connection.flush();
                } catch (Exception e) {
                    if (connection.isOpen()) {
                        e.printStackTrace();

                        try {
                            removeConnection(id);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private void sendListOfConnectedPlayers2Clients() throws IOException {
        /* Send the client the list of players. */
        String[] playerNames = getPlayerNames();

        ClientCommand command = new SetPropertyClientCommand(getNextClientCommandId(),
                ClientControlInterface.CONNECTED_CLIENTS, playerNames);

        send2All(command);
    }

    private void sendWorldUpdatedCommand() {
        /* Send the world to the clients. */
        confirmationID = getNextClientCommandId();

        SetWorldClientCommand command = new SetWorldClientCommand(confirmationID,
                getWorld());

        send2All(command);
    }

    public void setNewPlayersAllowed(boolean newPlayersAllowed) {
        this.newPlayersAllowed = newPlayersAllowed;
    }

    public void setServerGameModel(ServerGameModel serverGameModel) {
        this.serverGameModel = serverGameModel;

        MoveReceiver moveExecuter = new MoveReceiver() {
                public void processMove(Move move) {
                    MoveStatus ms = move.doMove(getWorld(), Player.AUTHORITATIVE);

                    if (ms.ok) {
                        send2All(move);
                    } else {
                        logger.warning(ms.message);
                    }
                }
            };

        serverGameModel.init(moveExecuter);
    }

    public void stop() {
        // TODO Auto-generated method stub
    }

    public void stopGame() {
        logger.info("Stop game.");
    }

    /**
     * Updates the game model, then reads and deals with the outstanding messages
     * from each of the connected clients.  This method is synchronized to prevent moves
     * being sent out while addConnection(.) is executing.
     */
    public synchronized void update() {
        if (null != serverGameModel) {
            serverGameModel.update();
        }

        try {
            Iterator it = acceptedConnections.keySet().iterator();
            int numConnections = 0;

            while (it.hasNext()) {
                Integer playerID = (Integer)it.next();
                Connection2Client connection = (Connection2Client)acceptedConnections.get(playerID);

                if (connection.isOpen()) {
                    FreerailsSerializable[] messages = connection.readFromClient();

                    for (int i = 0; i < messages.length; i++) {
                        if (messages[i] instanceof ServerCommand) {
                            ServerCommand command = (ServerCommand)messages[i];
                            CommandStatus status = command.execute(this);
                            logger.fine(command.toString());
                            connection.writeToClient(status);
                        } else if (messages[i] instanceof CommandStatus) {
                            CommandStatus commandStatus = (CommandStatus)messages[i];

                            if (commandStatus.getId() == this.confirmationID) {
                                /*
                                 * The client is confirming that they have
                                 * updated their world object to the current
                                 * version.
                                 */
                                this.confirmedPlayers.add(playerID);
                                logger.fine("Confirmed player " + playerID);
                            }

                            logger.fine(messages[i].toString());
                        } else if (messages[i] instanceof Move ||
                                messages[i] instanceof PreMove) {
                            Player player = getWorld().getPlayer(playerID.intValue());
                            FreerailsPrincipal principal = player.getPrincipal();

                            Move move;
                            boolean isMove = messages[i] instanceof Move;

                            if (isMove) {
                                move = (Move)messages[i];
                            } else {
                                PreMove pm = (PreMove)messages[i];
                                move = pm.generateMove(getWorld());
                            }

                            MoveStatus status = move.tryDoMove(this.getWorld(),
                                    principal);

                            if (status.isOk()) {
                                move.doMove(getWorld(), principal);

                                /* We don't send the move to the client that submitted it.*/
                                send2AllExcept(connection, move);
                            }

                            if (isMove) {
                                connection.writeToClient(status);
                            } else {
                                connection.writeToClient(PreMoveStatus.fromMoveStatus(
                                        status));
                            }
                        } else {
                            logger.fine(messages[i].toString());
                        }
                    }

                    connection.flush();
                } else {
                    /* Remove connection.*/
                    this.removeConnection(playerID);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}