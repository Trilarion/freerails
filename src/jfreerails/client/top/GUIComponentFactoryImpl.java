package jfreerails.client.top;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
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
import jfreerails.client.renderer.MapRenderer;
import jfreerails.client.renderer.ViewLists;
import jfreerails.client.renderer.ZoomedOutMapRenderer;
import jfreerails.client.view.*;
import jfreerails.client.view.CashJLabel;
import jfreerails.client.view.DateJLabel;
import jfreerails.client.view.DetailMapView;
import jfreerails.client.view.DialogueBoxController;
import jfreerails.client.view.MainMapAndOverviewMapMediator;
import jfreerails.client.view.MapCursor;
import jfreerails.client.view.MapViewJComponentConcrete;
import jfreerails.client.view.MapViewMoveReceiver;
import jfreerails.client.view.ModelRoot;
import jfreerails.client.view.OverviewMapJComponent;
import jfreerails.client.view.StationPlacementCursor;
import jfreerails.client.view.TrainsJTabPane;
import jfreerails.controller.MoveChainFork;
import jfreerails.controller.MoveReceiver;
import jfreerails.controller.StationBuilder;
import jfreerails.controller.UntriedMoveReceiver;
import jfreerails.move.Move;
import jfreerails.move.WorldChangedEvent;
import jfreerails.world.top.ReadOnlyWorld;


