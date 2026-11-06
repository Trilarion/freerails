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

import freerails.client.ActionRoot;
import freerails.client.ModelRootImpl;
import freerails.client.ModelRootProperty;
import freerails.client.ModelRootListener;
import freerails.client.renderer.map.MapRenderer;
import freerails.client.view.FreerailsCursor;
import freerails.controller.BuildMode;
import freerails.controller.BuildTrackController;
import freerails.model.track.BuildTrackStrategy;
import freerails.model.track.TrackCategory;
import freerails.model.track.TrackType;
import freerails.model.operations.train.Train;
import freerails.model.simulation.Speed;
import freerails.move.ChangeGameSpeedMove;
import freerails.util.Vector2D;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

/**
 * JavaFX gameplay shell matching the current main-map and sidebar arrangement.
 */
public final class JavaFxGameplayWindow {

    private final Stage stage = new Stage();
    private final int screenMode;
    private final Label dateLabel = new Label();
    private final Label cashLabel = new Label();
    private final Label statusLabel = new Label("Waiting for the game to start...");
    private final VBox mapHolder = new VBox();
    private final VBox overviewHolder = new VBox();
    private final TabPane gameplayTabs = new TabPane();
    private final ListView<Train> trainList = new ListView<>();
    private final TextArea terrainInfo = createReadOnlyTextArea();
    private final TextArea stationInfo = createReadOnlyTextArea();
    private final ComboBox<BuildMode> modeSelector = new ComboBox<>();
    private final ComboBox<TrackType> trackSelector = new ComboBox<>();
    private final ComboBox<TrackType> bridgeSelector = new ComboBox<>();
    private final ComboBox<TrackType> tunnelSelector = new ComboBox<>();
    private final ComboBox<TrackType> stationSelector = new ComboBox<>();
    private final ModelRootListener modelRootListener = this::modelPropertyChanged;
    private JavaFxMapView mapView;
    private JavaFxLauncherClient client;
    private ModelRootImpl modelRoot;
    private Object updateLock;
    private JavaFxGameplayDialogs dialogs;

    /**
     * Creates the JavaFX gameplay stage on the JavaFX application thread.
     *
     * @param screenMode selected windowed or full-screen mode
     */
    public JavaFxGameplayWindow(int screenMode) {
        this.screenMode = screenMode;
        MenuBar menuBar = createMenuBar();
        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(createGameplayLayout());

        statusLabel.setMaxWidth(Double.MAX_VALUE);
        statusLabel.setWrapText(true);
        statusLabel.setStyle("-fx-text-fill: #8b0000;");
        HBox status = new HBox(18, dateLabel, cashLabel, statusLabel);
        status.setAlignment(Pos.CENTER_LEFT);
        status.setPadding(new Insets(5, 8, 5, 8));
        HBox.setHgrow(statusLabel, Priority.ALWAYS);

        BorderPane shell = new BorderPane(root);
        shell.setBottom(status);
        stage.setTitle("Freerails");
        stage.setMinWidth(900);
        stage.setMinHeight(560);
        stage.setScene(new Scene(shell, 1100, 750));
        stage.setOnCloseRequest(event -> {
            event.consume();
            confirmExit();
        });
    }

    /**
     * Displays the JavaFX gameplay window and connects its close lifecycle.
     *
     * @param gameClient active network and game client
     */
    public void show(JavaFxLauncherClient gameClient) {
        client = Objects.requireNonNull(gameClient, "gameClient");
        if (screenMode == freerails.client.ClientConstants.FULL_SCREEN) {
            stage.setFullScreen(true);
        }
        stage.show();
        stage.toFront();
    }

