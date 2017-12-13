/*
 * Copyright (C) 2002 Luke Lindsay
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

/*
 * DialogueBoxController.java
 *
 * Created on 29 December 2002, 02:05
 */

package org.railz.client.view;
import java.awt.*;
import java.awt.event.*;
import java.util.NoSuchElementException;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.*;

import org.railz.client.common.ScreenHandler;
import org.railz.client.model.ModelRoot;
import org.railz.client.renderer.ViewLists;
import org.railz.controller.MoveChainFork;
import org.railz.controller.UntriedMoveReceiver;
import org.railz.move.ChangeProductionAtEngineShopMove;
import org.railz.move.Move;
import org.railz.util.*;
import org.railz.world.building.*;
import org.railz.world.player.*;
import org.railz.world.station.ProductionAtEngineShop;
import org.railz.world.station.StationModel;
import org.railz.world.top.KEY;
import org.railz.world.top.NonNullElements;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.top.WorldIterator;
import org.railz.world.track.FreerailsTile;

/**
 * This class is responsible for displaying dialogue boxes, adding borders
 * to them as appropriate, and returning focus to the last focus owner after a
 * dialogue box has been closed.  It is also responsible for adding components
 * that need to update in response to moves to the MoveChainFork.
 * @author  lindsal8
 */
public class DialogueBoxController {
    private GUIRoot guiRoot;
    private Component dialog;
    private JFrame frame;
    private SelectEngineJPanel selectEngine;
    private NewsPaperJPanel newspaper;
    private SelectWagonsJPanel selectWagons;
    private HtmlJPanel showControls;
    private HtmlJPanel about;
    private HtmlJPanel how2play;
    private TerrainInfoJPanel terrainInfo;
    private StationInfoJPanel stationInfo;    
    private TrainDialogueJPanel trainDialogueJPanel;
    private ReadOnlyWorld world;
    private ViewLists viewLists;
    private UntriedMoveReceiver moveReceiver;
    private ModelRoot modelRoot;
    
    private Component defaultFocusOwner = null;
    
    private LineBorder defaultBorder =
    new LineBorder(new java.awt.Color(0, 0, 0), 3);
    
    private ReadOnlyWorld w;
    private ViewLists vl;
    
    /**
     * Use this ActionListener to close a dialogue without performing any
     * other action.
    */
    private class CloseButtonListener implements ActionListener {
	private Component dialog;
	
	public CloseButtonListener(Component dialog) {
	    this.dialog = dialog;
	}

        public void actionPerformed(ActionEvent arg0) {
            closeContent(dialog);
        }
    };
    
