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

package freerails.client.componentfactory;

import freerails.client.UserInputOnMapController;
import freerails.client.UserMessageGenerator;
import freerails.util.ui.ActionAdapter;
import freerails.util.ui.ActionAdapter.MappedButtonModel;
import freerails.client.ModelRootImpl;
import freerails.client.model.ServerControlModel;
import freerails.client.renderer.map.DetailMapRenderer;
import freerails.client.renderer.map.MapViewComponentConcrete;
import freerails.controller.BuildTrackController;
import freerails.client.renderer.map.MapRenderer;
import freerails.client.renderer.RendererRoot;
import freerails.client.renderer.map.ZoomedOutMapRenderer;
import freerails.client.view.*;
import freerails.controller.ModelRoot;
import freerails.move.ChangeGameSpeedMove;
import freerails.network.LocalConnection;
import freerails.util.Vector2D;
import freerails.world.*;
import freerails.world.game.GameSpeed;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.world.ReadOnlyWorld;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Creates and wires up the GUI components.
 */
public class GUIComponentFactoryImpl implements GUIComponentFactory, WorldMapListener, WorldListListener {

    /**
     * Whether to show certain 'cheat' menus used for testing.
     */
    private static final Logger logger = Logger.getLogger(GUIComponentFactoryImpl.class.getName());
    private final ActionRoot actionRoot;
    private final BuildMenu buildMenu;
    private final CashLabel cashlabel;
    private final ClientFrame clientFrame;
    private final DateLabel datelabel;
    private final DialogueBoxController dialogueBoxController;
    private final JScrollPane mainMapScrollPane1;
    private final MapViewComponentConcrete mapViewJComponent;
    private final ModelRootImpl modelRoot;
    private final JPanel overviewMapContainer;
    private final StationTypesPopup stationTypesPopup;
    /**
     * This is the panel at the bottom right of the screen.
     */
    private final RHSTabPane trainsJTabPane;
    private final UserInputOnMapController userInputOnMapController;
    private final UserMessageGenerator userMessageGenerator;
    private boolean isSetup = false;
    private DetailMapRenderer mainMap;
    private MapRenderer overviewMap;
    private ServerControlModel sc;
    private ActionAdapter speedActions;
    private JMenuItem stationInfoJMenuItem;
    private JMenuItem trainListJMenuItem;
    private JMenuItem trainOrdersJMenuItem;
    private ReadOnlyWorld world;

    /**
     * @param mr
     * @param ar
     */
    public GUIComponentFactoryImpl(ModelRootImpl mr, ActionRoot ar) {
        modelRoot = mr;
        actionRoot = ar;
        userInputOnMapController = new UserInputOnMapController(modelRoot, ar);
        buildMenu = new BuildMenu();
        mapViewJComponent = new MapViewComponentConcrete();
        mainMapScrollPane1 = new JScrollPane();
        Rectangle r = new Rectangle(10, 10, 10, 10);
        overviewMapContainer = new OverviewMapComponent(r);
        stationTypesPopup = new StationTypesPopup();

        MainMapAndOverviewMapMediator mediator = new MainMapAndOverviewMapMediator();
        mediator.setup(overviewMapContainer, mainMapScrollPane1.getViewport(), mapViewJComponent, r);

        trainsJTabPane = new RHSTabPane();
        datelabel = new DateLabel();

        cashlabel = new CashLabel();

        clientFrame = new ClientFrame(this);
        dialogueBoxController = new DialogueBoxController(clientFrame, modelRoot);
        actionRoot.setDialogueBoxController(dialogueBoxController);

        modelRoot.addSplitMoveReceiver(move -> {
            if (move instanceof ChangeGameSpeedMove) {
                ChangeGameSpeedMove speedMove = (ChangeGameSpeedMove) move;

                for (Action action: speedActions.getActions()) {
                    String actionName = (String) action.getValue(Action.NAME);

                    if (actionName.equals(actionRoot.getServerControls().getGameSpeedDesc(speedMove.getNewSpeed()))) {
                        speedActions.setSelectedItem(actionName);
                    }

                    break;
                }
            }
        });
        userMessageGenerator = new UserMessageGenerator(modelRoot, actionRoot);
        modelRoot.addCompleteMoveReceiver(userMessageGenerator);
    }

