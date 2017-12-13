/*
 * Copyright (C) 2004 Robert Tuck
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

package org.railz.client.view;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

import org.railz.client.model.ModelRoot;
import org.railz.client.renderer.ViewLists;
import org.railz.world.top.KEY;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.train.*;
import org.railz.world.cargo.CargoBundle;
import org.railz.world.cargo.CargoType;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.player.Player;

/**
 *  This JPanel displays an engine and a number of wagons.
 * @author Robert Tuck 
 */
public class TrainViewJPanel2 extends JPanel {
    private ModelRoot modelRoot;
    
    private ReadOnlyWorld w;
    
    private ViewLists vl;
    
    private int scheduleOrderNumber;
    
    private ScheduleIterator scheduleIterator;
    
    private int height = 20;
    
    private Image[] images = new Image[0];
    
    /** Whether this JPanel should one of the trains orders from the schedule instead of the trains current formation.*/
    private boolean showingOrder = false;
    
    /** If true, the train is drawn in the center to the JPanel; if false,
     *the train is drawn left aligned.
     */
    private boolean centerTrain = false;
    
    private int trainWidth = 0;
    
    boolean selected = false;
    
    private Color backgoundColor = (java.awt.Color) javax.swing.UIManager.getDefaults().get("List.background");
    
    private Color selectedColor = (java.awt.Color) javax.swing.UIManager.getDefaults().get("List.selectionBackground");
    
    private TrainModel train;

    public TrainViewJPanel2() {
	/* so that we can be JavaBeany */
	addMouseListener(mouseListener);
	addMouseMotionListener(mouseListener);
    }

    public TrainViewJPanel2(ModelRoot mr, TrainModel tm) {
	this();
	setup(mr, tm);		  
    }

    public void setCenterTrain(boolean b){
        this.centerTrain = b;
    }
    
    private boolean showCargo = false;

    /**
     * Whether to show cargo info showing how much cargo the train is carrying
     */
    public void setShowCargo(boolean b) {
	showCargo = b;
    }

    public static final int NO_WAGON_SELECTED = -1;
    private int selectedWagon = NO_WAGON_SELECTED;

    private int highlightedWagon = -1;
    
    /**
     * displays current train consist
     */
    public void display() {
        showingOrder = false;
	int cargoAmounts[] = new int [w.size(KEY.CARGO_TYPES)];
	if (train != null) {
	    if (showCargo) {
		CargoBundle cargoBundle = (CargoBundle) w.get(KEY.CARGO_BUNDLES,
			train.getCargoBundleNumber());

		//evaluate amount of each cargo on the train
		String cargoText = "Empty train";
		int totalAmount = 0;
		for (int i = 0; i < cargoAmounts.length; i++) {
		    cargoAmounts[i] = cargoBundle.getAmount(i);
		    if (cargoAmounts[i] > 0) {
			CargoType cargoType = (CargoType) w.get(KEY.CARGO_TYPES, i,
				Player.AUTHORITATIVE);
			String cargoTypeName = cargoType.getDisplayName();
			if (totalAmount > 0) {
			    cargoText += ", ";
			} else {
			    cargoText = "";
			}
			cargoText += cargoTypeName + " (" + cargoAmounts[i] + ")";
			totalAmount += cargoAmounts[i];
		    }
		}
		setToolTipText(cargoText);
	    }
        
	    //Set up the array of images.
	    images = new Image[1 + train.getNumberOfWagons()];
	    images[0] =
		vl.getTrainImages().getSideOnEngineImage(
			train.getEngineType(),
			height);
	    for (int i = 0; i < train.getNumberOfWagons(); i++) {
		WagonType wagonType = (WagonType) w.get(KEY.WAGON_TYPES,
			train.getWagon(i));
		if (showCargo) {
		    int wagonCapacity = wagonType.getCapacity();
		    int x = (cargoAmounts[wagonType.getCargoType()] >
			    wagonCapacity) ? wagonCapacity :
			cargoAmounts[wagonType.getCargoType()];
		    images[i + 1] =
			vl.getTrainImages().getSideOnWagonImage(
				train.getWagon(i),
				height, (x * 100) / wagonCapacity);
		    cargoAmounts[wagonType.getCargoType()] -= x;
		} else {
		    images[i+1] = vl.getTrainImages().getSideOnWagonImage
			(train.getWagon(i), height);
		}
	    } 
	}else {
	    images = new Image[0];
	}
	resetPreferredSize();
    }
    
    private void resetPreferredSize() {
        int width = 0;
        for (int i = 0; i < images.length; i++) {
            width += images[i].getWidth(null);
        }
        this.trainWidth = width;
        this.setPreferredSize(new Dimension(width, height));
	setMinimumSize(new Dimension(Math.max(200, width), height));
    }
    
    public void setup(ModelRoot mr, TrainModel tm) {
	modelRoot = mr;
        w = modelRoot.getWorld();
        vl = modelRoot.getViewLists();
	setTrain(tm);
    }
    
    public void setTrain(TrainModel tm) {
	train = new TrainModel(tm);
	selectedWagon = NO_WAGON_SELECTED;
	display();
	repaint();
    }

    public int getHeight() {
        return height;
    }
    
    public void setHeight(int i) {
        height = i;
	resetPreferredSize();
    }
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int x = 0;
        if(this.centerTrain){
            x = (this.getWidth() - this.trainWidth) /2;
        }
        
        for (int i = 0; i < images.length; i++) {
	    if (highlightedWagon >= 0 && 
		    highlightedWagon == i - 1) {
		Color c = g.getColor();
		g.setColor(selectedColor);
		g.fillRect(x, 0, images[i].getWidth(null), height);
		g.setColor(c);
	    }
            g.drawImage(images[i], x, 0, null);
            x += images[i].getWidth(null);
        }
    }

    private int getWagonAt(int x, int y) {
	int imageX = centerTrain ? (getWidth() - trainWidth) / 2 : 0;
	imageX += images[0].getWidth(null);

	if (y < 0 || y > height || x < imageX)
	    return -1;

	for (int i = 1; i < images.length; i++) {
	    imageX += images[i].getWidth(null);
	    if (x < imageX) 
		return i - 1;
	}
	return -1;   
    }

    private MouseInputListener mouseListener = new MouseInputAdapter() {
	public void mouseMoved(MouseEvent e) {
	    int newWagon = getWagonAt(e.getX(), e.getY());
	    if (newWagon != highlightedWagon) {
		highlightedWagon = newWagon;
		repaint();
	    }
	}

	public void mouseClicked(MouseEvent e) {
	    mouseMoved(e);
	    selectedWagon = highlightedWagon;

	    if (highlightedWagon < 0)
		return;

	    fireAction(new ActionEvent(TrainViewJPanel2.this,
			ActionEvent.ACTION_PERFORMED, "WAGON SELECTED"));
	}
    };

    public int getSelectedWagon() {
	return selectedWagon;
    }

    private ArrayList actionListeners = new ArrayList();
    
    public void addActionListener(ActionListener l) {
	synchronized (actionListeners) {
	    actionListeners.add(l);
	}
    }

    public void removeActionListener(ActionListener l) {
	synchronized (actionListeners) {
	    actionListeners.remove(l);
	}
    }

    private void fireAction(ActionEvent e) {
	ActionListener[] listeners;
	synchronized (actionListeners) {
	    listeners = (ActionListener[])
		actionListeners.toArray(new
			ActionListener[actionListeners.size()]);
	}
	for (int i = 0; i < listeners.length; i++) {
	    listeners[i].actionPerformed(e);
	}
    }
}

