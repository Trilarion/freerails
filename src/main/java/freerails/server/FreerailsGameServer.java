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
import java.util.concurrent.CountDownLatch;

/**
 * When executed by a thread, this class does the following: reads and executes
 * moves and commands received from connected clients; sends moves and commands
 * to connected clients.
 *
 * <p>Players are identified by their player id, which is their index in the
 * list of registered players and is returned to the client in
 * {@link LogOnResponse#id()}. Passwords are only checked in
 * {@link #logon(LogOnCredentials)}. All mutable state is guarded by this
 * server's monitor.
 */
public class FreerailsGameServer implements ServerControlInterface, GameServer, Runnable {

    private static final TaggedLogger logger = Logger.tag("FreerailsGameServer");
    /**
     * Used as a property name for property change events.
     */
    public static final String CONNECTED_PLAYERS = "CONNECTED_PLAYERS";
    // Ids of the players who are currently logged on.
    private final SortedSet<Integer> loggedOn = new TreeSet<>();
    // Client connections of logged-on players, keyed by player id.
    private final SortedMap<Integer, Connection> connections = new TreeMap<>();
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private final SaveGamesManager saveGamesManager;
    private final ScenarioSource scenarioSource;
    private final CountDownLatch status = new CountDownLatch(1);
    // Registered players; the index is the player id.
    private List<LogOnCredentials> players = new ArrayList<>();
    private ServerGameModel serverGameModel;
    private final MoveReceiver serverMoveReceiver = this::processServerMove;

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
        String[] before = getConnectedPlayerNames();
        logger.debug("Adding connection..");
        logger.debug("Waiting for login details..");

        try {
            LogOnCredentials request = (LogOnCredentials) connection.receiveObject();
            logger.debug("Trying to login player: " + request.username());

            LogOnResponse response = logon(request);
            if (response.success()) {
                connections.put(response.id(), connection);
            }
            connection.sendObject(response);
            if (response.success()) {
                logger.debug("Login successful");

                // Just send to the new client.
                Serializable setMaps = new SetPropertyCommandToClient(ClientProperty.MAPS_AVAILABLE, new ArrayList<>(Arrays.asList(scenarioSource.getAvailableScenarioNames())));
                Serializable setSaveGames = new SetPropertyCommandToClient(ClientProperty.SAVED_GAMES, new ArrayList<>(Arrays.asList(saveGamesManager.getSaveGameNames())));

                connection.sendObject(setMaps);
                connection.sendObject(setSaveGames);

                /*
                 * If there is a game in progress, we need to send the client a
                 * copy of the world object.
                 */
                if (isGameStarted()) {
                    Serializable command = new SetWorldCommandToClient(serverGameModel.getWorld());
                    connection.sendObject(command);
                }

                // Send to all clients.
                sendListOfConnectedPlayers2Clients();

                propertyChangeSupport.firePropertyChange(CONNECTED_PLAYERS, before, getConnectedPlayerNames());
            } else {
                connection.close();
            }
        } catch (IOException e) {
            logger.warn(e, "Could not complete client login.");
            removeConnection(connection);
        } catch (IllegalStateException e) {
            logger.debug("Client disconnected before completing login.", e);
            removeConnection(connection);
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
        int numberOpenConnections = 0;
        for (Connection connection : connections.values()) {
            if (connection.isOpen()) {
                numberOpenConnections++;
            }
        }
        return numberOpenConnections;
    }

    /**
     * Returns the usernames of all players registered for the current game,
     * whether or not they are currently logged on.
     *
     * @return the player usernames, ordered by player id
     */
    public synchronized String[] getPlayerNames() {
        String[] playerNames = new String[players.size()];

        for (int i = 0; i < players.size(); i++) {
            playerNames[i] = players.get(i).username();
        }

        return playerNames;
    }

    /**
     * Returns the usernames of the players who are currently logged on.
     *
     * @return the logged-on player usernames, ordered by player id
     */
    public synchronized String[] getConnectedPlayerNames() {
        return loggedOn.stream()
                .map(id -> players.get(id).username())
                .toArray(String[]::new);
    }

