/*
 * Created on 23-Nov-2004
 *
 */
package experimental;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.swing.JFrame;

import jfreerails.client.common.ImageManager;
import jfreerails.client.common.ImageManagerImpl;
import jfreerails.client.common.ModelRootImpl;
import jfreerails.client.view.BuildTrackJPanel;
import jfreerails.server.OldWorldImpl;
import jfreerails.server.parser.Track_TilesHandlerImpl;
import jfreerails.world.player.Player;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;
import jfreerails.world.track.TrackRule;

/**
 * @author Luke
 *
 */
public class BuildTabTester {
    
    public static void main(String[] args) {
        
        ImageManager imageManager = new ImageManagerImpl("/jfreerails/client/graphics/");
        
        URL track_xml_url = OldWorldImpl.class.getResource(
                "/jfreerails/data/track_tiles.xml");
        
        Track_TilesHandlerImpl trackSetFactory = new Track_TilesHandlerImpl(track_xml_url);
        List<TrackRule> rules = trackSetFactory.getRuleList();
//        for(TrackRule rule: rules){
//            System.out.println(rule);
//            String typeName = rule.getTypeName();
//            getImage(imageManager, typeName);
//        }
//        getImage(imageManager, "turn_off");
//        getImage(imageManager, "build_stations");
//        getImage(imageManager, "build_track");
//        getImage(imageManager, "bulldozer");
        
        World w = new WorldImpl();
        trackSetFactory.addTrackRules(w);
        ModelRootImpl mr = new ModelRootImpl();
        Player p = new Player("Test");
        w.addPlayer(p);
        mr.setup(w, p.getPrincipal());
        
        JFrame frame = new JFrame();
        BuildTrackJPanel bt = new BuildTrackJPanel();
        bt.setup(mr, null, null);
        
        frame.add(bt);
        frame.setSize(400, 600);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
    }
    
    private static void getImage(ImageManager imageManager, String typeName) {
        try {
            
            String relativeFileName = "icons" + File.separator +
                    typeName+".png";
            relativeFileName = relativeFileName.replace(' ', '_');
            
            Image im = imageManager.getImage(relativeFileName);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
