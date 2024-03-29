/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
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
import freerails.move.Status;
import freerails.move.TryMoveStatus;
import freerails.move.generator.MoveGenerator;
import freerails.network.*;
import freerails.network.command.*;
import freerails.scenario.MapCreator;
import freerails.scenario.SaveGamesManager;

import freerails.model.world.World;
import freerails.model.player.Player;
import freerails.util.network.Connection;
import org.apache.log4j.Logger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * When executed by a thread, this class does the following: reads and executes
 * moves and commands received from connected clients; sends moves and commands
 * to connected clients.
 */
public class FreerailsGameServer implements ServerControlInterface, GameServer, GameModel, Runnable {

    private static final Logger logger = Logger.getLogger(FreerailsGameServer.class.getName());
    /**
     * Used as a property name for property change events.
     */
    public static final String CONNECTED_PLAYERS = "CONNECTED_PLAYERS";
    // TODO give new connections an ID and use it as identification
    private final Map<LogOnCredentials, Connection> acceptedConnections = new HashMap<>();
    // Contains the user names of the players who are currently logged on.
    private final Collection<LogOnCredentials> currentlyLoggedOn = new HashSet<>();
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private final SaveGamesManager saveGamesManager;
    private final CountDownLatch status = new CountDownLatch(1);
    // TODO new players allowed used meaningfully
    private boolean newPlayersAllowed = true;
    private ArrayList<LogOnCredentials> players = new ArrayList<>();
    private ServerGameModel serverGameModel;

    /**
     * @param saveGamesManager
     */
    public FreerailsGameServer(SaveGamesManager saveGamesManager) {
        this.saveGamesManager = saveGamesManager;
    }

    /**
     * @param connection
     */
    @Override
    public synchronized void addConnection(Connection connection) {
        String[] before = getPlayerNames();
        logger.debug("Adding connection..");
        logger.debug("Waiting for login details..");

        try {
            LogOnCredentials request = (LogOnCredentials) connection.receiveObject();
            logger.debug("Trying to login player: " + request.getUsername());

            LogOnResponse response = logon(request);
            connection.sendObject(response);
            LogOnCredentials p = new LogOnCredentials(request.getUsername(), request.getPassword());
            if (response.isSuccess()) {
                logger.debug("Login successful");

                synchronized (acceptedConnections) {
                    acceptedConnections.put(p, connection);
                }

                // Just send to the new client.
                Serializable setMaps = new SetPropertyCommandToClient(ClientProperty.MAPS_AVAILABLE, new ArrayList<>(Arrays.asList(MapCreator.getAvailableMapNames())));
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
        } catch (IOException e) {}
    }

    /**
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * @return
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
     * @return
     */
    public String[] getPlayerNames() {
        String[] playerNames = new String[players.size()];

        for (int i = 0; i < players.size(); i++) {
            playerNames[i] = players.get(i).getUsername();
        }

        return playerNames;
    }

    /**
     * @param newPlayersAllowed
     */
    public void setNewPlayersAllowed(boolean newPlayersAllowed) {
        this.newPlayersAllowed = newPlayersAllowed;
    }

    private boolean isPlayer(String username) {
        for (LogOnCredentials p : players) {
            if (p.getUsername().equals(username)) return true;
        }
        return false;
    }

    /**
     * @param saveGameName
     * @throws IOException
     */
    @Override
    public void loadGame(String saveGameName) throws IOException {
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
     * @param player
     */
    public void logoff(int player) {
        LogOnCredentials np = players.get(player);
        currentlyLoggedOn.remove(np);
    }

    /**
     * @param credentials
     * @return
     */
    public LogOnResponse logon(LogOnCredentials credentials) {
        boolean isReturningPlayer = isPlayer(credentials.getUsername());

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
     * @param mapName
     */
    @Override
    public void newGame(String mapName) {
        newPlayersAllowed = false;

        World world = null;
        try {
            world = MapCreator.newMap(mapName);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }

        String[] passwords = new String[players.size()];

        // Add players to world.
        for (int i = 0; i < players.size(); i++) {
            String name = players.get(i).getUsername();
            Player player = new Player(i, name);

            // add a new player
            Move addPlayerMove = new AddPlayerMove(player);
            addPlayerMove.apply(world);
            passwords[i] = players.get(i).getPassword();
        }

        serverGameModel.setWorld(world, passwords);
        setServerGameModel(serverGameModel);

        sendWorldUpdatedCommand();
        logger.debug("newGame");
    }

    private void removeConnection(LogOnCredentials p) {
        String[] before = getPlayerNames();
        Connection connection = acceptedConnections.get(p);

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

    @Override
    public void run() {
        status.countDown();
    }

    /**
     * @param saveGameName
     */
    @Override
    public void saveGame(String saveGameName) {
        logger.info("save game as " + saveGameName);

        try {
            saveGamesManager.saveGame(saveGameName, serverGameModel);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Serializable request = new SetPropertyCommandToClient(ClientProperty.SAVED_GAMES, new ArrayList<>(Arrays.asList(saveGamesManager.getSaveGameNames())));
        sendToAll(request);
    }

    private void sendToAll(Serializable message) {
        sendToAllExcept(null, message);
    }

    /**
     * Sends the specified message to all connections except the specified one.
     */
    private void sendToAllExcept(Connection dontSend2, Serializable message) {

        for (LogOnCredentials p : acceptedConnections.keySet()) {
            Connection connection = acceptedConnections.get(p);

            if (dontSend2 != connection) {
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

    private void sendListOfConnectedPlayers2Clients() {
        // Send the client the list of players.

        Serializable request = new SetPropertyCommandToClient(ClientProperty.CONNECTED_CLIENTS, new ArrayList<>(Arrays.asList(getPlayerNames())));

        sendToAll(request);
    }

    private void sendWorldUpdatedCommand() {
        // Send the world to the clients.

        Serializable command = new SetWorldCommandToClient(serverGameModel.getWorld());

        sendToAll(command);
    }

    // TODO this is very convoluted with ServerGameModel and MoveReceiver cyclically referencing each other
    /**
     * @param serverGameModel
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
                        if (message instanceof CommandToServer) {
                            CommandToServer command = (CommandToServer) message;
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
                    // Remove connection.
                    removeConnection(credentials);
                }
            }
        } catch (IOException e) {
        }
    }

    /**
     *
     */
    @Override
    public void refreshSavedGames() {
        Serializable setMaps = new SetPropertyCommandToClient(ClientProperty.MAPS_AVAILABLE, new ArrayList<>(Arrays.asList(MapCreator.getAvailableMapNames())));
        Serializable setSaveGames = new SetPropertyCommandToClient(ClientProperty.SAVED_GAMES, new ArrayList<>(Arrays.asList(saveGamesManager.getSaveGameNames())));
        sendToAll(setMaps);
        sendToAll(setSaveGames);
    }

    public CountDownLatch getStatus() {
        return status;
    }
}