    private ActionListener closeCurrentDialogue =  new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	    closeContent();
	}
    };

    public DialogueBoxController(JFrame frame, ModelRoot mr,
	    GUIRoot gr) {
	guiRoot = gr;
        modelRoot = mr;
	this.frame = frame;
        modelRoot.setDialogueBoxController(this);
    }
    
    public void setup() {
    	this.w = modelRoot.getWorld();
    	this.vl = modelRoot.getViewLists();
        
        moveReceiver = modelRoot.getReceiver();
        
        if (w == null)
            throw new NullPointerException();
        if (vl == null)
            throw new NullPointerException();
        
        this.world = w;
        viewLists = vl;
        
        //Setup the various dialogue boxes.
        // setup the terrain info dialogue.
        terrainInfo = new TerrainInfoJPanel();
        terrainInfo.setup(w, vl);
        
        // setup the supply and demand at station dialogue.
        stationInfo = new StationInfoJPanel();
        stationInfo.setup(modelRoot, guiRoot);
        
        // setup the 'show controls' dialogue
        showControls = new HtmlJPanel(DialogueBoxController.class.getResource("/org/railz/client/view/game_controls.html"));
        
        about = new HtmlJPanel(DialogueBoxController.class.getResource("/org/railz/client/view/about.htm"));
        
        how2play = new HtmlJPanel(DialogueBoxController.class.getResource("/org/railz/client/view/how_to_play.htm"));
        
        newspaper = new NewsPaperJPanel();
        newspaper.setup(w, vl, closeCurrentDialogue);
        
        selectWagons = new SelectWagonsJPanel();
        
        final ReadOnlyWorld finalROW = this.world;
        //So that inner class can reference it.
        selectWagons.setup(modelRoot, new ActionListener() {
            
            public void actionPerformed(ActionEvent arg0) {
		WorldIterator wi = new NonNullElements(KEY.STATIONS, finalROW,
		    modelRoot.getPlayerPrincipal());
                if (wi.next()) {
                    
                    StationModel station = (StationModel) wi.getElement();
                    
                    ProductionAtEngineShop before = station.getProduction();
                    int engineType = selectEngine.getEngineType();
                    int[] wagonTypes = selectWagons.getWagons();
                    ProductionAtEngineShop after =
                    new ProductionAtEngineShop(engineType, wagonTypes);
                    
                    Move m = new ChangeProductionAtEngineShopMove
		    (before, after, wi.getIndex(),
		     modelRoot.getPlayerPrincipal());
                    moveReceiver.processMove(m);
                }
                closeContent();
            }
            
        });
        
        trainDialogueJPanel = new TrainDialogueJPanel();
        trainDialogueJPanel.setup(modelRoot, guiRoot);
    }
    
    public void showNewspaper(String headline) {
        newspaper.setHeadline(headline);
        showContent(newspaper);
    }
    
    public void showTrainOrders() {
	WorldIterator wi = new NonNullElements(KEY.TRAINS, world,
		modelRoot.getPlayerPrincipal());
        if (!wi.next()) {
            modelRoot.getUserMessageLogger().println("Cannot" +
            " show train orders since there are no" +
            " trains!");
        } else {
            trainDialogueJPanel.display();
            this.showContent(trainDialogueJPanel);
        }
    }
    
    public void showSelectEngine() {
	WorldIterator wi = new NonNullElements(KEY.STATIONS, world,
		modelRoot.getPlayerPrincipal());
        if (!wi.next()) {
            modelRoot.getUserMessageLogger().println("Can't" +
            " build train since there are no stations");
        } else {
	    //Set up select engine dialogue.
	    selectEngine = new SelectEngineJPanel();
	    selectEngine.setCancelButtonActionListener(closeCurrentDialogue);
	    selectEngine.setup(modelRoot, new ActionListener() {
		    public void actionPerformed(ActionEvent arg0) {
			closeContent();
			showSelectWagons();
		    }
		    });
            showContent(selectEngine);
        }
    }

    public void showBalanceSheet() {
	BalanceSheetJPanel bs = new BalanceSheetJPanel(modelRoot);
	showContent(bs);
    }
    
    public void showProfitLoss() {
	ProfitLossJPanel pl = new ProfitLossJPanel();
	pl.setup(modelRoot);
	showContent(pl);
    }
    
    public void showGameControls() {
        
        showContent(this.showControls);
    }
    
    public void showAbout() {
        showContent(this.about);
    }
    
    public void showHow2Play() {
        showContent(this.how2play);
    }
    
    public void showSelectWagons() {
        
        selectWagons.resetSelectedWagons();
        selectWagons.setEngineType(selectEngine.getEngineType());
        showContent(selectWagons);
    }
    
    public void showTerrainInfo(int x, int y) {
        terrainInfo.setLocation(new Point (x, y));
	showContent(terrainInfo);
    }
    
    public void showStationInfo(int stationNumber) {
        try {
            stationInfo.setStation(stationNumber);
            showContent(stationInfo);
        } catch (NoSuchElementException e) {
            System.err.println("Station " + stationNumber + " does not exist!");
        }
    }
    
    public void showTrainList() {
	if (world.size(KEY.TRAINS, modelRoot.getPlayerPrincipal()) > 0) {
	    final TrainListJPanel trainList = new TrainListJPanel();		
	    trainList.setup(modelRoot, closeCurrentDialogue);
	    trainList.setShowTrainDetailsActionListener(
		    new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
			int id = trainList.getSelectedTrainID();
			closeContent();
			if(id != -1){
			    trainDialogueJPanel.display();
			    showContent(trainDialogueJPanel);
			}
		    }
	    });
	    showContent(trainList);
	} else {
            modelRoot.getUserMessageLogger().println("There are" +
            " no trains to display!");
        }
    }
    
    /**
     * Shows the JOptionPane. Blocks until the pane is closed.
     */
    public void showOptionPane(JOptionPane component) {
        JComponent contentPanel;
	boolean addCloseButton = false;
	contentPanel = component;
        
        contentPanel.setBorder(defaultBorder);
	switch (guiRoot.getScreenHandler().getMode()) {
	    case ScreenHandler.FULL_SCREEN:
		JInternalFrame jif = component.createInternalFrame
			(frame, Resources.get("Railz Dialog"));
		dialog = jif;
		jif.show();
		break;
	    default:
		JDialog jd = component.createDialog
			(frame, Resources.get("Railz Dialog"));
		dialog = jd;
		jd.show();
		break;
	}
    }

    public void showContent(JComponent component) {
        JComponent contentPanel;
	boolean addCloseButton = false;
        if (!((component instanceof SelectEngineJPanel) ||
		    (component instanceof SelectWagonsJPanel) ||
		    (component instanceof TrainListJPanel))) {
            contentPanel = new javax.swing.JPanel();
            contentPanel.setLayout(new java.awt.GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.weightx = 1.0;
            constraints.weighty = 1.0;
	    constraints.fill = GridBagConstraints.BOTH;
            contentPanel.add(component, constraints);
            
	    addCloseButton = true;
        } else {
            contentPanel = component;
        }
        
        contentPanel.setBorder(defaultBorder);
	switch (guiRoot.getScreenHandler().getMode()) {
	    case ScreenHandler.FULL_SCREEN:
		JInternalFrame jif = new JInternalFrame
		    (Resources.get("Railz Dialog"), true);
		if (addCloseButton) {
		    GridBagConstraints constraints = new GridBagConstraints();
		    constraints.gridx = 0;
		    constraints.gridy = 1;
		    JButton closeButton = new JButton(Resources.get("Close"));
		    closeButton.addActionListener(new CloseButtonListener(jif));
		    contentPanel.add(closeButton, constraints);
		}
		jif.getContentPane().add(contentPanel);
		jif.pack();
		jif.setLocation((frame.getWidth() - jif.getWidth()) / 2,
			(frame.getHeight() - jif.getHeight()) / 2);

		frame.getLayeredPane().add(jif, JLayeredPane.MODAL_LAYER);
		dialog = jif;
		jif.show();
		break;
	    default:
		JDialog jd = new JDialog(frame, Resources.get("Railz Dialog"),
			    false);
		if (addCloseButton) {
		    GridBagConstraints constraints = new GridBagConstraints
			();
		    constraints.gridx = 0;
		    constraints.gridy = 1;
		    JButton closeButton = new JButton(Resources.get("Close"));
		    closeButton.addActionListener(new CloseButtonListener(jd));
		    contentPanel.add(closeButton, constraints);
		}
		jd.getContentPane().add(contentPanel);
		jd.pack();
		jd.setLocation(frame.getX() +
			(frame.getWidth() - jd.getWidth()) / 2,
			frame.getY() +
			(frame.getHeight() - jd.getHeight()) / 2);
		dialog = jd;
		jd.show();
		break;
	}
    }

    private void closeContent(Component dialog) {
        dialog.setVisible(false);
        if (null != defaultFocusOwner) {
            defaultFocusOwner.requestFocus();
        }
    }

    public void closeContent() {
	closeContent(dialog);
    }
    
    public void setDefaultFocusOwner(Component defaultFocusOwner) {
        this.defaultFocusOwner = defaultFocusOwner;
    }
    
    public void showStationOrTerrainInfo(int x, int y) {
        FreerailsTile tile = world.getTile(x, y);
	BuildingTile bTile = tile.getBuildingTile();
	if (bTile != null) {
	    BuildingType bType = (BuildingType) world.get(KEY.BUILDING_TYPES,
		    tile.getTrackRule(), Player.AUTHORITATIVE);
	    if (bType.getCategory() == BuildingType.CATEGORY_STATION) {
		for (int i = 0; i < world.size(KEY.STATIONS,
			    modelRoot.getPlayerPrincipal()); i++) {
		    StationModel station =
			(StationModel) world.get(KEY.STATIONS, i,
						 modelRoot.getPlayerPrincipal());
		    if (null != station && station.x == x && station.y == y) {
			this.showStationInfo(i);
			return;
		    }
		}
		throw new IllegalStateException(
			"Could find station at " + x + ", " + y);
	    }
        }
	this.showTerrainInfo(x, y);
    }

    public Component createDialog(JComponent content, String title) {
	Component dialog;
	switch (guiRoot.getScreenHandler().getMode()) {
	    case ScreenHandler.FULL_SCREEN:
		JInternalFrame jif = new CustomInternalFrame(title,
			true);
		jif.getContentPane().add(content);
		jif.pack();
		jif.setLocation((frame.getWidth() - jif.getWidth()) / 2,
			(frame.getHeight() - jif.getHeight()) / 2);

		frame.getLayeredPane().add(jif, JLayeredPane.MODAL_LAYER);
		jif.setDefaultCloseOperation(jif.DISPOSE_ON_CLOSE);
		dialog = jif;
		break;
	    default:
		CustomDialog jd = new CustomDialog(frame, title, false);
		jd.getContentPane().add(content);
		jd.pack();
		jd.setLocation(frame.getX() +
			(frame.getWidth() - jd.getWidth()) / 2,
		       	frame.getY() +
			(frame.getHeight() - jd.getHeight()) / 2);
		jd.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog = jd;
		break;
	}
	dialog.setVisible(true);
	return dialog;
    }

    private class CustomInternalFrame extends JInternalFrame {
	private InternalFrameListener listener = new InternalFrameAdapter() {
	    public void internalFrameClosed(InternalFrameEvent e) {
		CustomInternalFrame.this.processComponentEvent
		    (new ComponentEvent(CustomInternalFrame.this,
					ComponentEvent.COMPONENT_HIDDEN));
	    }
	};

	public CustomInternalFrame(String title, boolean resizable) {
	    super(title, resizable);
	    addInternalFrameListener(listener);
	}
    }

    private class CustomDialog extends JDialog {
	private WindowListener listener = new WindowAdapter() {
	    public void windowClosed(WindowEvent e) {
		CustomDialog.this.processComponentEvent
		    (new ComponentEvent(CustomDialog.this,
					ComponentEvent.COMPONENT_HIDDEN));
	    }
	};

	public CustomDialog(Frame owner, String title, boolean modal) {
	    super(owner, title, modal);
	    addWindowListener(listener);
	}
    }
}
