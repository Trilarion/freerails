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
import freerails.client.renderer.map.MapRenderer;
import freerails.client.view.FreerailsCursor;
import freerails.controller.BuildMode;
import freerails.controller.BuildTrackController;
import freerails.model.operations.train.Train;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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
    private JavaFxMapView mapView;
    private JavaFxLauncherClient client;
    private ModelRootImpl modelRoot;
    private Object updateLock;

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
        stage.setOnCloseRequest(event -> closeGame());
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
                modelRoot = newModelRoot;
                this.updateLock = updateLock;
                mapView = new JavaFxMapView(
                        modelRoot, mainRenderer, overviewRenderer,
                        cursor, actionRoot, buildTrackController, updateLock, this::showError);
                mapHolder.getChildren().setAll(mapView.getMainView());
                overviewHolder.getChildren().setAll(mapView.getOverviewView());
                date = modelRoot.getWorld().getClock().getCurrentDateAsString();
                cash = freerails.client.MoneyFormatter.format(
                        modelRoot.getWorld().getCurrentBalance(modelRoot.getPlayer()));
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
        gameplayTabs.getTabs().setAll(
                new Tab("Terrain Info", new Label("Terrain details")),
                new Tab("Station Info", new Label("Station details")),
                createBuildTrackTab(),
                new Tab("Train List", trainList));
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
        ComboBox<BuildMode> modeSelector = new ComboBox<>();
        modeSelector.getItems().setAll(
                BuildMode.BUILD_TRACK, BuildMode.UPGRADE_TRACK, BuildMode.REMOVE_TRACK);
        modeSelector.setValue(BuildMode.BUILD_TRACK);
        modeSelector.setOnAction(event -> {
            if (modelRoot != null) {
                BuildMode mode = modeSelector.getValue();
                modelRoot.setProperty(ModelRootProperty.TRACK_BUILDER_MODE, mode);
                if (client != null) {
                    var producer = client.getActionRoot().getTrackMoveProducer();
                    if (producer != null) {
                        synchronized (updateLock) {
                            producer.setTrackBuilderMode(mode);
                        }
                    }
                }
            }
        });
        VBox content = new VBox(8, new Label("Map action"), modeSelector,
                new Label("Drag on the map to build a route."));
        content.setPadding(new Insets(8));
        Tab tab = new Tab("Build Track", content);
        tab.setClosable(false);
        return tab;
    }

    /**
     * Creates placeholder menu positions for surfaces scheduled in milestone 4.
     *
     * @return the gameplay menu bar
     */
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().setAll(
                new Menu("Game"),
                new Menu("Build"),
                new Menu("Broker"),
                new Menu("Display"),
                new Menu("Reports"));
        MenuItem exit = new MenuItem("Exit Game");
        exit.setOnAction(event -> stage.close());
        menuBar.getMenus().getFirst().getItems().add(exit);
        return menuBar;
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
