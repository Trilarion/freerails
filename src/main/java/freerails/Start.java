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

package freerails;

import freerails.client.ClientConstants;
import freerails.client.launcher.LauncherFrame;
import freerails.client.launcher.LauncherServices;
import freerails.io.savegame.FullSaveGameManager;
import freerails.io.scenario.ClasspathScenarioSource;
import freerails.server.FreerailsGameServer;
import freerails.server.FullServerGameModel;
import freerails.util.network.Connection;
import freerails.util.network.ServerSocketAcceptor;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import java.awt.EventQueue;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * Composes the desktop client and game server.
 */
public final class Start {

    private static final TaggedLogger logger = Logger.tag("Start");

    enum Role {
        BOTH,
        CLIENT,
        SERVER
    }

    private Start() {}

    public static void main(String[] args) {
        Role role = parseRole(args);
        if (!ClientConstants.FREERAILS_USER_FOLDER.exists()
                && !ClientConstants.FREERAILS_USER_FOLDER.mkdirs()) {
            throw new IllegalStateException("Could not create user data folder: "
                    + ClientConstants.FREERAILS_USER_FOLDER);
        }
        if (ClientConstants.OPTIONS_FILE.exists()) {
            Options.load(ClientConstants.OPTIONS_FILE);
        }
        LauncherServices services = null;
        if (role != Role.CLIENT) {
            try {
                services = new LocalServerServices();
            } catch (IOException e) {
                throw new IllegalStateException("Could not start the local server.", e);
            }
        }

        LauncherServices finalServices = services;
        EventQueue.invokeLater(() -> {
            Integer forcedMode = switch (role) {
                case BOTH -> null;
                case CLIENT -> ClientConstants.MODE_JOIN_NETWORK_GAME;
                case SERVER -> ClientConstants.MODE_SERVER_ONLY;
            };
            LauncherFrame frame = new LauncherFrame(finalServices, forcedMode, role == Role.SERVER);
            frame.setLocationByPlatform(true);
            frame.setVisible(true);
            Options.save(ClientConstants.OPTIONS_FILE);
        });
    }

    static Role parseRole(String[] args) {
        Objects.requireNonNull(args, "args");
        if (args.length == 0 || (args.length == 1 && "--both".equals(args[0]))) {
            return Role.BOTH;
        }
        if (args.length == 1 && "--client".equals(args[0])) {
            return Role.CLIENT;
        }
        if (args.length == 1 && "--server".equals(args[0])) {
            return Role.SERVER;
        }
        throw new IllegalArgumentException("Usage: freerails [--both|--client|--server]");
    }

    private static final class LocalServerServices implements LauncherServices {

        private final FreerailsGameServer server;
        private final CopyOnWriteArrayList<Consumer<String[]>> playerListeners = new CopyOnWriteArrayList<>();
        private final BlockingQueue<Socket> sockets = new LinkedBlockingQueue<>();
        private ServerSocketAcceptor acceptor;
        private int listeningPort = -1;

        private LocalServerServices() throws IOException {
            server = new FreerailsGameServer(new ClasspathScenarioSource(), new FullSaveGameManager());
            server.setServerGameModel(new FullServerGameModel());
            server.addPropertyChangeListener(event -> notifyPlayerListeners());
            startServerUpdates();
            startConnectionDispatcher();
            startHosting(Options.PORT);
        }

        private void startServerUpdates() {
            Thread serverThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    long startTime = System.currentTimeMillis();
                    server.update();
                    long elapsed = System.currentTimeMillis() - startTime;
                    if (elapsed < ClientConstants.SERVERUPDATE) {
                        try {
                            Thread.sleep(ClientConstants.SERVERUPDATE - elapsed);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }, "FreerailsGameServer");
            serverThread.setDaemon(true);
            serverThread.start();
        }

        private void startConnectionDispatcher() {
            Thread connectionThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Socket socket = sockets.take();
                        try {
                            server.addConnection(Connection.make(socket));
                        } catch (IOException e) {
                            logger.warn(e, "Could not establish an accepted client connection.");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "Server Connection Dispatcher");
            connectionThread.setDaemon(true);
            connectionThread.start();
        }

        @Override
        public synchronized int startHosting(int port) throws IOException {
            if (acceptor != null && listeningPort == port && acceptor.isOpen()) {
                return acceptor.getLocalPort();
            }
            if (acceptor != null) {
                acceptor.close();
            }
            acceptor = new ServerSocketAcceptor(
                    new InetSocketAddress(InetAddress.getLoopbackAddress(), port), sockets);
            listeningPort = port;
            return acceptor.getLocalPort();
        }

        @Override
        public void loadGame(String saveGameName) throws IOException {
            server.loadGame(saveGameName);
            notifyPlayerListeners();
        }

        @Override
        public void startServerGame(String scenarioName) {
            server.newGame(scenarioName);
            notifyPlayerListeners();
        }

        @Override
        public String[] getPlayerNames() {
            return server.getPlayerNames();
        }

        @Override
        public void addConnectedPlayersListener(Consumer<String[]> listener) {
            playerListeners.add(Objects.requireNonNull(listener, "listener"));
            listener.accept(getPlayerNames());
        }

        private void notifyPlayerListeners() {
            String[] playerNames = getPlayerNames();
            for (Consumer<String[]> listener : playerListeners) {
                EventQueue.invokeLater(() -> listener.accept(playerNames));
            }
        }
    }
}
