package jfreerails.client.view;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import jfreerails.client.common.ModelRoot;
import jfreerails.client.renderer.ViewLists;
import jfreerails.world.top.ReadOnlyWorld;


/**  The tabbed panel that sits in the lower right hand corner of the screen, note does not only display trains.
 * @author rob
 */
public class RHSJTabPane extends JTabbedPane
    implements PropertyChangeListener {
    private final TerrainInfoJPanel terrainInfoPanel;
    private final StationInfoJPanel stationInfoPanel;
    private final TrainListJPanel trainListPanel;
    private ReadOnlyWorld world;
    private final BuildJPane buildJPane;    
    private int stationInfoIndex;
    private int trainListIndex;


    public RHSJTabPane() {
    	ImageIcon trainListIcon;
    	ImageIcon buildTrackIcon;
    	ImageIcon stationInfoIcon;
    	    	
        /* set up trainsJTabbedPane */
        setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        terrainInfoPanel = new TerrainInfoJPanel();
        trainListPanel = new TrainListJPanel();
        trainListPanel.removeButtons();

        URL terrainInfoIconUrl = getClass().getResource("/jfreerails/client/graphics/icons/terrain_info.png");
        ImageIcon terrainInfoIcon = new ImageIcon(terrainInfoIconUrl);

        URL stationInfoIconUrl = getClass().getResource("/jfreerails/client/graphics/icons/station_info.png");
        stationInfoIcon = new ImageIcon(stationInfoIconUrl);
		URL buildTrackIconUrl = getClass().getResource("/jfreerails/client/graphics/icons/track_new.png");
        buildTrackIcon = new ImageIcon(buildTrackIconUrl);
		URL trainListIconUrl = getClass().getResource("/jfreerails/client/graphics/icons/train_list.png");
        trainListIcon = new ImageIcon(trainListIconUrl);
		//Note titles set to null so only the icon appears at the top of the top.
        JScrollPane terrainInfoJScrollPane = new JScrollPane(terrainInfoPanel);
        terrainInfoJScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        addTab(null, terrainInfoIcon, terrainInfoJScrollPane, "Terrain Info");
        stationInfoPanel = new StationInfoJPanel();
        stationInfoPanel.removeCloseButton();
//		Don't show the station info tab until it has been rewritten to take up less space.        
//        JScrollPane stationInfoJScrollPane = new JScrollPane(stationInfoPanel);
//        stationInfoJScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//        addTab(null, stationInfoIcon, stationInfoJScrollPane, "Station Info");
//        this.stationInfoIndex= this.getTabCount()-1;

        buildJPane = new BuildJPane();
        trainListPanel.setTrainViewHeight(20);
        addTab(null, buildTrackIcon, buildJPane, "Build Track");
        addTab(null, trainListIcon, trainListPanel, "Train List");
        this.trainListIndex= this.getTabCount()-1;
        
        /* These values were picked by trial and error! */
        this.setMinimumSize( new Dimension(250, 200));

    }

    public void setup(final ActionRoot actionRoot, ViewLists vl,
        final ModelRoot modelRoot) {
        world = modelRoot.getWorld();
        terrainInfoPanel.setup(world, vl);
        stationInfoPanel.setup(modelRoot, vl, null);
        buildJPane.setup(actionRoot, vl, modelRoot);

        ActionListener showTrain = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int id = trainListPanel.getSelectedTrainID();
                    actionRoot.getDialogueBoxController().showTrainOrders(id);
                }
            };

        trainListPanel.setShowTrainDetailsActionListener(showTrain);
        trainListPanel.setup(modelRoot, vl, null);
        modelRoot.addPropertyChangeListener(this);
    }

    /** Updates the Terrain Info Panel if the specfied PropertyChangeEvent
     * was triggered by the cursor moving.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(ModelRoot.CURSOR_POSITION)) {
            Point p = (Point)evt.getNewValue();
            terrainInfoPanel.setTerrainType(world.getTile(p.x, p.y)
                                                 .getTerrainTypeNumber());
        }
    }
    
    public void setTrainTabEnabled(boolean enabled){    	
    	this.setEnabledAt(this.trainListIndex, enabled);
    }
    
    public void setStationTabEnabled(boolean enabled){
    	//this.setEnabledAt(this.stationInfoIndex, enabled);    	
    }
}