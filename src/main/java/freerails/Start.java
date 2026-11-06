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
import freerails.client.launcher.JavaFxLauncher;
import freerails.client.launcher.LauncherFrame;
import freerails.client.launcher.LauncherServices;
import freerails.io.savegame.FullSaveGameManager;
import freerails.io.scenario.ClasspathScenarioSource;
import freerails.server.FreerailsGameServer;
import freerails.server.FullServerGameModel;
import freerails.util.network.Connection;
import freerails.util.network.ServerSocketAcceptor;
import org.tinylog.Logger;

import java.awt.EventQueue;
import java.io.File;
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
 * Composes the desktop client and optional local game server.
 */
public final class Start {

    /** Selects which application roles are started. */
    enum Role {
        BOTH,
        CLIENT,
        SERVER
    }

    /** Selects which desktop UI implementation is started. */
    enum Ui {
        SWING,
        JAVAFX
    }

    /** Holds the parsed application role and UI selection. */
    record Arguments(Role role, Ui ui) {}

    /** Prevents construction of this application entry point. */
    private Start() {}

    /**
     * Starts the selected application roles and displays the launcher.
     *
     * @param args command-line role and UI selection
     */
    public static void main(String[] args) {
        Arguments arguments = parseArguments(args);
        Role role = arguments.role();
        // Prepare the user's settings before starting the selected services.
        if (!ClientConstants.FREERAILS_USER_FOLDER.exists()
                && !ClientConstants.FREERAILS_USER_FOLDER.mkdirs()) {
            throw new IllegalStateException("Could not create user data folder: "
                    + ClientConstants.FREERAILS_USER_FOLDER);
        }
        System.setProperty("tinylog.writerFile.file",
                new File(ClientConstants.FREERAILS_USER_FOLDER, "freerails.log").getAbsolutePath());
        if (ClientConstants.OPTIONS_FILE.exists()) {
            Options.load(ClientConstants.OPTIONS_FILE);
        }
        LauncherServices services = null;
        if (role != Role.CLIENT) {
            try {
                services = new LocalServerServices(role);
            } catch (IOException e) {
                throw new IllegalStateException("Could not start the local server.", e);
            }
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (role != Role.CLIENT) {
                Logger.tag("Start").info("Server stopped.");
            }
            if (role != Role.SERVER) {
                Logger.tag("Start").info("Client stopped.");
            }
        }, "FreeRails shutdown logger"));

        LauncherServices finalServices = services;
        Integer forcedMode = switch (role) {
            case BOTH -> null;
            case CLIENT -> ClientConstants.MODE_JOIN_NETWORK_GAME;
            case SERVER -> ClientConstants.MODE_SERVER_ONLY;
        };
        boolean allowServerOnly = role == Role.SERVER;
        if (arguments.ui() == Ui.JAVAFX) {
            JavaFxLauncher.launch(
                    finalServices,
                    forcedMode,
                    allowServerOnly,
                    () -> EventQueue.invokeLater(() -> {
                        showSwingLauncher(finalServices, forcedMode, allowServerOnly);
                        Options.save(ClientConstants.OPTIONS_FILE);
                    }),
                    () -> {
                        if (role != Role.SERVER) {
                            Logger.tag("Start").info("Client started.");
                        }
                        Options.save(ClientConstants.OPTIONS_FILE);
                    },
                    () -> Options.save(ClientConstants.OPTIONS_FILE));
            return;
        }
        EventQueue.invokeLater(() -> {
            showSwingLauncher(finalServices, forcedMode, allowServerOnly);
            if (role != Role.SERVER) {
                Logger.tag("Start").info("Client started.");
            }
            Options.save(ClientConstants.OPTIONS_FILE);
        });
    }

    /**
     * Creates and displays the existing Swing launcher.
     *
     * @param services application services supplied to the launcher
     * @param forcedMode the launcher mode required by the selected role, or {@code null}
     * @param allowServerOnly whether the server-only launcher mode is available
     */
    private static void showSwingLauncher(
            LauncherServices services, Integer forcedMode, boolean allowServerOnly) {
        LauncherFrame frame = new LauncherFrame(services, forcedMode, allowServerOnly);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }

    /**
     * Parses the command-line application role and UI selection.
     *
     * @param args command-line arguments
     * @return the selected application role and UI
     * @throws NullPointerException if {@code args} is null
     * @throws IllegalArgumentException if an argument is unknown or roles conflict
     */
    static Arguments parseArguments(String[] args) {
        Objects.requireNonNull(args, "args");
        Role role = null;
        Ui ui = Ui.SWING;
        for (String arg : args) {
            if ("--javafx".equals(arg)) {
                if (ui == Ui.JAVAFX) {
                    throw new IllegalArgumentException(usage());
                }
                ui = Ui.JAVAFX;
                continue;
            }
            Role requestedRole = switch (arg) {
                case "--both" -> Role.BOTH;
                case "--client" -> Role.CLIENT;
                case "--server" -> Role.SERVER;
                default -> throw new IllegalArgumentException(usage());
            };
            if (role != null) {
                throw new IllegalArgumentException(usage());
            }
            role = requestedRole;
        }
        return new Arguments(role == null ? Role.BOTH : role, ui);
    }

    /**
     * Parses the command-line application role, retaining the legacy role-only helper.
     *
     * @param args command-line arguments
     * @return the selected application role
     */
    static Role parseRole(String[] args) {
        return parseArguments(args).role();
    }

    /** Returns the command-line usage string. */
    private static String usage() {
        return "Usage: freerails [--javafx] [--both|--client|--server]";
    }

    /**
     * Selects the interface address used by the local server for the application role.
     *
     * @param role application role
     * @param port requested listening port
     * @return loopback for local-host games, or a wildcard address for server-only games
     */
    static InetSocketAddress serverSocketAddress(Role role, int port) {
        return role == Role.SERVER
                ? new InetSocketAddress(port)
                : new InetSocketAddress(InetAddress.getLoopbackAddress(), port);
    }

    /** Provides the local server operations used by the launcher. */
    private static final class LocalServerServices implements LauncherServices {

        private final Role role;
        private final FreerailsGameServer server;
        private final CopyOnWriteArrayList<Consumer<String[]>> playerListeners = new CopyOnWriteArrayList<>();
        private final BlockingQueue<Socket> sockets = new LinkedBlockingQueue<>();
        private ServerSocketAcceptor acceptor;
        private int listeningPort = -1;

        /**
         * Creates the local server and starts its update, connection, and acceptor threads.
         *
         * @throws IOException if the local server cannot bind its listening socket
         */
        private LocalServerServices(Role role) throws IOException {
            this.role = Objects.requireNonNull(role, "role");
            server = new FreerailsGameServer(new ClasspathScenarioSource(), new FullSaveGameManager());
            server.setServerGameModel(new FullServerGameModel());
            server.addPropertyChangeListener(event -> notifyPlayerListeners());
            startServerUpdates();
            startConnectionDispatcher();
            startHosting(Options.PORT);
        }

        /** Runs server updates at the configured interval on a daemon thread. */
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

        /** Dispatches accepted sockets to the game server on a daemon thread. */
        private void startConnectionDispatcher() {
            Thread connectionThread = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Socket socket = sockets.take();
                        try {
                            server.addConnection(Connection.make(socket));
                        } catch (IOException e) {
                            Logger.tag("Start").warn(e, "Could not establish an accepted client connection.");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "Server Connection Dispatcher");
            connectionThread.setDaemon(true);
            connectionThread.start();
        }

        /**
         * Starts or reuses the local server socket on the requested port.
         *
         * @param port the requested listening port, or zero to choose an available port
         * @return the port actually bound
         * @throws IOException if the server socket cannot be opened
         */
        @Override
        public synchronized int startHosting(int port) throws IOException {
            if (acceptor != null && listeningPort == port && acceptor.isOpen()) {
                return acceptor.getLocalPort();
            }
            if (acceptor != null) {
                acceptor.close();
            }
            acceptor = new ServerSocketAcceptor(serverSocketAddress(role, port), sockets);
            listeningPort = port;
            int actualPort = acceptor.getLocalPort();
            Logger.tag("Start").info("Server started on port " + actualPort + ".");
            return actualPort;
        }

        /**
         * Loads a saved game and refreshes connected-player listeners.
         *
         * @param saveGameName the saved game to load
         * @throws IOException if the saved game cannot be loaded
         */
        @Override
        public void loadGame(String saveGameName) throws IOException {
            server.loadGame(saveGameName);
            notifyPlayerListeners();
        }

        /**
         * Starts a scenario on the local server and refreshes connected-player listeners.
         *
         * @param scenarioName the scenario to start
         */
        @Override
        public void startServerGame(String scenarioName) {
            server.newGame(scenarioName);
            notifyPlayerListeners();
        }

        /**
         * Returns the names of players currently connected to the local server.
         *
         * @return connected player names
         */
        @Override
        public String[] getPlayerNames() {
            return server.getPlayerNames();
        }

        /**
         * Registers a listener and immediately supplies the current player names.
         *
         * @param listener receives connected-player updates
         */
        @Override
        public void addConnectedPlayersListener(Consumer<String[]> listener) {
            playerListeners.add(Objects.requireNonNull(listener, "listener"));
            listener.accept(getPlayerNames());
        }

        /** Sends the current player names to registered listeners on the event queue. */
        private void notifyPlayerListeners() {
            String[] playerNames = getPlayerNames();
            for (Consumer<String[]> listener : playerListeners) {
                EventQueue.invokeLater(() -> listener.accept(playerNames));
            }
        }
    }
}