    private void countStations() {
        WorldIterator stations = new NonNullElementWorldIterator(KEY.STATIONS, modelRoot.getWorld(), modelRoot.getPrincipal());
        boolean enabled;

        enabled = stations.size() > 0;

        trainsJTabPane.setStationTabEnabled(enabled);
        stationInfoJMenuItem.setEnabled(enabled);
    }

    private void countTrains() {
        WorldIterator trains = new NonNullElementWorldIterator(KEY.TRAINS, modelRoot.getWorld(), modelRoot.getPrincipal());
        boolean enabled;

        enabled = trains.size() > 0;

        trainsJTabPane.setTrainTabEnabled(enabled);
        trainListJMenuItem.setEnabled(enabled);
        trainOrdersJMenuItem.setEnabled(enabled);
    }

    /**
     * @return
     */
    public JMenu createBuildMenu() {
        return buildMenu;
    }

    /**
     * @return
     */
    public JLabel createCashJLabel() {
        return cashlabel;
    }

    /**
     * @param title
     * @return
     */
    public JFrame createClientJFrame(String title) {
        clientFrame.setTitle(title);

        return clientFrame;
    }

    /**
     * @return
     */
    public JLabel createDateJLabel() {
        return datelabel;
    }

    /**
     * @return
     */
    public JMenu createBrokerMenu() {
        JMenu brokerMenu = new JMenu("Broker");

        JMenuItem callBrokerJMenuItem = new JMenuItem("Call Broker");
        callBrokerJMenuItem.addActionListener(e -> dialogueBoxController.showBrokerScreen());

        brokerMenu.add(callBrokerJMenuItem);

        return brokerMenu;
    }

    /**
     * @return
     */
    public JMenu createDisplayMenu() {
        JMenu displayMenu = new JMenu("Display");
        displayMenu.setMnemonic(68);

        trainOrdersJMenuItem = new JMenuItem("Train Orders");
        trainOrdersJMenuItem.addActionListener(e -> dialogueBoxController.showTrainOrders());

        stationInfoJMenuItem = new JMenuItem("Station Info");
        stationInfoJMenuItem.addActionListener(e -> dialogueBoxController.showStationInfo(0));

        trainListJMenuItem = new JMenuItem("Train List");
        trainListJMenuItem.addActionListener(e -> dialogueBoxController.showTrainList());

        displayMenu.add(trainOrdersJMenuItem);
        displayMenu.add(stationInfoJMenuItem);
        displayMenu.add(trainListJMenuItem);

        displayMenu.addSeparator();

        // Add menu items to control what gets displayed on the map.
        final JCheckBoxMenuItem showCargoMenuItem = new JCheckBoxMenuItem("Show cargo at stations", true);
        displayMenu.add(showCargoMenuItem);
        showCargoMenuItem.addActionListener(e -> {
            modelRoot.setProperty(ModelRoot.Property.SHOW_CARGO_AT_STATIONS, showCargoMenuItem.isSelected());
            mapViewJComponent.refreshAll();
        });

        final JCheckBoxMenuItem showStationNamesMenuItem = new JCheckBoxMenuItem("Show station names", true);
        displayMenu.add(showStationNamesMenuItem);
        showStationNamesMenuItem.addActionListener(e -> {
            modelRoot.setProperty(ModelRoot.Property.SHOW_STATION_NAMES, showStationNamesMenuItem.isSelected());
            mapViewJComponent.refreshAll();
        });

        final JCheckBoxMenuItem showStationBordersMenuItem = new JCheckBoxMenuItem("Show sphere-of-influence around stations", true);
        displayMenu.add(showStationBordersMenuItem);
        showStationBordersMenuItem.addActionListener(e -> {
            modelRoot.setProperty(ModelRoot.Property.SHOW_STATION_BORDERS, showStationBordersMenuItem.isSelected());
            mapViewJComponent.refreshAll();
        });

        final JCheckBoxMenuItem playSoundsMenuItem = new JCheckBoxMenuItem("Play sounds", false);
        modelRoot.setProperty(ModelRoot.Property.PLAY_SOUNDS, Boolean.FALSE);
        displayMenu.add(playSoundsMenuItem);
        playSoundsMenuItem.addActionListener(e -> modelRoot.setProperty(ModelRoot.Property.PLAY_SOUNDS, playSoundsMenuItem.isSelected()));
        boolean showFps = Boolean.parseBoolean(System.getProperty("SHOWFPS"));

        final JCheckBoxMenuItem showFPSMenuItem = new JCheckBoxMenuItem("Show FPS stats", showFps);
        displayMenu.add(showFPSMenuItem);
        showFPSMenuItem.addActionListener(e -> {
            String newValue = String.valueOf(showFPSMenuItem.isSelected());
            System.setProperty("SHOWFPS", newValue);
        });

        return displayMenu;
    }

