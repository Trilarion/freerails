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

package freerails.client.launcher;

import freerails.Options;
import freerails.client.ClientConstants;
import freerails.io.savegame.FullSaveGameManager;
import freerails.io.scenario.ClasspathScenarioSource;
import freerails.network.LogOnResponse;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hosts the JavaFX setup wizard and hands completed games to the existing Swing gameplay window.
 */
public final class JavaFxLauncher extends Application {

    private static LaunchRequest launchRequest;

    private final ExecutorService backgroundExecutor =
            Executors.newSingleThreadExecutor(task -> {
                Thread thread = new Thread(task, "JavaFX launcher operations");
                thread.setDaemon(true);
                return thread;
            });

    private Stage stage;
    private final StackPane pageContainer = new StackPane();
    private final Label statusLabel = new Label();
    private final Button backButton = new Button("Back...");
    private final Button nextButton = new Button("Next...");
    private final Button swingButton = new Button("Use Swing launcher");
    private final ToggleGroup gameTypeGroup = new ToggleGroup();
    private final RadioButton startNetworkButton = new RadioButton("Start a network game");
    private final RadioButton joinNetworkButton = new RadioButton("Join a network game");
    private final RadioButton serverOnlyButton = new RadioButton("Server only");
    private final ListView<String> scenarioList = new ListView<>();
    private final ListView<String> saveGameList = new ListView<>();
    private final TextField serverPortField = new TextField();
    private final TextField playerNameField = new TextField();
    private final ComboBox<String> savedPlayerNames = new ComboBox<>();
    private final TextField remoteHostField = new TextField();
    private final TextField remotePortField = new TextField();
    private final RadioButton windowedButton = new RadioButton("Windowed");
    private final RadioButton fullScreenButton = new RadioButton("Full screen");
    private final ListView<String> connectedPlayersList = new ListView<>();

    private Node gameTypePage;
    private Node mapSelectionPage;
    private Node clientOptionsPage;
    private Node connectedPlayersPage;
    private VBox remoteServerBox;
    private Step currentStep = Step.GAME_TYPE;
    private String[] allowedPlayerNames;
    private LauncherClient client;
    private boolean busy;
    private boolean serverOnlyReady;
    private boolean serverRunning;
    private MessageKind currentStatusKind;

    /** Prevents construction outside the JavaFX application lifecycle. */
    public JavaFxLauncher() {}

    /**
     * Starts JavaFX with launcher services, role restrictions, and lifecycle callbacks.
     *
     * @param services application services supplied to the launcher, or {@code null} for a client-only role
     * @param forcedMode the launcher mode required by the selected role, or {@code null}
     * @param allowServerOnly whether the server-only launcher mode is available
     * @param swingFallback displays the Swing launcher when selected
     * @param onStarted runs after the JavaFX launcher is displayed
     * @param onStopped runs when the JavaFX application shuts down
     */
    public static void launch(
            LauncherServices services,
            Integer forcedMode,
            boolean allowServerOnly,
            Runnable swingFallback,
            Runnable onStarted,
            Runnable onStopped) {
        launchRequest = new LaunchRequest(
                services,
                forcedMode,
                allowServerOnly,
                Objects.requireNonNull(swingFallback, "swingFallback"),
                Objects.requireNonNull(onStarted, "onStarted"),
                Objects.requireNonNull(onStopped, "onStopped"));
        Application.launch(JavaFxLauncher.class);
    }

    /**
     * Creates and displays the JavaFX launcher wizard.
     *
     * @param primaryStage the primary JavaFX window
     */
    @Override
    public void start(Stage primaryStage) {
        LaunchRequest request = Objects.requireNonNull(launchRequest, "launchRequest");
        stage = primaryStage;
        configureControls(request);
        buildPages();

        statusLabel.setWrapText(true);
        statusLabel.setMinHeight(26);
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(statusLabel, Priority.ALWAYS);

        backButton.setOnAction(event -> goBack());
        nextButton.setOnAction(event -> goNext());
        swingButton.setOnAction(event -> {
            request.swingFallback().run();
            stage.close();
        });

        HBox navigation = new HBox(8, backButton, swingButton, nextButton);
        navigation.setAlignment(Pos.CENTER_RIGHT);
        BorderPane root = new BorderPane();
        root.setTop(statusLabel);
        root.setCenter(pageContainer);
        root.setBottom(navigation);
        BorderPane.setMargin(statusLabel, new Insets(10, 12, 4, 12));
        BorderPane.setMargin(pageContainer, new Insets(8, 12, 8, 12));
        BorderPane.setMargin(navigation, new Insets(4, 12, 12, 12));

        stage.setTitle("Freerails Launcher");
        stage.setMinWidth(600);
        stage.setMinHeight(450);
        stage.setScene(new Scene(root, 650, 500));
        showStep(Step.GAME_TYPE);
        stage.show();

        if (request.services() != null) {
            request.services().addConnectedPlayersListener(
                    names -> Platform.runLater(() -> showConnectedPlayers(names)));
        }
        request.onStarted().run();
    }