    /**
     * Returns whether a game is in progress. New players may only join before
     * a game starts: {@link #newGame(String)} and {@link #loadGame(String)} fix
     * the player list into the world, so later joiners would have no player.
     *
     * @return true if a game world is installed
     */
    private boolean isGameStarted() {
        return serverGameModel != null && serverGameModel.getWorld() != null;
    }

    /**
     * Returns the id of the registered player with the specified username.
     *
     * @param username the username
     * @return the player id, or -1 if no such player is registered
     */
    private int findPlayerId(String username) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).username().equals(username)) return i;
        }
        return -1;
    }

    /**
     * Loads a saved game. Logged-on players who take part in the saved game
     * keep their connection under their player id in that game; all others
     * are disconnected.
     *
     * @param saveGameName the name of the saved game to load
     * @throws IOException if the operation cannot complete because of an input/output error
     */
    @Override
    public synchronized void loadGame(String saveGameName) throws IOException {
        logger.info("load game " + saveGameName);
        String[] before = getConnectedPlayerNames();

        SavedGame savedGame = saveGamesManager.loadGame(saveGameName);
        List<LogOnCredentials> newPlayers = new ArrayList<>(savedGame.credentials());

        SortedSet<Integer> newLoggedOn = new TreeSet<>();
        SortedMap<Integer, Connection> newConnections = new TreeMap<>();
        for (int oldId : loggedOn) {
            int newId = newPlayers.indexOf(players.get(oldId));
            Connection connection = connections.get(oldId);
            if (newId < 0) {
                // Not a participant in the loaded game.
                if (connection != null && connection.isOpen()) {
                    connection.close();
                }
            } else {
                newLoggedOn.add(newId);
                if (connection != null) {
                    newConnections.put(newId, connection);
                }
            }
        }
        loggedOn.clear();
        loggedOn.addAll(newLoggedOn);
        connections.clear();
        connections.putAll(newConnections);
        players = newPlayers;

        serverGameModel.setWorld(savedGame.world());
        sendWorldUpdatedCommand();
        sendListOfConnectedPlayers2Clients();
        propertyChangeSupport.firePropertyChange(CONNECTED_PLAYERS, before, getConnectedPlayerNames());
    }

    /**
     * Logs off the player with the specified id and closes their connection.
     *
     * @param player the id of the player to log off
     */
    public synchronized void logoff(int player) {
        disconnectPlayer(player);
    }

    /**
     * Authenticates credentials and, on success, marks the player as logged on.
     *
     * @param credentials the credentials to authenticate
     * @return the login response
     */
    public synchronized LogOnResponse logon(LogOnCredentials credentials) {
        int id = findPlayerId(credentials.username());

        if (id < 0) {
            if (isGameStarted()) {
                return LogOnResponse.failure("New logins not allowed.");
            }
            players.add(credentials);
            id = players.size() - 1;
        } else {
            if (!players.get(id).password().equals(credentials.password())) {
                return LogOnResponse.failure("Incorrect password.");
            }
            if (loggedOn.contains(id)) {
                return LogOnResponse.failure("Already logged on.");
            }
        }
        loggedOn.add(id);
        return LogOnResponse.success(id);
    }

    /**
     * Creates a new game from the specified scenario map.
     *
     * @param mapName the name of the scenario map to load
     */
    @Override
    public synchronized void newGame(String mapName) {
        World world;
        try {
            ScenarioData scenario = scenarioSource.loadScenario(mapName);
            world = ScenarioWorldInitializer.createWorld(scenario);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not load scenario: " + mapName, e);
        }

        // Add players to world.
        for (int i = 0; i < players.size(); i++) {
            String name = players.get(i).username();
            Player player = new Player(i, name);

            // add a new player
            Move addPlayerMove = new AddPlayerMove(player);
            addPlayerMove.apply(world);
        }

        serverGameModel.setWorld(world);

        sendWorldUpdatedCommand();
        logger.info("Started scenario: " + mapName);
    }

    /**
     * Logs off the player with the specified id, closes their connection and
     * informs the remaining clients and listeners.
     *
     * @param player the player id
     */
    private void disconnectPlayer(int player) {
        String[] before = getConnectedPlayerNames();
        Connection connection = connections.remove(player);
        boolean wasLoggedOn = loggedOn.remove(player);
        if (connection == null && !wasLoggedOn) {
            return;
        }

        /*
         * Fix for bug 1047439 Shutting down remote client crashes server We get
         * an IllegalStateException if we try to disconnect a connection that is
         * not open.
         */
        if (connection != null && connection.isOpen()) {
            connection.close();
        }

        sendListOfConnectedPlayers2Clients();
        propertyChangeSupport.firePropertyChange(CONNECTED_PLAYERS, before, getConnectedPlayerNames());
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
    public synchronized void saveGame(String saveGameName) {
        logger.info("save game as " + saveGameName);

        try {
            saveGamesManager.saveGame(saveGameName, new SavedGame(serverGameModel.getWorld(), players));
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
     * Connections that fail are removed after the message has been sent to the
     * others.
     */
    private void sendToAllExcept(Connection donotSend2, Serializable message) {
        List<Integer> failed = new ArrayList<>();

        for (Map.Entry<Integer, Connection> entry : connections.entrySet()) {
            Connection connection = entry.getValue();

            if (donotSend2 != connection) {
                try {
                    connection.sendObject(message);
                } catch (Exception e) {
                    failed.add(entry.getKey());
                }
            }
        }

        for (int player : failed) {
            disconnectPlayer(player);
        }
    }

    /**
     * Sends the list of logged-on players to all clients.
     */
    private void sendListOfConnectedPlayers2Clients() {
        Serializable request = new SetPropertyCommandToClient(ClientProperty.CONNECTED_CLIENTS, new ArrayList<>(Arrays.asList(getConnectedPlayerNames())));

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

    /**
     * Installs the server-side model.
     *
     * @param serverGameModel the server-side model to install
     */
    public synchronized void setServerGameModel(ServerGameModel serverGameModel) {
        this.serverGameModel = serverGameModel;
    }

    /**
     * Applies a move generated by the server game model to the world and sends it to all clients.
     *
     * @param move the move generated by the server game model
     */
    private void processServerMove(Move move) {
        Status status = move.applicable(serverGameModel.getWorld());

        if (status.isSuccess()) {
            move.apply(serverGameModel.getWorld());
            sendToAll(move);
        } else {
            logger.warn(status.getMessage());
        }
    }

    /**
     * Updates the game model, then reads and deals with the outstanding
     * messages from each of the connected clients. This method is synchronized
     * to prevent moves being sent out while addConnection(.) is executing.
     */
    public synchronized void update() {
        if (null != serverGameModel) {
            serverGameModel.update(serverMoveReceiver);
        }

        // Iterate over a snapshot: commands such as loading a game and failed
        // sends may change the connections while messages are processed.
        for (Connection connection : List.copyOf(connections.values())) {
            try {
                if (connection.isOpen()) {
                    processMessages(connection);
                } else {
                    IOException readerFailure = connection.getReaderFailure();
                    if (readerFailure != null) {
                        logger.warn(readerFailure, "Client connection closed after a reader failure.");
                    }
                    removeConnection(connection);
                }
            } catch (IOException e) {
                logger.warn(e, "Could not process client connection.");
            }
        }
    }

    /**
     * Removes the specified connection and logs its player off. A connection
     * that is not registered is just closed.
     *
     * @param connection the connection to remove
     */
    private void removeConnection(Connection connection) {
        for (Map.Entry<Integer, Connection> entry : connections.entrySet()) {
            if (entry.getValue() == connection) {
                disconnectPlayer(entry.getKey());
                return;
            }
        }
        if (connection.isOpen()) {
            connection.close();
        }
    }

    /**
     * Reads and executes the outstanding commands and moves from one client.
     *
     * @param connection the client connection
     * @throws IOException if a reply cannot be sent
     */
    private void processMessages(Connection connection) throws IOException {
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
    }

    /**
     * Sends the current scenario and saved-game lists to all clients.
     */
    @Override
    public synchronized void refreshSavedGames() {
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