    /**
     * Creates map renderers and wires the JavaFX map controls for a new world.
     *
     * @param newModelRoot game state and client properties
     * @param mainRenderer detailed map renderer
     * @param overviewRenderer overview map renderer
     * @param cursor map cursor painter
     * @param actionRoot existing game action services
     * @param buildTrackController asynchronous track preview controller
     * @param updateLock synchronizes game, render, and input access
     * @param title title for the player session
     */
    public void setup(
            ModelRootImpl newModelRoot,
            MapRenderer mainRenderer,
            MapRenderer overviewRenderer,
            FreerailsCursor cursor,
            ActionRoot actionRoot,
            BuildTrackController buildTrackController,
            Object updateLock,
            String title) {
        Objects.requireNonNull(newModelRoot, "modelRoot");
        Platform.runLater(() -> {
            String date;
            String cash;
            synchronized (updateLock) {
                if (mapView != null) {
                    mapView.close();
                }
                if (modelRoot != null) {
                    modelRoot.removePropertyChangeListener(modelRootListener);
                }
                modelRoot = newModelRoot;
                this.updateLock = updateLock;
                modelRoot.addPropertyChangeListener(modelRootListener);
                dialogs = new JavaFxGameplayDialogs(stage, modelRoot, updateLock, this::showError);
                mapView = new JavaFxMapView(
                        modelRoot, mainRenderer, overviewRenderer,
                        cursor, actionRoot, buildTrackController, updateLock, this::showError);
                mapHolder.getChildren().setAll(mapView.getMainView());
                overviewHolder.getChildren().setAll(mapView.getOverviewView());
                configureBuildSelectors();
                date = modelRoot.getWorld().getClock().getCurrentDateAsString();
                cash = freerails.client.MoneyFormatter.format(
                        modelRoot.getWorld().getCurrentBalance(modelRoot.getPlayer()));
                refreshContextTabsLocked();
            }
            stage.setTitle(title);
            statusLabel.setText("");
            updateStatus(date, cash);
            refreshTrainList();
        });
    }

    /**
     * Publishes date and balance values captured from the game update thread.
     *
     * @param date current game date
     * @param cash formatted player balance
     */
    public void updateStatus(String date, String cash) {
        Platform.runLater(() -> {
            dateLabel.setText(date);
            cashLabel.setText(cash);
        });
    }

    /**
     * Refreshes the JavaFX train selection list from a consistent game snapshot.
     */
    public void refreshTrainList() {
        Platform.runLater(() -> {
            if (modelRoot == null || updateLock == null) {
                return;
            }
            List<Train> trains;
            synchronized (updateLock) {
                trains = new ArrayList<>(modelRoot.getWorld().getTrains(modelRoot.getPlayer()));
            }
            Train selected = trainList.getSelectionModel().getSelectedItem();
            int selectedId = selected == null ? -1 : selected.getId();
            trainList.getItems().setAll(trains);
            if (selectedId >= 0) {
                for (int index = 0; index < trains.size(); index++) {
                    if (trains.get(index).getId() == selectedId) {
                        trainList.getSelectionModel().select(index);
                        break;
                    }
                }
            }
            refreshContextTabs();
        });
    }

    /**
     * Displays a visible error in the gameplay status area.
     *
     * @param message error description
     */
    public void showError(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }

