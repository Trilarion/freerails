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

import freerails.client.ModelRootImpl;
import freerails.client.ModelRootProperty;
import freerails.client.MoneyFormatter;
import freerails.model.ModelConstants;
import freerails.model.building.EngineShop;
import freerails.model.economy.reporting.BalanceSheetGenerator;
import freerails.model.economy.reporting.IncomeStatementGenerator;
import freerails.model.economy.Money;
import freerails.model.economy.StockPrice;
import freerails.model.economy.StockPriceCalculator;
import freerails.model.economy.TransactionUtils;
import freerails.model.economy.transaction.ItemTransaction;
import freerails.model.economy.transaction.TransactionCategory;
import freerails.model.economy.transaction.aggregator.FinancialDataAggregator;
import freerails.model.economy.transaction.aggregator.NetWorthAggregator;
import freerails.model.operations.train.Engine;
import freerails.model.operations.train.Train;
import freerails.model.operations.train.TrainTemplate;
import freerails.model.operations.train.schedule.ScheduleState;
import freerails.model.operations.train.schedule.TrainOrder;
import freerails.model.player.Player;
import freerails.model.world.UnmodifiableWorld;
import freerails.move.BuildEngineShopMove;
import freerails.move.AddTransactionMove;
import freerails.move.CompostMove;
import freerails.move.ChangeProductionAtEngineShopMove;
import freerails.move.Move;
import freerails.move.Status;
import freerails.move.UpdateTrainMove;
import freerails.move.mapupdatemove.ChangeTileMove;
import freerails.model.terrain.Terrain;
import freerails.model.terrain.TerrainTile;
import freerails.network.command.LoadGameCommandToServer;
import freerails.network.command.RefreshListOfGamesCommandToServer;
import freerails.network.command.SaveGameCommandToServer;
import freerails.model.simulation.Time;
import freerails.model.operations.cargo.Cargo;
import freerails.model.operations.station.Station;
import freerails.client.ClientConstants;
import freerails.client.view.BrokerScreenGenerator;
import freerails.util.Vector2D;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.web.WebView;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Presents JavaFX gameplay dialogs backed by existing model and server commands.
 */
final class JavaFxGameplayDialogs {

    private static final Pattern TEMPLATE_TOKEN = Pattern.compile("\\$([^$]+)\\$");
    private final Stage owner;
    private final ModelRootImpl modelRoot;
    private final Object updateLock;
    private final Consumer<String> showError;
    private ListView<String> savedGamesList;
    private Node loadButton;

