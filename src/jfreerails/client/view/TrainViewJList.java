/*
 * TrainView.java
 *
 * Created on 22 August 2003, 20:49
 */

package jfreerails.client.view;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JList;
import javax.swing.ListCellRenderer;

import jfreerails.client.renderer.ViewLists;
import jfreerails.world.top.ReadOnlyWorld;
/**
 *  This JList displays an engine and a number of wagons.
 * @author  Luke Lindsay
 */
public class TrainViewJList extends JList implements View, ListCellRenderer {

	private ReadOnlyWorld w;

	private ViewLists vl;

	private int trainNumber;

	private TrainConsistListModel trainConsistListModel;

	private WagonView wagonView;

	/** Creates a new instance of TrainView */
	public TrainViewJList(ReadOnlyWorld w, ViewLists vl, int trainNumber) {

		wagonView = new WagonView();
		setup(w, vl, null);
		this.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
		this.setVisibleRowCount(1);

		display(trainNumber);

		this.setCellRenderer(wagonView);

		this.setBackground(Color.GRAY);
		//wagonView.setHeight(140);
	}

	public void display(int trainNumber) {
		this.trainNumber = trainNumber;
		trainConsistListModel = new TrainConsistListModel(w, trainNumber);
		this.setModel(trainConsistListModel);
	}

	public void setup(
		ReadOnlyWorld w,
		ViewLists vl,
		java.awt.event.ActionListener submitButtonCallBack) {
		this.w = w;
		this.vl = vl;
		wagonView.setup(w, vl, null);
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

}
