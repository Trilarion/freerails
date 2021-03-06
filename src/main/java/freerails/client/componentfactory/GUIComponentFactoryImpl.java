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

import freerails.client.*;
import freerails.client.renderer.map.*;
import freerails.client.renderer.map.detail.DetailMapRenderer;
import freerails.client.renderer.map.detail.DetailMapViewComponentConcrete;
import freerails.client.renderer.map.overview.OverviewMapComponent;
import freerails.client.renderer.map.overview.OverviewMapRenderer;
import freerails.model.world.*;
import freerails.util.ui.ActionAdapter;
import freerails.util.ui.ActionAdapter.MappedButtonModel;
import freerails.client.model.ServerControlModel;
import freerails.controller.BuildTrackController;
import freerails.client.renderer.RendererRoot;
import freerails.client.view.*;
import freerails.move.ChangeGameSpeedMove;
import freerails.util.Vec2D;
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
public class GUIComponentFactoryImpl implements GUIComponentFactory, WorldMapListener {

    /**
     * Whether to show certain 'cheat' menus used for testing.
     */
    private static final Logger logger = Logger.getLogger(GUIComponentFactoryImpl.class.getName());
    private final ActionRoot actionRoot;
    private final JMenu buildMenu; // let's you select a track type
    private final CashLabel cashlabel;
    private final ClientFrame clientFrame;
    private final DateLabel datelabel;
    private final DialogueBoxController dialogueBoxController;
    private final JScrollPane mainMapScrollPane1;
    private final DetailMapViewComponentConcrete mapViewJComponent;
    private final ModelRootImpl modelRoot;
    private final JPanel overviewMapContainer;
    private final StationTypesPopup stationTypesPopup;
    /**
     * This is the panel at the bottom right of the screen.
     */
    private final RHSTabPane trainsJTabPane;
    private final UserInputOnMapController userInputOnMapController;
    private final UserMessageGenerator userMessageGenerator;
    private DetailMapRenderer mainMap;
    private MapRenderer overviewMap;
    private ActionAdapter speedActions;
    private JMenuItem stationInfoJMenuItem;
    private JMenuItem trainListJMenuItem;
    private JMenuItem trainOrdersJMenuItem;
    private UnmodifiableWorld world;
    private BuildTrackController buildTrackController;
    /**
     * @param modelRoot
     * @param actionRoot
     */
    public GUIComponentFactoryImpl(ModelRootImpl modelRoot, ActionRoot actionRoot) {
        this.modelRoot = modelRoot;
        this.actionRoot = actionRoot;
        userInputOnMapController = new UserInputOnMapController(modelRoot, actionRoot);
        buildMenu = new JMenu();
        mapViewJComponent = new DetailMapViewComponentConcrete();
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
        dialogueBoxController = new DialogueBoxController(clientFrame, this.modelRoot);
        this.actionRoot.setDialogueBoxController(dialogueBoxController);

        // TODO only split move receiver so far
        this.modelRoot.addSplitMoveReceiver(move -> {
            if (move instanceof ChangeGameSpeedMove) {
                ChangeGameSpeedMove speedMove = (ChangeGameSpeedMove) move;

                // TODO this loop does not loop!
                for (Action action: speedActions.getActions()) {
                    String actionName = (String) action.getValue(Action.NAME);

                    if (actionName.equals(this.actionRoot.getServerControls().getGameSpeedDesc(speedMove.getSpeed().getTicksPerSecond()))) {
                        speedActions.setSelectedItem(actionName);
                    }

                    break;
                }
            }
        });
        userMessageGenerator = new UserMessageGenerator(this.modelRoot, this.actionRoot);
        this.modelRoot.addCompleteMoveReceiver(userMessageGenerator);
    }

    // TODO this does more then just count stations! currently not updated anymore
    private void countStations() {
        boolean enabled = world.getStations(modelRoot.getPlayer()).size() > 0;

        trainsJTabPane.setStationTabEnabled(enabled);
        stationInfoJMenuItem.setEnabled(enabled);
    }

    // TODO currently after AddTrainMove the count is not updated!
    private void countTrains() {
        boolean enabled = !modelRoot.getWorld().getTrains(modelRoot.getPlayer()).isEmpty();

        trainsJTabPane.setTrainTabEnabled(enabled);
        trainListJMenuItem.setEnabled(enabled);
        trainOrdersJMenuItem.setEnabled(enabled);
    }

    /**
     * @return
     */
    @Override
    public JMenu createBuildMenu() {
        return buildMenu;
    }

