/*
 * Created on 23-Nov-2004
 *
 */
package experimental;

import java.net.URL;

import javax.swing.JFrame;

import jfreerails.client.common.ModelRootImpl;
import jfreerails.client.view.BuildTrackJPanel;
import jfreerails.server.OldWorldImpl;
import jfreerails.server.parser.Track_TilesHandlerImpl;
import jfreerails.world.player.Player;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;

/**
 * @author Luke
 *
 */
public class BuildTabTester {
    
    public static void main(String[] args) {
        
     
        URL track_xml_url = OldWorldImpl.class.getResource(
                "/jfreerails/data/track_tiles.xml");
        
        Track_TilesHandlerImpl trackSetFactory = new Track_TilesHandlerImpl(track_xml_url);
       
        
        World w = new WorldImpl();
        trackSetFactory.addTrackRules(w);
        ModelRootImpl mr = new ModelRootImpl();
        Player p = new Player("Test");
        w.addPlayer(p);
        mr.setup(w, p.getPrincipal());
        
        JFrame frame = new JFrame();
        BuildTrackJPanel bt = new BuildTrackJPanel();
        bt.setup(mr, null, null, null);
        
        frame.add(bt);
        frame.setSize(400, 600);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
    }
}