    /**
     * Creates JavaFX dialogs for the active game client.
     *
     * @param owner gameplay window
     * @param modelRoot game state and client properties
     * @param updateLock synchronizes access to game state
     * @param showError displays recoverable errors
     */
    JavaFxGameplayDialogs(
            Stage owner, ModelRootImpl modelRoot, Object updateLock, Consumer<String> showError) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.modelRoot = Objects.requireNonNull(modelRoot, "modelRoot");
        this.updateLock = Objects.requireNonNull(updateLock, "updateLock");
        this.showError = Objects.requireNonNull(showError, "showError");
    }

    /**
     * Prompts for a save-game name and sends the save request to the server.
     */
    void showSaveGame() {
        TextInputDialog dialog = new TextInputDialog(modelRoot.getPlayer().getName());
        dialog.initOwner(owner);
        dialog.setTitle("Save Game");
        dialog.setHeaderText("Save the current game");
        dialog.setContentText("Save game name:");
        dialog.showAndWait().ifPresent(name -> {
            String trimmed = name.trim();
            if (trimmed.isEmpty()) {
                showError.accept("Enter a name for the saved game.");
                return;
            }
            String filename = trimmed.endsWith(".sav") ? trimmed : trimmed + ".sav";
            synchronized (updateLock) {
                modelRoot.setProperty(ModelRootProperty.QUICK_MESSAGE, "Saving game " + filename);
            }
            modelRoot.sendCommand(new SaveGameCommandToServer(filename));
        });
    }

    /**
     * Displays the server's saved-game list and sends a selected load request.
     */
    void showLoadGame() {
        ButtonType load = new ButtonType("Load", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancel = ButtonType.CANCEL;
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("Load Game");
        dialog.setHeaderText("Select a saved game to load");
        dialog.getDialogPane().getButtonTypes().setAll(load, cancel);

        savedGamesList = new ListView<>();
        savedGamesList.setPrefSize(360, 240);
        Button refresh = new Button("Refresh");
        refresh.setOnAction(event -> requestSavedGames());
        VBox content = new VBox(8, savedGamesList, refresh);
        VBox.setVgrow(savedGamesList, Priority.ALWAYS);
        dialog.getDialogPane().setContent(content);
        loadButton = dialog.getDialogPane().lookupButton(load);
        loadButton.setDisable(true);
        savedGamesList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> loadButton.setDisable(newValue == null));
        updateSavedGamesList();
        requestSavedGames();

        dialog.showAndWait().filter(button -> button == load).ifPresent(button -> {
            String selected = savedGamesList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError.accept("Select a saved game to load.");
                return;
            }
            modelRoot.sendCommand(new LoadGameCommandToServer(selected));
        });
        savedGamesList = null;
        loadButton = null;
    }

    /**
     * Refreshes the open load dialog after saved-game data arrives from the server.
     */
    void savedGamesChanged() {
        if (savedGamesList != null) {
            updateSavedGamesList();
        }
    }

    /**
     * Opens a JavaFX chooser for an engine shop and builds it at the selected station.
     */
    void showBuildEngineShop() {
        List<Station> availableStations;
        synchronized (updateLock) {
            UnmodifiableWorld world = modelRoot.getWorld();
            Collection<EngineShop> shops = world.getEngineShops(modelRoot.getPlayer());
            availableStations = world.getStations(modelRoot.getPlayer()).stream()
                    .filter(station -> shops.stream().noneMatch(
                            shop -> shop.getLocation().equals(station.getLocation())))
                    .toList();
        }

        if (availableStations.isEmpty()) {
            showError.accept("Build a station first, or all stations already have an engine shop.");
            return;
        }
        List<String> choices = availableStations.stream().map(this::stationLabel).toList();
        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.getFirst(), choices);
        dialog.initOwner(owner);
        dialog.setTitle("Build Engine Shop");
        dialog.setHeaderText("Construction cost: " + BuildEngineShopMove.BUILD_COST);
        dialog.setContentText("Choose a station:");
        dialog.showAndWait().ifPresent(choice -> {
            int selectedIndex = choices.indexOf(choice);
            if (selectedIndex < 0) {
                showError.accept("The selected station is no longer available.");
                return;
            }
            Station station = availableStations.get(selectedIndex);
            Status status;
            synchronized (updateLock) {
                UnmodifiableWorld world = modelRoot.getWorld();
                int nextId = world.getEngineShops(modelRoot.getPlayer()).stream()
                        .mapToInt(EngineShop::getId).max().orElse(-1) + 1;
                status = modelRoot.applyMove(new BuildEngineShopMove(
                        modelRoot.getPlayer(), new EngineShop(nextId, station.getLocation())));
            }
            if (!status.isSuccess()) {
                showError.accept(status.getMessage());
            }
        });
    }

    /**
     * Builds an available industry on the current map cursor tile.
     */
    void showBuildIndustry() {
        List<Terrain> options;
        synchronized (updateLock) {
            options = modelRoot.getWorld().getTerrains().stream()
                    .filter(terrain -> terrain.getIndustryBuildCost() != null)
                    .toList();
        }
        if (options.isEmpty()) {
            showError.accept("No industries can be built in this game.");
            return;
        }
        List<String> labels = options.stream().map(terrain -> "Build " + terrain.getName()
                + " ($" + terrain.getIndustryBuildCost() + ")").toList();
        ChoiceDialog<String> dialog = new ChoiceDialog<>(labels.getFirst(), labels);
        dialog.initOwner(owner);
        dialog.setTitle("Build Industry");
        dialog.setHeaderText("Choose an industry for the selected map tile");
        dialog.showAndWait().ifPresent(choice -> {
            int index = labels.indexOf(choice);
            if (index < 0) {
                showError.accept("The selected industry is no longer available.");
                return;
            }
            String validationError = null;
            Status status = null;
            synchronized (updateLock) {
                Vector2D location = (Vector2D) modelRoot.getProperty(ModelRootProperty.CURSOR_POSITION);
                UnmodifiableWorld world = modelRoot.getWorld();
                if (location == null || !world.boundsContain(location)) {
                    validationError = "Select a valid map tile before building an industry.";
                } else {
                    Terrain terrain = options.get(index);
                    Move changeTile = new ChangeTileMove(
                            new TerrainTile(terrain.getId(), world.getTile(location).trackPiece()), location);
                    ItemTransaction transaction = new ItemTransaction(
                            TransactionCategory.INDUSTRIES, Money.opposite(terrain.getIndustryBuildCost()),
                            world.getClock().getCurrentTime(), 1, terrain.getId());
                    status = modelRoot.applyMove(new CompostMove(List.of(
                            changeTile, new AddTransactionMove(modelRoot.getPlayer(), transaction))));
                }
            }
            if (validationError != null) {
                showError.accept(validationError);
            } else if (status != null && !status.isSuccess()) {
                showError.accept(status.getMessage());
            }
        });
    }

    /**
     * Configures train production at a selected engine shop.
     */
    void showBuildTrain() {
        List<EngineShop> shops;
        List<Engine> engines;
        List<Cargo> cargoTypes;
        synchronized (updateLock) {
            UnmodifiableWorld world = modelRoot.getWorld();
            shops = List.copyOf(world.getEngineShops(modelRoot.getPlayer()));
            engines = List.copyOf(world.getEngines());
            cargoTypes = List.copyOf(world.getCargos());
        }
        if (shops.isEmpty()) {
            showError.accept("Build an engine shop at a station before building trains.");
            return;
        }
        if (engines.isEmpty() || cargoTypes.isEmpty()) {
            showError.accept("This game has no available engine or wagon types.");
            return;
        }

        List<String> shopLabels = shops.stream().map(shop -> "Engine Shop " + shop.getId()
                + " at " + shop.getLocation()).toList();
        ChoiceDialog<String> shopDialog = new ChoiceDialog<>(shopLabels.getFirst(), shopLabels);
        shopDialog.initOwner(owner);
        shopDialog.setTitle("Build Train");
        shopDialog.setHeaderText("Choose an engine shop");
        var shopChoice = shopDialog.showAndWait();
        if (shopChoice.isEmpty()) {
            return;
        }
        int shopIndex = shopLabels.indexOf(shopChoice.get());
        if (shopIndex < 0) {
            showError.accept("The selected engine shop is no longer available.");
            return;
        }

        List<String> engineLabels = engines.stream()
                .map(engine -> engine.getName() + " (" + engine.getPrice() + ")").toList();
        ChoiceDialog<String> engineDialog = new ChoiceDialog<>(engineLabels.getFirst(), engineLabels);
        engineDialog.initOwner(owner);
        engineDialog.setTitle("Build Train");
        engineDialog.setHeaderText("Choose an engine");
        var engineChoice = engineDialog.showAndWait();
        if (engineChoice.isEmpty()) {
            return;
        }
        int engineIndex = engineLabels.indexOf(engineChoice.get());
        if (engineIndex < 0) {
            showError.accept("The selected engine is no longer available.");
            return;
        }

        List<Integer> wagonTypes = chooseWagons(cargoTypes);
        if (wagonTypes == null) {
            return;
        }
        Status status;
        synchronized (updateLock) {
            status = modelRoot.applyMove(new ChangeProductionAtEngineShopMove(
                    List.of(new TrainTemplate(engines.get(engineIndex).getId(), wagonTypes)),
                    shops.get(shopIndex).getId(), modelRoot.getPlayer()));
        }
        if (!status.isSuccess()) {
            showError.accept(status.getMessage());
        }
    }

    /**
     * Shows the current orders assigned to the selected train.
     *
     * @param train train whose schedule is displayed
     */
    void showTrainOrders(Train train) {
        ListView<TrainOrder> orders = new ListView<>();
        orders.setPrefSize(460, 240);
        orders.setCellFactory(list -> new ListCell<>() {
            /**
             * Displays a train stop with its current-order marker.
             *
             * @param order train stop
             * @param empty whether the cell has no item
             */
            @Override
            protected void updateItem(TrainOrder order, boolean empty) {
                super.updateItem(order, empty);
                if (empty || order == null) {
                    setText(null);
                } else {
                    setText((order.isOneShot() ? "Urgent: " : "")
                            + stationName(order.getStationId())
                            + " — " + order.getLoadingPolicy()
                            + (order.isWaitUntilFull() ? ", wait until full" : ""));
                }
            }
        });
        Runnable refresh = () -> {
            List<TrainOrder> snapshot = new ArrayList<>();
            int currentIndex;
            synchronized (updateLock) {
                var schedule = trainSchedule(train.getId());
                for (int index = 0; index < schedule.getNumberOfOrders(); index++) {
                    snapshot.add(schedule.getOrder(index));
                }
                currentIndex = schedule.getCurrentOrderIndex();
            }
            if (!orders.getItems().equals(snapshot)) {
                orders.getItems().setAll(snapshot);
                if (currentIndex >= 0) {
                    orders.getSelectionModel().select(currentIndex);
                }
            }
        };
        Button addStop = new Button("Add stop");
        addStop.setOnAction(event -> addTrainStop(train.getId(), false, refresh));
        Button urgentStop = new Button("Urgent stop");
        urgentStop.setOnAction(event -> addTrainStop(train.getId(), true, refresh));
        Button goTo = new Button("Go to now");
        goTo.setOnAction(event -> updateSelectedOrder(train.getId(), orders, refresh,
                schedule -> schedule.setOrderToGoto(orders.getSelectionModel().getSelectedIndex())));
        Button changeStation = new Button("Change station");
        changeStation.setOnAction(event -> changeTrainStopStation(train.getId(), orders, refresh));
        Button removeStop = new Button("Remove stop");
        removeStop.setOnAction(event -> updateSelectedOrder(train.getId(), orders, refresh,
                schedule -> schedule.removeOrder(orders.getSelectionModel().getSelectedIndex())));
        Button moveUp = new Button("Move up");
        moveUp.setOnAction(event -> updateSelectedOrder(train.getId(), orders, refresh,
                schedule -> schedule.pullUp(orders.getSelectionModel().getSelectedIndex())));
        Button moveDown = new Button("Move down");
        moveDown.setOnAction(event -> updateSelectedOrder(train.getId(), orders, refresh,
                schedule -> schedule.pushDown(orders.getSelectionModel().getSelectedIndex())));
        Button keepConsist = new Button("Keep consist");
        keepConsist.setOnAction(event -> updateSelectedOrder(train.getId(), orders, refresh,
                schedule -> schedule.setOrder(orders.getSelectionModel().getSelectedIndex(),
                        selectedOrder(schedule, orders).withPolicy(
                                TrainOrder.LoadingPolicy.KEEP_CONSIST, List.of(), false))));
        Button autoConsist = new Button("Auto consist");
        autoConsist.setOnAction(event -> updateSelectedOrder(train.getId(), orders, refresh,
                schedule -> schedule.setOrder(orders.getSelectionModel().getSelectedIndex(),
                        selectedOrder(schedule, orders).withPolicy(
                                TrainOrder.LoadingPolicy.AUTO_CONSIST, List.of(), false))));
        Button toggleWait = new Button("Toggle wait until full");
        toggleWait.setOnAction(event -> updateSelectedOrder(
                train.getId(), orders, refresh, schedule -> {
                    TrainOrder order = selectedOrder(schedule, orders);
                    if (!order.hasSpecifiedConsist() || order.getConsist().isEmpty()) {
                        throw new IllegalStateException(
                                "Select a stop with a specified wagon consist first.");
                    }
                    return schedule.setOrder(orders.getSelectionModel().getSelectedIndex(),
                            order.withPolicy(TrainOrder.LoadingPolicy.SPECIFIED_CONSIST,
                                    order.getConsist(), !order.isWaitUntilFull()));
                }));
        Button addWagon = new Button("Add wagon");
        addWagon.setOnAction(event -> addWagonToStop(train.getId(), orders, refresh));
        Button removeWagon = new Button("Remove last wagon");
        removeWagon.setOnAction(event -> updateSelectedOrder(train.getId(), orders, refresh, schedule -> {
            TrainOrder order = selectedOrder(schedule, orders);
            if (!order.hasSpecifiedConsist() || order.getConsist().isEmpty()) {
                throw new IllegalStateException("The selected stop has no specified wagons.");
            }
            List<Integer> consist = new ArrayList<>(order.getConsist());
            consist.removeLast();
            return schedule.setOrder(orders.getSelectionModel().getSelectedIndex(),
                    order.withPolicy(TrainOrder.LoadingPolicy.SPECIFIED_CONSIST,
                            consist, order.isWaitUntilFull() && !consist.isEmpty()));
        }));
        VBox actions = new VBox(6, addStop, urgentStop, goTo, changeStation, removeStop,
                moveUp, moveDown, keepConsist, autoConsist, toggleWait, addWagon, removeWagon);
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("Train Orders");
        dialog.setHeaderText("Train " + train.getId() + " schedule");
        ScrollPane actionPane = new ScrollPane(actions);
        actionPane.setFitToWidth(true);
        actionPane.setPrefWidth(175);
        dialog.getDialogPane().setContent(new HBox(10, orders, actionPane));
        dialog.getDialogPane().setPrefSize(760, 420);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        Timeline scheduleRefresh = new Timeline(new KeyFrame(Duration.millis(250), event -> refresh.run()));
        scheduleRefresh.setCycleCount(Timeline.INDEFINITE);
        dialog.setOnShown(event -> scheduleRefresh.play());
        dialog.setOnHidden(event -> scheduleRefresh.stop());
        refresh.run();
        dialog.showAndWait();
        scheduleRefresh.stop();
    }

    /**
     * Displays the balance-sheet generator values in a JavaFX dialog.
     *
     * @param title report title
     */
    void showBalanceSheet(String title) {
        String report;
        try {
            synchronized (updateLock) {
                var generator = new BalanceSheetGenerator(modelRoot.getWorld(), modelRoot.getPlayer());
                report = populateTemplate(ClientConstants.VIEW_BALANCE_SHEET, generator.getTemplateValues());
            }
        } catch (IllegalStateException exception) {
            showError.accept(exception.getMessage());
            return;
        }
        showHtmlDialog(title, report);
    }

    /**
     * Displays the income-statement generator values in a JavaFX dialog.
     *
     * @param title report title
     */
    void showIncomeStatement(String title) {
        String report;
        try {
            synchronized (updateLock) {
                var generator = new IncomeStatementGenerator(modelRoot.getWorld(), modelRoot.getPlayer());
                generator.calculateAll();
                report = populateTemplate(ClientConstants.VIEW_INCOME_STATEMENT, generator.getTemplateValues());
            }
        } catch (IllegalStateException exception) {
            showError.accept(exception.getMessage());
            return;
        }
        showHtmlDialog(title, report);
    }

    /**
     * Displays players ranked by net worth and station count.
     *
     * @param title report title
     */
    void showLeaderboard(String title) {
        StringBuilder report = new StringBuilder();
        synchronized (updateLock) {
            UnmodifiableWorld world = modelRoot.getWorld();
            Time[] times = {Time.ZERO, world.getClock().getCurrentTime()};
            List<Player> players = new ArrayList<>(world.getPlayers());
            java.util.            Map<Player, Money> netWorths = new HashMap<>();
            for (Player player : players) {
                var netWorth = new NetWorthAggregator(world, player, times);
                netWorth.aggregate();
                netWorths.put(player, netWorth.getValues()[0]);
            }
            players.sort((first, second) -> {
                return Long.compare(netWorths.get(second).amount(), netWorths.get(first).amount());
            });
            for (Player player : players) {
                report.append(player.getName()).append(" — ")
                        .append(MoneyFormatter.format(netWorths.get(player)))
                        .append(" net worth, ")
                        .append(world.getStations(player).size()).append(" stations\n");
            }
        }
        showTextDialog(title, report.toString());
    }

    /**
     * Displays each player's yearly net-worth data in a JavaFX line chart.
     *
     * @param title report title
     */
    void showNetWorthGraph(String title) {
        LineChart<Number, Number> chart;
        synchronized (updateLock) {
            UnmodifiableWorld world = modelRoot.getWorld();
            int startYear = world.getClock().getStartYear();
            int currentYear = world.getClock().getCurrentYear();
            NumberAxis xAxis = new NumberAxis(startYear, startYear + 100, 10);
            NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Year");
            yAxis.setLabel("Net worth");
            chart = new LineChart<>(xAxis, yAxis);
            chart.setTitle(title);
            chart.setCreateSymbols(false);
            for (Player player : world.getPlayers()) {
                Time[] times = new Time[101];
                for (int year = 0; year <= 100; year++) {
                    times[year] = world.getClock().getTimeAtStartOfYear(startYear + year);
                }
                var aggregator = new NetWorthAggregator(world, player, times);
                aggregator.aggregate();
                XYChart.Series<Number, Number> series = new XYChart.Series<>();
                series.setName(player.getName());
                var values = aggregator.getValues();
                int lastYear = Math.min(100, currentYear - startYear);
                for (int year = 0; year <= lastYear; year++) {
                    series.getData().add(new XYChart.Data<>(startYear + year, values[year].amount()));
                }
                chart.getData().add(series);
            }
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle(title);
        dialog.getDialogPane().setContent(chart);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    /**
     * Presents bond and share-trading actions backed by the existing transaction rules.
     */
    void showBroker() {
        WebView brokerView = new WebView();
        brokerView.setPrefSize(560, 260);
        Label summary = new Label();
        ComboBox<Player> targetPlayer = new ComboBox<>();
        targetPlayer.setCellFactory(list -> new ListCell<>() {
            /**
             * Displays a railroad's player name.
             *
             * @param player player
             * @param empty whether the cell has no item
             */
            @Override
            protected void updateItem(Player player, boolean empty) {
                super.updateItem(player, empty);
                setText(empty || player == null ? null : player.getName());
            }
        });
        targetPlayer.setButtonCell(new ListCell<>() {
            /**
             * Displays the selected railroad's player name.
             *
             * @param player player
             * @param empty whether the cell has no item
             */
            @Override
            protected void updateItem(Player player, boolean empty) {
                super.updateItem(player, empty);
                setText(empty || player == null ? null : player.getName());
            }
        });
        List<Player> players;
        synchronized (updateLock) {
            players = List.copyOf(modelRoot.getWorld().getPlayers());
        }
        targetPlayer.getItems().setAll(players);
        if (!players.isEmpty()) {
            targetPlayer.setValue(players.getFirst());
        }

        Button buy = new Button("Buy 10,000 shares");
        Button sell = new Button("Sell 10,000 shares");
        Button issueBond = new Button("Issue bond");
        Button repayBond = new Button("Repay bond");
        Runnable refresh = () -> refreshBrokerControls(
                brokerView, summary, targetPlayer, buy, sell, issueBond, repayBond);
        targetPlayer.setOnAction(event -> refresh.run());
        buy.setOnAction(event -> {
            tradeShares(targetPlayer.getValue(), ModelConstants.STOCK_BUNDLE_SIZE);
            refresh.run();
        });
        sell.setOnAction(event -> {
            tradeShares(targetPlayer.getValue(), -ModelConstants.STOCK_BUNDLE_SIZE);
            refresh.run();
        });
        issueBond.setOnAction(event -> {
            changeBond(true);
            refresh.run();
        });
        repayBond.setOnAction(event -> {
            changeBond(false);
            refresh.run();
        });
        refresh.run();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("Broker");
        dialog.setHeaderText("Manage company finances and shares");
        dialog.getDialogPane().setContent(new VBox(10, brokerView, summary, targetPlayer,
                new HBox(8, buy, sell), new HBox(8, issueBond, repayBond)));
        dialog.getDialogPane().setPrefSize(620, 480);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    /**
     * Adds a regular or one-shot stop after choosing an owned station.
     *
     * @param trainId train receiving the stop
     * @param urgent whether to insert it as an urgent stop
     * @param refresh reloads the displayed schedule after the move
     */
    private void addTrainStop(int trainId, boolean urgent, Runnable refresh) {
        Station station = chooseStation();
        if (station == null) {
            return;
        }
        String error = null;
        synchronized (updateLock) {
            try {
                ScheduleState schedule = trainSchedule(trainId);
                ScheduleState updated;
                if (urgent) {
                    if (!schedule.canAddUrgentOrder()) {
                        error = "An urgent stop already exists or the schedule is full.";
                        updated = null;
                    } else {
                        updated = schedule.addUrgentOrder(TrainOrder.keepConsist(station.getId()));
                    }
                } else {
                    if (!schedule.canAddOrder()) {
                        error = "The train schedule is full.";
                        updated = null;
                    } else {
                        updated = schedule.addOrder(TrainOrder.keepConsist(station.getId()));
                    }
                }
                if (updated != null) {
                    Status status = applySchedule(trainId, updated);
                    if (!status.isSuccess()) {
                        error = status.getMessage();
                    }
                }
            } catch (IllegalStateException exception) {
                error = exception.getMessage();
            }
        }
        if (error != null) {
            showError.accept(error);
            return;
        }
        refresh.run();
    }

    /**
     * Changes the station for the selected train order.
     *
     * @param trainId train whose order is changed
     * @param orders displayed stop list
     * @param refresh reloads the displayed schedule after the move
     */
    private void changeTrainStopStation(int trainId, ListView<TrainOrder> orders, Runnable refresh) {
        if (orders.getSelectionModel().getSelectedIndex() < 0) {
            showError.accept("Select a train stop first.");
            return;
        }
        Station station = chooseStation();
        if (station == null) {
            return;
        }
        updateSelectedOrder(trainId, orders, refresh, schedule ->
                schedule.setOrder(orders.getSelectionModel().getSelectedIndex(),
                        selectedOrder(schedule, orders).withStation(station.getId())));
    }

    /**
     * Adds one selected cargo type to the selected stop's consist.
     *
     * @param trainId train receiving the wagon
     * @param orders displayed stop list
     * @param refresh reloads the displayed schedule after the move
     */
    private void addWagonToStop(int trainId, ListView<TrainOrder> orders, Runnable refresh) {
        if (orders.getSelectionModel().getSelectedIndex() < 0) {
            showError.accept("Select a train stop first.");
            return;
        }
        List<Cargo> cargoTypes;
        synchronized (updateLock) {
            cargoTypes = List.copyOf(modelRoot.getWorld().getCargos());
        }
        List<String> labels = cargoTypes.stream().map(Cargo::getName).toList();
        if (labels.isEmpty()) {
            showError.accept("This game has no wagon types.");
            return;
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(labels.getFirst(), labels);
        dialog.initOwner(owner);
        dialog.setTitle("Add Wagon");
        dialog.setHeaderText("Choose a wagon type");
        dialog.showAndWait().ifPresent(choice -> {
            int cargoIndex = labels.indexOf(choice);
            if (cargoIndex < 0) {
                showError.accept("The selected wagon type is no longer available.");
                return;
            }
            int wagonType = cargoTypes.get(cargoIndex).getId();
            updateSelectedOrder(trainId, orders, refresh, schedule -> {
                TrainOrder order = selectedOrder(schedule, orders);
                List<Integer> consist = new ArrayList<>(
                        order.hasSpecifiedConsist() ? order.getConsist() : List.of());
                if (consist.size() >= ModelConstants.MAX_NUMBER_OF_WAGONS) {
                    throw new IllegalStateException("The selected stop already has the maximum number of wagons.");
                }
                consist.add(wagonType);
                return schedule.setOrder(orders.getSelectionModel().getSelectedIndex(),
                        order.withPolicy(TrainOrder.LoadingPolicy.SPECIFIED_CONSIST,
                                consist, order.isWaitUntilFull()));
            });
        });
    }

    /**
     * Applies an immutable schedule update for the selected order.
     *
     * @param trainId train whose schedule is changed
     * @param orders displayed stop list
     * @param refresh reloads the displayed schedule after the move
     * @param update creates the updated schedule
     */
    private void updateSelectedOrder(
            int trainId,
            ListView<TrainOrder> orders,
            Runnable refresh,
            UnaryOperator<ScheduleState> update) {
        int selectedIndex = orders.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) {
            showError.accept("Select a train stop first.");
            return;
        }
        String error = null;
        synchronized (updateLock) {
            try {
                ScheduleState current = trainSchedule(trainId);
                if (selectedIndex >= current.getNumberOfOrders()) {
                    error = "The selected train stop no longer exists.";
                } else {
                    Status status = applySchedule(trainId, update.apply(current));
                    if (!status.isSuccess()) {
                        error = status.getMessage();
                    }
                }
            } catch (IllegalArgumentException | IllegalStateException exception) {
                error = exception.getMessage();
            }
        }
        if (error != null) {
            showError.accept(error);
            return;
        }
        refresh.run();
    }

    /**
     * Returns the currently selected order from a schedule.
     *
     * @param schedule current immutable schedule
     * @param orders displayed stop list
     * @return selected train order
     */
    private static TrainOrder selectedOrder(ScheduleState schedule, ListView<TrainOrder> orders) {
        int index = orders.getSelectionModel().getSelectedIndex();
        if (index < 0 || index >= schedule.getNumberOfOrders()) {
            throw new IllegalStateException("Select a train stop first.");
        }
        return schedule.getOrder(index);
    }

    /**
     * Copies the latest train schedule under the game-state lock.
     *
     * @param trainId stable train identifier
     * @return mutable schedule value for a move
     */
    private ScheduleState trainSchedule(int trainId) {
        Train train = modelRoot.getWorld().getTrain(modelRoot.getPlayer(), trainId);
        return new ScheduleState(train.getSchedule());
    }

    /**
     * Sends an updated train schedule to the authoritative server.
     *
     * @param trainId stable train identifier
     * @param schedule updated schedule
     */
    private Status applySchedule(int trainId, ScheduleState schedule) {
        return modelRoot.applyMove(
                new UpdateTrainMove(modelRoot.getPlayer(), trainId, null, null, schedule));
    }

    /**
     * Prompts for a station owned by the current player.
     *
     * @return selected station, or null when cancelled or unavailable
     */
    private Station chooseStation() {
        List<Station> stations;
        synchronized (updateLock) {
            stations = List.copyOf(modelRoot.getWorld().getStations(modelRoot.getPlayer()));
        }
        if (stations.isEmpty()) {
            showError.accept("Build a station before editing a train schedule.");
            return null;
        }
        List<String> labels = stations.stream().map(this::stationLabel).toList();
        ChoiceDialog<String> dialog = new ChoiceDialog<>(labels.getFirst(), labels);
        dialog.initOwner(owner);
        dialog.setTitle("Select Station");
        dialog.setHeaderText("Choose a station");
        var result = dialog.showAndWait();
        if (result.isEmpty()) {
            return null;
        }
        int index = labels.indexOf(result.get());
        return index < 0 ? null : stations.get(index);
    }

    /**
     * Resolves a station label for a train order.
     *
     * @param stationId station identifier
     * @return station name or a stable identifier fallback
     */
    private String stationName(int stationId) {
        synchronized (updateLock) {
            return modelRoot.getWorld().getStations(modelRoot.getPlayer()).stream()
                    .filter(station -> station.getId() == stationId)
                    .map(Station::getStationName)
                    .findFirst()
                    .orElse("Station " + stationId);
        }
    }

    /**
     * Refreshes the broker balances and enables actions allowed by current holdings.
     *
     * @param summary current company details
     * @param target selected railroad
     * @param buy buy-share button
     * @param sell sell-share button
     * @param issueBond issue-bond button
     * @param repayBond repay-bond button
     */
    private void refreshBrokerControls(
            WebView brokerView,
            Label summary,
            ComboBox<Player> target,
            Button buy,
            Button sell,
            Button issueBond,
            Button repayBond) {
        String html;
        String details;
        boolean canIssueBond;
        boolean canRepayBond;
        boolean canBuy = false;
        boolean canSell = false;
        try {
            synchronized (updateLock) {
                UnmodifiableWorld world = modelRoot.getWorld();
                Player player = modelRoot.getPlayer();
                FinancialDataAggregator ownData = new FinancialDataAggregator(world, player);
                html = brokerHtml(world);
                details = "Cash: " + world.getCurrentBalance(player)
                        + "\nOutstanding bonds: " + ownData.getBonds();
                canIssueBond = ownData.canIssueBond();
                canRepayBond = ownData.getBonds() > 0;
                Player selected = target.getValue();
                if (selected != null) {
                    int playerIndex = world.getPlayerIndex(selected.getId());
                    FinancialDataAggregator selectedData = new FinancialDataAggregator(world, selected);
                    StockPrice price = new StockPriceCalculator(world).calculate()[playerIndex];
                    details += "\n" + selected.getName() + " share price: " + price.currentPrice
                            + "\nShares held: " + ownData.getStockInRRs()[playerIndex];
                    Money buyPrice = selected.equals(player) ? price.treasuryBuyPrice : price.buyPrice;
                    canBuy = selectedData.sharesHeldByPublic() >= ModelConstants.STOCK_BUNDLE_SIZE;
                    if (selected.equals(player)) {
                        canBuy &= ModelConstants.STOCK_BUNDLE_SIZE + selectedData.treasuryStock()
                                < selectedData.totalShares();
                    }
                    canBuy &= buyPrice.amount()
                            <= world.getCurrentBalance(player).amount() / ModelConstants.STOCK_BUNDLE_SIZE;
                    canSell = ownData.getStockInRRs()[playerIndex] >= ModelConstants.STOCK_BUNDLE_SIZE;
                }
            }
        } catch (IllegalStateException exception) {
            showError.accept(exception.getMessage());
            return;
        }
        brokerView.getEngine().loadContent(html);
        summary.setText(details);
        issueBond.setDisable(!canIssueBond);
        repayBond.setDisable(!canRepayBond);
        buy.setDisable(!canBuy);
        sell.setDisable(!canSell);
    }

    /**
     * Applies a stock transaction for the selected railroad.
     *
     * @param target railroad whose shares are traded
     * @param quantity positive for buying, negative for selling
     */
    private void tradeShares(Player target, int quantity) {
        if (target == null) {
            showError.accept("Select a railroad before trading shares.");
            return;
        }
        Status status;
        synchronized (updateLock) {
            UnmodifiableWorld world = modelRoot.getWorld();
            Player player = modelRoot.getPlayer();
            int index = world.getPlayerIndex(target.getId());
            StockPrice stockPrice = new StockPriceCalculator(world).calculate()[index];
            Money sharePrice = target.equals(player)
                    ? (quantity > 0 ? stockPrice.treasuryBuyPrice : stockPrice.treasurySellPrice)
                    : (quantity > 0 ? stockPrice.buyPrice : stockPrice.sellPrice);
            ItemTransaction transaction = TransactionUtils.buyOrSellStock(
                    target.getId(), quantity, sharePrice, world.getClock().getCurrentTime());
            status = modelRoot.applyMove(new AddTransactionMove(player, transaction));
        }
        if (!status.isSuccess()) {
            showError.accept(status.getMessage());
        }
    }

    /**
     * Issues or repays one bond using the existing transaction rules.
     *
     * @param issue true to issue, false to repay
     */
    private void changeBond(boolean issue) {
        String error = null;
        Status status = null;
        synchronized (updateLock) {
            UnmodifiableWorld world = modelRoot.getWorld();
            Player player = modelRoot.getPlayer();
            FinancialDataAggregator data = new FinancialDataAggregator(world, player);
            ItemTransaction transaction;
            if (issue) {
                if (!data.canIssueBond()) {
                    error = "The maximum number of bonds is already outstanding.";
                    transaction = null;
                } else {
                    transaction = TransactionUtils.issueBond(
                            data.nextBondInterestRate(), world.getClock().getCurrentTime());
                }
            } else {
                if (data.getBonds() <= 0) {
                    error = "There are no bonds to repay.";
                    transaction = null;
                } else {
                    transaction = TransactionUtils.repayBond(
                            5, world.getClock().getCurrentTime());
                }
            }
            if (transaction != null) {
                status = modelRoot.applyMove(new AddTransactionMove(player, transaction));
            }
        }
        if (error != null) {
            showError.accept(error);
        } else if (!status.isSuccess()) {
            showError.accept(status.getMessage());
        }
    }

    /**
     * Adds, removes, or clears wagon choices before committing train production.
     *
     * @param cargoTypes available wagon cargo types
     * @return selected cargo type IDs or null if cancelled
     */
    private List<Integer> chooseWagons(List<Cargo> cargoTypes) {
        Dialog<List<Integer>> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("Build Train");
        dialog.setHeaderText("Add up to " + ModelConstants.MAX_NUMBER_OF_WAGONS + " wagons");
        ButtonType build = new ButtonType("Queue Train", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(build, ButtonType.CANCEL);

        ComboBox<Cargo> cargoSelector = new ComboBox<>(FXCollections.observableArrayList(cargoTypes));
        cargoSelector.setCellFactory(list -> cargoCell());
        cargoSelector.setButtonCell(cargoCell());
        ListView<Cargo> consist = new ListView<>();
        consist.setPrefHeight(150);
        Button add = new Button("Add wagon");
        Button remove = new Button("Remove selected");
        add.setOnAction(event -> {
            if (cargoSelector.getValue() == null) {
                return;
            }
            if (consist.getItems().size() >= ModelConstants.MAX_NUMBER_OF_WAGONS) {
                showError.accept("The maximum train length is "
                        + ModelConstants.MAX_NUMBER_OF_WAGONS + " wagons.");
                return;
            }
            consist.getItems().add(cargoSelector.getValue());
        });
        remove.setOnAction(event -> {
            int selected = consist.getSelectionModel().getSelectedIndex();
            if (selected >= 0) {
                consist.getItems().remove(selected);
            }
        });
        HBox buttons = new HBox(8, add, remove);
        dialog.getDialogPane().setContent(new VBox(8, cargoSelector, consist, buttons));
        dialog.setResultConverter(button -> button == build
                ? consist.getItems().stream().map(Cargo::getId).toList()
                : null);
        return dialog.showAndWait().orElse(null);
    }

    /**
     * Creates a cargo cell that displays its user-facing name.
     *
     * @return cargo list cell
     */
    private static ListCell<Cargo> cargoCell() {
        return new ListCell<>() {
            /**
             * Displays the selected cargo type by name.
             *
             * @param cargo cargo type
             * @param empty whether the cell has no item
             */
            @Override
            protected void updateItem(Cargo cargo, boolean empty) {
                super.updateItem(cargo, empty);
                setText(empty || cargo == null ? null : cargo.getName());
            }
        };
    }

    /**
     * Populates a JavaFX WebView from an existing report template and token map.
     *
     * @param resource classpath resource path
     * @param values template tokens
     * @return populated HTML report
     */
    static String populateTemplate(String resource, Map<String, ?> values) {
        URL url = JavaFxGameplayDialogs.class.getResource(resource);
        if (url == null) {
            throw new IllegalStateException("Could not find report template " + resource);
        }
        String template;
        try (InputStream input = url.openStream()) {
            template = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read report template " + resource, exception);
        }
        return replaceTemplateTokens(template, values);
    }

    /**
     * Replaces every report template token and fails when a value is missing.
     *
     * @param template HTML template string
     * @param values template token values
     * @return populated template
     */
    static String replaceTemplateTokens(String template, Map<String, ?> values) {
        Matcher matcher = TEMPLATE_TOKEN.matcher(template);
        StringBuffer output = new StringBuffer();
        while (matcher.find()) {
            String token = matcher.group(1);
            if (!values.containsKey(token)) {
                throw new IllegalStateException("No value provided for report field " + token);
            }
            matcher.appendReplacement(output,
                    Matcher.quoteReplacement(String.valueOf(values.get(token))));
        }
        matcher.appendTail(output);
        return output.toString();
    }

    /**
     * Renders the broker template for the local player and other companies.
     *
     * @param world current game world
     * @return combined broker HTML
     */
    private static String brokerHtml(UnmodifiableWorld world) {
        StringBuilder html = new StringBuilder("<html>");
        for (Player player : world.getPlayers()) {
            html.append(populateTemplate(
                    ClientConstants.VIEW_BROKER,
                    new BrokerScreenGenerator(world, player).getTemplateValues()));
        }
        return html.append("</html>").toString();
    }

    /**
     * Opens a JavaFX WebView dialog for a populated HTML report.
     *
     * @param title report title
     * @param html report document
     */
    private void showHtmlDialog(String title, String html) {
        WebView view = new WebView();
        view.getEngine().loadContent(html);
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle(title);
        dialog.getDialogPane().setContent(view);
        dialog.getDialogPane().setPrefSize(620, 480);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    /**
     * Opens a scrollable JavaFX text dialog.
     *
     * @param title dialog title
     * @param text dialog contents
     */
    private void showTextDialog(String title, String text) {
        TextArea area = new TextArea(text);
        area.setEditable(false);
        area.setWrapText(true);
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle(title);
        dialog.getDialogPane().setContent(area);
        dialog.getDialogPane().setPrefSize(620, 480);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    /**
     * Requests the latest server-side saved-game names.
     */
    private void requestSavedGames() {
        modelRoot.sendCommand(new RefreshListOfGamesCommandToServer());
    }

    /**
     * Reads and displays the saved-game property with strict type validation.
     */
    private void updateSavedGamesList() {
        List<String> names;
        synchronized (updateLock) {
            Object value = modelRoot.getProperty(ModelRootProperty.SAVED_GAMES_LIST);
            if (!(value instanceof List<?> values)) {
                showError.accept("The server returned an invalid saved-games list.");
                return;
            }
            names = new ArrayList<>(values.size());
            for (Object item : values) {
                if (!(item instanceof String name)) {
                    showError.accept("The server returned an invalid saved-game entry.");
                    return;
                }
                names.add(name);
            }
        }
        savedGamesList.getItems().setAll(names);
        if (loadButton != null) {
            loadButton.setDisable(savedGamesList.getSelectionModel().getSelectedItem() == null);
        }
    }

    /**
     * Formats a station name and location for station selection dialogs.
     *
     * @param station station to describe
     * @return station label
     */
    private String stationLabel(Station station) {
        Vector2D location = station.getLocation();
        return station.getStationName() + " (" + location.x() + ", " + location.y() + ")";
    }
}
