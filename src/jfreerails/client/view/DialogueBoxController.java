/*
 * DialogueBoxController.java
 *
 * Created on 29 December 2002, 02:05
 */

package jfreerails.client.view;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import jfreerails.client.common.MyGlassPanel;
import jfreerails.client.renderer.ViewLists;
import jfreerails.move.ChangeProductionAtEngineShopMove;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.world.station.ProductionAtEngineShop;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;
/**
 *
 * @author  lindsal8 
 */
public class DialogueBoxController {

    
    private SelectEngineJPanel selectEngine;
    private MyGlassPanel glassPanel;
    private NewsPaperJPanel newspaper;
    private SelectWagonsJPanel selectWagons;
    
    private World w;
    
    /** Creates new DialogueBoxController */
    public DialogueBoxController(MyGlassPanel gp, World world, ViewLists vl) {
    	this.w = world;
        glassPanel = gp;
        selectEngine = new SelectEngineJPanel(this);
		selectEngine.setup(w, vl, new ActionListener(){
			
			public void actionPerformed(ActionEvent arg0) {
				showSelectWagons();				
			}			
			
		}	);
        newspaper = new NewsPaperJPanel();
        newspaper.setup(w, vl, new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                closeCurrentDialogue();
            }
        });
        
        selectWagons = new SelectWagonsJPanel();
		selectWagons.setup(w, vl, new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				StationModel station = (StationModel)w.get(KEY.STATIONS, 0);
				ProductionAtEngineShop before = station.getProduction();
				int engineType = selectEngine.getEngineType();
				int [] wagonTypes = selectWagons.getWagons();
				ProductionAtEngineShop after = new ProductionAtEngineShop(engineType, wagonTypes);
				
				Move m = new ChangeProductionAtEngineShopMove(before, after, 0);
				MoveStatus ms = m.doMove(w);
				if(!ms.ok){
					System.out.println("Couldn't change production at station: "+ms.getMessage());
				}else{
					System.out.println("Production at station changed.");
				}
				closeCurrentDialogue();
			}
			
			
		}	);
    }
    
    public void closeCurrentDialogue(){
        System.out.println("closeCurrentDialogue()");
        glassPanel.setVisible(false);
    }
    
    public void showNewspaper(String headline){
        newspaper.setHeadline(headline);
        glassPanel.showContent(newspaper);
        glassPanel.setVisible(true);
    }
    
    public void showSelectEngine(){
    	if(w.size(KEY.STATIONS)==0){
    		System.out.println("Can't build train since there are no stations");
    	}else{    	
        	System.out.println("showSelectEngine()");
        	glassPanel.showContent(selectEngine);
        	glassPanel.setVisible(true);
    	}
    }
    
    public void showSelectWagons(){
        System.out.println("showSelectWagons");
        glassPanel.showContent(selectWagons);
        glassPanel.setVisible(true);
    }
}