    /**
     * Creates the map/sidebar layout used by the existing gameplay window.
     *
     * @return JavaFX gameplay layout
     */
    private HBox createGameplayLayout() {
        mapHolder.setMinSize(400, 300);
        VBox.setVgrow(mapHolder, Priority.ALWAYS);
        HBox.setHgrow(mapHolder, Priority.ALWAYS);

        overviewHolder.setMinSize(200, 200);
        overviewHolder.setPrefSize(200, 200);
        trainList.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            /**
             * Displays a train's stable identifier in the roster.
             *
             * @param train train to display
             * @param empty whether the cell has no item
             */
            @Override
            protected void updateItem(Train train, boolean empty) {
                super.updateItem(train, empty);
                setText(empty || train == null ? null : "Train " + train.getId());
            }
        });
        trainList.setOnMouseClicked(event -> centerSelectedTrain());
        Button trainOrders = new Button("Train orders");
        trainOrders.setMaxWidth(Double.MAX_VALUE);
        trainOrders.setOnAction(event -> showSelectedTrainOrders());
        VBox trainContent = new VBox(6, trainList, trainOrders);
        VBox.setVgrow(trainList, Priority.ALWAYS);
        gameplayTabs.getTabs().setAll(
                new Tab("Terrain Info", terrainInfo),
                new Tab("Station Info", stationInfo),
                createBuildTrackTab(),
                new Tab("Train List", trainContent));
        gameplayTabs.getTabs().forEach(tab -> tab.setClosable(false));

        VBox sidebar = new VBox(8, overviewHolder, gameplayTabs);
        sidebar.setPadding(new Insets(8));
        sidebar.setMinWidth(400);
        sidebar.setPrefWidth(420);
        VBox.setVgrow(gameplayTabs, Priority.ALWAYS);

        VBox leftSide = new VBox(mapHolder);
        leftSide.setPadding(new Insets(8, 0, 8, 8));
        VBox.setVgrow(mapHolder, Priority.ALWAYS);
        HBox layout = new HBox(leftSide, sidebar);
        HBox.setHgrow(leftSide, Priority.ALWAYS);
        return layout;
    }

    /**
     * Creates the track-mode selector in the existing build tab location.
     *
     * @return JavaFX tab for basic map build-mode selection
     */
    private Tab createBuildTrackTab() {
        modeSelector.getItems().setAll(
                BuildMode.BUILD_TRACK, BuildMode.UPGRADE_TRACK, BuildMode.REMOVE_TRACK,
                BuildMode.BUILD_STATION, BuildMode.IGNORE_TRACK);
        modeSelector.setValue(BuildMode.BUILD_TRACK);
        modeSelector.setOnAction(event -> {
            if (modelRoot != null && updateLock != null) {
                BuildMode mode = modeSelector.getValue();
                synchronized (updateLock) {
                    modelRoot.setProperty(ModelRootProperty.TRACK_BUILDER_MODE, mode);
                    stationSelector.setDisable(mode != BuildMode.BUILD_STATION);
                    trackSelector.setDisable(mode == BuildMode.BUILD_STATION
                            || mode == BuildMode.IGNORE_TRACK);
                    bridgeSelector.setDisable(mode == BuildMode.BUILD_STATION
                            || mode == BuildMode.IGNORE_TRACK);
                    tunnelSelector.setDisable(mode == BuildMode.BUILD_STATION
                            || mode == BuildMode.IGNORE_TRACK);
                    if (mode != BuildMode.BUILD_STATION) {
                        var stationBuildModel = client == null
                                ? null : client.getActionRoot().getStationBuildModel();
                        if (stationBuildModel != null) {
                            stationBuildModel.cancelStationPlacement();
                        }
                    }
                    if (client != null) {
                        var producer = client.getActionRoot().getTrackMoveProducer();
                        if (producer != null) {
                            producer.setTrackBuilderMode(mode);
                        }
                    }
                }
            }
        });
        stationSelector.setPromptText("Select station type");
        stationSelector.setOnAction(event -> {
            if (client != null && stationSelector.getValue() != null) {
                var stationBuildModel = client.getActionRoot().getStationBuildModel();
                if (stationBuildModel != null) {
                    synchronized (updateLock) {
                        stationBuildModel.chooseStationType(stationSelector.getValue().getId());
                    }
                }
            }
        });
        configureTrackSelector(trackSelector, TrackCategory.TRACK);
        configureTrackSelector(bridgeSelector, TrackCategory.BRIDGE);
        configureTrackSelector(tunnelSelector, TrackCategory.TUNNEL);
        configureTrackSelector(stationSelector, TrackCategory.STATION);
        trackSelector.setOnAction(event -> updateBuildTrackStrategy());
        bridgeSelector.setOnAction(event -> updateBuildTrackStrategy());
        tunnelSelector.setOnAction(event -> updateBuildTrackStrategy());
        Button buildTrain = new Button("Build train...");
        buildTrain.setMaxWidth(Double.MAX_VALUE);
        buildTrain.setOnAction(event -> {
            if (dialogs != null) {
                dialogs.showBuildTrain();
            }
        });
        Button engineShop = new Button("Build engine shop...");
        engineShop.setMaxWidth(Double.MAX_VALUE);
        engineShop.setOnAction(event -> {
            if (dialogs != null) {
                dialogs.showBuildEngineShop();
            }
        });
        VBox content = new VBox(8, new Label("Map action"), modeSelector,
                new Label("Track type"), trackSelector,
                new Label("Bridge type"), bridgeSelector,
                new Label("Tunnel type"), tunnelSelector,
                new Label("Station type"), stationSelector,
                new Label("Drag across the map to build, upgrade, or remove track. "
                        + "Choose a station type, then click a tile to build a station."),
                buildTrain, engineShop);
        content.setPadding(new Insets(8));
        Tab tab = new Tab("Build Track", content);
        tab.setClosable(false);
        return tab;
    }

    /**
     * Creates the gameplay menu bar.
     *
     * @return the gameplay menu bar
     */
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().setAll(
                createGameMenu(), createBuildMenu(), createBrokerMenu(),
                createDisplayMenu(), createReportsMenu());
        return menuBar;
    }

    /**
     * Creates the game menu with server actions and save/load operations.
     *
     * @return the JavaFX game menu
     */
    private Menu createGameMenu() {
        Menu game = new Menu("Game");
        Menu newGame = new Menu("New Game...");
        newGame.setOnShowing(event -> {
            newGame.getItems().clear();
            if (client == null) {
                return;
            }
            for (String scenarioName : new freerails.io.scenario.ClasspathScenarioSource()
                    .getAvailableScenarioNames()) {
                MenuItem item = new MenuItem(scenarioName);
                item.setOnAction(ignored -> {
                    if (modelRoot != null) {
                        modelRoot.sendCommand(new freerails.network.command.NewGameCommandToServer(scenarioName));
                    }
                });
                newGame.getItems().add(item);
            }
        });
        MenuItem load = new MenuItem("Load Game...");
        load.setOnAction(event -> {
            if (dialogs != null) {
                dialogs.showLoadGame();
            }
        });
        MenuItem save = new MenuItem("Save Game...");
        save.setOnAction(event -> {
            if (dialogs != null) {
                dialogs.showSaveGame();
            }
        });
        Menu speedMenu = new Menu("Game speed");
        speedMenu.setOnShowing(event -> populateSpeedMenu(speedMenu));
        MenuItem exit = new MenuItem("Exit Game...");
        exit.setOnAction(event -> confirmExit());
        game.getItems().setAll(newGame, load, save, speedMenu, exit);
        return game;
    }

    /**
     * Creates build commands for track, station, train, and engine-shop actions.
     *
     * @return the JavaFX build menu
     */
    private Menu createBuildMenu() {
        Menu build = new Menu("Build");
        MenuItem buildTrain = new MenuItem("Build Train...");
        buildTrain.setOnAction(event -> {
            if (dialogs != null) {
                dialogs.showBuildTrain();
            }
        });
        MenuItem buildShop = new MenuItem("Build Engine Shop...");
        buildShop.setOnAction(event -> {
            if (dialogs != null) {
                dialogs.showBuildEngineShop();
            }
        });
        MenuItem buildIndustry = new MenuItem("Build Industry at Cursor...");
        buildIndustry.setOnAction(event -> {
            if (dialogs != null) {
                dialogs.showBuildIndustry();
            }
        });
        MenuItem trackTab = new MenuItem("Build Track");
        trackTab.setOnAction(event -> selectBuildMode(BuildMode.BUILD_TRACK));
        MenuItem stationTab = new MenuItem("Build Station");
        stationTab.setOnAction(event -> selectBuildMode(BuildMode.BUILD_STATION));
        MenuItem upgradeTrack = new MenuItem("Upgrade Track");
        upgradeTrack.setOnAction(event -> selectBuildMode(BuildMode.UPGRADE_TRACK));
        MenuItem removeTrack = new MenuItem("Remove Track");
        removeTrack.setOnAction(event -> selectBuildMode(BuildMode.REMOVE_TRACK));
        build.getItems().setAll(buildTrain, buildShop, buildIndustry,
                trackTab, stationTab, upgradeTrack, removeTrack);
        return build;
    }

    /**
     * Creates access to the existing broker workflow through its JavaFX report view.
     *
     * @return the JavaFX broker menu
     */
    private Menu createBrokerMenu() {
        Menu broker = new Menu("Broker");
        MenuItem callBroker = new MenuItem("Call Broker...");
        callBroker.setOnAction(event -> {
            if (dialogs != null) {
                dialogs.showBroker();
            }
        });
        broker.getItems().add(callBroker);
        return broker;
    }

    /**
     * Creates display toggles backed by client model properties.
     *
     * @return the JavaFX display menu
     */
    private Menu createDisplayMenu() {
        Menu display = new Menu("Display");
        MenuItem trainListItem = new MenuItem("Train List");
        trainListItem.setOnAction(event -> gameplayTabs.getSelectionModel().select(3));
        MenuItem stationInfoItem = new MenuItem("Station Info");
        stationInfoItem.setOnAction(event -> gameplayTabs.getSelectionModel().select(1));
        MenuItem trainOrdersItem = new MenuItem("Train Orders");
        trainOrdersItem.setOnAction(event -> showSelectedTrainOrders());
        CheckMenuItem cargo = createDisplayToggle(
                "Show cargo at stations", ModelRootProperty.SHOW_CARGO_AT_STATIONS);
        CheckMenuItem names = createDisplayToggle(
                "Show station names", ModelRootProperty.SHOW_STATION_NAMES);
        CheckMenuItem borders = createDisplayToggle(
                "Show station influence", ModelRootProperty.SHOW_STATION_BORDERS);
        CheckMenuItem sounds = createDisplayToggle("Play sounds", ModelRootProperty.PLAY_SOUNDS);
        display.getItems().setAll(trainListItem, stationInfoItem, trainOrdersItem,
                new javafx.scene.control.SeparatorMenuItem(), cargo, names, borders, sounds);
        return display;
    }

    /**
     * Creates the reports menu and connects each report to a JavaFX view.
     *
     * @return the JavaFX reports menu
     */
    private Menu createReportsMenu() {
        Menu reports = new Menu("Reports");
        MenuItem balance = new MenuItem("Balance Sheet");
        balance.setOnAction(event -> showReport("Balance Sheet", 0));
        MenuItem income = new MenuItem("Income Statement");
        income.setOnAction(event -> showReport("Income Statement", 1));
        MenuItem leaderboard = new MenuItem("Leaderboard");
        leaderboard.setOnAction(event -> showReport("Leaderboard", 2));
        MenuItem netWorth = new MenuItem("Net Worth Graph");
        netWorth.setOnAction(event -> showReport("Net Worth Graph", 3));
        reports.getItems().setAll(balance, income, leaderboard, netWorth);
        return reports;
    }

    /**
     * Updates the speed actions shown for the active client.
     *
     * @param speedMenu menu to populate
     */
    private void populateSpeedMenu(Menu speedMenu) {
        speedMenu.getItems().clear();
        if (modelRoot == null || updateLock == null) {
            return;
        }
        ToggleGroup group = new ToggleGroup();
        Speed currentSpeed;
        synchronized (updateLock) {
            currentSpeed = modelRoot.getWorld().getSpeed();
        }
        for (Speed speed : Speed.values()) {
            RadioMenuItem item = new RadioMenuItem(speed.name());
            item.setToggleGroup(group);
            item.setSelected(speed == currentSpeed);
            item.setOnAction(event -> {
                synchronized (updateLock) {
                    modelRoot.applyMove(new ChangeGameSpeedMove(speed));
                }
            });
            speedMenu.getItems().add(item);
        }
    }

    /**
     * Creates a model-backed display option.
     *
     * @param text visible option name
     * @param property model property controlled by this option
     * @return the initialized display toggle
     */
    private CheckMenuItem createDisplayToggle(String text, ModelRootProperty property) {
        CheckMenuItem item = new CheckMenuItem(text);
        item.setSelected(property != ModelRootProperty.PLAY_SOUNDS);
        item.setOnAction(event -> {
            if (modelRoot != null && updateLock != null) {
                synchronized (updateLock) {
                    modelRoot.setProperty(property, item.isSelected());
                }
            }
        });
        return item;
    }

    /**
     * Selects the build tab and changes its active map mode.
     *
     * @param mode map build mode
     */
    private void selectBuildMode(BuildMode mode) {
        gameplayTabs.getSelectionModel().select(2);
        modeSelector.setValue(mode);
    }

    /**
     * Shows the active train's current orders.
     */
    private void showSelectedTrainOrders() {
        Train selected = trainList.getSelectionModel().getSelectedItem();
        if (dialogs != null && selected != null) {
            dialogs.showTrainOrders(selected);
        } else {
            showError("Select a train to view its orders.");
        }
    }

    /**
     * Opens a report in a JavaFX dialog.
     *
     * @param title report title
     * @param report report selector
     */
    private void showReport(String title, int report) {
        if (dialogs == null) {
            showError("The game is not ready to display reports.");
            return;
        }
        switch (report) {
            case 0 -> dialogs.showBalanceSheet(title);
            case 1 -> dialogs.showIncomeStatement(title);
            case 2 -> dialogs.showLeaderboard(title);
            case 3 -> dialogs.showNetWorthGraph(title);
            default -> throw new IllegalArgumentException("Unknown report: " + report);
        }
    }

    /**
     * Requests confirmation before closing the JavaFX client.
     */
    private void confirmExit() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION,
                "Exit the current game?", javafx.scene.control.ButtonType.YES,
                javafx.scene.control.ButtonType.NO);
        alert.initOwner(stage);
        alert.setTitle("Exit Game");
        alert.setHeaderText("Do you want to exit?");
        alert.showAndWait().filter(button -> button == javafx.scene.control.ButtonType.YES)
                .ifPresent(button -> {
                    stage.setOnCloseRequest(null);
                    closeGame();
                    stage.close();
                });
    }

    /**
     * Formats terrain and station details after cursor position changes.
     *
     * @param property changed model property
     * @param oldValue previous property value
     * @param newValue new property value
     */
    private void modelPropertyChanged(ModelRootProperty property, Object oldValue, Object newValue) {
        if (property == ModelRootProperty.CURSOR_POSITION
                || property == ModelRootProperty.SAVED_GAMES_LIST) {
            Platform.runLater(() -> {
                if (property == ModelRootProperty.CURSOR_POSITION) {
                    refreshContextTabs();
                }
                if (dialogs != null && property == ModelRootProperty.SAVED_GAMES_LIST) {
                    dialogs.savedGamesChanged();
                }
            });
        }
    }

    /**
     * Refreshes the contextual tabs while holding the game-state lock.
     */
    private void refreshContextTabs() {
        if (modelRoot == null || updateLock == null) {
            return;
        }
        synchronized (updateLock) {
            refreshContextTabsLocked();
        }
    }

    /**
     * Builds terrain and station information from one consistent world snapshot.
     */
    private void refreshContextTabsLocked() {
        Vector2D position = (Vector2D) modelRoot.getProperty(ModelRootProperty.CURSOR_POSITION);
        if (position == null || !modelRoot.getWorld().boundsContain(position)) {
            terrainInfo.setText("Move the map cursor over a tile to see terrain details.");
            stationInfo.setText("Move the map cursor over a station to see its cargo details.");
            return;
        }
        var world = modelRoot.getWorld();
        var terrain = world.getTerrain(world.getTile(position).terrainTypeId());
        StringBuilder terrainText = new StringBuilder()
                .append(terrain.getName()).append(" at ").append(position).append('\n')
                .append("Track construction: $").append(terrain.getTrackConstructionCostPerMile())
                .append(" per mile.\n");
        if (terrain.getIndustryBuildCost() != null) {
            terrainText.append("Industry construction: $")
                    .append(terrain.getIndustryBuildCost()).append('\n');
        }
        terrain.getProductions().forEach(production -> terrainText.append("Supplies ")
                .append(world.getCargo(production.cargoId()).getName())
                .append(": ").append(production.rate()
                        / freerails.model.ModelConstants.UNITS_OF_CARGO_PER_WAGON).append(" cars/year\n"));
        terrain.getConsumptions().forEach(consumption -> terrainText.append("Demands ")
                .append(world.getCargo(consumption.cargoId()).getName()).append('\n'));
        terrain.getConversions().forEach(conversion -> terrainText.append("Converts ")
                .append(world.getCargo(conversion.sourceCargoId()).getName()).append(" to ")
                .append(world.getCargo(conversion.productCargoId()).getName()).append('\n'));
        terrainInfo.setText(terrainText.toString());

        int stationId = freerails.model.operations.station.StationUtils.getStationIdAtLocation(
                world, modelRoot.getPlayer(), position);
        if (stationId < 0) {
            stationInfo.setText("No station at " + position + '.');
            return;
        }
        var station = world.getStation(modelRoot.getPlayer(), stationId);
        StringBuilder stationText = new StringBuilder()
                .append(station.getStationName()).append(" at ").append(position).append('\n');
        var cargoWaiting = station.getCargoBatchBundle();
        world.getCargos().forEach(cargo -> {
            int amount = station.getSupply().getSupply(cargo.getId());
            int waiting = cargoWaiting.getAmountOfType(cargo.getId());
            if (station.getDemandForCargo().isCargoDemanded(cargo.getId()) || amount > 0) {
                stationText.append(cargo.getName())
                        .append(" — demand: ")
                        .append(station.getDemandForCargo().isCargoDemanded(cargo.getId()) ? "yes" : "no")
                        .append(", supply: ").append(amount
                                / freerails.model.ModelConstants.UNITS_OF_CARGO_PER_WAGON)
                        .append(" cars/year, waiting: ").append(waiting
                                / freerails.model.ModelConstants.UNITS_OF_CARGO_PER_WAGON)
                        .append(" loads\n");
            }
        });
        stationInfo.setText(stationText.toString());
    }

    /**
     * Populates available track choices from the active game world.
     *
     * @param selector JavaFX selector to populate
     * @param category required track category
     */
    private void configureTrackSelector(ComboBox<TrackType> selector, TrackCategory category) {
        selector.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            /**
             * Displays a track type by its configured name.
             *
             * @param type track type
             * @param empty whether the cell has no item
             */
            @Override
            protected void updateItem(TrackType type, boolean empty) {
                super.updateItem(type, empty);
                setText(empty || type == null ? null : type.getName());
            }
        });
        selector.setButtonCell(new javafx.scene.control.ListCell<>() {
            /**
             * Displays the selected track type by its configured name.
             *
             * @param type track type
             * @param empty whether the cell has no item
             */
            @Override
            protected void updateItem(TrackType type, boolean empty) {
                super.updateItem(type, empty);
                setText(empty || type == null ? null : type.getName());
            }
        });
        selector.setUserData(category);
    }

    /**
     * Initializes track selectors and the default multi-category build strategy.
     */
    private void configureBuildSelectors() {
        trackSelector.getItems().clear();
        bridgeSelector.getItems().clear();
        tunnelSelector.getItems().clear();
        stationSelector.getItems().clear();
        trackSelector.setValue(null);
        bridgeSelector.setValue(null);
        tunnelSelector.setValue(null);
        stationSelector.setValue(null);
        for (TrackType type : modelRoot.getWorld().getTrackTypes()) {
            ComboBox<TrackType> selector = switch (type.getCategory()) {
                case TRACK -> trackSelector;
                case BRIDGE -> bridgeSelector;
                case TUNNEL -> tunnelSelector;
                case STATION -> stationSelector;
            };
            selector.getItems().add(type);
            if (selector.getValue() == null) {
                selector.setValue(type);
            }
        }
        modeSelector.setValue(BuildMode.BUILD_TRACK);
        stationSelector.setDisable(true);
        updateBuildTrackStrategy();
    }

    /**
     * Applies the current rail, bridge, and tunnel choices to map construction.
     */
    private void updateBuildTrackStrategy() {
        if (modelRoot == null || trackSelector.getValue() == null) {
            return;
        }
        synchronized (updateLock) {
            java.util.List<Integer> ruleIds = new java.util.ArrayList<>();
            ruleIds.add(trackSelector.getValue().getId());
            ruleIds.add(bridgeSelector.getValue() == null ? null : bridgeSelector.getValue().getId());
            ruleIds.add(tunnelSelector.getValue() == null ? null : tunnelSelector.getValue().getId());
            modelRoot.setProperty(ModelRootProperty.BUILD_TRACK_STRATEGY,
                    BuildTrackStrategy.getMultipleRuleInstance(ruleIds, modelRoot.getWorld()));
        }
    }

    /**
     * Creates an editable, wrapping text area for contextual information.
     *
     * @return a read-only text area
     */
    private static TextArea createReadOnlyTextArea() {
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        return textArea;
    }

    /**
     * Centers the map on the selected train's current tile.
     */
    private void centerSelectedTrain() {
        Train selected = trainList.getSelectionModel().getSelectedItem();
        if (selected == null || modelRoot == null || mapView == null) {
            return;
        }
        synchronized (updateLock) {
            var time = modelRoot.getWorld().getClock().getCurrentTime();
            mapView.centerOnTile(selected.getLocation(time));
        }
    }

    /**
     * Stops the client and releases map rendering when the stage closes.
     */
    private void closeGame() {
        if (client != null) {
            client.stop();
        }
        if (mapView != null) {
            mapView.close();
        }
        Platform.exit();
    }
}
