/*
 * DialogueBoxController.java
 *
 * Created on 29 December 2002, 02:05
 */

package jfreerails.client.view;
import javax.swing.JPanel;

import jfreerails.client.common.MyGlassPanel;
/**
 *
 * @author  lindsal8 
 */
public class DialogueBoxController extends java.lang.Object {

    
    private JPanel selectEngine;
    private MyGlassPanel glassPanel;
    private NewsPaperJPanel newspaper;
    private SelectWagonsJPanel selectWagons;
    
    /** Creates new DialogueBoxController */
    public DialogueBoxController(MyGlassPanel gp) {
        glassPanel = gp;
        selectEngine = new SelectEngineJPanel(this);
        newspaper = new NewsPaperJPanel();
        selectWagons = new SelectWagonsJPanel();
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
        System.out.println("showSelectEngine()");
        glassPanel.showContent(selectEngine);
        glassPanel.setVisible(true);
    }
    
    public void showSelectWagons(){
        System.out.println("showSelectWagons");
        glassPanel.showContent(selectWagons);
        glassPanel.setVisible(true);
    }
}
