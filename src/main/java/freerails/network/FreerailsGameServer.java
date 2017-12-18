/*
 * Created on Apr 17, 2004
 */
package freerails.network;

import freerails.controller.*;
import freerails.move.AddPlayerMove;
import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.world.common.FreerailsSerializable;
import freerails.world.common.ImStringList;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.player.Player;
import freerails.world.top.World;
import org.apache.log4j.Logger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * When executed by a thread, this class does the following: reads and executes
 * moves and commands received from connected clients; sends moves and commands
 * to connected clients.
 *
 * @author Luke
 * @see InetConnectionAccepter
 * @see Connection2Client
 */
public class FreerailsGameServer implements ServerControlInterface, GameServer,
        Runnable {
    /**
     * Used as a property name for property change events.
     */
    public static final String CONNECTED_PLAYERS = "CONNECTED_PLAYERS";

    private static final Logger logger = Logger
            .getLogger(FreerailsGameServer.class.getName());
    private final HashMap<NameAndPassword, Connection2Client> acceptedConnections = new HashMap<>();
    /**
     * The players who have confirmed that they have received the last copy of
     * the world object sent.
     */
    private final HashSet<NameAndPassword> confirmedPlayers = new HashSet<>();
    /* Contains the user names of the players who are currently logged on. */
    private final HashSet<NameAndPassword> currentlyLoggedOn = new HashSet<>();
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
            this);
    private final SavedGamesManager savedGamesManager;
    private final SynchronizedFlag status = new SynchronizedFlag(false);
    private int commandID = 0;
    /**
     * ID of the last SetWorldMessage2Client sent out. Used to keep track of
     * which clients have updated their world object to the current version.
     */
    private int confirmationID = Integer.MIN_VALUE; /*
     * Don't default 0 to avoid
     * mistaken confirmations.
     */
    private boolean newPlayersAllowed = true;
    private ArrayList<NameAndPassword> players = new ArrayList<>();
    private ServerGameModel serverGameModel = new SimpleServerGameModel();

    /**
     *
     * @param gamesManager
     */
    public FreerailsGameServer(SavedGamesManager gamesManager) {
        this.savedGamesManager = gamesManager;
    }

    /**
     *
     * @param gamesManager
     * @return
     */
    public static FreerailsGameServer startServer(SavedGamesManager gamesManager) {
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

    /**
     *
     * @param connection
     */
    public synchronized void addConnection(Connection2Client connection) {
        String[] before = getPlayerNames();
        if (logger.isDebugEnabled()) {
            logger.debug("Adding connection..");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Waiting for login details..");
        }

        try {
            LogOnRequest request = (LogOnRequest) connection
                    .waitForObjectFromClient();
            if (logger.isDebugEnabled()) {
                logger
                        .debug("Trying to login player: "
                                + request.getUsername());
            }

            LogOnResponse response = this.logon(request);
            connection.writeToClient(response);
            connection.flush();
            NameAndPassword p = new NameAndPassword(request.getUsername(),
                    request.getPassword());
            if (response.isSuccessful()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Login successful");
                }

                synchronized (acceptedConnections) {
                    acceptedConnections.put(p, connection);
                }

                /* Just send to the new client. */
                Message2Client setMaps = new SetPropertyMessage2Client(
                        getNextClientCommandId(),
                        ClientControlInterface.ClientProperty.MAPS_AVAILABLE,
                        new ImStringList(savedGamesManager.getNewMapNames()));
                ImStringList savedGameNames = new ImStringList(
                        savedGamesManager.getSaveGameNames());
                Message2Client setSaveGames = new SetPropertyMessage2Client(
                        getNextClientCommandId(),
                        ClientControlInterface.ClientProperty.SAVED_GAMES,
                        savedGameNames);
                connection.writeToClient(setMaps);
                connection.writeToClient(setSaveGames);

                // no need to flush since it is done in
                // sendListOfConnectedPlayers2Clients()

                /*
                 * If there is a game in progress, we need to send the client a
                 * copy of the world object.
                 */
                if (null != serverGameModel && null != getWorld()) {
                    SetWorldMessage2Client command = new SetWorldMessage2Client(
                            confirmationID, getWorld());
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
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     *
     * @param l
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    /**
     *
     * @return
     */
    public synchronized int countOpenConnections() {
        Iterator<NameAndPassword> it = acceptedConnections.keySet().iterator();
        int numConnections = 0;

        while (it.hasNext()) {
            Connection2Client connection = acceptedConnections.get(it.next());

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

    /**
     *
     * @return
     */
    public String[] getPlayerNames() {
        String[] playerNames = new String[players.size()];

        for (int i = 0; i < players.size(); i++) {
            playerNames[i] = players.get(i).username;
        }

        return playerNames;
    }

    private World getWorld() {
        return serverGameModel.getWorld();
    }

    boolean isConfirmed(int player) {
        if (logger.isDebugEnabled()) {
            logger.debug("confirmedPlayers.size()=" + confirmedPlayers.size());
        }

        return confirmedPlayers.contains(players.get(player));
    }

    /**
     *
     * @return
     */
    public boolean isNewPlayersAllowed() {
        return newPlayersAllowed;
    }

    /**
     *
     * @param newPlayersAllowed
     */
    public void setNewPlayersAllowed(boolean newPlayersAllowed) {
        this.newPlayersAllowed = newPlayersAllowed;
    }

    private boolean isPlayer(String username) {
        for (NameAndPassword p : players) {
            if (p.username.equals(username))
                return true;
        }
        return false;
    }

    /**
     *
     * @param saveGameName
     * @throws IOException
     */
    public void loadgame(String saveGameName) throws IOException {
        logger.info("load game " + saveGameName);
        newPlayersAllowed = false;
        confirmedPlayers.clear();

        ServerGameModel loadedGame;

        loadedGame = (ServerGameModel) savedGamesManager.loadGame(saveGameName);
        String[] passwords = loadedGame.getPasswords();
        World w = loadedGame.getWorld();
        assert passwords.length == w.getNumberOfPlayers();
        ArrayList<NameAndPassword> newPlayers = new ArrayList<>();
        for (int i = 0; i < passwords.length; i++) {
            Player player = w.getPlayer(i);
            NameAndPassword nap = new NameAndPassword(player.getName(),
                    passwords[i]);
            newPlayers.add(nap);
        }
        /*
         * Remove any currently logged on players who are not participants in
         * the game we are loading.
         */
        for (NameAndPassword nap : players) {
            if (!newPlayers.contains(nap) && currentlyLoggedOn.contains(nap)) {
                removeConnection(nap);
            }
        }
        players = newPlayers;
        setServerGameModel(loadedGame);
        sendWorldUpdatedCommand();
    }

    /**
     *
     * @param player
     */
    public void logoff(int player) {
        NameAndPassword np = players.get(player);
        currentlyLoggedOn.remove(np);
    }

    /**
     *
     * @param lor
     * @return
     */
    public LogOnResponse logon(LogOnRequest lor) {
        NameAndPassword p = new NameAndPassword(lor.getUsername(), lor
                .getPassword());
        boolean isReturningPlayer = isPlayer(lor.getUsername());

        if (!this.newPlayersAllowed && !isReturningPlayer) {
            return LogOnResponse.rejected("New logins not allowed.");
        }

        if (currentlyLoggedOn.contains(p)) {
            return LogOnResponse.rejected("Already logged on.");
        }

        if (isReturningPlayer) {
            if (!players.contains(p)) {
                return LogOnResponse.rejected("Incorrect password.");
            }
        } else {
            players.add(p);
        }
        currentlyLoggedOn.add(p);
        return LogOnResponse.accepted(players.indexOf(p));
    }

    /**
     *
     * @param mapName
     * @param numAI
     */
    public void newGame(String mapName, int numAI) {
        for (int i = 0; i < numAI; ++i) {
            NameAndPassword aiPlayer = new NameAndPassword("AI" + i, null);

            players.add(aiPlayer);
        }
        this.newGame(mapName);
    }

    /**
     *
     * @param mapName
     */
    public void newGame(String mapName) {
        newPlayersAllowed = false;
        confirmedPlayers.clear();

        try {
            World world = (World) savedGamesManager.newMap(mapName);

            String[] passwords = new String[players.size()];

            /* Add players to world. */
            for (int i = 0; i < players.size(); i++) {
                String name = players.get(i).username;
                Player p = new Player(name, i);

                Move addPlayerMove = AddPlayerMove.generateMove(world, p);
                MoveStatus ms = addPlayerMove.doMove(world,
                        Player.AUTHORITATIVE);
                if (!ms.ok)
                    throw new IllegalStateException();
                passwords[i] = players.get(i).password;
            }

            serverGameModel.setWorld(world, passwords);
            setServerGameModel(serverGameModel);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        sendWorldUpdatedCommand();

        if (logger.isDebugEnabled()) {
            logger.debug("newGame");
        }
    }

    private void removeConnection(NameAndPassword p) throws IOException {
        String[] before = getPlayerNames();
        Connection2Client connection = acceptedConnections.get(p);

        /*
         * Fix for bug 1047439 Shutting down remote client crashes server We get
         * an IllegalStateException if we try to disconnect a connection that is
         * not open.
         */
        if (connection.isOpen()) {
            connection.disconnect();
        }

        this.currentlyLoggedOn.remove(p);

        String[] after = getPlayerNames();
        propertyChangeSupport.firePropertyChange("CONNECTED_PLAYERS", before,
                after);
    }

    /**
     *
     * @param l
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    public void run() {
        status.open();
        status.close();
    }

    /**
     *
     * @param saveGameName
     */
    public void savegame(String saveGameName) {
        logger.info("save game as " + saveGameName);
        try {
            savedGamesManager.saveGame(serverGameModel, saveGameName);
            String[] saves = savedGamesManager.getSaveGameNames();
            Message2Client request = new SetPropertyMessage2Client(
                    getNextClientCommandId(),
                    ClientControlInterface.ClientProperty.SAVED_GAMES,
                    new ImStringList(saves));
            send2All(request);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void send2All(FreerailsSerializable message) {
        send2AllExcept(null, message);
    }

    /**
     * Sends the specified message to all connections except the specified one.
     */
    private void send2AllExcept(Connection2Client dontSend2,
                                FreerailsSerializable message) {

        for (NameAndPassword p : acceptedConnections.keySet()) {
            Connection2Client connection = acceptedConnections.get(p);

            if (dontSend2 != connection) {
                try {
                    connection.writeToClient(message);
                    connection.flush();
                } catch (Exception e) {
                    if (connection.isOpen()) {
                        e.printStackTrace();

                        try {
                            removeConnection(p);
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

        Message2Client request = new SetPropertyMessage2Client(
                getNextClientCommandId(),
                ClientControlInterface.ClientProperty.CONNECTED_CLIENTS,
                new ImStringList(playerNames));

        send2All(request);
    }

    private void sendWorldUpdatedCommand() {
        /* Send the world to the clients. */
        confirmationID = getNextClientCommandId();

        SetWorldMessage2Client command = new SetWorldMessage2Client(
                confirmationID, getWorld());

        send2All(command);
    }

    /**
     *
     * @param serverGameModel
     */
    public void setServerGameModel(ServerGameModel serverGameModel) {
        this.serverGameModel = serverGameModel;

        MoveReceiver moveExecuter = new MoveReceiver() {
            public void processMove(Move move) {
                MoveStatus ms = move.doMove(getWorld(), Player.AUTHORITATIVE);

                if (ms.ok) {
                    send2All(move);
                } else {
                    logger.warn(ms.message);
                }
            }
        };

        serverGameModel.init(moveExecuter);
    }

    /**
     *
     */
    public void stop() {
        // TODO Auto-generated method stub
    }

    /**
     *
     */
    public void stopGame() {
        logger.info("Stop game.");
    }

    /**
     * Updates the game model, then reads and deals with the outstanding
     * messages from each of the connected clients. This method is synchronized
     * to prevent moves being sent out while addConnection(.) is executing.
     */
    public synchronized void update() {
        if (null != serverGameModel) {
            serverGameModel.update();
        }

        try {
            for (NameAndPassword player : acceptedConnections.keySet()) {
                Connection2Client connection = acceptedConnections.get(player);

                if (connection.isOpen()) {
                    FreerailsSerializable[] messages = connection
                            .readFromClient();

                    for (FreerailsSerializable message : messages) {
                        if (message instanceof Message2Server) {
                            Message2Server message2 = (Message2Server) message;
                            MessageStatus cStatus = message2.execute(this);
                            if (logger.isDebugEnabled()) {
                                logger.debug(message2.toString());
                            }
                            connection.writeToClient(cStatus);
                        } else if (message instanceof MessageStatus) {
                            MessageStatus messageStatus = (MessageStatus) message;

                            if (messageStatus.getId() == this.confirmationID) {
                                /*
                                 * The client is confirming that they have
                                 * updated their world object to the current
                                 * version.
                                 */
                                this.confirmedPlayers.add(player);
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Confirmed player " + player);
                                }
                            }

                            if (logger.isDebugEnabled()) {
                                logger.debug(message.toString());
                            }
                        } else if (message instanceof Move
                                || message instanceof PreMove) {
                            Player player2 = getWorld().getPlayer(
                                    players.indexOf(player));
                            FreerailsPrincipal principal = player2
                                    .getPrincipal();

                            Move move;
                            boolean isMove = message instanceof Move;

                            if (isMove) {
                                move = (Move) message;
                            } else {
                                PreMove pm = (PreMove) message;
                                move = pm.generateMove(getWorld());
                            }

                            MoveStatus mStatus = move.tryDoMove(
                                    this.getWorld(), principal);

                            if (mStatus.isOk()) {
                                move.doMove(getWorld(), principal);

                                /*
                                 * We don't send the move to the client that
                                 * submitted it.
                                 */
                                send2AllExcept(connection, move);
                            }

                            if (isMove) {
                                connection.writeToClient(mStatus);
                            } else {
                                connection.writeToClient(PreMoveStatus
                                        .fromMoveStatus(mStatus));
                            }
                        } else {
                            if (logger.isDebugEnabled()) {
                                logger.debug(message.toString());
                            }
                        }
                    }

                    connection.flush();
                } else {
                    /* Remove connection. */
                    this.removeConnection(player);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    public void refreshSavedGames() {
        Message2Client setMaps = new SetPropertyMessage2Client(
                getNextClientCommandId(),
                ClientControlInterface.ClientProperty.MAPS_AVAILABLE,
                new ImStringList(savedGamesManager.getNewMapNames()));
        ImStringList savedGameNames = new ImStringList(savedGamesManager
                .getSaveGameNames());
        Message2Client setSaveGames = new SetPropertyMessage2Client(
                getNextClientCommandId(),
                ClientControlInterface.ClientProperty.SAVED_GAMES,
                savedGameNames);
        send2All(setMaps);
        send2All(setSaveGames);
    }
}