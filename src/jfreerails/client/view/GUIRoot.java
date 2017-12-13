package jfreerails.client.view;

import java.awt.Rectangle;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import jfreerails.client.common.ScreenHandler;
import jfreerails.client.renderer.ViewLists;
import jfreerails.client.renderer.ZoomedOutMapRenderer;
import jfreerails.client.top.ClientJFrame;
import jfreerails.client.model.ModelRoot;
import jfreerails.client.model.ModelRootListener;
import jfreerails.controller.MoveChainFork;
import jfreerails.controller.MoveReceiver;
import jfreerails.controller.StationBuilder;
import jfreerails.controller.UntriedMoveReceiver;
import jfreerails.world.top.ReadOnlyWorld;

/**
 * A central point for coordinating GUI components.
 */
public class GUIRoot implements ModelRootListener {
    private ModelRoot modelRoot;

    private DialogueBoxController dialogueBoxController;
    private ReadOnlyWorld world;
    private MapViewJComponentConcrete mapViewJComponent;
    DetailMapView mainMap;
    ClientJFrame clientJFrame;
    private MainMapAndOverviewMapMediator mediator;
    private ScreenHandler screenHandler;

    public GUIRoot(ModelRoot mr) {
        modelRoot = mr;

        modelRoot.addModelRootListener(this);
        mediator = new MainMapAndOverviewMapMediator();
        clientJFrame = new ClientJFrame(this, modelRoot);
        dialogueBoxController = new DialogueBoxController(clientJFrame,
                modelRoot, this);
    }

    private void setup() {
        ViewLists viewLists = modelRoot.getViewLists();
        world = modelRoot.getWorld();

        if (!viewLists.validate(world)) {
            throw new IllegalArgumentException("The specified" +
                " ViewLists are not comaptible with the clients world!");
        }

        dialogueBoxController.setup();

        clientJFrame.setup();
    }

    public ScreenHandler getScreenHandler() {
	return screenHandler;
    }

    public void setScreenHandler(ScreenHandler sh) {
	screenHandler = sh;
    }

    public DialogueBoxController getDialogueBoxController() {
	return dialogueBoxController;
    }

    public MainMapAndOverviewMapMediator getMapMediator() {
	return mediator;
    }

    public void setMapMediator(MainMapAndOverviewMapMediator m) {
	mediator = m;
    }

    public MapViewJComponentConcrete getMapViewJComponent() {
	return mapViewJComponent;
    }

    public void setMapViewJComponent(MapViewJComponentConcrete mapView) {
	mapViewJComponent = mapView;
    }

    public JFrame getClientJFrame() {
        return clientJFrame;
    }

    public void modelRootChanged() {
        setup();
    }

    public void update() {
    }
}
