/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

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
