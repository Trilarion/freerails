/*
 * Created on Apr 17, 2004
 */
package jfreerails.controller.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;


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
    private static final Logger logger = Logger.getLogger(FreerailsGameServer.class.getName());
    private final SynchronizedFlag status = new SynchronizedFlag(false);
    private final SavedGamesManager savedGamesManager;
    private boolean newPlayersAllowed = true;
    private ArrayList players = new ArrayList();
    private HashMap username2password = new HashMap();
    private HashMap id2username = new HashMap();

    /* Contains the usernames of the players who are currently logged on.*/
    private HashSet currentlyLoggedOn = new HashSet();

    /**
     * The players who have cofirmed that they have received the last copy of
     * the world object sent.
     */
    private HashSet confirmedPlayers = new HashSet();
    private HashMap acceptedConnections = new HashMap();
    private int commandID = 0;
    private World world;

    /**
     * ID of the last SetWorldClientCommand sent out. Used to keep track of
     * which clients have updated their world object to the current version.
     */
    private int confirmationID = Integer.MIN_VALUE; //Don't default to avoid

    // mistaken confirmations.
    public FreerailsGameServer(SavedGamesManager gamesManager) {
        this.savedGamesManager = gamesManager;
    }

    private int getNextClientCommandId() {
        return commandID++;
    }

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

    public void logoff(int player) {
        String username = (String)players.get(player);
        currentlyLoggedOn.remove(username);
    }

    public void loadgame(String saveGameName) {
        // TODO Auto-generated method stub
    }

    public void savegame() {
        // TODO Auto-generated method stub
    }

    public void stopGame() {
        // TODO Auto-generated method stub
    }

    public void newGame(String mapName) {
        this.newPlayersAllowed = false;
        confirmedPlayers.clear();
        world = new WorldImpl(10, 10);

        /* Add players to world. */
        for (int i = 0; i < players.size(); i++) {
            String name = (String)players.get(i);
            Player p = new Player(name, null, i); //public key set to null!
            int index = world.addPlayer(p);
            assert i == index;
        }

        /* Send the world to the clients. */
        confirmationID = getNextClientCommandId();

        SetWorldClientCommand command = new SetWorldClientCommand(confirmationID,
                world);

        send2All(command);

        logger.fine("newGame");
    }

    public synchronized void addConnection(Connection2Client connection) {
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

                /* Send to all clients. */
                sendListOfConnectedPlayers2Clients();
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

    private void sendListOfConnectedPlayers2Clients() throws IOException {
        /* Send the client the list of players. */
        String[] playerNames = new String[players.size()];

        for (int i = 0; i < players.size(); i++) {
            playerNames[i] = (String)players.get(i);
        }

        ClientCommand command = new SetPropertyClientCommand(getNextClientCommandId(),
                ClientControlInterface.CONNECTED_CLIENTS, playerNames);

        send2All(command);
    }

    private void send2All(FreerailsSerializable message) {
        Iterator it = acceptedConnections.keySet().iterator();

        while (it.hasNext()) {
            Integer id = (Integer)it.next();
            Connection2Client connection = (Connection2Client)acceptedConnections.get(id);

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

    private void removeConnection(Integer id) throws IOException {
        Connection2Client connection = (Connection2Client)acceptedConnections.get(id);
        connection.disconnect();

        String userName = (String)players.get(id.intValue());
        this.currentlyLoggedOn.remove(userName);
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

    public void stop() {
        // TODO Auto-generated method stub
    }

    public void run() {
        status.open();
        status.close();
    }

    public boolean isNewPlayersAllowed() {
        return newPlayersAllowed;
    }

    public void setNewPlayersAllowed(boolean newPlayersAllowed) {
        this.newPlayersAllowed = newPlayersAllowed;
    }

    /**
     * Reads and deals with the outstanding messages from each of the connected
     * clients.
     */
    public void update() {
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
                        } else if (messages[i] instanceof Move) {
                            Move move = (Move)messages[i];
                            FreerailsPrincipal principal = world.getPlayer(playerID.intValue())
                                                                .getPrincipal();
                            MoveStatus status = move.tryDoMove(this.world,
                                    principal);

                            if (status.isOk()) {
                                move.doMove(world, principal);
                                send2All(move);
                            } else {
                                connection.writeToClient(status);
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

    World getCopyOfWorld() {
        return this.world.defensiveCopy();
    }

    boolean isConfirmed(int player) {
        logger.fine("confirmedPlayers.size()=" + confirmedPlayers.size());

        boolean isConfirmed = confirmedPlayers.contains(new Integer(player));

        return isConfirmed;
    }
}