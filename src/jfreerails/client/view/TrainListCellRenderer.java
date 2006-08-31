/*
 * TrainView.java
 *
 * Created on 22 August 2003, 20:49
 */
package jfreerails.client.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;

import javax.swing.Action;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import jfreerails.client.renderer.RenderersRoot;
import jfreerails.controller.ModelRoot;
import jfreerails.world.common.ImInts;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.WorldListListener;
import jfreerails.world.train.ImmutableSchedule;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainOrdersModel;

/**
 * This JPanel displays an engine and a number of wagons.
 * 
 * @author Luke Lindsay
 */
public class TrainListCellRenderer extends JPanel implements View, ListCellRenderer,
		WorldListListener {
	private static final long serialVersionUID = 3546076964969591093L;

	private ReadOnlyWorld w;

	private RenderersRoot vl;

	private int trainNumber = -1;

	private int scheduleOrderNumber;

	private int scheduleID = -1;

	private int height = 100;

	private FreerailsPrincipal principal;

	private Image[] images = new Image[0];

	/**
	 * Whether this JPanel should one of the trains orders from the schedule
	 * instead of the trains current formation.
	 */
	private boolean showingOrder = false;

	/**
	 * If true, the train is drawn in the center to the JPanel; if false, the
	 * train is drawn left aligned.
	 */
	private boolean centerTrain = false;

	private int trainWidth = 0;

	private boolean selected = false;

	private final Color backgoundColor = (java.awt.Color) javax.swing.UIManager.getDefaults().get(
			"List.background");

	private final Color selectedColor = (java.awt.Color) javax.swing.UIManager.getDefaults().get(
			"List.selectionBackground");

	private final Color selectedColorNotFocused = Color.LIGHT_GRAY;

	public TrainListCellRenderer() {
		this.setOpaque(false);
	}

	public TrainListCellRenderer(ModelRoot mr, RenderersRoot vl) {
		setup(mr, vl, null);
		this.setBackground(backgoundColor);
	}

	public void setCenterTrain(boolean b) {
		this.centerTrain = b;
	}

	public void display(int newTrainNumber) {
		showingOrder = false;
		this.trainNumber = newTrainNumber;

		TrainModel train = (TrainModel) w.get(principal, KEY.TRAINS, trainNumber);		
		display(train.getEngineType(), train.getConsist());
		resetPreferredSize();
	}
	
	private void display(int engine, ImInts wagons){
		images = new Image[1 + wagons.size()];
		// images[0] = vl.getTrainImages().getSideOnEngineImage(
		// train.getEngineType(), height);
		String engineFilename = vl.getEngineImages(engine).sideOnFileName;
		try {
			images[0] = vl.getScaledImage(engineFilename, height);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(engineFilename);
		}
		for (int i = 0; i < wagons.size(); i++) {
			// images[i + 1] = vl.getTrainImages().getSideOnWagonImage(
			// order.consist.get(i), height);
			int wagonType = wagons.get(i);
			String wagonFilename = vl.getWagonImages(wagonType).sideOnFileName;
			try {
				images[i + 1] = vl.getScaledImage(wagonFilename, height);
			} catch (IOException e) {
				e.printStackTrace();
				throw new IllegalArgumentException(wagonFilename);
			}

		}
	}

	public void display(int newTrainNumber, int newScheduleOrderID) {
		showingOrder = true;
		this.trainNumber = newTrainNumber;
		this.scheduleOrderNumber = newScheduleOrderID;

		TrainModel train = (TrainModel) w.get(principal, KEY.TRAINS, trainNumber);
		this.scheduleID = train.getScheduleID();

		ImmutableSchedule s = (ImmutableSchedule) w.get(principal, KEY.TRAIN_SCHEDULES, scheduleID);
		TrainOrdersModel order = s.getOrder(newScheduleOrderID);

		// Set up the array of images.
		if (null != order.consist) {
			display(train.getEngineType(), order.consist);
		} else {
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
	}

	public void setup(ModelRoot mr, RenderersRoot vl, Action closeAction) {
		this.w = mr.getWorld();
		this.vl = vl;
		this.principal = mr.getPrincipal();
	}

	public Component getListCellRendererComponent(JList list, Object value, int index,
			boolean isSelected, boolean cellHasFocus) {

		int trainID = NonNullElements.row2index(w, KEY.TRAINS, principal, index);
		display(trainID);

		selected = isSelected;

		if (selected) {
			if (list.isFocusOwner()) {
				setBackground(selectedColor);
			} else {
				setBackground(selectedColorNotFocused);
			}
		} else {
			setBackground(backgoundColor);
		}

		return this;
	}

	@Override
	public int getHeight() {
		return height;
	}

	public void setHeight(int i) {
		height = i;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		int x = 0;

		if (this.centerTrain) {
			x = (this.getWidth() - this.trainWidth) / 2;
		}

		for (int i = 0; i < images.length; i++) {
			g.drawImage(images[i], x, 0, null);
			x += images[i].getWidth(null);
		}
	}

	public void listUpdated(KEY key, int index, FreerailsPrincipal p) {
		if (showingOrder) {
			if (KEY.TRAIN_SCHEDULES == key && this.scheduleID == index) {
				this.display(this.trainNumber, this.scheduleOrderNumber);
			}
		} else {
			if (KEY.TRAINS == key && this.trainNumber == index) {
				this.display(this.trainNumber);
			}
		}
	}

	public void itemAdded(KEY key, int index, FreerailsPrincipal p) {
	}

	public void itemRemoved(KEY key, int index, FreerailsPrincipal p) {
	}
}