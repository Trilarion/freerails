package jfreerails.client.top;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Random;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import jfreerails.client.common.ActionAdapter;
import jfreerails.client.common.ModelRoot;
import jfreerails.client.renderer.MapRenderer;
import jfreerails.client.renderer.ViewLists;
import jfreerails.client.renderer.ZoomedOutMapRenderer;
import jfreerails.client.view.ActionRoot;
import jfreerails.client.view.CashJLabel;
import jfreerails.client.view.DateJLabel;
import jfreerails.client.view.DetailMapRenderer;
import jfreerails.client.view.DialogueBoxController;
import jfreerails.client.view.MainMapAndOverviewMapMediator;
import jfreerails.client.view.MapViewJComponentConcrete;
import jfreerails.client.view.OverviewMapJComponent;
import jfreerails.client.view.ServerControlModel;
import jfreerails.client.view.StationPlacementCursor;
import jfreerails.client.view.RHSJTabPane;
import jfreerails.controller.MoveReceiver;
import jfreerails.move.ChangeGameSpeedMove;
import jfreerails.move.ChangeProductionAtEngineShopMove;
import jfreerails.move.Move;
import jfreerails.world.common.GameSpeed;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.station.ProductionAtEngineShop;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.WorldIterator;
import jfreerails.world.top.WorldListListener;
import jfreerails.world.top.WorldMapListener;


/**
 * Creates and wires up the GUI components.
 * @author Luke
 */
