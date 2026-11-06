/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.server;

import freerails.move.AddPlayerMove;
import freerails.move.Move;
import freerails.move.MoveReceiver;
import freerails.move.Status;
import freerails.move.TryMoveStatus;
import freerails.move.generator.MoveGenerator;
import freerails.model.player.Player;
import freerails.model.scenario.ScenarioData;
import freerails.model.scenario.ScenarioWorldInitializer;
import freerails.model.world.World;
import freerails.network.*;
import freerails.network.command.*;
import freerails.util.network.Connection;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * When executed by a thread, this class does the following: reads and executes
 * moves and commands received from connected clients; sends moves and commands
 * to connected clients.
 */
public class FreerailsGameServer implements ServerControlInterface, GameServer, GameModel, Runnable {

    private static final TaggedLogger logger = Logger.tag("FreerailsGameServer");
    /**
     * Used as a property name for property change events.
     */
    public static final String CONNECTED_PLAYERS = "CONNECTED_PLAYERS";
    // TODO give new connections an ID and use it as identification
    private final Map<LogOnCredentials, Connection> acceptedConnections = new ConcurrentHashMap<>();
    // Contains the user names of the players who are currently logged on.
    private final Collection<LogOnCredentials> currentlyLoggedOn = new HashSet<>();
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private final SaveGamesManager saveGamesManager;
    private final ScenarioSource scenarioSource;
    private final CountDownLatch status = new CountDownLatch(1);
    // TODO new players allowed used meaningfully
    private boolean newPlayersAllowed = true;
    private ArrayList<LogOnCredentials> players = new ArrayList<>();
    private ServerGameModel serverGameModel;

    /**
     * Creates a server that uses the supplied scenario source and save-game manager.
     *
     * @param scenarioSource the source used to list and load scenarios
     * @param saveGamesManager the manager used to load and save games
     */
    public FreerailsGameServer(ScenarioSource scenarioSource, SaveGamesManager saveGamesManager) {
        this.scenarioSource = Objects.requireNonNull(scenarioSource, "scenarioSource");
        this.saveGamesManager = Objects.requireNonNull(saveGamesManager, "saveGamesManager");
    }

    /**
     * Handles a new client connection, including authentication and initial state delivery.
     *
     * @param connection the client connection to add to the server
     */
    @Override
    public synchronized void addConnection(Connection connection) {
        String[] before = getPlayerNames();
        logger.debug("Adding connection..");
        logger.debug("Waiting for login details..");

        try {
            LogOnCredentials request = (LogOnCredentials) connection.receiveObject();
            logger.debug("Trying to login player: " + request.username());

            LogOnResponse response = logon(request);
            connection.sendObject(response);
            LogOnCredentials p = new LogOnCredentials(request.username(), request.password());
            if (response.success()) {
                logger.debug("Login successful");

                synchronized (acceptedConnections) {
                    acceptedConnections.put(p, connection);
                }

                // Just send to the new client.
                Serializable setMaps = new SetPropertyCommandToClient(ClientProperty.MAPS_AVAILABLE, new ArrayList<>(Arrays.asList(scenarioSource.getAvailableScenarioNames())));
                Serializable setSaveGames = new SetPropertyCommandToClient(ClientProperty.SAVED_GAMES, new ArrayList<>(Arrays.asList(saveGamesManager.getSaveGameNames())));

                connection.sendObject(setMaps);
                connection.sendObject(setSaveGames);

                // no need to flush since it is done in
                // sendListOfConnectedPlayers2Clients()

                /*
                 * If there is a game in progress, we need to send the client a
                 * copy of the world object.
                 */
                if (null != serverGameModel && null != serverGameModel.getWorld()) {
                    Serializable command = new SetWorldCommandToClient(serverGameModel.getWorld());
                    connection.sendObject(command);
                }

                // Send to all clients.
                sendListOfConnectedPlayers2Clients();

                String[] after = getPlayerNames();
                propertyChangeSupport.firePropertyChange("CONNECTED_PLAYERS", before, after);
            } else {
                connection.close();
            }
        } catch (IOException e) {
            logger.warn(e, "Could not complete client login.");
            connection.close();
        } catch (IllegalStateException e) {
            logger.debug("Client disconnected before completing login.", e);
            connection.close();
        }
    }

    /**
     * Registers a listener for server property changes.
     *
     * @param listener the property-change listener to register
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Returns the number of currently open client connections.
     *
     * @return the number of open connections
     */
    @Override
    public synchronized int getNumberOpenConnections() {
        Iterator<LogOnCredentials> it = acceptedConnections.keySet().iterator();
        int numberOpenConnections = 0;

        while (it.hasNext()) {
            Connection connection = acceptedConnections.get(it.next());
            if (connection.isOpen()) {
                numberOpenConnections++;
            }
        }

        return numberOpenConnections;
    }

    /**
     * Returns the usernames of the players configured for the current game.
     *
     * @return the player usernames
     */
    public String[] getPlayerNames() {
        String[] playerNames = new String[players.size()];

        for (int i = 0; i < players.size(); i++) {
            playerNames[i] = players.get(i).username();
        }

        return playerNames;
    }