    /**
     * Shuts down background operations and persists launcher options.
     */
    @Override
    public void stop() {
        backgroundExecutor.shutdownNow();
        if (launchRequest != null) {
            launchRequest.onStopped().run();
        }
    }

    /**
     * Configures role-specific choices and restores saved user options.
     *
     * @param request the active launcher request
     */
    private void configureControls(LaunchRequest request) {
        startNetworkButton.setToggleGroup(gameTypeGroup);
        joinNetworkButton.setToggleGroup(gameTypeGroup);
        serverOnlyButton.setToggleGroup(gameTypeGroup);
        startNetworkButton.setSelected(true);
        serverOnlyButton.setVisible(request.allowServerOnly());
        serverOnlyButton.setManaged(request.allowServerOnly());
        if (request.forcedMode() != null) {
            startNetworkButton.setSelected(request.forcedMode() == ClientConstants.MODE_START_NETWORK_GAME);
            joinNetworkButton.setSelected(request.forcedMode() == ClientConstants.MODE_JOIN_NETWORK_GAME);
            serverOnlyButton.setSelected(request.forcedMode() == ClientConstants.MODE_SERVER_ONLY);
            startNetworkButton.setDisable(request.forcedMode() != ClientConstants.MODE_START_NETWORK_GAME);
            joinNetworkButton.setDisable(request.forcedMode() != ClientConstants.MODE_JOIN_NETWORK_GAME);
            serverOnlyButton.setDisable(request.forcedMode() != ClientConstants.MODE_SERVER_ONLY);
        }

        scenarioList.getItems().setAll(new ClasspathScenarioSource().getAvailableScenarioNames());
        saveGameList.getItems().setAll(new FullSaveGameManager().getSaveGameNames());
        if (saveGameList.getItems().isEmpty()) {
            if (!scenarioList.getItems().isEmpty()) {
                scenarioList.getSelectionModel().selectFirst();
            }
        } else {
            saveGameList.getSelectionModel().selectFirst();
        }
        scenarioList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                saveGameList.getSelectionModel().clearSelection();
            }
            refreshNextButton();
        });
        saveGameList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                scenarioList.getSelectionModel().clearSelection();
            }
            refreshNextButton();
        });
        serverPortField.setText(String.valueOf(Options.PORT));
        serverPortField.setPromptText("Port (0-65535)");
        serverPortField.textProperty().addListener((observable, oldValue, newValue) -> {
            persistOptions();
            refreshNextButton();
        });

        playerNameField.setText(Options.NAME);
        remoteHostField.setText(Options.IP);
        remotePortField.setText(String.valueOf(Options.PORT));
        remoteHostField.textProperty().addListener((observable, oldValue, newValue) -> {
            persistOptions();
            refreshNextButton();
        });
        remotePortField.textProperty().addListener((observable, oldValue, newValue) -> {
            persistOptions();
            refreshNextButton();
        });
        playerNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            persistOptions();
            refreshNextButton();
        });
        savedPlayerNames.valueProperty().addListener((observable, oldValue, newValue) -> {
            persistOptions();
            refreshNextButton();
        });
        updatePlayerNameChoices();

        ToggleGroup screenModeGroup = new ToggleGroup();
        windowedButton.setToggleGroup(screenModeGroup);
        fullScreenButton.setToggleGroup(screenModeGroup);
        boolean fullscreen = Boolean.TRUE.equals(Options.MAINWINDOW_FULLSCREEN);
        windowedButton.setSelected(!fullscreen);
        fullScreenButton.setSelected(fullscreen);
        windowedButton.selectedProperty().addListener((observable, oldValue, newValue) -> persistOptions());
        fullScreenButton.selectedProperty().addListener((observable, oldValue, newValue) -> persistOptions());
    }

    /**
     * Creates all wizard pages and wires their shared controls.
     */
    private void buildPages() {
        gameTypePage = buildGameTypePage();
        mapSelectionPage = buildMapSelectionPage();
        clientOptionsPage = buildClientOptionsPage();
        connectedPlayersPage = buildConnectedPlayersPage();
    }

    /**
     * Creates the launcher role selection page.
     *
     * @return the game type page
     */
    private Node buildGameTypePage() {
        VBox page = new VBox(12,
                new Label("Select Game Type"),
                startNetworkButton,
                joinNetworkButton,
                serverOnlyButton);
        page.setPadding(new Insets(16));
        page.setAlignment(Pos.TOP_LEFT);
        return page;
    }

    /**
     * Creates scenario, save-game, and host-port controls.
     *
     * @return the map selection page
     */
    private Node buildMapSelectionPage() {
        scenarioList.setPlaceholder(new Label("No scenarios are available."));
        saveGameList.setPlaceholder(new Label("No saved games are available."));
        VBox newGame = titledList("New Game", scenarioList);
        VBox loadGame = titledList("Load game", saveGameList);
        HBox choices = new HBox(12, newGame, loadGame);
        HBox.setHgrow(newGame, Priority.ALWAYS);
        HBox.setHgrow(loadGame, Priority.ALWAYS);

        HBox serverPort = new HBox(8, new Label("Server port:"), serverPortField);
        serverPort.setAlignment(Pos.CENTER_LEFT);
        VBox page = new VBox(12, choices, serverPort);
        page.setPadding(new Insets(8));
        VBox.setVgrow(choices, Priority.ALWAYS);
        return page;
    }

    /**
     * Creates a titled list section.
     *
     * @param title the section title
     * @param list the list control
     * @return the titled section
     */
    private static VBox titledList(String title, ListView<String> list) {
        Label heading = new Label(title);
        heading.setStyle("-fx-font-weight: bold;");
        VBox section = new VBox(6, heading, list);
        section.setPadding(new Insets(8));
        section.setStyle("-fx-border-color: derive(-fx-base, -25%); -fx-border-radius: 3;");
        VBox.setVgrow(list, Priority.ALWAYS);
        return section;
    }

    /**
     * Creates player, remote-server, and display-mode controls.
     *
     * @return the client options page
     */
    private Node buildClientOptionsPage() {
        Label playerLabel = new Label("Player name:");
        GridPane playerRow = new GridPane();
        playerRow.setHgap(8);
        playerRow.add(playerLabel, 0, 0);
        playerRow.add(playerNameField, 1, 0);
        playerRow.add(savedPlayerNames, 1, 0);
        GridPane.setHgrow(playerNameField, Priority.ALWAYS);
        GridPane.setHgrow(savedPlayerNames, Priority.ALWAYS);

        VBox playerDetails = titledBox("Player Details", playerRow);
        GridPane remoteGrid = new GridPane();
        remoteGrid.setHgap(8);
        remoteGrid.setVgap(8);
        remoteGrid.add(new Label("Host name:"), 0, 0);
        remoteGrid.add(remoteHostField, 1, 0);
        remoteGrid.add(new Label("Port:"), 0, 1);
        remoteGrid.add(remotePortField, 1, 1);
        GridPane.setHgrow(remoteHostField, Priority.ALWAYS);
        remoteServerBox = titledBox("Remote server address", remoteGrid);

        VBox screenMode = titledBox("Screen Mode", new VBox(6, windowedButton, fullScreenButton));
        VBox page = new VBox(12, playerDetails, remoteServerBox, screenMode);
        page.setPadding(new Insets(8));
        return page;
    }

    /**
     * Creates a titled control group.
     *
     * @param title the group title
     * @param content the group contents
     * @return the titled group
     */
    private static VBox titledBox(String title, Node content) {
        Label heading = new Label(title);
        heading.setStyle("-fx-font-weight: bold;");
        VBox section = new VBox(8, heading, content);
        section.setPadding(new Insets(8));
        section.setStyle("-fx-border-color: derive(-fx-base, -25%); -fx-border-radius: 3;");
        return section;
    }

    /**
     * Creates the live connected-player display.
     *
     * @return the connected players page
     */
    private Node buildConnectedPlayersPage() {
        Label heading = new Label("Connected Players");
        heading.setStyle("-fx-font-weight: bold;");
        connectedPlayersList.setPlaceholder(new Label("No players are connected."));
        VBox page = new VBox(10, heading, connectedPlayersList);
        page.setPadding(new Insets(16));
        VBox.setVgrow(connectedPlayersList, Priority.ALWAYS);
        return page;
    }

    /**
     * Advances through the selected launcher workflow.
     */
    private void goNext() {
        clearStatus();
        switch (currentStep) {
            case GAME_TYPE -> {
                if (selectedMode() == ClientConstants.MODE_JOIN_NETWORK_GAME) {
                    showStep(Step.CLIENT_OPTIONS);
                } else {
                    showStep(Step.MAP_SELECTION);
                }
            }
            case MAP_SELECTION -> mapSelectionNext();
            case CLIENT_OPTIONS -> clientOptionsNext();
            case CONNECTED_PLAYERS -> connectedPlayersNext();
        }
    }

    /**
     * Navigates to the preceding applicable setup page.
     */
    private void goBack() {
        clearStatus();
        switch (currentStep) {
            case MAP_SELECTION -> showStep(Step.GAME_TYPE);
            case CLIENT_OPTIONS -> {
                if (selectedMode() == ClientConstants.MODE_JOIN_NETWORK_GAME) {
                    showStep(Step.GAME_TYPE);
                } else {
                    showStep(Step.MAP_SELECTION);
                }
            }
            case GAME_TYPE, CONNECTED_PLAYERS -> {
                // There is no preceding step for these pages.
            }
        }
    }

    /**
     * Validates map selection and either loads it or starts the server-only role.
     */
    private void mapSelectionNext() {
        if (!hasSelectedMap()) {
            showStatus("Select a scenario or saved game.", MessageKind.ERROR);
            return;
        }
        int port;
        try {
            port = parsePort(serverPortField.getText());
        } catch (IllegalArgumentException e) {
            showStatus(e.getMessage(), MessageKind.ERROR);
            return;
        }
        persistOptions();
        if (selectedMode() == ClientConstants.MODE_SERVER_ONLY) {
            startServerOnly(port, selectedSavedGame());
        } else if (isSavedGameSelected()) {
            String savedGame = selectedSavedGame();
            runOperation("Loading saved game...", () -> {
                requireServices().loadGame(savedGame);
                return requireServices().getPlayerNames();
            }, names -> {
                allowedPlayerNames = names;
                updatePlayerNameChoices();
                showStep(Step.CLIENT_OPTIONS);
            });
        } else {
            allowedPlayerNames = null;
            updatePlayerNameChoices();
            showStep(Step.CLIENT_OPTIONS);
        }
    }

    /**
     * Validates client options and starts the selected local or remote connection.
     */
    private void clientOptionsNext() {
        String playerName = selectedPlayerName();
        if (playerName == null || playerName.isEmpty()) {
            showStatus("Please set a name for your player.", MessageKind.ERROR);
            return;
        }
        persistOptions();
        if (selectedMode() == ClientConstants.MODE_START_NETWORK_GAME) {
            int port;
            try {
                port = parsePort(serverPortField.getText());
            } catch (IllegalArgumentException e) {
                showStatus(e.getMessage(), MessageKind.ERROR);
                return;
            }
            startLocalClient(playerName, port, selectedScreenMode());
        } else {
            String host = remoteHostField.getText();
            if (host == null || host.isEmpty()) {
                showStatus("Please enter a host name.", MessageKind.ERROR);
                return;
            }
            int port;
            try {
                port = parsePort(remotePortField.getText());
            } catch (IllegalArgumentException e) {
                showStatus(e.getMessage(), MessageKind.ERROR);
                return;
            }
            startRemoteClient(playerName, host, port, selectedScreenMode());
        }
    }

    /**
     * Starts a server-only session or completes the hosted local game.
     */
    private void connectedPlayersNext() {
        if (selectedMode() == ClientConstants.MODE_SERVER_ONLY) {
            if (serverRunning) {
                stage.close();
            } else {
                boolean savedGameSelected = isSavedGameSelected();
                String scenario = selectedScenario();
                runOperation("Starting server game...", () -> {
                    if (!savedGameSelected) {
                        requireServices().startServerGame(scenario);
                    }
                    return null;
                }, ignored -> {
                    serverRunning = true;
                    nextButton.setText("Close");
                    backButton.setDisable(true);
                    showStatus("Server is running. Close this window to stop it.", MessageKind.INFO);
                    refreshNextButton();
                });
            }
            return;
        }
        if (client == null) {
            showStatus("The local client is not connected.", MessageKind.ERROR);
            return;
        }
        boolean savedGameSelected = isSavedGameSelected();
        String scenario = selectedScenario();
        runOperation("Starting game...", () -> {
            if (!savedGameSelected) {
                requireServices().startServerGame(scenario);
            }
            return null;
        }, ignored -> {
            LauncherFrame.startThread(client);
            stage.close();
        });
    }

    /**
     * Loads a server-only save, binds the server, and displays connected players.
     *
     * @param port the requested listening port
     * @param savedGame the selected saved-game name, or {@code null} for a new scenario
     */
    private void startServerOnly(int port, String savedGame) {
        runOperation("Starting server...", () -> {
            if (savedGame != null) {
                requireServices().loadGame(savedGame);
            }
            int actualPort = requireServices().startHosting(port);
            return new ServerSetup(actualPort, requireServices().getPlayerNames());
        }, setup -> {
            serverOnlyReady = true;
            showConnectedPlayers(setup.playerNames());
            showStep(Step.CONNECTED_PLAYERS);
            nextButton.setText("Start server");
            backButton.setDisable(true);
            showStatus("Listening on port " + setup.port() + ".", MessageKind.INFO);
        });
    }

    /**
     * Starts the local server and connects a Swing gameplay client to it.
     *
     * @param playerName the player name
     * @param requestedPort the requested server port
     * @param screenMode the selected display mode
     */
    private void startLocalClient(String playerName, int requestedPort, int screenMode) {
        runOperation("Starting local game...", () -> {
            int port = requireServices().startHosting(requestedPort);
            LauncherClient newClient = createLauncherClient(playerName, screenMode);
            LogOnResponse response = newClient.connect(
                    InetAddress.getLoopbackAddress().getHostAddress(), port, playerName, "password");
            if (!response.success()) {
                disposeClientWindow(newClient);
                throw new IllegalStateException("Could not connect to the local server: " + response.message());
            }
            return newClient;
        }, connectedClient -> {
            client = connectedClient;
            showStep(Step.CONNECTED_PLAYERS);
            showStatus("Connected to the local server. Start when ready.", MessageKind.INFO);
        });
    }

    /**
     * Connects to a remote server and hands the client to the Swing gameplay loop.
     *
     * @param playerName the player name
     * @param host the remote server host
     * @param port the remote server port
     * @param screenMode the selected display mode
     */
    private void startRemoteClient(String playerName, String host, int port, int screenMode) {
        runOperation("Connecting to server...", () -> {
            LauncherClient newClient = createLauncherClient(playerName, screenMode);
            LogOnResponse response = newClient.connect(host, port, playerName, "password");
            if (!response.success()) {
                disposeClientWindow(newClient);
                throw new IllegalStateException(response.message());
            }
            return newClient;
        }, connectedClient -> {
            client = connectedClient;
            showStatus("Logged on and waiting for the game to start.", MessageKind.INFO);
            LauncherFrame.startThread(connectedClient);
            stage.close();
        });
    }

    /**
     * Creates the Swing gameplay client on the Swing event-dispatch thread.
     *
     * @param playerName the player name
     * @param screenMode the selected display mode
     * @return the initialized launcher client
     * @throws IllegalStateException if Swing initialization fails
     */
    private static LauncherClient createLauncherClient(String playerName, int screenMode) {
        LauncherClient[] result = new LauncherClient[1];
        try {
            EventQueue.invokeAndWait(() -> result[0] = new LauncherClient(playerName, screenMode));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while creating the gameplay window.", e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Could not create the gameplay window.", e.getCause());
        }
        return result[0];
    }

    /**
     * Disposes of a gameplay frame when server login fails.
     *
     * @param failedClient the client whose connection failed
     */
    private static void disposeClientWindow(LauncherClient failedClient) {
        EventQueue.invokeLater(() -> failedClient.getScreenHandler().frame.dispose());
    }

    /**
     * Runs server or network work away from the JavaFX application thread.
     *
     * @param progressMessage the status displayed while the operation runs
     * @param operation the operation to execute
     * @param onSuccess handles the completed operation on the JavaFX thread
     * @param <T> the operation result type
     */
    private <T> void runOperation(
            String progressMessage, CheckedSupplier<T> operation, java.util.function.Consumer<T> onSuccess) {
        busy = true;
        pageContainer.setDisable(true);
        showStatus(progressMessage, MessageKind.INFO);
        refreshNextButton();
        backgroundExecutor.submit(() -> {
            try {
                T result = operation.get();
                Platform.runLater(() -> {
                    busy = false;
                    pageContainer.setDisable(false);
                    onSuccess.accept(result);
                    refreshNextButton();
                });
            } catch (Exception e) {
                Logger.tag("JavaFxLauncher").warn(e, "Launcher operation failed.");
                Platform.runLater(() -> {
                    busy = false;
                    pageContainer.setDisable(false);
                    showStatus(errorMessage(e), MessageKind.WARNING);
                    refreshNextButton();
                });
            }
        });
    }

    /**
     * Returns the selected launcher mode.
     *
     * @return the launcher mode constant
     */
    private int selectedMode() {
        if (joinNetworkButton.isSelected()) {
            return ClientConstants.MODE_JOIN_NETWORK_GAME;
        }
        if (serverOnlyButton.isSelected()) {
            return ClientConstants.MODE_SERVER_ONLY;
        }
        return ClientConstants.MODE_START_NETWORK_GAME;
    }

    /**
     * Returns the selected player name, if one is available.
     *
     * @return the selected player name or {@code null}
     */
    private String selectedPlayerName() {
        if (savedPlayerNames.isVisible()) {
            return savedPlayerNames.getValue();
        }
        return playerNameField.getText();
    }

    /**
     * Returns the selected display mode.
     *
     * @return the selected client display mode
     */
    private int selectedScreenMode() {
        return fullScreenButton.isSelected()
                ? ClientConstants.FULL_SCREEN
                : ClientConstants.WINDOWED_MODE;
    }

    /**
     * Updates the player-name controls for an unrestricted or saved-game player list.
     */
    private void updatePlayerNameChoices() {
        if (allowedPlayerNames == null) {
            playerNameField.setVisible(true);
            playerNameField.setManaged(true);
            savedPlayerNames.setVisible(false);
            savedPlayerNames.setManaged(false);
            playerNameField.setEditable(true);
        } else if (allowedPlayerNames.length == 1) {
            playerNameField.setText(allowedPlayerNames[0]);
            playerNameField.setVisible(true);
            playerNameField.setManaged(true);
            playerNameField.setEditable(false);
            savedPlayerNames.setVisible(false);
            savedPlayerNames.setManaged(false);
        } else {
            savedPlayerNames.getItems().setAll(allowedPlayerNames);
            savedPlayerNames.getSelectionModel().selectFirst();
            playerNameField.setVisible(false);
            playerNameField.setManaged(false);
            savedPlayerNames.setVisible(true);
            savedPlayerNames.setManaged(true);
        }
        refreshNextButton();
    }

    /**
     * Displays the latest connected player names, including the empty-list message.
     *
     * @param names connected player names
     */
    private void showConnectedPlayers(String[] names) {
        if (names == null || names.length == 0) {
            connectedPlayersList.getItems().setAll("No players are connected.");
        } else {
            connectedPlayersList.getItems().setAll(names);
        }
    }

    /**
     * Shows the requested wizard page and updates navigation controls.
     *
     * @param step the page to display
     */
    private void showStep(Step step) {
        currentStep = step;
        pageContainer.getChildren().setAll(switch (step) {
            case GAME_TYPE -> gameTypePage;
            case MAP_SELECTION -> mapSelectionPage;
            case CLIENT_OPTIONS -> clientOptionsPage;
            case CONNECTED_PLAYERS -> connectedPlayersPage;
        });
        remoteServerBox.setVisible(step == Step.CLIENT_OPTIONS
                && selectedMode() == ClientConstants.MODE_JOIN_NETWORK_GAME);
        remoteServerBox.setManaged(remoteServerBox.isVisible());
        backButton.setVisible(step != Step.GAME_TYPE && step != Step.CONNECTED_PLAYERS);
        backButton.setManaged(backButton.isVisible());
        if (!serverOnlyReady) {
            nextButton.setText(step == Step.CONNECTED_PLAYERS ? "Start" : "Next...");
        }
        clearStatus();
        refreshNextButton();
    }

    /**
     * Enables navigation only when the current page has valid required values.
     */
    private void refreshNextButton() {
        if (nextButton == null) {
            return;
        }
        boolean valid = switch (currentStep) {
            case GAME_TYPE, CONNECTED_PLAYERS -> true;
            case MAP_SELECTION -> hasSelectedMap() && validServerPort();
            case CLIENT_OPTIONS -> validClientOptions();
        };
        nextButton.setDisable(busy || (!serverRunning && !valid));
        backButton.setDisable(busy || currentStep == Step.GAME_TYPE
                || currentStep == Step.CONNECTED_PLAYERS || serverRunning);
        swingButton.setDisable(busy);
        if (!busy && !valid) {
            if (currentStatusKind != MessageKind.ERROR) {
                showStatus(validationMessage(), MessageKind.ERROR);
            }
        } else if (valid && currentStatusKind == MessageKind.ERROR) {
            clearStatus();
        }
    }

    /**
     * Returns the validation message for the current page.
     *
     * @return the first applicable validation message
     */
    private String validationMessage() {
        return switch (currentStep) {
            case GAME_TYPE, CONNECTED_PLAYERS -> "";
            case MAP_SELECTION -> !hasSelectedMap()
                    ? "Select a scenario or saved game."
                    : invalidPortMessage(serverPortField.getText());
            case CLIENT_OPTIONS -> {
                if (allowedPlayerNames != null && allowedPlayerNames.length == 0) {
                    yield "No players are available in this saved game.";
                }
                String name = selectedPlayerName();
                if (name == null || name.isEmpty()) {
                    yield "Please set a name for your player.";
                }
                if (selectedMode() == ClientConstants.MODE_START_NETWORK_GAME) {
                    yield invalidPortMessage(serverPortField.getText());
                }
                String host = remoteHostField.getText();
                if (host == null || host.isEmpty()) {
                    yield "Please enter a host name.";
                }
                yield invalidPortMessage(remotePortField.getText());
            }
        };
    }

    /**
     * Returns an invalid-port message when the supplied value is malformed.
     *
     * @param value the entered port
     * @return the validation message, or an empty string when the port is valid
     */
    private static String invalidPortMessage(String value) {
        return isValidPort(value) ? "" : "A valid port value is between 0 and 65535.";
    }

    /**
     * Checks whether a scenario or save game is selected.
     *
     * @return {@code true} when a map selection exists
     */
    private boolean hasSelectedMap() {
        return scenarioList.getSelectionModel().getSelectedItem() != null
                || saveGameList.getSelectionModel().getSelectedItem() != null;
    }

    /**
     * Checks whether the map page has a valid port.
     *
     * @return {@code true} when the port is valid
     */
    private boolean validServerPort() {
        try {
            parsePort(serverPortField.getText());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Checks the visible client-option values without changing the status message.
     *
     * @return {@code true} when the current client options are valid
     */
    private boolean validClientOptions() {
        String name = selectedPlayerName();
        if (name == null || name.isEmpty()) {
            return false;
        }
        if (selectedMode() == ClientConstants.MODE_START_NETWORK_GAME) {
            return validServerPort();
        }
        String host = remoteHostField.getText();
        if (host == null || host.isEmpty()) {
            return false;
        }
        try {
            parsePort(remotePortField.getText());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Returns whether the save-game list currently has a selection.
     *
     * @return {@code true} when a saved game is selected
     */
    private boolean isSavedGameSelected() {
        return saveGameList.getSelectionModel().getSelectedItem() != null;
    }

    /**
     * Returns the selected scenario name.
     *
     * @return the selected scenario name
     */
    private String selectedScenario() {
        return scenarioList.getSelectionModel().getSelectedItem();
    }

    /**
     * Returns the selected save-game name.
     *
     * @return the selected save-game name
     */
    private String selectedSavedGame() {
        return saveGameList.getSelectionModel().getSelectedItem();
    }

    /**
     * Persists currently valid JavaFX control values into the shared options object.
     */
    private void persistOptions() {
        String name = selectedPlayerName();
        if (name != null && !name.isEmpty()) {
            Options.NAME = name;
        }
        String host = remoteHostField.getText();
        if (host != null && !host.isEmpty()) {
            Options.IP = host;
        }
        String portText = switch (selectedMode()) {
            case ClientConstants.MODE_START_NETWORK_GAME, ClientConstants.MODE_SERVER_ONLY -> serverPortField.getText();
            default -> remotePortField.getText();
        };
        if (isValidPort(portText)) {
            Options.PORT = parsePort(portText);
        }
        Options.MAINWINDOW_FULLSCREEN = fullScreenButton.isSelected();
    }

    /**
     * Checks whether a port value is in the supported range.
     *
     * @param value the entered port
     * @return {@code true} when the value is valid
     */
    private static boolean isValidPort(String value) {
        try {
            parsePort(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Parses a port number in the supported TCP port range.
     *
     * @param value the entered port
     * @return the parsed port
     * @throws IllegalArgumentException if the value is not a port from 0 to 65535
     */
    static int parsePort(String value) {
        if (value == null) {
            throw invalidPortException();
        }
        int port;
        try {
            port = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw invalidPortException();
        }
        if (port < 0 || port > 65535) {
            throw invalidPortException();
        }
        return port;
    }

    /**
     * Creates the standard invalid-port exception.
     *
     * @return the invalid-port exception
     */
    private static IllegalArgumentException invalidPortException() {
        return new IllegalArgumentException("A valid port value is between 0 and 65535.");
    }

    /**
     * Returns launcher services or reports an invalid role configuration.
     *
     * @return the configured launcher services
     */
    private LauncherServices requireServices() {
        LauncherServices services = launchRequest.services();
        if (services == null) {
            throw new IllegalStateException("This launcher role has no local server services.");
        }
        return services;
    }

    /**
     * Displays a status message with severity-specific coloring.
     *
     * @param message the status message
     * @param kind the message kind
     */
    private void showStatus(String message, MessageKind kind) {
        statusLabel.setText(message);
        currentStatusKind = kind;
        String color = switch (kind) {
            case INFO -> "#333333";
            case WARNING -> "#9a6700";
            case ERROR -> "#b42318";
        };
        statusLabel.setStyle("-fx-text-fill: " + color + ";");
    }

    /**
     * Clears the launcher status message.
     */
    private void clearStatus() {
        statusLabel.setText("");
        statusLabel.setStyle("");
        currentStatusKind = null;
    }

    /**
     * Returns a useful message for an operation failure.
     *
     * @param error the operation failure
     * @return the failure message
     */
    private static String errorMessage(Exception error) {
        String message = error.getMessage();
        return message == null || message.isBlank() ? error.getClass().getSimpleName() : message;
    }

    /**
     * Defines the pages in the JavaFX launcher flow.
     */
    private enum Step {
        GAME_TYPE,
        MAP_SELECTION,
        CLIENT_OPTIONS,
        CONNECTED_PLAYERS
    }

    /**
     * Defines the severity of a launcher status message.
     */
    private enum MessageKind {
        INFO,
        WARNING,
        ERROR
    }

    /**
     * Holds launch configuration and lifecycle callbacks for the active JavaFX application.
     *
     * @param services application services supplied to the launcher
     * @param forcedMode required launcher mode, if any
     * @param allowServerOnly whether server-only mode is available
     * @param swingFallback displays the Swing launcher when selected
     * @param onStarted runs after the JavaFX launcher is displayed
     * @param onStopped runs when JavaFX shuts down
     */
    private record LaunchRequest(
            LauncherServices services,
            Integer forcedMode,
            boolean allowServerOnly,
            Runnable swingFallback,
            Runnable onStarted,
            Runnable onStopped) {}

    /**
     * Holds a server's bound port and current players after setup.
     *
     * @param port the port actually bound
     * @param playerNames the configured player names
     */
    private record ServerSetup(int port, String[] playerNames) {}

    /**
     * Supplies an operation that may fail while using server or network services.
     *
     * @param <T> the result type
     */
    @FunctionalInterface
    private interface CheckedSupplier<T> {
        /**
         * Executes the operation.
         *
         * @return the operation result
         * @throws Exception if the operation cannot complete
         */
        T get() throws Exception;
    }
}
