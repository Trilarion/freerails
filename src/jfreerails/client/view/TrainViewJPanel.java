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

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import jfreerails.client.renderer.ViewLists;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.train.TrainModel;
/**
 *  This JPanel displays an engine and a number of wagons.
 * @author  Luke Lindsay
 */
public class TrainViewJPanel extends JPanel implements View, ListCellRenderer {

	private ReadOnlyWorld w;

	private ViewLists vl;

	private int trainNumber;

	private TrainConsistListModel trainConsistListModel;
	
	private int height = 100;
	
	private Image[] images;

	/** Creates a new instance of TrainView */
	public TrainViewJPanel(ReadOnlyWorld w, ViewLists vl, int trainNumber) {

		
		setup(w, vl, null);
		

		display(trainNumber);

		

		this.setBackground(Color.GRAY);
		//wagonView.setHeight(140);
	}

	public void display(int trainNumber) {
		this.trainNumber = trainNumber;
		TrainModel train = (TrainModel)w.get(KEY.TRAINS, trainNumber);
		
		//Set up the array of images.
		images = new Image[1+train.getNumberOfWagons()];
		images[0]= vl.getTrainImages().getSideOnEngineImage(train.getEngineType(), height);
		for(int i = 0 ; i < train.getNumberOfWagons() ; i ++ ){
			images[i+1] = vl.getTrainImages().getSideOnWagonImage(train.getWagon(i), height);
		}
		resetPreferredSize();
		
	}
	
	private void resetPreferredSize(){
		int width = 0;
		for(int i = 0 ; i < images.length ; i ++ ){
			width += images[i].getWidth(null);
		}
		this.setPreferredSize(new Dimension(width, height));
	}

	public void setup(
		ReadOnlyWorld w,
		ViewLists vl,
		java.awt.event.ActionListener submitButtonCallBack) {
		this.w = w;
		this.vl = vl;		
	}

	public Component getListCellRendererComponent(
		JList list,
		Object value,
		int index,
		boolean isSelected,
		boolean cellHasFocus) {
		display(index);
		return this;
	}

	
	public int getHeight() {
		return height;
	}

	public void setHeight(int i) {
		height = i;
	}

	
	protected void paintComponent(Graphics g) {		
		super.paintComponent(g);
		int x = 0;
		for(int i = 0 ; i < images.length ; i ++ ){
			g.drawImage(images[i], x, 0 , null);
			x += images[i].getWidth(null);
		}		
	}

}