    /**
     * Sets whether new players may join the current game.
     *
     * @param newPlayersAllowed whether new players may join the game
     */
    public void setNewPlayersAllowed(boolean newPlayersAllowed) {
        this.newPlayersAllowed = newPlayersAllowed;
    }

    /**
     * Returns whether a player with the specified username is registered.
     *
     * @param username the username
     * @return true if player, otherwise false
     */
    private boolean isPlayer(String username) {
        for (LogOnCredentials p : players) {
            if (p.username().equals(username)) return true;
        }
        return false;
    }

    /**
     * @param saveGameName the name of the saved game to load or save
     * @throws IOException if the operation cannot complete because of an input/output error
     */
    @Override
    public synchronized void loadGame(String saveGameName) throws IOException {
        logger.info("load game " + saveGameName);
        newPlayersAllowed = false;

        ServerGameModel serverGameModel = saveGamesManager.loadGame(saveGameName);
        String[] passwords = serverGameModel.getPasswords();
        World world = serverGameModel.getWorld();
        assert passwords.length == world.getPlayers().size();
        ArrayList<LogOnCredentials> newPlayers = new ArrayList<>();
        for (int i = 0; i < passwords.length; i++) {
            Player player = world.getPlayer(i);
            LogOnCredentials nap = new LogOnCredentials(player.getName(), passwords[i]);
            newPlayers.add(nap);
        }
        /*
         * Remove any currently logged on players who are not participants in
         * the game we are loading.
         */
        for (LogOnCredentials nap : players) {
            if (!newPlayers.contains(nap) && currentlyLoggedOn.contains(nap)) {
                removeConnection(nap);
            }
        }
        players = newPlayers;
        setServerGameModel(serverGameModel);
        sendWorldUpdatedCommand();
    }

    /**
     * Removes the player at the specified index from the logged-in set.
     *
     * @param player the player who owns or performs the operation
     */
    public void logoff(int player) {
        LogOnCredentials np = players.get(player);
        currentlyLoggedOn.remove(np);
    }

    /**
     * Authenticates credentials and returns the login result.
     *
     * @param credentials the credentials to authenticate
     * @return the login response
     */
    public LogOnResponse logon(LogOnCredentials credentials) {
        boolean isReturningPlayer = isPlayer(credentials.username());

        if (!newPlayersAllowed && !isReturningPlayer) {
            return new LogOnResponse(false, "New logins not allowed.");
        }

        if (currentlyLoggedOn.contains(credentials)) {
            return new LogOnResponse(false, "Already logged on.");
        }

        if (isReturningPlayer) {
            if (!players.contains(credentials)) {
                return new LogOnResponse(false, "Incorrect password.");
            }
        } else {
            players.add(credentials);
        }
        currentlyLoggedOn.add(credentials);
        // return accepted LogOnResponse
        return new LogOnResponse(true, players.indexOf(credentials));
    }

    /**
     * Creates a new game from the specified scenario map.
     *
     * @param mapName the name of the scenario map to load
     */
    @Override
    public synchronized void newGame(String mapName) {
        newPlayersAllowed = false;

        World world;
        try {
            ScenarioData scenario = scenarioSource.loadScenario(mapName);
            world = ScenarioWorldInitializer.createWorld(scenario);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not load scenario: " + mapName, e);
        }

        String[] passwords = new String[players.size()];

        // Add players to world.
        for (int i = 0; i < players.size(); i++) {
            String name = players.get(i).username();
            Player player = new Player(i, name);

            // add a new player
            Move addPlayerMove = new AddPlayerMove(player);
            addPlayerMove.apply(world);
            passwords[i] = players.get(i).password();
        }

        serverGameModel.setWorld(world, passwords);
        setServerGameModel(serverGameModel);

        sendWorldUpdatedCommand();
        logger.info("Started scenario: " + mapName);
    }

    /**
     * Removes the remove connection from this freerails game server.
     *
     * @param p the p
     */
    private void removeConnection(LogOnCredentials p) {
        String[] before = getPlayerNames();
        Connection connection = acceptedConnections.remove(p);
        if (connection == null) {
            return;
        }

        /*
         * Fix for bug 1047439 Shutting down remote client crashes server We get
         * an IllegalStateException if we try to disconnect a connection that is
         * not open.
         */
        if (connection.isOpen()) {
            connection.close();
        }

        currentlyLoggedOn.remove(p);

        String[] after = getPlayerNames();
        propertyChangeSupport.firePropertyChange("CONNECTED_PLAYERS", before, after);
    }

    /**
     * Performs the run operation.
     */
    @Override
    public void run() {
        status.countDown();
    }

