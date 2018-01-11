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

package freerails.network;

import freerails.controller.*;
import freerails.move.AddPlayerMove;
import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.server.ServerGameModel;
import freerails.server.SimpleServerGameModel;
import freerails.util.ImmutableList;
import freerails.util.SynchronizedFlag;
import freerails.world.World;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.player.Player;
import org.apache.log4j.Logger;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * When executed by a thread, this class does the following: reads and executes
 * moves and commands received from connected clients; sends moves and commands
 * to connected clients.
 *
 * @see InetConnectionAccepter
 * @see ConnectionToClient
 */
public class FreerailsGameServer implements ServerControlInterface, GameServer,
        Runnable {

    /**
     * Used as a property name for property change events.
     */
    public static final String CONNECTED_PLAYERS = "CONNECTED_PLAYERS";

    private static final Logger logger = Logger
            .getLogger(FreerailsGameServer.class.getName());
    private final Map<NameAndPassword, ConnectionToClient> acceptedConnections = new HashMap<>();
    /**
     * The players who have confirmed that they have received the last copy of
     * the world object sent.
     */
    private final HashSet<NameAndPassword> confirmedPlayers = new HashSet<>();
    /* Contains the user names of the players who are currently logged on. */
    private final Collection<NameAndPassword> currentlyLoggedOn = new HashSet<>();
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
            this);
    private final SaveGamesManager saveGamesManager;
    private final SynchronizedFlag status = new SynchronizedFlag(false);
    private int commandID = 0;
    /**
     * ID of the last SetWorldMessageToClient sent out. Used to keep track of
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
     * @param gamesManager
     */
    public FreerailsGameServer(SaveGamesManager gamesManager) {
        saveGamesManager = gamesManager;
    }

    /**
     * @param gamesManager
     * @return
     */
    public static FreerailsGameServer startServer(SaveGamesManager gamesManager) {
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
            throw new IllegalStateException();
        }
    }

    /**
     * @param connection
     */
    public synchronized void addConnection(ConnectionToClient connection) {
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

            LogOnResponse response = logon(request);
            connection.writeToClient(response);
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
                Serializable setMaps = new SetPropertyMessageToClient(
                        getNextClientCommandId(),
                        ClientControlInterface.ClientProperty.MAPS_AVAILABLE,
                        new ImmutableList<>(saveGamesManager.getNewMapNames()));

                ImmutableList<String> savedGameNames = new ImmutableList<>(
                        saveGamesManager.getSaveGameNames());
                Serializable setSaveGames = new SetPropertyMessageToClient(
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
                    Serializable command = new SetWorldMessageToClient(
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
        }
    }

    /**
     * @param l
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    /**
     * @return
     */
    public synchronized int countOpenConnections() {
        Iterator<NameAndPassword> it = acceptedConnections.keySet().iterator();
        int numConnections = 0;

        while (it.hasNext()) {
            ConnectionToClient connection = acceptedConnections.get(it.next());

            if (connection.isOpen()) {
                numConnections++;
            }
        }

        return numConnections;
    }

    World getCopyOfWorld() {
        return getWorld().defensiveCopy();
    }

    private int getNextClientCommandId() {
        return commandID++;
    }

    /**
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
     * @return
     */
    public boolean isNewPlayersAllowed() {
        return newPlayersAllowed;
    }

    /**
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
     * @param saveGameName
     * @throws IOException
     */
    public void loadgame(String saveGameName) throws IOException {
        logger.info("load game " + saveGameName);
        newPlayersAllowed = false;
        confirmedPlayers.clear();

        ServerGameModel loadedGame;

        loadedGame = (ServerGameModel) saveGamesManager.loadGame(saveGameName);
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
     * @param player
     */
    public void logoff(int player) {
        NameAndPassword np = players.get(player);
        currentlyLoggedOn.remove(np);
    }

    /**
     * @param lor
     * @return
     */
    public LogOnResponse logon(LogOnRequest lor) {
        NameAndPassword p = new NameAndPassword(lor.getUsername(), lor
                .getPassword());
        boolean isReturningPlayer = isPlayer(lor.getUsername());

        if (!newPlayersAllowed && !isReturningPlayer) {
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
     * @param mapName
     */
    public void newGame(String mapName) {
        newPlayersAllowed = false;
        confirmedPlayers.clear();

        try {
            World world = (World) saveGamesManager.newMap(mapName);

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
        }

        sendWorldUpdatedCommand();

        if (logger.isDebugEnabled()) {
            logger.debug("newGame");
        }
    }

    private void removeConnection(NameAndPassword p) throws IOException {
        String[] before = getPlayerNames();
        ConnectionToClient connection = acceptedConnections.get(p);

        /*
         * Fix for bug 1047439 Shutting down remote client crashes server We get
         * an IllegalStateException if we try to disconnect a connection that is
         * not open.
         */
        if (connection.isOpen()) {
            connection.disconnect();
        }

        currentlyLoggedOn.remove(p);

        String[] after = getPlayerNames();
        propertyChangeSupport.firePropertyChange("CONNECTED_PLAYERS", before,
                after);
    }

    public void run() {
        status.open();
        status.close();
    }

    /**
     * @param saveGameName
     */
    public void savegame(String saveGameName) {
        logger.info("save game as " + saveGameName);
        try {
            saveGamesManager.saveGame(serverGameModel, saveGameName);
            String[] saves = saveGamesManager.getSaveGameNames();
            Serializable request = new SetPropertyMessageToClient(
                    getNextClientCommandId(),
                    ClientControlInterface.ClientProperty.SAVED_GAMES,
                    new ImmutableList<>(saves));

            send2All(request);
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
    }

    private void send2All(Serializable message) {
        send2AllExcept(null, message);
    }

    /**
     * Sends the specified message to all connections except the specified one.
     */
    private void send2AllExcept(ConnectionToClient dontSend2,
                                Serializable message) {

        for (NameAndPassword p : acceptedConnections.keySet()) {
            ConnectionToClient connection = acceptedConnections.get(p);

            if (dontSend2 != connection) {
                try {
                    connection.writeToClient(message);
                } catch (Exception e) {
                    if (connection.isOpen()) {

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

    private void sendListOfConnectedPlayers2Clients() {
        /* Send the client the list of players. */
        String[] playerNames = getPlayerNames();

        Serializable request = new SetPropertyMessageToClient(
                getNextClientCommandId(),
                ClientControlInterface.ClientProperty.CONNECTED_CLIENTS,
                new ImmutableList<>(playerNames));

        send2All(request);
    }

    private void sendWorldUpdatedCommand() {
        /* Send the world to the clients. */
        confirmationID = getNextClientCommandId();

        Serializable command = new SetWorldMessageToClient(
                confirmationID, getWorld());

        send2All(command);
    }

    /**
     * @param serverGameModel
     */
    public void setServerGameModel(ServerGameModel serverGameModel) {
        this.serverGameModel = serverGameModel;

        MoveReceiver moveExecuter = move -> {
            MoveStatus ms = move.doMove(getWorld(), Player.AUTHORITATIVE);

            if (ms.ok) {
                send2All(move);
            } else {
                logger.warn(ms.message);
            }
        };

        serverGameModel.initialize(moveExecuter);
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
                ConnectionToClient connection = acceptedConnections.get(player);

                if (connection.isOpen()) {
                    Serializable[] messages = connection
                            .readFromClient();

                    for (Serializable message : messages) {
                        if (message instanceof MessageToServer) {
                            MessageToServer message2 = (MessageToServer) message;
                            MessageStatus cStatus = message2.execute(this);
                            if (logger.isDebugEnabled()) {
                                logger.debug(message2.toString());
                            }
                            connection.writeToClient(cStatus);
                        } else if (message instanceof MessageStatus) {
                            MessageStatus messageStatus = (MessageStatus) message;

                            if (messageStatus.getId() == confirmationID) {
                                /*
                                 * The client is confirming that they have
                                 * updated their world object to the current
                                 * version.
                                 */
                                confirmedPlayers.add(player);
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
                                    getWorld(), principal);

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

                } else {
                    /* Remove connection. */
                    removeConnection(player);
                }
            }
        } catch (IOException e) {
        }
    }

    /**
     *
     */
    public void refreshSavedGames() {
        Serializable setMaps = new SetPropertyMessageToClient(
                getNextClientCommandId(),
                ClientControlInterface.ClientProperty.MAPS_AVAILABLE,
                new ImmutableList<>(saveGamesManager.getNewMapNames()));
        ImmutableList<String> savedGameNames = new ImmutableList<>(saveGamesManager
                .getSaveGameNames());
        Serializable setSaveGames = new SetPropertyMessageToClient(
                getNextClientCommandId(),
                ClientControlInterface.ClientProperty.SAVED_GAMES,
                savedGameNames);
        send2All(setMaps);
        send2All(setSaveGames);
    }
}