public class GUIComponentFactoryImpl implements GUIComponentFactory,
    MoveReceiver {
    private ModelRoot modelRoot;
    private StationPlacementCursor stationPlacementCursor;
    private ServerControlModel sc;
    private DateJLabel datejLabel;
    private CashJLabel cashjLabel;

    /**
     * This is the panel at the bottom right of the screen
     */
    private TrainsJTabPane trainsJTabPane;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JLabel messageJLabel;
    private final DialogueBoxController dialogueBoxController;
    private ViewLists viewLists;

    // private GUIClient client;
    private ReadOnlyWorld world;
    private MainMapAndOverviewMapMediator mediator;
    MapCursor cursor;
    UserInputOnMapController userInputOnMapController;
    StationTypesPopup stationTypesPopup;
    BuildMenu buildMenu;
    JMenu displayMenu;
    JPanel overviewMapContainer;
    JScrollPane mainMapContainer;
    MapViewJComponentConcrete mapViewJComponent;
    private JScrollPane mainMapScrollPane1;
    MapRenderer overviewMap;
    DetailMapView mainMap;
    Rectangle r = new Rectangle(10, 10, 10, 10);
    ClientJFrame clientJFrame;
    UserMessageGenerator userMessageGenerator;

    public GUIComponentFactoryImpl(ModelRoot mr) {
        modelRoot = mr;
        userInputOnMapController = new UserInputOnMapController(modelRoot);
        buildMenu = new jfreerails.client.top.BuildMenu();
        mapViewJComponent = new MapViewJComponentConcrete();
        mainMapScrollPane1 = new JScrollPane();
        overviewMapContainer = new OverviewMapJComponent(r);
        stationTypesPopup = new StationTypesPopup();
        this.mediator = new MainMapAndOverviewMapMediator(overviewMapContainer,
                mainMapScrollPane1.getViewport(), mapViewJComponent, r);

        //glassPanel = new MyGlassPanel();
        //glassPanel.showContent(new NewsPaperJPanel());
        //clientJFrame.setGlassPane(glassPanel);
        trainsJTabPane = new TrainsJTabPane();
        datejLabel = new DateJLabel();

        cashjLabel = new CashJLabel();
        messageJLabel = new javax.swing.JLabel("Message");

        clientJFrame = new ClientJFrame(this);
        dialogueBoxController = new DialogueBoxController(clientJFrame,
                modelRoot);
    }

    public void setup(ViewLists vl, ReadOnlyWorld w) {
        viewLists = vl;
        world = w;

        UntriedMoveReceiver receiver = modelRoot.getReceiver();

        /* create the models */
        modelRoot.setWorld(world, receiver, viewLists);

        clientJFrame.setup();

        if (!vl.validate(world)) {
            throw new IllegalArgumentException("The specified" +
                " ViewLists are not comaptible with the clients" + "world!");
        }

        //create the main and overview maps
        mainMap = new DetailMapView(world, viewLists);
        overviewMap = new ZoomedOutMapRenderer(world);

        //init the move handlers
        MoveReceiver overviewmapMoveReceiver = new MapViewMoveReceiver(mainMap);

        MoveChainFork moveFork = modelRoot.getMoveChainFork();
        moveFork.addSplitMoveReceiver(overviewmapMoveReceiver);

        MoveReceiver mainmapMoveReceiver = new MapViewMoveReceiver(overviewMap);
        moveFork.addSplitMoveReceiver(mainmapMoveReceiver);

        StationBuilder sb = new StationBuilder(receiver, w);

        stationTypesPopup.setup(modelRoot, mainMap.getStationRadius());

        mapViewJComponent.setup(mainMap, w);
        modelRoot.setCursor(mapViewJComponent.getMapCursor());
        this.cursor = modelRoot.getCursor();
        //setup the the main and overview map JComponents
        dialogueBoxController.setDefaultFocusOwner(mapViewJComponent);

        userInputOnMapController.setup(mapViewJComponent,
            modelRoot.getTrackMoveProducer(), stationTypesPopup,
            this.modelRoot, dialogueBoxController, receiver);

        buildMenu.setup(world, modelRoot);
        mainMapScrollPane1.setViewportView(this.mapViewJComponent);

        ((OverviewMapJComponent)overviewMapContainer).setup(overviewMap);

        datejLabel.setup(world, null, null);
        cashjLabel.setup(world, null, null);
        trainsJTabPane.setup(world, vl, modelRoot);

        MapCursor mapCursor = modelRoot.getCursor();
        mapCursor.addCursorEventListener(trainsJTabPane);
        trainsJTabPane.setMapCursor(mapCursor);
        dialogueBoxController.setup(world, vl, modelRoot.getMoveChainFork(),
            modelRoot.getReceiver(), mapCursor);
        stationPlacementCursor = new StationPlacementCursor(modelRoot,
                mainMap.getStationRadius(), mapViewJComponent);
        modelRoot.setUserMessageLogger(this.mapViewJComponent);

        userMessageGenerator = new UserMessageGenerator(this.modelRoot, world);
        moveFork.add(userMessageGenerator);
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

        JMenuItem trainOrdersJMenuItem = new JMenuItem("Train Orders");
        trainOrdersJMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dialogueBoxController.showTrainOrders();
                }
            });

        JMenuItem stationInfoJMenuItem = new JMenuItem("Station Info");
        stationInfoJMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dialogueBoxController.showStationInfo(0);
                }
            });

        JMenuItem trainListJMenuItem = new JMenuItem("Train List");
        trainListJMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    dialogueBoxController.showTrainList();
                }
            });

        displayMenu.add(trainOrdersJMenuItem);
        displayMenu.add(stationInfoJMenuItem);
        displayMenu.add(trainListJMenuItem);

        return displayMenu;
    }

    public JMenu createGameMenu() {
        sc = modelRoot.getServerControls();

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
                    ButtonGroup bg = new ButtonGroup();

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

        //Set up the gamespeed submenu.
        ButtonGroup group = new ButtonGroup();
        ActionAdapter speedActions = sc.getSetTargetTickPerSecondActions();
        JMenu gameSpeedSubMenu = new JMenu("Game Speed...");

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
        gameMenu.add(gameSpeedSubMenu);
        gameMenu.add(newspaperJMenuItem);
        gameMenu.addSeparator();
        gameMenu.add(quitJMenuItem);

        return gameMenu;
    }

    private void addMainMapAndOverviewMapMediatorIfNecessary() {
        //if (this.mainMapContainer != null
        //	&& this.overviewMapContainer != null
        //	&& null == this.mediator) {
        //	//Rectangle r = this.overviewMapContainer.getMainMapVisibleRect();
        //
        //}
    }

    ViewLists getViewLists() {
        return this.viewLists;
    }

    ReadOnlyWorld getAddTrackRules() {
        return this.world;
    }

    public JFrame createClientJFrame(String title) {
        clientJFrame.setTitle(title);

        return clientJFrame;
    }

    public JMenu createHelpMenu() {
        helpMenu = new javax.swing.JMenu("Help");

        JMenuItem showControls = new JMenuItem("Show game controls");
        showControls.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    dialogueBoxController.showGameControls();
                }
            });

        helpMenu.add(showControls);

        return helpMenu;
    }

    public JTabbedPane createTrainsJTabPane() {
        return trainsJTabPane;
    }

    public JLabel createCashJLabel() {
        return cashjLabel;
    }

    public JLabel createMessagePanel() {
        return messageJLabel;
    }

    public JLabel createDateJLabel() {
        return datejLabel;
    }

    private void worldModelChanged() {
        /*
         * XXX this is temporary - we should have a formal object to store
         * the clients copy of the model, connections to the server, etc.
         */
        ReadOnlyWorld world = this.modelRoot.getWorld();
        ViewLists viewLists = getViewLists();

        if (!viewLists.validate(world)) {
            throw new IllegalArgumentException();
        }

        setup(viewLists, world);
    }

    public void processMove(Move m) {
        if (m instanceof WorldChangedEvent) {
            worldModelChanged();
        }
    }
}