    /**
     * Saves the current server game model under the supplied name.
     *
     * @param saveGameName the name of the saved game to load or save
     */
    @Override
    public void saveGame(String saveGameName) {
        logger.info("save game as " + saveGameName);

        try {
            saveGamesManager.saveGame(saveGameName, serverGameModel);
        } catch (IOException e) {
            logger.error(e, "Could not save game as " + saveGameName);
        }
        Serializable request = new SetPropertyCommandToClient(ClientProperty.SAVED_GAMES, new ArrayList<>(Arrays.asList(saveGamesManager.getSaveGameNames())));
        sendToAll(request);
    }

    /**
     * Performs send to all operation.
     *
     * @param message the message
     */
    private void sendToAll(Serializable message) {
        sendToAllExcept(null, message);
    }

    /**
     * Sends the specified message to all connections except the specified one.
     */
    private void sendToAllExcept(Connection donotSend2, Serializable message) {

        for (LogOnCredentials p : acceptedConnections.keySet()) {
            Connection connection = acceptedConnections.get(p);

            if (donotSend2 != connection) {
                try {
                    connection.sendObject(message);
                } catch (Exception e) {
                    if (connection.isOpen()) {

                        removeConnection(p);
                    }
                }
            }
        }
    }

    /**
     * Performs the send list of connected players2 clients operation.
     */
    private void sendListOfConnectedPlayers2Clients() {
        // Send the client the list of players.

        Serializable request = new SetPropertyCommandToClient(ClientProperty.CONNECTED_CLIENTS, new ArrayList<>(Arrays.asList(getPlayerNames())));

        sendToAll(request);
    }

    /**
     * Performs the send world updated command operation.
     */
    private void sendWorldUpdatedCommand() {
        // Send the world to the clients.

        Serializable command = new SetWorldCommandToClient(serverGameModel.getWorld());

        sendToAll(command);
    }

    // TODO this is very convoluted with ServerGameModel and MoveReceiver cyclically referencing each other
    /**
     * Installs the server-side model and connects its move receiver to this server.
     *
     * @param serverGameModel the server-side model to install
     */
    public void setServerGameModel(ServerGameModel serverGameModel) {
        this.serverGameModel = serverGameModel;

        MoveReceiver moveReceiver = move -> {
            Status status = move.applicable(this.serverGameModel.getWorld());

            if (status.isSuccess()) {
                move.apply(this.serverGameModel.getWorld());
                sendToAll(move);
            } else {
                logger.warn(status.getMessage());
            }
        };

        serverGameModel.initialize(moveReceiver);
    }

    /**
     * Updates the game model, then reads and deals with the outstanding
     * messages from each of the connected clients. This method is synchronized
     * to prevent moves being sent out while addConnection(.) is executing.
     */
    @Override
    public synchronized void update() {
        if (null != serverGameModel) {
            serverGameModel.update();
        }

        try {
            for (LogOnCredentials credentials : acceptedConnections.keySet()) {
                Connection connection = acceptedConnections.get(credentials);

                if (connection.isOpen()) {
                    List<Serializable> messages = connection.getReceivedObjects();

                    for (Serializable message : messages) {
                        if (message instanceof CommandToServer command) {
                            CommandStatus cStatus = command.execute(this);
                            logger.debug(command.toString());
                            connection.sendObject(cStatus);
                        } else if (message instanceof Move || message instanceof MoveGenerator) {
                            Move move;
                            boolean isMove = message instanceof Move;

                            if (isMove) {
                                move = (Move) message;
                            } else {
                                MoveGenerator moveGenerator = (MoveGenerator) message;
                                move = moveGenerator.generate(serverGameModel.getWorld());
                            }

                            Status mStatus = move.applicable(serverGameModel.getWorld());

                            if (mStatus.isSuccess()) {
                                move.apply(serverGameModel.getWorld());

                                /*
                                 * We don't send the move to the client that
                                 * submitted it.
                                 */
                                sendToAllExcept(connection, move);
                            }

                            if (isMove) {
                                connection.sendObject(mStatus);
                            } else {
                                connection.sendObject(TryMoveStatus.fromMoveStatus(mStatus));
                            }
                        } else {
                            logger.debug(message.toString());
                        }
                    }
                } else {
                    IOException readerFailure = connection.getReaderFailure();
                    if (readerFailure != null) {
                        logger.warn(readerFailure, "Client connection closed after a reader failure.");
                    }
                    // Remove connection.
                    removeConnection(credentials);
                }
            }
        } catch (IOException e) {
            logger.warn(e, "Could not process client connection.");
        }
    }

    /**
     * Coordinates client connections, game sessions, and the server-side game model.
     */
    @Override
    public void refreshSavedGames() {
        Serializable setMaps = new SetPropertyCommandToClient(ClientProperty.MAPS_AVAILABLE, new ArrayList<>(Arrays.asList(scenarioSource.getAvailableScenarioNames())));
        Serializable setSaveGames = new SetPropertyCommandToClient(ClientProperty.SAVED_GAMES, new ArrayList<>(Arrays.asList(saveGamesManager.getSaveGameNames())));
        sendToAll(setMaps);
        sendToAll(setSaveGames);
    }

    /**
     * Returns the status.
     *
     * @return the status
     */
    public CountDownLatch getStatus() {
        return status;
    }
}