    /**
     * @return
     */
    public JMenu createGameMenu() {
        sc = actionRoot.getServerControls();

        JMenu gameMenu = new JMenu("Game");
        gameMenu.setMnemonic(71);

        JMenuItem quitJMenuItem = new JMenuItem("Exit Game");
        quitJMenuItem.setMnemonic(88);

        quitJMenuItem.addActionListener(e -> System.exit(0));

        final JMenu newGameJMenu = new JMenu(sc.getNewGameAction());
        newGameJMenu.addMenuListener(new MenuListener() {

            public void menuCanceled(MenuEvent e) {
            }

            public void menuDeselected(MenuEvent e) {
            }

            public void menuSelected(MenuEvent e) {
                newGameJMenu.removeAll();

                for (Action action: sc.getMapNames().getActions()) {
                    JMenuItem mi = new JMenuItem(action);
                    newGameJMenu.add(mi);
                }
            }
        });

        JMenuItem saveGameJMenuItem = new JMenuItem(sc.getSaveGameAction());

        JMenuItem loadGameJMenuItem = new JMenuItem(sc.getLoadGameAction());

        // Set up the game speed sub-menu.
        JMenu gameSpeedSubMenu = new JMenu("Game Speed");

        ButtonGroup group = new ButtonGroup();

        speedActions = sc.getSetTargetTickPerSecondActions();

        Enumeration<MappedButtonModel> buttonModels = speedActions.getButtonModels();

        for (Action action: speedActions.getActions()) {
            JRadioButtonMenuItem mi = new JRadioButtonMenuItem(action);
            mi.setModel(buttonModels.nextElement());
            group.add(mi);
            gameSpeedSubMenu.add(mi);
        }

        gameMenu.add(newGameJMenu);
        gameMenu.addSeparator();
        gameMenu.add(loadGameJMenuItem);
        gameMenu.add(saveGameJMenuItem);
        gameMenu.addSeparator();

        gameMenu.add(gameSpeedSubMenu);
        gameMenu.addSeparator();
        gameMenu.add(quitJMenuItem);

        return gameMenu;
    }

    /**
     * @return
     */
    public JMenu createHelpMenu() {
        JMenu helpMenu = new JMenu("Help");

        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> dialogueBoxController.showAbout());

        JMenuItem how2play = new JMenuItem("Getting started");
        how2play.addActionListener(e -> dialogueBoxController.showHow2Play());

        JMenuItem showControls = new JMenuItem("Show game controls");
        showControls.addActionListener(e -> dialogueBoxController.showGameControls());