public class GUIComponentFactoryImpl implements GUIComponentFactory,
    WorldMapListener, WorldListListener {
    private static final Logger logger = Logger.getLogger(GUIComponentFactoryImpl.class.getName());

    /** Whether to show certain 'cheat' menus used for testing.*/
    private static final boolean CHEAT = (System.getProperty("cheat") != null);
    private final ModelRoot modelRoot;
    private final ActionRoot actionRoot;
    private ServerControlModel sc;
    private final DateJLabel datejLabel;
    private final CashJLabel cashjLabel;

    /**
     * This is the panel at the bottom right of the screen.
     */
    private final RHSJTabPane trainsJTabPane;
    private javax.swing.JMenu helpMenu;
    private final DialogueBoxController dialogueBoxController;
    private ViewLists viewLists;
    private ReadOnlyWorld world;
    private final UserInputOnMapController userInputOnMapController;
    private final StationTypesPopup stationTypesPopup;
    private final BuildMenu buildMenu;
    private JMenu displayMenu;
    private final JPanel overviewMapContainer;
    private final MapViewJComponentConcrete mapViewJComponent;
    private final JScrollPane mainMapScrollPane1;
    private MapRenderer overviewMap;
    private DetailMapRenderer mainMap;
    private final Rectangle r = new Rectangle(10, 10, 10, 10);
    private final ClientJFrame clientJFrame;
    private UserMessageGenerator userMessageGenerator;
    private ActionAdapter speedActions;
    private JMenuItem trainOrdersJMenuItem;
    private JMenuItem trainListJMenuItem;
    private JMenuItem stationInfoJMenuItem;

    public GUIComponentFactoryImpl(ModelRoot mr, ActionRoot ar) {
        modelRoot = mr;
        actionRoot = ar;
        userInputOnMapController = new UserInputOnMapController(modelRoot);
        buildMenu = new jfreerails.client.top.BuildMenu();
        mapViewJComponent = new MapViewJComponentConcrete();
        mainMapScrollPane1 = new JScrollPane();
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
                modelRoot, actionRoot);

        modelRoot.addSplitMoveReceiver(new MoveReceiver() {
                public void processMove(Move move) {
                    if (move instanceof ChangeGameSpeedMove) {
                        ChangeGameSpeedMove speedMove = (ChangeGameSpeedMove)move;

                        for (Enumeration enum = speedActions.getActions();
                                enum.hasMoreElements();) {
                            Action action = (Action)enum.nextElement();
                            String actionName = (String)action.getValue(Action.NAME);

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

    /** Called when a new game is started or a game is loaded.
     * <p><b>Be extremely careful with the references of objects allocated in
     * this method to avoid memory leaks - see bug 967677 (OutOfMemoryError after starting several new games). </b></p>
     */
    public void setup(ViewLists vl, ReadOnlyWorld w) {
        viewLists = vl;
        world = w;
        modelRoot.addMapListener(this);
        modelRoot.addListListener(this);

        if (!vl.validate(world)) {
            throw new IllegalArgumentException("The specified" +
                " ViewLists are not comaptible with the clients" + "world!");
        }

        //create the main and overview maps
        mainMap = new DetailMapRenderer(world, viewLists, modelRoot);

        Dimension maxSize = new Dimension(200, 200);
        overviewMap = ZoomedOutMapRenderer.getInstance(world, maxSize);

        stationTypesPopup.setup(actionRoot, mainMap.getStationRadius());

        mapViewJComponent.setup(mainMap, modelRoot);

        //setup the the main and overview map JComponents
        dialogueBoxController.setDefaultFocusOwner(mapViewJComponent);

        userInputOnMapController.setup(mapViewJComponent,
            actionRoot.getTrackMoveProducer(), stationTypesPopup,
            this.modelRoot, dialogueBoxController,
            mapViewJComponent.getMapCursor(), mainMap.getBuildTrack());

        buildMenu.setup(actionRoot);
        mainMapScrollPane1.setViewportView(this.mapViewJComponent);

        ((OverviewMapJComponent)overviewMapContainer).setup(overviewMap);

        datejLabel.setup(modelRoot, vl, null);
        cashjLabel.setup(modelRoot, vl, null);
        trainsJTabPane.setup(actionRoot, vl, modelRoot);

        dialogueBoxController.setup(modelRoot, vl);

        StationPlacementCursor stationPlacementCursor = new StationPlacementCursor(actionRoot,
                mainMap.getStationRadius(), mapViewJComponent);

        int gameSpeed = ((GameSpeed)world.get(ITEM.GAME_SPEED)).getSpeed();

        /* Set the selected game speed radio button.*/
        String actionName = actionRoot.getServerControls().getGameSpeedDesc(gameSpeed);
        speedActions.setSelectedItem(actionName);
        userMessageGenerator.logSpeed();

        /* Count stations and trains to determine if we need to display the station and
         * train menu items and tabs.3
         */
        countStations();
        countTrains();
    }

    public JPanel createOverviewMap() {
        return overviewMapContainer;
    }

    public JScrollPane createMainMap() {
        return mainMapScrollPane1;
    }

    public JMenu createBuildMenu() {
        return buildMenu;
    }

    public JMenu createDisplayMenu() {
        displayMenu = new JMenu("Display");
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

        //Add menu items to control what gets displayed on the map.
        final JCheckBoxMenuItem showCargoMenuItem = new JCheckBoxMenuItem("Show cargo at stations",
                true);
        displayMenu.add(showCargoMenuItem);
        showCargoMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    modelRoot.setProperty(ModelRoot.SHOW_CARGO_AT_STATIONS,
                        new Boolean(showCargoMenuItem.isSelected()));
                    mapViewJComponent.refreshAll();
                }
            });

        final JCheckBoxMenuItem showStationNamesMenuItem = new JCheckBoxMenuItem("Show station names",
                true);
        displayMenu.add(showStationNamesMenuItem);
        showStationNamesMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    modelRoot.setProperty(ModelRoot.SHOW_STATION_NAMES,
                        new Boolean(showStationNamesMenuItem.isSelected()));
                    mapViewJComponent.refreshAll();
                }
            });

        final JCheckBoxMenuItem showStationBordersMenuItem = new JCheckBoxMenuItem("Show sphere-of-influence around stations",
                true);
        displayMenu.add(showStationBordersMenuItem);
        showStationBordersMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    modelRoot.setProperty(ModelRoot.SHOW_STATION_BORDERS,
                        new Boolean(showStationBordersMenuItem.isSelected()));
                    mapViewJComponent.refreshAll();
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
                public void menuSelected(MenuEvent e) {
                    newGameJMenu.removeAll();

                    Enumeration actions = sc.getMapNames().getActions();

                    while (actions.hasMoreElements()) {
                        JMenuItem mi = new JMenuItem((Action)actions.nextElement());
                        newGameJMenu.add(mi);
                    }
                }

                public void menuCanceled(MenuEvent e) {
                }

                public void menuDeselected(MenuEvent e) {
                }
            });

        JMenuItem saveGameJMenuItem = new JMenuItem(sc.getSaveGameAction());

        JMenuItem loadGameJMenuItem = new JMenuItem(sc.getLoadGameAction());

        JMenuItem newspaperJMenuItem = new JMenuItem("Newspaper");
        newspaperJMenuItem.setMnemonic(78);

        newspaperJMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dialogueBoxController.showNewspaper("Headline");
                    //glassPanel.setVisible(true);
                }
            });

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

        //Set up the gamespeed submenu.
        JMenu gameSpeedSubMenu = new JMenu("Game Speed");

        ButtonGroup group = new ButtonGroup();

        speedActions = sc.getSetTargetTickPerSecondActions();

        Enumeration buttonModels = speedActions.getButtonModels();
        Enumeration actions = speedActions.getActions();

        while (buttonModels.hasMoreElements()) {
            JRadioButtonMenuItem mi = new JRadioButtonMenuItem((Action)actions.nextElement());
            mi.setModel((ButtonModel)buttonModels.nextElement());
            group.add(mi);
            gameSpeedSubMenu.add(mi);
        }

        gameMenu.add(newGameJMenu);
        gameMenu.addSeparator();
        gameMenu.add(loadGameJMenuItem);
        gameMenu.add(saveGameJMenuItem);
        gameMenu.addSeparator();
        gameMenu.add(balanceSheetJMenuItem);
        gameMenu.add(incomeStatementJMenuItem);
        gameMenu.add(gameSpeedSubMenu);
        gameMenu.add(newspaperJMenuItem);
        gameMenu.addSeparator();
        gameMenu.add(quitJMenuItem);

        if (CHEAT) {
            /** For testing.*/
            final ActionListener build200trains = new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        WorldIterator wi = new NonNullElements(KEY.STATIONS,
                                modelRoot.getWorld(), modelRoot.getPrincipal());

                        if (wi.next()) {
                            Random randy = new Random();
                            StationModel station = (StationModel)wi.getElement();

                            ProductionAtEngineShop[] before = station.getProduction();
                            int numberOfEngineTypes = modelRoot.getWorld().size(SKEY.ENGINE_TYPES) -
                                1;
                            int numberOfcargoTypes = modelRoot.getWorld().size(SKEY.CARGO_TYPES) -
                                1;
                            ProductionAtEngineShop[] after = new ProductionAtEngineShop[200];

                            for (int i = 0; i < after.length; i++) {
                                int engineType = randy.nextInt(numberOfEngineTypes);
                                int[] wagonTypes = new int[] {
                                        randy.nextInt(numberOfcargoTypes),
                                        randy.nextInt(numberOfcargoTypes),
                                        randy.nextInt(numberOfcargoTypes)
                                    };
                                ProductionAtEngineShop paes = new ProductionAtEngineShop(engineType,
                                        wagonTypes);
                                after[i] = paes;
                            }

                            Move m = new ChangeProductionAtEngineShopMove(before,
                                    after, wi.getIndex(),
                                    modelRoot.getPrincipal());
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

    public JFrame createClientJFrame(String title) {
        clientJFrame.setTitle(title);

        return clientJFrame;
    }

    public JMenu createHelpMenu() {
        helpMenu = new javax.swing.JMenu("Help");

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
        showJavaProperties.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    dialogueBoxController.showJavaProperties();
                }
            });

        helpMenu.add(showControls);
        helpMenu.add(how2play);
        helpMenu.add(showJavaProperties);
        helpMenu.add(about);

        return helpMenu;
    }

    public JTabbedPane createTrainsJTabPane() {
        return trainsJTabPane;
    }

    public JLabel createCashJLabel() {
        return cashjLabel;
    }

    public JLabel createDateJLabel() {
        return datejLabel;
    }

    /** Listens for changes on the map, for instance when track is built, and
         * refreshes the map views.
         */
    public void tilesChanged(Rectangle tilesChanged) {
        Point tile = new Point();
        logger.fine("TilesChanged = " + tilesChanged);

        // Fix for bug 967673 (Crash when building track close to edge of map). 
        Rectangle mapRect = new Rectangle(0, 0, world.getMapWidth(),
                world.getMapHeight());
        tilesChanged = tilesChanged.intersection(mapRect);

        for (tile.x = tilesChanged.x;
                tile.x < (tilesChanged.x + tilesChanged.width); tile.x++) {
            for (tile.y = tilesChanged.y;
                    tile.y < (tilesChanged.y + tilesChanged.height);
                    tile.y++) {
                mainMap.refreshTile(tile.x, tile.y);
                overviewMap.refreshTile(tile.x, tile.y);
            }
        }
    }

    private void countStations() {
        NonNullElements stations = new NonNullElements(KEY.STATIONS,
                modelRoot.getWorld(), modelRoot.getPrincipal());
        boolean enabled;

        if (stations.size() > 0) {
            enabled = true;
        } else {
            enabled = false;
        }

        this.trainsJTabPane.setStationTabEnabled(enabled);
        this.stationInfoJMenuItem.setEnabled(enabled);
    }

    private void countTrains() {
        NonNullElements trains = new NonNullElements(KEY.TRAINS,
                modelRoot.getWorld(), modelRoot.getPrincipal());
        boolean enabled;

        if (trains.size() > 0) {
            enabled = true;
        } else {
            enabled = false;
        }

        this.trainsJTabPane.setTrainTabEnabled(enabled);
        this.trainListJMenuItem.setEnabled(enabled);
        this.trainOrdersJMenuItem.setEnabled(enabled);
    }

    public void listUpdated(KEY key, int index, FreerailsPrincipal principal) {
        boolean rightPrincipal = principal.equals(this.modelRoot.getPrincipal());

        if (KEY.TRAINS == key && rightPrincipal) {
            countTrains();
        } else if (KEY.STATIONS == key && rightPrincipal) {
            countStations();
        }
    }

    public void itemAdded(KEY key, int index, FreerailsPrincipal principal) {
        boolean rightPrincipal = principal.equals(this.modelRoot.getPrincipal());

        if (KEY.TRAINS == key && rightPrincipal) {
            countTrains();
        } else if (KEY.STATIONS == key && rightPrincipal) {
            countStations();
        }
    }

    public void itemRemoved(KEY key, int index, FreerailsPrincipal principal) {
        //do nothing
    }
}