/*
 * Copyright (C) 2004 Luke Lindsay
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
 * SelectStationJPanel.java
 *
 * Created on 06 February 2004, 16:34
 */

package jfreerails.client.view;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.NoSuchElementException;
import javax.swing.JComponent;

import jfreerails.client.renderer.*;
import jfreerails.client.model.ModelRoot;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.NullTrackPiece;
import jfreerails.world.train.Schedule;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainOrdersModel;
/**
 * This JPanel lets the user select a stations from a map and add it to a train
 *  schedule.
 *
 * @author  Luke
 */
public class SelectStationJPanel extends javax.swing.JPanel {
    private ModelRoot modelRoot;
    private ReadOnlyWorld world;
    private ActionListener submitButtonCallBack;
    private int selectedOrderNumber = 1;
    private int trainID = 0;
    private double scale = 1;
    private boolean needsUpdating = true;
    private int selectedStationID = 0;
    private StationMapJPanel stationMapJPanel;
    
    public void addActionListener(ActionListener l) {
	submitButtonCallBack = l;
    }

    public void removeActionListener(ActionListener l) {
	if (submitButtonCallBack == l)
	    submitButtonCallBack = null;
    }

    private class StationMapJPanel extends JComponent {
	private ActionListener listener;
	private Schedule schedule;
	private Rectangle imageRect = new Rectangle();
	private Rectangle visibleMapTiles = new Rectangle();
	private ZoomedOutMapRenderer zomr;
	private ReadOnlyWorld world;

	public void setStationSchedule(Schedule s) {
	    schedule = s;
	}