        JMenuItem showJavaProperties = new JMenuItem("Show Java Properties");
        showJavaProperties.addActionListener(e -> dialogueBoxController.showJavaProperties());

        helpMenu.add(showControls);
        helpMenu.add(how2play);
        helpMenu.add(showJavaProperties);
        helpMenu.add(about);

        return helpMenu;
    }

    /**
     * @return
     */
    public JScrollPane createMainMap() {
        return mainMapScrollPane1;
    }

    /**
     * @return
     */
    public JPanel createOverviewMap() {
        return overviewMapContainer;
    }

    /**
     * @return
     */
    public JMenu createReportsMenu() {
        JMenu reportsMenu = new JMenu("Reports");

        JMenuItem incomeStatementJMenuItem = new JMenuItem("Income Statement");
        incomeStatementJMenuItem.addActionListener(e -> dialogueBoxController.showIncomeStatement());

        JMenuItem balanceSheetJMenuItem = new JMenuItem("Balance Sheet");
        balanceSheetJMenuItem.addActionListener(e -> dialogueBoxController.showBalanceSheet());
        JMenuItem leaderBoardJMenuItem = new JMenuItem("Leaderboard");
        leaderBoardJMenuItem.addActionListener(e -> dialogueBoxController.showLeaderBoard());

        JMenuItem networthGraphJMenuItem = new JMenuItem("Networth Graph");
        networthGraphJMenuItem.addActionListener(e -> dialogueBoxController.showNetworthGraph());

        reportsMenu.add(balanceSheetJMenuItem);
        reportsMenu.add(incomeStatementJMenuItem);
        reportsMenu.add(leaderBoardJMenuItem);
        reportsMenu.add(networthGraphJMenuItem);

        return reportsMenu;
    }

    /**
     * @return
     */
    public JTabbedPane createTrainsJTabPane() {
        return trainsJTabPane;
    }

    /**
     * @return
     */
    public BuildTrackController getBuildTrackController() {
        return mainMap.getBuildTrackController();
    }

    /**
     * @return
     */
    public boolean isSetup() {
        return isSetup;
    }

    /**
     * @param key
     * @param index
     * @param principal
     */
    public void itemAdded(KEY key, int index, FreerailsPrincipal principal) {
        boolean rightPrincipal = principal.equals(modelRoot.getPrincipal());

        if (KEY.TRAINS == key && rightPrincipal) {
            countTrains();
        } else if (KEY.STATIONS == key && rightPrincipal) {
            countStations();
        }
    }

    /**
     * @param key
     * @param index
     * @param principal
     */
    public void itemRemoved(KEY key, int index, FreerailsPrincipal principal) {
    }

    /**
     * @param key
     * @param index
     * @param principal
     */
    public void listUpdated(KEY key, int index, FreerailsPrincipal principal) {
        boolean rightPrincipal = principal.equals(modelRoot.getPrincipal());

        if (KEY.TRAINS == key && rightPrincipal) {
            countTrains();
        } else if (KEY.STATIONS == key && rightPrincipal) {
            countStations();
        }
    }

    /**
     * Called when a new game is started or a game is loaded.
     *
     * <b>Be extremely careful with the references of objects allocated in this
     * method to avoid memory leaks - see bug 967677 (OutOfMemoryError after
     * starting several new games). </b>
     */
    public void setup(RendererRoot vl, ReadOnlyWorld world) throws IOException {
        /*
         * Set the cursor position. The initial cursor position is 0,0. However,
         * if a game is loaded or a new game is started and the map size is the
         * same as the last map size, then the cursor should take the position
         * it had on the last map.
         */
        Vector2D cursorPosition = Vector2D.ZERO;
        if (null != this.world) {
            if (world.getMapWidth() == this.world.getMapWidth() && world.getMapHeight() == this.world.getMapHeight()) {
                cursorPosition = (Vector2D) modelRoot.getProperty(ModelRoot.Property.CURSOR_POSITION);
            }
        }
        this.world = world;
        modelRoot.addMapListener(this);
        modelRoot.addListListener(this);

        if (!vl.validate(this.world)) {
            throw new IllegalArgumentException("The specified" + " RendererRoot are not compatible with the clients" + "world!");
        }

        // create the main and overview maps
        mainMap = new DetailMapRenderer(this.world, vl, modelRoot);

        Dimension maxSize = new Dimension(200, 200);
        overviewMap = ZoomedOutMapRenderer.getInstance(this.world, maxSize);

        stationTypesPopup.setup(modelRoot, actionRoot, mainMap.getStationRadius());

        mapViewJComponent.setup(mainMap, modelRoot, vl);

        // setup the the main and overview map JComponents
        dialogueBoxController.setDefaultFocusOwner(mapViewJComponent);

        userInputOnMapController.setup(mapViewJComponent, actionRoot.getTrackMoveProducer(), stationTypesPopup, modelRoot, dialogueBoxController, getBuildTrackController());

        buildMenu.setup(actionRoot);
        mainMapScrollPane1.setViewportView(mapViewJComponent);

        ((OverviewMapComponent) overviewMapContainer).setup(overviewMap);

        datelabel.setup(modelRoot, vl, null);
        cashlabel.setup(modelRoot, vl, null);
        trainsJTabPane.setup(actionRoot, vl, modelRoot);

        dialogueBoxController.setup(modelRoot, vl);

        StationPlacementCursor.wireUp(actionRoot, mainMap.getStationRadius(), mapViewJComponent);

        int gameSpeed = ((GameSpeed) this.world.get(ITEM.GAME_SPEED)).getSpeed();

        // Set the selected game speed radio button.
        String actionName = actionRoot.getServerControls().getGameSpeedDesc(gameSpeed);
        speedActions.setSelectedItem(actionName);
        userMessageGenerator.logSpeed();

        /*
         * Count stations and trains to determine if we need to display the
         * station and train menu items and tabs.3
         */
        countStations();
        countTrains();

        String name = modelRoot.getPrincipal().getName();
        String serverDetails = (String) modelRoot.getProperty(ModelRoot.Property.SERVER);
        String frameTitle;
        if (serverDetails.equals(LocalConnection.SERVER_IN_SAME_JVM)) {
            frameTitle = name + " - Freerails";
        } else {
            frameTitle = name + " - " + serverDetails + " - Freerails";
        }

        clientFrame.setTitle(frameTitle);
        isSetup = true;
        modelRoot.setProperty(ModelRoot.Property.CURSOR_POSITION, cursorPosition);
        mapViewJComponent.requestFocus();
    }

    // TODO change to often, ~10% of time
    /**
     * Listens for changes on the map, for instance when track is built, and
     * refreshes the map views.
     */
    public void tilesChanged(Rectangle tilesChanged) {
        logger.debug("TilesChanged = " + tilesChanged);

        // If lots of tiles have changed, do a complete refresh.
        int size = tilesChanged.width * tilesChanged.height;

        if (size > 100) {
            mainMap.refreshAll();
            overviewMap.refreshAll();
        } else {
            // Fix for bug 967673 (Crash when building track close to edge of
            // map).
            Rectangle mapRect = new Rectangle(0, 0, world.getMapWidth(), world.getMapHeight());
            tilesChanged = tilesChanged.intersection(mapRect);

            for (int tileX = tilesChanged.x; tileX < (tilesChanged.x + tilesChanged.width); tileX++) {
                for (int tileY = tilesChanged.y; tileY < (tilesChanged.y + tilesChanged.height); tileY++) {
                    Vector2D p = new Vector2D(tileX, tileY);
                    mainMap.refreshTile(p);
                    overviewMap.refreshTile(p);
                }
            }
        }
    }
}