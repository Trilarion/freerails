package jfreerails.server;

import java.net.URL;
import jfreerails.server.common.TileSetFactory;
import jfreerails.server.parser.Track_TilesHandlerImpl;
import jfreerails.util.FreerailsProgressMonitor;
import jfreerails.world.common.GameCalendar;
import jfreerails.world.common.GameSpeed;
import jfreerails.world.common.GameTime;
import jfreerails.world.top.GameRules;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.WagonAndEngineTypesFactory;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;
import org.xml.sax.SAXException;


/** This class sets up a World object.
 * @author luke
 * */
public class OldWorldImpl {
    public static World createWorldFromMapFile(String mapName,
        FreerailsProgressMonitor pm) {
        pm.setMessage("Setting up world.");
        pm.setValue(0);
        pm.setMax(7);

        int progess = 0;

        TileSetFactory tileFactory = new NewTileSetFactoryImpl();
        pm.setValue(++progess);

        WorldImpl w = new WorldImpl();
        pm.setValue(++progess);

        WagonAndEngineTypesFactory wetf = new WagonAndEngineTypesFactory();
        pm.setValue(++progess);
        wetf.addTypesToWorld(w);
        pm.setValue(++progess);

        tileFactory.addTerrainTileTypesList(w);
        pm.setValue(++progess);

        URL track_xml_url = OldWorldImpl.class.getResource(
                "/jfreerails/data/track_tiles.xml");

        Track_TilesHandlerImpl trackSetFactory = new Track_TilesHandlerImpl(track_xml_url);
        pm.setValue(++progess);

        trackSetFactory.addTrackRules(w);
        pm.setValue(++progess);

        //Load the terrain map
        URL map_url = OldWorldImpl.class.getResource("/jfreerails/data/" +
                mapName + ".png");
        MapFactory.setupMap(map_url, w, pm);

        //Load the city names
        URL cities_xml_url = OldWorldImpl.class.getResource("/jfreerails/data/" +
                mapName + "_cities.xml");

        try {
            InputCityNames.readCityNames(w, cities_xml_url);
        } catch (SAXException e) {
        }

        //Randomly position the city tiles
        NewCityTilePositioner ctp = new NewCityTilePositioner(w);
        ctp.initCities();

        //Set the time..
        w.set(ITEM.CALENDAR, new GameCalendar(1200, 1840));
        w.set(ITEM.TIME, new GameTime(0));
        w.set(ITEM.GAME_SPEED, new GameSpeed(10));
        w.set(ITEM.GAME_RULES, GameRules.DEFAULT_RULES);

        /* Note, money used to get added to player accounts here, now
         * it is done when players are added. See AddPlayerMove
         */
        return w;
    }
}