	public StationMapJPanel(ReadOnlyWorld w) {
	    world = w;
	    addComponentListener(new java.awt.event.ComponentAdapter() {
		    public void componentResized(java.awt.event.ComponentEvent
			evt) {
			formComponentResized(evt);
		    }
		    public void componentShown(java.awt.event.ComponentEvent
			evt) {
			formComponentShown(evt);
		    }
	    });
	    addMouseListener(new java.awt.event.MouseAdapter() {
		    public void mouseClicked(java.awt.event.MouseEvent evt) {
			formMouseClicked(evt);
		    }
	    });
	    addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
		    public void mouseMoved(java.awt.event.MouseEvent evt) {
			formMouseMoved(evt);
		    }
	    });
	}

	private void formComponentShown(java.awt.event.ComponentEvent evt) {
	    setZoom();
	}
    
	private void formComponentResized(java.awt.event.ComponentEvent evt) {
	    setZoom();
	}

	private void formMouseClicked(java.awt.event.MouseEvent evt) {
	    formMouseMoved(evt);
	    if (submitButtonCallBack != null)
		submitButtonCallBack.actionPerformed(null);
	}

	private void formMouseMoved(java.awt.event.MouseEvent evt) {
	    double x = evt.getX();
	    x = x / scale + visibleMapTiles.x;
	    double y = evt.getY();
	    y = y /scale +  visibleMapTiles.y;

	    NearestStationFinder stationFinder = new
		NearestStationFinder(modelRoot);
	    int station = stationFinder.findNearestStation((int)x, (int)y);

	    if(selectedStationID != station && station !=
		    NearestStationFinder.NOT_FOUND) {
		selectedStationID = station;
		cargoWaitingAndDemandedJPanel1.display(selectedStationID);
		repaint();
	    }
	}

	/**
	 * Sets the zoom based on the size of the component and the positions of
	 * the stations.
	 */
	private void setZoom() {
	    imageRect = this.getBounds();
	    System.out.println("size of rect is " + imageRect);
	    int maxMapX = world.getMapWidth() - 1;
	    int maxMapY = world.getMapHeight() - 1;

	    int topLeftX = maxMapX;
	    int topLeftY = maxMapY;
	    int bottomRightX = 0;
	    int bottomRightY = 0;

	    NonNullElements it = new NonNullElements(KEY.STATIONS, world,
		    modelRoot.getPlayerPrincipal());
	    while(it.next()){
		StationModel station = (StationModel)it.getElement();
		if(station.x < topLeftX) topLeftX = station.x;
		if(station.y < topLeftY) topLeftY = station.y;
		if(station.x > bottomRightX) bottomRightX = station.x;
		if(station.y > bottomRightY) bottomRightY = station.y;
	    }
	    //Add some padding.
	    topLeftX -= 10;
	    topLeftY -= 10;
	    bottomRightX += 10;
	    bottomRightY += 10;
	    topLeftX = (topLeftX < 0) ? 0 : topLeftX;
	    topLeftY = (topLeftY < 0) ? 0 : topLeftY;
	    bottomRightX = (bottomRightX > maxMapX) ? maxMapX : bottomRightX;
	    bottomRightY = (bottomRightY > maxMapY) ? maxMapY : bottomRightY;
	    int width = bottomRightX - topLeftX;
	    int height = bottomRightY - topLeftY;

	    visibleMapTiles = new Rectangle(topLeftX, topLeftY, width, height);
	    boolean heightConstraintBinds = (visibleMapTiles.getHeight() /
		    visibleMapTiles.getWidth() ) > (imageRect.getHeight() /
		    imageRect.getWidth() );
	    if(heightConstraintBinds){
		scale = ((double) imageRect.getHeight()) /
		    height;
		int centre = (int) (topLeftX + bottomRightX) / 2;
		int widthInMapCoords = Math.min((int) ((imageRect.width) /
		    scale), maxMapX);
		centre = Math.max(centre, widthInMapCoords / 2);
		centre = Math.min(centre, maxMapX - widthInMapCoords / 2);
		/* adjust x coords */
		topLeftX = centre - widthInMapCoords / 2;
		bottomRightX = centre + widthInMapCoords / 2;
	    } else {
		scale = ((double) imageRect.getWidth()) /
		    width;
		int centre = (int) (topLeftY + bottomRightY) / 2;
		int heightInMapCoords = Math.min((int) (imageRect.height /
			    scale), maxMapY);
		centre = Math.max(centre, heightInMapCoords / 2);
		centre = Math.min(centre, maxMapY - heightInMapCoords / 2);
		/* adjust x coords */
		topLeftY = centre - heightInMapCoords / 2;
		bottomRightY = centre + heightInMapCoords / 2;
	    }
	    System.out.println("scaled is " + scale);
	    visibleMapTiles = new Rectangle(topLeftX, topLeftY, bottomRightX -
		    topLeftX, bottomRightY - topLeftY);
	    System.out.println("new mapRect is " + visibleMapTiles);
	    zomr = new ZoomedOutMapRenderer(world, imageRect.width,
		    imageRect.height, topLeftX, topLeftY, bottomRightX -
		    topLeftX, bottomRightY - topLeftY);
	    
	    needsUpdating = false;
	    repaint();
	}

	protected void paintComponent(Graphics g){
	    if(needsUpdating) {
		this.setZoom();
	    }

	    super.paintComponent(g);
	    /*
	     * TODO show other players stations if they allow use by this
	     * player
	    */
	    Graphics2D g2 = (Graphics2D)g;
	    NonNullElements it = new NonNullElements(KEY.STATIONS, world,
		    modelRoot.getPlayerPrincipal());

	    //Draw background
	    zomr.paintRect(g2, null);

	    //Draw stations
	    while(it.next()){

		/*
		 * (1)	The selected station is drawn green.
		 * (2)	Non-selected stations which are on the schedule are
		 * drawn blue.
		 * (3)	Other stations are drawn white.
		 * (4)	If, for instance,  station X is the first stop on the
		 * schedule, "1" is drawn above the station.
		 * (5)	If, for instance,  station X is the first and third
		 * stop on the schedule, "1, 3" is drawn above the station.
		 * (6)	The stop numbers drawn above the stations are drawn
		 * using the same colour as used to draw the station.
		 */
		StationModel station = (StationModel)it.getElement();
		double x = station.x - visibleMapTiles.x;
		x = x * scale;
		double y = station.y - visibleMapTiles.y;
		y = y * scale;
		int xInt = (int)x;
		int yInt = (int)y;

		String stopNumbersString ="";
		boolean stationIsOnSchedule = false;
		for(int orderNumber = 0; orderNumber <
			schedule.getNumOrders(); orderNumber++) {
		    int stationID = (orderNumber == selectedOrderNumber)
			? selectedStationID :
			schedule.getOrder(orderNumber).getStationNumber().index;
		    if(it.getIndex() == stationID){
			if(stationIsOnSchedule){
			    stopNumbersString = stopNumbersString + ", " + 
				String.valueOf(orderNumber+1);
			}else{
			    stopNumbersString = String.valueOf(orderNumber+1);
			}
			stationIsOnSchedule = true;
		    }
		}
		if(stationIsOnSchedule){
		    if(it.getIndex() == selectedStationID){
			g2.setColor(Color.GREEN);
		    }else{
			g2.setColor(Color.BLUE);
		    }
		    g2.drawString(stopNumbersString, xInt, yInt-4);
		}else{
		    g2.setColor(Color.WHITE);
		}
		g2.fillRect(xInt, yInt, 10, 10);
	    }        
	}
    }

    /** Creates new form SelectStationJPanel */
    public SelectStationJPanel() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        cargoWaitingAndDemandedJPanel1 = new jfreerails.client.view.CargoWaitingAndDemandedJPanel();
        jLabel1 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(500, 350));
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });

        cargoWaitingAndDemandedJPanel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0)));
        cargoWaitingAndDemandedJPanel1.setPreferredSize(new java.awt.Dimension(150, 300));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weighty = 1.0;
        add(cargoWaitingAndDemandedJPanel1, gridBagConstraints);

        jLabel1.setText("Train #1 Stop 1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        add(jLabel1, gridBagConstraints);

    }//GEN-END:initComponents
            
    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        try{
            int lastSelectedStationId = this.selectedStationID;
	    OneTileMoveVector v =
		OneTileMoveVector.getInstanceMappedToKey(evt.getKeyCode());
            //now find nearest station in direction of the vector.
	    NearestStationFinder stationFinder = new
		NearestStationFinder(modelRoot);
	    int station =
		stationFinder.findNearestStationInDirection
		(this.selectedStationID, v);
            
	    if(selectedStationID != station && station !=
		    NearestStationFinder.NOT_FOUND){
                selectedStationID = station;
                cargoWaitingAndDemandedJPanel1.display(selectedStationID);
                this.validate();
                this.repaint();
            }
        }catch (NoSuchElementException e){
            if(evt.getKeyCode() == KeyEvent.VK_ENTER){
                submitButtonCallBack.actionPerformed(null);
            }
            //The key pressed isn't mapped to a OneTileMoveVector so do nothing.
        }
    }//GEN-LAST:event_formKeyPressed
            
    public void display(int newTrainID, int orderNumber){
        this.trainID = newTrainID;
        this.selectedOrderNumber = orderNumber;
        
        //Set the selected station to the current station for the specified order.
	TrainModel train = (TrainModel)world.get(KEY.TRAINS, this.trainID,
		modelRoot.getPlayerPrincipal());
        Schedule schedule = (Schedule)world.get(KEY.TRAIN_SCHEDULES, train.getScheduleID());
	stationMapJPanel.setStationSchedule(schedule);

        TrainOrdersModel order = schedule.getOrder(selectedOrderNumber);
        this.selectedStationID = order.getStationNumber().index;
        
        //Set the text on the title JLabel.
        this.jLabel1.setText("Train #"+String.valueOf(trainID+1)+" Stop "+String.valueOf(selectedOrderNumber+1));
        
        //Set the station info panel to show the current selected station.
        cargoWaitingAndDemandedJPanel1.display(selectedStationID);
	revalidate();
    }
    
    public void setup(ModelRoot mr) {
	modelRoot = mr;
        this.world = mr.getWorld();
        cargoWaitingAndDemandedJPanel1.setup(modelRoot, null);
	stationMapJPanel = new StationMapJPanel(world);
        java.awt.GridBagConstraints gridBagConstraints = new
	    java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.weightx = 1.0;
	gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(7, 7, 7, 7);
        add(stationMapJPanel, gridBagConstraints);
    }
    
    public int getSelectedStationID(){
        return this.selectedStationID;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private jfreerails.client.view.CargoWaitingAndDemandedJPanel cargoWaitingAndDemandedJPanel1;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
    
}