    /**
     * @return
     */
    @Override
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
    @Override
    public JLabel createDateJLabel() {
        return datelabel;
    }

    /**
     * @return
     */
    @Override
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
    @Override
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
            modelRoot.setProperty(ModelRootProperty.SHOW_CARGO_AT_STATIONS, showCargoMenuItem.isSelected());
            mapViewJComponent.refreshAll();
        });

        final JCheckBoxMenuItem showStationNamesMenuItem = new JCheckBoxMenuItem("Show station names", true);
        displayMenu.add(showStationNamesMenuItem);
        showStationNamesMenuItem.addActionListener(e -> {
            modelRoot.setProperty(ModelRootProperty.SHOW_STATION_NAMES, showStationNamesMenuItem.isSelected());
            mapViewJComponent.refreshAll();
        });

        final JCheckBoxMenuItem showStationBordersMenuItem = new JCheckBoxMenuItem("Show sphere-of-influence around stations", true);
        displayMenu.add(showStationBordersMenuItem);
        showStationBordersMenuItem.addActionListener(e -> {
            modelRoot.setProperty(ModelRootProperty.SHOW_STATION_BORDERS, showStationBordersMenuItem.isSelected());
            mapViewJComponent.refreshAll();
        });

        final JCheckBoxMenuItem playSoundsMenuItem = new JCheckBoxMenuItem("Play sounds", false);
        modelRoot.setProperty(ModelRootProperty.PLAY_SOUNDS, Boolean.FALSE);
        displayMenu.add(playSoundsMenuItem);
        playSoundsMenuItem.addActionListener(e -> modelRoot.setProperty(ModelRootProperty.PLAY_SOUNDS, playSoundsMenuItem.isSelected()));

        return displayMenu;
    }

    /**
     * @return
     */
    @Override
    public JMenu createGameMenu() {
        ServerControlModel serverControlModel = actionRoot.getServerControls();

        JMenu gameMenu = new JMenu("Game");
        gameMenu.setMnemonic(71);

        JMenuItem quitJMenuItem = new JMenuItem("Exit Game");
        quitJMenuItem.setMnemonic(88);

        quitJMenuItem.addActionListener(e -> System.exit(0));

        final JMenu newGameJMenu = new JMenu(serverControlModel.getNewGameAction());
        newGameJMenu.addMenuListener(new MenuListener() {

            @Override
            public void menuCanceled(MenuEvent e) {
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuSelected(MenuEvent e) {
                newGameJMenu.removeAll();

                for (Action action: serverControlModel.getMapNames().getActions()) {
                    JMenuItem mi = new JMenuItem(action);
                    newGameJMenu.add(mi);
                }
            }
        });

        JMenuItem saveGameJMenuItem = new JMenuItem(serverControlModel.getSaveGameAction());

        JMenuItem loadGameJMenuItem = new JMenuItem(serverControlModel.getLoadGameAction());

        // Set up the game speed sub-menu.
        JMenu gameSpeedSubMenu = new JMenu("Game Clock.Speed");

        ButtonGroup group = new ButtonGroup();

        speedActions = serverControlModel.getSetTargetTickPerSecondActions();

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

    // TODO the whole help menu is pretty dead, needs redesign
    /**
     * @return
     */
    @Override
    public JMenu createHelpMenu() {
        JMenu helpMenu = new JMenu("Help");

        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> dialogueBoxController.showAbout());

        JMenuItem how2play = new JMenuItem("Getting started");
        how2play.addActionListener(e -> dialogueBoxController.showHow2Play());

        JMenuItem showControls = new JMenuItem("Show game controls");
        showControls.addActionListener(e -> dialogueBoxController.showGameControls());

        helpMenu.add(showControls);
        helpMenu.add(how2play);
        helpMenu.add(about);

        return helpMenu;
    }

    /**
     * @return
     */
    @Override
    public JScrollPane createMainMap() {
        return mainMapScrollPane1;
    }

    /**
     * @return
     */
    @Override
    public JPanel createOverviewMap() {
        return overviewMapContainer;
    }

    /**
     * @return
     */
    @Override
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
    @Override
    public JTabbedPane createTrainsJTabPane() {
        return trainsJTabPane;
    }

    /**
     * @return
     */
    public BuildTrackController getBuildTrackController() {
        return buildTrackController;
    }

    // TODO when is this called? when a station is added? need to listen to world changes again (mostly call to countStations())

    /**
     * Called when a new game is started or a game is loaded.
     *
     * <b>Be extremely careful with the references of objects allocated in this
     * method to avoid memory leaks - see bug 967677 (OutOfMemoryError after
     * starting several new games).</b>
     */
    public void setup(RendererRoot rendererRoot, UnmodifiableWorld world) throws IOException {
        /*
         * Set the cursor position. The initial cursor position is 0,0. However,
         * if a game is loaded or a new game is started and the map size is the
         * same as the last map size, then the cursor should take the position
         * it had on the last map.
         */
        Vec2D cursorPosition = Vec2D.ZERO;
        if (null != this.world) {
            if (world.getMapSize().equals(this.world.getMapSize())) {
                cursorPosition = (Vec2D) modelRoot.getProperty(ModelRootProperty.CURSOR_POSITION);
            }
        }
        this.world = world;
        modelRoot.addMapListener(this);

        // TODO this should be a test, all rendererroots should be compatible
        if (!rendererRoot.validate(this.world)) {
            throw new IllegalArgumentException("The specified" + " RendererRoot are not compatible with the clients" + "world!");
        }

        // create the main and overview maps
        buildTrackController = new BuildTrackController(world, modelRoot);
        mainMap = new DetailMapRenderer(this.world, rendererRoot, modelRoot);

        Dimension maxSize = new Dimension(200, 200);
        overviewMap = OverviewMapRenderer.getInstance(this.world, maxSize);

        stationTypesPopup.setup(modelRoot, actionRoot, mainMap.getStationRadius());

        mapViewJComponent.setup(mainMap, modelRoot, rendererRoot);

        // setup the the main and overview map JComponents
        dialogueBoxController.setDefaultFocusOwner(mapViewJComponent);

        userInputOnMapController.setup(mapViewJComponent, actionRoot.getTrackMoveProducer(), stationTypesPopup, modelRoot, dialogueBoxController, buildTrackController);


        // build menu setup
        buildMenu.removeAll();
        buildMenu.setText("Build");
        buildMenu.add(actionRoot.getBuildTrainDialogAction());


        mainMapScrollPane1.setViewportView(mapViewJComponent);

        ((OverviewMapComponent) overviewMapContainer).setup(overviewMap);

        datelabel.setup(modelRoot, rendererRoot, null);
        cashlabel.setup(modelRoot, rendererRoot, null);
        trainsJTabPane.setup(actionRoot, rendererRoot, modelRoot);

        dialogueBoxController.setup(modelRoot, rendererRoot);

        StationPlacementCursor.wireUp(actionRoot, mainMap.getStationRadius(), mapViewJComponent);

        // TODO this is so that initially the game speed is displayed, this could also be done just by moves
        int gameSpeed = world.getSpeed().getTicksPerSecond();
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

        String name = modelRoot.getPlayer().getName();
        String serverDetails = (String) modelRoot.getProperty(ModelRootProperty.SERVER);
        String frameTitle;
        // frameTitle = name + " - Freerails";
        frameTitle = name + " - " + serverDetails + " - Freerails";

        clientFrame.setTitle(frameTitle);
        modelRoot.setProperty(ModelRootProperty.CURSOR_POSITION, cursorPosition);
        mapViewJComponent.requestFocus();
    }

    // TODO change too often, ~10% of time
    /**
     * Part of the WorldMapListener interface, invoked by MoveChainFork upon a MapUpdateMove
     *
     * Listens for changes on the map, for instance when track is built, and
     * refreshes the map views.
     */
    @Override
    public void tilesChanged(Rectangle tilesChanged) {
        logger.debug("TilesChanged = " + tilesChanged);

        // If lots of tiles have changed, do a complete refresh.
        int size = tilesChanged.width * tilesChanged.height;

        if (size > 100) {
            mainMap.refreshAll();
            overviewMap.refreshAll();
        } else {
            // TODO this bug still exists, but why? (coordinates outside, what effect has this)
            // Fix for bug 967673 (Crash when building track close to edge of map).
            Vec2D mapSize = world.getMapSize();
            Rectangle mapRect = new Rectangle(0, 0, mapSize.x, mapSize.y);
            tilesChanged = tilesChanged.intersection(mapRect);

            for (int tileX = tilesChanged.x; tileX < tilesChanged.x + tilesChanged.width; tileX++) {
                for (int tileY = tilesChanged.y; tileY < tilesChanged.y + tilesChanged.height; tileY++) {
                    Vec2D p = new Vec2D(tileX, tileY);
                    mainMap.refreshTile(p);
                    overviewMap.refreshTile(p);
                }
            }
        }
    }
}