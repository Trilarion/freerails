/*
 * Created on Sep 8, 2004
 *
 */
package jfreerails.client.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Random;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.JPanel;

import jfreerails.client.common.ModelRoot;
import jfreerails.client.renderer.ViewLists;
import jfreerails.world.common.Money;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.TransactionAggregator;
/**
 * A JPanel that displays the details of the players
 * ordered by net worth.
 * 
 * @author Luke
 *
 */
public class LeaderBoardJPanel extends JPanel implements View {

	private JList playersList = null;
	
	private ActionListener m_submitButtonCallBack = null;
	
	private Vector values;
	/**
	 * This method initializes 
	 * 
	 */
	public LeaderBoardJPanel() {
		super();
		
		values = new Vector();
		Random rand = new Random();
		for(int i = 0 ; i < 5; i ++){
		    PlayerDetails p = new PlayerDetails();
		    p.networth = new Money(rand.nextInt(100)); 
		    values.add(p);
		}
		initialize();
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {      
        this.add(getPlayersList(), null);      
        java.awt.event.MouseAdapter mouseAdapter = new java.awt.event.MouseAdapter() { 
        	public void mouseClicked(java.awt.event.MouseEvent e) {
        	    if(null == m_submitButtonCallBack){
        	        System.err.println("mouseClicked");        		
        	    }else{        	       
        	         m_submitButtonCallBack.actionPerformed(new ActionEvent(this, 0, null));                           	             	                    	        
        	    }
        	}
        };
        this.addMouseListener(mouseAdapter);
        this.playersList.addMouseListener(mouseAdapter);
        this.setSize(getPreferredSize());
			
	}
	/**
	 * This method initializes jList	
	 * 	
	 * @return javax.swing.JList	
	 */    
	private JList getPlayersList() {
		if (playersList == null) {
			playersList = new JList();
			playersList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
			playersList.setRequestFocusEnabled(false);
			playersList.setEnabled(true);
			
			Collections.sort(values);			
			playersList.setListData(values);
		}
		return playersList;
	}
    public void setup(ModelRoot modelRoot, ViewLists vl, ActionListener submitButtonCallBack) {
        ReadOnlyWorld w = modelRoot.getWorld();
        values.clear();
        m_submitButtonCallBack = submitButtonCallBack;
        for(int player = 0; player< w.getNumberOfPlayers(); player++){
            PlayerDetails details = new PlayerDetails();
            FreerailsPrincipal principle = w.getPlayer(player).getPrincipal();
            details.name = principle.getName();
            NonNullElements stations = new NonNullElements(KEY.STATIONS, w, principle);
            details.stations = stations.size();
            TransactionAggregator networth = new NetWorthGraphJPanel.NetWorthCalculator(w, principle);
            details.networth = networth.calculateValue(); 
            values.add(details);
        }         
        Collections.sort(values);			
		playersList.setListData(values);
		setSize(getPreferredSize());
    }
    
    static class PlayerDetails implements Comparable{               
        
        String name = "player";
        Money networth = new Money(0);
        int stations = 0;       
        

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(name);      
            sb.append(", ");
            sb.append(networth.toString()); 
            sb.append(" net worth, ");             
            sb.append(stations);  
            sb.append("  stations.");                              
            return sb.toString();
        }
        public int compareTo(Object o) {
            if(o instanceof PlayerDetails){
                PlayerDetails test = (PlayerDetails)o;
                long l = test.networth.getAmount() - networth.getAmount();
                return (int)l;
            }else{
                return 0;
            }
        }
        
    }
  }  //  @jve:decl-index=0:visual-constraint="67,32"
