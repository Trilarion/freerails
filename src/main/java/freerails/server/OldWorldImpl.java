package freerails.server;

import java.net.URL;

import freerails.server.common.TileSetFactory;
import freerails.server.parser.Track_TilesHandlerImpl;
import freerails.util.FreerailsProgressMonitor;
import freerails.world.common.GameCalendar;
import freerails.world.common.GameSpeed;
import freerails.world.common.GameTime;
import freerails.world.top.GameRules;
import freerails.world.top.ITEM;
import freerails.world.top.WagonAndEngineTypesFactory;
import freerails.world.top.World;
import freerails.world.top.WorldImpl;

import org.xml.sax.SAXException;

/**
 * This class sets up a World object.
 * 
 * @author luke
 */
public class OldWorldImpl {
    /**
     * Note, the map name is converted to lower case and any spaces are replaced
     * with underscores.
     * 
     */
    public static World createWorldFromMapFile(String mapName,
            FreerailsProgressMonitor pm) {

        mapName = mapName.toLowerCase();
        mapName = mapName.replace(' ', '_');

        pm.setValue(0);
        pm.nextStep(7);

        int progess = 0;

        TileSetFactory tileFactory = new TileSetFactoryImpl();
        pm.setValue(++progess);

        WorldImpl w = new WorldImpl();
        pm.setValue(++progess);

        WagonAndEngineTypesFactory wetf = new WagonAndEngineTypesFactory();
        pm.setValue(++progess);
        wetf.addTypesToWorld(w);
        pm.setValue(++progess);

        tileFactory.addTerrainTileTypesList(w);
        pm.setValue(++progess);

        URL track_xml_url = OldWorldImpl.class
                .getResource("/freerails/data/track_tiles.xml");

        Track_TilesHandlerImpl trackSetFactory = new Track_TilesHandlerImpl(
                track_xml_url);
        pm.setValue(++progess);

        trackSetFactory.addTrackRules(w);
        pm.setValue(++progess);

        // Load the terrain map
        URL map_url = OldWorldImpl.class.getResource("/freerails/data/"
                + mapName + ".png");
        MapFactory.setupMap(map_url, w, pm);

        // Load the city names
        URL cities_xml_url = OldWorldImpl.class.getResource("/freerails/data/"
                + mapName + "_cities.xml");

        try {
            InputCityNames.readCityNames(w, cities_xml_url);
        } catch (SAXException e) {
        }

        // Randomly position the city tiles
        CityTilePositioner ctp = new CityTilePositioner(w);
        ctp.initCities();

        // Set the time..
        w.set(ITEM.CALENDAR, new GameCalendar(1200, 1840));
        w.setTime(new GameTime(0));
        w.set(ITEM.GAME_SPEED, new GameSpeed(10));
        w.set(ITEM.GAME_RULES, GameRules.DEFAULT_RULES);

        /*
         * Note, money used to get added to player accounts here, now it is done
         * when players are added. See AddPlayerMove
         */
        return w;
    }
}