package freerails.client.top;

import freerails.client.common.ActionAdapter;
import freerails.client.common.ActionAdapter.MappedButtonModel;
import freerails.client.common.ModelRootImpl;
import freerails.client.renderer.BuildTrackController;
import freerails.client.renderer.MapRenderer;
import freerails.client.renderer.RenderersRoot;
import freerails.client.renderer.ZoomedOutMapRenderer;
import freerails.client.view.*;
import freerails.controller.ModelRoot;
import freerails.move.ChangeGameSpeedMove;
import freerails.move.ChangeProductionAtEngineShopMove;
import freerails.move.Move;
import freerails.network.LocalConnection;
import freerails.network.MoveReceiver;
import freerails.world.common.GameSpeed;
import freerails.world.common.ImList;
import freerails.world.common.ImPoint;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.PlannedTrain;
import freerails.world.station.StationModel;
import freerails.world.top.*;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Random;

/**
 * Creates and wires up the GUI components.
 *
 * @author Luke
 */
public class GUIComponentFactoryImpl implements GUIComponentFactory,
        WorldMapListener, WorldListListener {

    /**
     * Whether to show certain 'cheat' menus used for testing.
     */
    private static final boolean CHEAT = (System.getProperty("cheat") != null);

    private static final Logger logger = Logger
            .getLogger(GUIComponentFactoryImpl.class.getName());

    private final ActionRoot actionRoot;

    private final BuildMenu buildMenu;

    private final CashJLabel cashjLabel;

    private final ClientJFrame clientJFrame;

    private final DateJLabel datejLabel;

    private final DialogueBoxController dialogueBoxController;
    private final JScrollPane mainMapScrollPane1;
    private final MapViewJComponentConcrete mapViewJComponent;
    private final ModelRootImpl modelRoot;
    private final JPanel overviewMapContainer;
    private final StationTypesPopup stationTypesPopup;
    /**
     * This is the panel at the bottom right of the screen.
     */
    private final RHSJTabPane trainsJTabPane;
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

    public GUIComponentFactoryImpl(ModelRootImpl mr, ActionRoot ar) {
        modelRoot = mr;
        actionRoot = ar;
        userInputOnMapController = new UserInputOnMapController(modelRoot, ar);
        buildMenu = new freerails.client.top.BuildMenu();
        mapViewJComponent = new MapViewJComponentConcrete();
        mainMapScrollPane1 = new JScrollPane();
        Rectangle r = new Rectangle(10, 10, 10, 10);
        overviewMapContainer = new OverviewMapJComponent(r);
        stationTypesPopup = new StationTypesPopup();

        MainMapAndOverviewMapMediator mediator = new MainMapAndOverviewMapMediator();
        mediator.setup(overviewMapContainer, mainMapScrollPane1.getViewport(),
                mapViewJComponent, r);

        trainsJTabPane = new RHSJTabPane();
        datejLabel = new DateJLabel();

        cashjLabel = new CashJLabel();

        clientJFrame = new ClientJFrame(this);
        dialogueBoxController = new DialogueBoxController(clientJFrame,
                modelRoot);
        actionRoot.setDialogueBoxController(dialogueBoxController);

        modelRoot.addSplitMoveReceiver(new MoveReceiver() {
            public void processMove(Move move) {
                if (move instanceof ChangeGameSpeedMove) {
                    ChangeGameSpeedMove speedMove = (ChangeGameSpeedMove) move;

                    for (Enumeration<Action> actionsEnum = speedActions
                            .getActions(); actionsEnum.hasMoreElements(); ) {
                        Action action = actionsEnum.nextElement();
                        String actionName = (String) action
                                .getValue(Action.NAME);

                        if (actionName.equals(actionRoot.getServerControls()
                                .getGameSpeedDesc(speedMove.getNewSpeed()))) {
                            speedActions.setSelectedItem(actionName);
                        }

                        break;
                    }
                }
            }
        });
        userMessageGenerator = new UserMessageGenerator(this.modelRoot,
                this.actionRoot);
        modelRoot.addCompleteMoveReceiver(userMessageGenerator);

    }

    private void countStations() {
        NonNullElements stations = new NonNullElements(KEY.STATIONS, modelRoot
                .getWorld(), modelRoot.getPrincipal());
        boolean enabled;

        enabled = stations.size() > 0;

        this.trainsJTabPane.setStationTabEnabled(enabled);
        this.stationInfoJMenuItem.setEnabled(enabled);
    }

    private void countTrains() {
        NonNullElements trains = new NonNullElements(KEY.TRAINS, modelRoot
                .getWorld(), modelRoot.getPrincipal());
        boolean enabled;

        enabled = trains.size() > 0;

        this.trainsJTabPane.setTrainTabEnabled(enabled);
        this.trainListJMenuItem.setEnabled(enabled);
        this.trainOrdersJMenuItem.setEnabled(enabled);
    }

    public JMenu createBuildMenu() {
        return buildMenu;
    }

    public JLabel createCashJLabel() {
        return cashjLabel;
    }

    public JFrame createClientJFrame(String title) {
        clientJFrame.setTitle(title);

        return clientJFrame;
    }

    public JLabel createDateJLabel() {
        return datejLabel;
    }

    public JMenu createBrokerMenu() {
        JMenu brokerMenu = new JMenu("Broker");

        JMenuItem callBrokerJMenuItem = new JMenuItem("Call Broker");
        callBrokerJMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialogueBoxController.showBrokerScreen();
            }
        });

        brokerMenu.add(callBrokerJMenuItem);

        return brokerMenu;
    }

    public JMenu createDisplayMenu() {
        JMenu displayMenu = new JMenu("Display");
        displayMenu.setMnemonic(68);

        trainOrdersJMenuItem = new JMenuItem("Train Orders");
        trainOrdersJMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialogueBoxController.showTrainOrders();
            }
        });

        stationInfoJMenuItem = new JMenuItem("Station Info");
        stationInfoJMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialogueBoxController.showStationInfo(0);
            }
        });

        trainListJMenuItem = new JMenuItem("Train List");
        trainListJMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialogueBoxController.showTrainList();
            }
        });

        displayMenu.add(trainOrdersJMenuItem);
        displayMenu.add(stationInfoJMenuItem);
        displayMenu.add(trainListJMenuItem);

        displayMenu.addSeparator();

        // Add menu items to control what gets displayed on the map.
        final JCheckBoxMenuItem showCargoMenuItem = new JCheckBoxMenuItem(
                "Show cargo at stations", true);
        displayMenu.add(showCargoMenuItem);
        showCargoMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                modelRoot.setProperty(
                        ModelRoot.Property.SHOW_CARGO_AT_STATIONS, showCargoMenuItem.isSelected());
                mapViewJComponent.refreshAll();
            }
        });

        final JCheckBoxMenuItem showStationNamesMenuItem = new JCheckBoxMenuItem(
                "Show station names", true);
        displayMenu.add(showStationNamesMenuItem);
        showStationNamesMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                modelRoot.setProperty(ModelRoot.Property.SHOW_STATION_NAMES,
                        showStationNamesMenuItem.isSelected());
                mapViewJComponent.refreshAll();
            }
        });

        final JCheckBoxMenuItem showStationBordersMenuItem = new JCheckBoxMenuItem(
                "Show sphere-of-influence around stations", true);
        displayMenu.add(showStationBordersMenuItem);
        showStationBordersMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                modelRoot.setProperty(ModelRoot.Property.SHOW_STATION_BORDERS,
                        showStationBordersMenuItem.isSelected());
                mapViewJComponent.refreshAll();
            }
        });

        final JCheckBoxMenuItem playSoundsMenuItem = new JCheckBoxMenuItem(
                "Play sounds", false);
        modelRoot.setProperty(ModelRoot.Property.PLAY_SOUNDS, Boolean.FALSE);
        displayMenu.add(playSoundsMenuItem);
        playSoundsMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                modelRoot.setProperty(ModelRoot.Property.PLAY_SOUNDS,
                        playSoundsMenuItem.isSelected());
            }
        });
        boolean showFps = Boolean.parseBoolean(System.getProperty("SHOWFPS"));

        final JCheckBoxMenuItem showFPSMenuItem = new JCheckBoxMenuItem(
                "Show FPS stats", showFps);
        displayMenu.add(showFPSMenuItem);
        showFPSMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String newValue = String.valueOf(showFPSMenuItem.isSelected());
                System.setProperty("SHOWFPS", newValue);
            }
        });

        return displayMenu;
    }

    public JMenu createGameMenu() {
        sc = actionRoot.getServerControls();

        JMenu gameMenu = new JMenu("Game");
        gameMenu.setMnemonic(71);

        JMenuItem quitJMenuItem = new JMenuItem("Exit Game");
        quitJMenuItem.setMnemonic(88);

        quitJMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        final JMenu newGameJMenu = new JMenu(sc.getNewGameAction());
        newGameJMenu.addMenuListener(new MenuListener() {

            public void menuCanceled(MenuEvent e) {
            }

            public void menuDeselected(MenuEvent e) {
            }

            public void menuSelected(MenuEvent e) {
                newGameJMenu.removeAll();

                Enumeration<Action> actions = sc.getMapNames().getActions();

                while (actions.hasMoreElements()) {
                    JMenuItem mi = new JMenuItem(actions.nextElement());
                    newGameJMenu.add(mi);
                }
            }
        });

        JMenuItem saveGameJMenuItem = new JMenuItem(sc.getSaveGameAction());

        JMenuItem loadGameJMenuItem = new JMenuItem(sc.getLoadGameAction());

        // Fix bug 1102806 Newspaper does nothing, so hide it.
        // JMenuItem newspaperJMenuItem = new JMenuItem("Newspaper");
        // newspaperJMenuItem.setMnemonic(78);

        // newspaperJMenuItem.addActionListener(new ActionListener() {
        // public void actionPerformed(ActionEvent e) {
        // dialogueBoxController.showNewspaper("Headline");
        // //glassPanel.setVisible(true);
        // }
        // });

        // Set up the game speed sub-menu.
        JMenu gameSpeedSubMenu = new JMenu("Game Speed");

        ButtonGroup group = new ButtonGroup();

        speedActions = sc.getSetTargetTickPerSecondActions();

        Enumeration<MappedButtonModel> buttonModels = speedActions
                .getButtonModels();
        Enumeration<Action> actions = speedActions.getActions();

        while (buttonModels.hasMoreElements()) {
            JRadioButtonMenuItem mi = new JRadioButtonMenuItem(actions
                    .nextElement());
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
        // gameMenu.add(newspaperJMenuItem);
        gameMenu.addSeparator();
        gameMenu.add(quitJMenuItem);

        if (CHEAT) {
            /* For testing. */
            final ActionListener build200trains = new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    WorldIterator wi = new NonNullElements(KEY.STATIONS,
                            modelRoot.getWorld(), modelRoot.getPrincipal());

                    if (wi.next()) {
                        Random randy = new Random();
                        StationModel station = (StationModel) wi.getElement();

                        ImList<PlannedTrain> before = station.getProduction();
                        int numberOfEngineTypes = modelRoot.getWorld().size(
                                SKEY.ENGINE_TYPES) - 1;
                        int numberOfcargoTypes = modelRoot.getWorld().size(
                                SKEY.CARGO_TYPES) - 1;
                        PlannedTrain[] temp = new PlannedTrain[200];

                        for (int i = 0; i < temp.length; i++) {
                            int engineType = randy.nextInt(numberOfEngineTypes);
                            int[] wagonTypes = new int[]{
                                    randy.nextInt(numberOfcargoTypes),
                                    randy.nextInt(numberOfcargoTypes),
                                    randy.nextInt(numberOfcargoTypes)};
                            PlannedTrain plannedTrain = new PlannedTrain(
                                    engineType, wagonTypes);
                            temp[i] = plannedTrain;
                        }
                        ImList<PlannedTrain> after = new ImList<>(
                                temp);
                        Move m = new ChangeProductionAtEngineShopMove(before,
                                after, wi.getIndex(), modelRoot.getPrincipal());
                        modelRoot.doMove(m);
                    }
                }
            };

            JMenuItem build200TrainsMenuItem = new JMenuItem(
                    "Build 200 trains!");
            build200TrainsMenuItem.addActionListener(build200trains);
            gameMenu.add(build200TrainsMenuItem);
        }

        return gameMenu;
    }

    public JMenu createHelpMenu() {
        JMenu helpMenu = new JMenu("Help");

        JMenuItem about = new JMenuItem("About");
        about.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dialogueBoxController.showAbout();
            }
        });

        JMenuItem how2play = new JMenuItem("Getting started");
        how2play.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dialogueBoxController.showHow2Play();
            }
        });

        JMenuItem showControls = new JMenuItem("Show game controls");
        showControls.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dialogueBoxController.showGameControls();
            }
        });

        JMenuItem showJavaProperties = new JMenuItem("Show Java Properties");
        showJavaProperties
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        dialogueBoxController.showJavaProperties();
                    }
                });

        JMenuItem showReportBug = new JMenuItem("Report Bug");
        showReportBug.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dialogueBoxController.showReportBug();
            }
        });

        helpMenu.add(showControls);
        helpMenu.add(how2play);
        helpMenu.add(showJavaProperties);
        helpMenu.add(showReportBug);
        helpMenu.add(about);

        return helpMenu;
    }

    public JScrollPane createMainMap() {
        return mainMapScrollPane1;
    }

    public JPanel createOverviewMap() {
        return overviewMapContainer;
    }

    public JMenu createReportsMenu() {
        JMenu reportsMenu = new JMenu("Reports");

        JMenuItem incomeStatementJMenuItem = new JMenuItem("Income Statement");
        incomeStatementJMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialogueBoxController.showIncomeStatement();
            }
        });

        JMenuItem balanceSheetJMenuItem = new JMenuItem("Balance Sheet");
        balanceSheetJMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialogueBoxController.showBalanceSheet();
            }
        });
        JMenuItem leaderBoardJMenuItem = new JMenuItem("Leaderboard");
        leaderBoardJMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialogueBoxController.showLeaderBoard();
            }
        });

        JMenuItem networthGraphJMenuItem = new JMenuItem("Networth Graph");
        networthGraphJMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialogueBoxController.showNetworthGraph();
            }
        });

        reportsMenu.add(balanceSheetJMenuItem);
        reportsMenu.add(incomeStatementJMenuItem);
        reportsMenu.add(leaderBoardJMenuItem);
        reportsMenu.add(networthGraphJMenuItem);

        return reportsMenu;
    }

    public JTabbedPane createTrainsJTabPane() {
        return trainsJTabPane;
    }

    public BuildTrackController getBuildTrackController() {
        return mainMap.getBuildTrackController();
    }

    public boolean isSetup() {
        return isSetup;
    }

    public void itemAdded(KEY key, int index, FreerailsPrincipal principal) {
        boolean rightPrincipal = principal
                .equals(this.modelRoot.getPrincipal());

        if (KEY.TRAINS == key && rightPrincipal) {
            countTrains();
        } else if (KEY.STATIONS == key && rightPrincipal) {
            countStations();
        }
    }

    public void itemRemoved(KEY key, int index, FreerailsPrincipal principal) {
        // do nothing
    }

    public void listUpdated(KEY key, int index, FreerailsPrincipal principal) {
        boolean rightPrincipal = principal
                .equals(this.modelRoot.getPrincipal());

        if (KEY.TRAINS == key && rightPrincipal) {
            countTrains();
        } else if (KEY.STATIONS == key && rightPrincipal) {
            countStations();
        }
    }

    /**
     * Called when a new game is started or a game is loaded.
     * <p>
     * <b>Be extremely careful with the references of objects allocated in this
     * method to avoid memory leaks - see bug 967677 (OutOfMemoryError after
     * starting several new games). </b>
     * </p>
     */
    public void setup(RenderersRoot vl, ReadOnlyWorld w) throws IOException {
        /*
         * Set the cursor position. The initial cursor position is 0,0. However,
         * if a game is loaded or a new game is started and the map size is the
         * same as the last map size, then the cursor should take the position
         * it had on the last map.
         */
        ImPoint cursorPosition = new ImPoint(0, 0);
        if (null != world) {
            if (w.getMapWidth() == world.getMapWidth()
                    && w.getMapHeight() == world.getMapHeight()) {
                cursorPosition = (ImPoint) modelRoot
                        .getProperty(ModelRoot.Property.CURSOR_POSITION);
            }
        }
        world = w;
        modelRoot.addMapListener(this);
        modelRoot.addListListener(this);

        if (!vl.validate(world)) {
            throw new IllegalArgumentException("The specified"
                    + " RenderersRoot are not compatible with the clients"
                    + "world!");
        }

        // create the main and overview maps
        mainMap = new DetailMapRenderer(world, vl, modelRoot);

        Dimension maxSize = new Dimension(200, 200);
        overviewMap = ZoomedOutMapRenderer.getInstance(world, maxSize);

        stationTypesPopup.setup(modelRoot, actionRoot, mainMap
                .getStationRadius());

        mapViewJComponent.setup(mainMap, modelRoot, vl);

        // setup the the main and overview map JComponents
        dialogueBoxController.setDefaultFocusOwner(mapViewJComponent);

        userInputOnMapController.setup(mapViewJComponent, actionRoot
                        .getTrackMoveProducer(), stationTypesPopup, this.modelRoot,
                dialogueBoxController, mapViewJComponent.getMapCursor(),
                getBuildTrackController());

        buildMenu.setup(actionRoot);
        mainMapScrollPane1.setViewportView(this.mapViewJComponent);

        ((OverviewMapJComponent) overviewMapContainer).setup(overviewMap);

        datejLabel.setup(modelRoot, vl, null);
        cashjLabel.setup(modelRoot, vl, null);
        trainsJTabPane.setup(actionRoot, vl, modelRoot);

        dialogueBoxController.setup(modelRoot, vl);

        StationPlacementCursor.wireUp(actionRoot, mainMap.getStationRadius(),
                mapViewJComponent);

        int gameSpeed = ((GameSpeed) world.get(ITEM.GAME_SPEED)).getSpeed();

        /* Set the selected game speed radio button. */
        String actionName = actionRoot.getServerControls().getGameSpeedDesc(
                gameSpeed);
        speedActions.setSelectedItem(actionName);
        userMessageGenerator.logSpeed();

        /*
         * Count stations and trains to determine if we need to display the
         * station and train menu items and tabs.3
         */
        countStations();
        countTrains();

        String name = modelRoot.getPrincipal().getName();
        String serverDetails = (String) modelRoot
                .getProperty(ModelRoot.Property.SERVER);
        String frameTitle;
        if (serverDetails.equals(LocalConnection.SERVER_IN_SAME_JVM)) {
            frameTitle = name + " - Freerails";
        } else {
            frameTitle = name + " - " + serverDetails + " - Freerails";
        }

        clientJFrame.setTitle(frameTitle);
        isSetup = true;
        modelRoot.setProperty(ModelRoot.Property.CURSOR_POSITION,
                cursorPosition);
        mapViewJComponent.requestFocus();
    }

    /**
     * Listens for changes on the map, for instance when track is built, and
     * refreshes the map views. 666 changes to often -> 10% of time
     */
    public void tilesChanged(Rectangle tilesChanged) {
        if (logger.isDebugEnabled()) {
            logger.debug("TilesChanged = " + tilesChanged);
        }
        // If lots of tiles have changed, do a complete refresh.
        int size = tilesChanged.width * tilesChanged.height;

        if (size > 100) {
            mainMap.refreshAll();
            overviewMap.refreshAll();
        } else {
            Point tile = new Point();

            // Fix for bug 967673 (Crash when building track close to edge of
            // map).
            Rectangle mapRect = new Rectangle(0, 0, world.getMapWidth(), world
                    .getMapHeight());
            tilesChanged = tilesChanged.intersection(mapRect);

            for (tile.x = tilesChanged.x; tile.x < (tilesChanged.x + tilesChanged.width); tile.x++) {
                for (tile.y = tilesChanged.y; tile.y < (tilesChanged.y + tilesChanged.height); tile.y++) {
                    mainMap.refreshTile(tile.x, tile.y);
                    overviewMap.refreshTile(tile.x, tile.y);
                }
            }
        }
    }
}