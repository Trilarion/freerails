package jfreerails.server;

import java.net.URL;
import jfreerails.server.common.TileSetFactory;
import jfreerails.server.common.TrackSetFactory;
import jfreerails.server.parser.Track_TilesHandlerImpl;
import jfreerails.util.FreerailsProgressMonitor;
import jfreerails.world.city.CityTilePositioner;
import jfreerails.world.city.InputCityNames;
import jfreerails.world.common.*;
import jfreerails.world.player.Player;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.KEY;
import jfreerails.world.top.WagonAndEngineTypesFactory;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;
import org.xml.sax.SAXException;


/** This class sets up a World object.*/
public class OldWorldImpl {
    private World w;

    public OldWorldImpl(TileSetFactory tileFactory,
        TrackSetFactory trackSetFactory, World world) {
        if (null == tileFactory || null == trackSetFactory || null == world) {
            throw new java.lang.NullPointerException(
                "Null pointer passed to WorldImpl constructor");
        }

        this.w = world;
        tileFactory.addTerrainTileTypesList(w);
        trackSetFactory.addTrackRules(w);
    }

    /**
     * TODO This would be better implemented in a config file, or better
     * still dynamically determined by scanning the directory.
     */
    public static String[] getMapNames() {
        return new String[] {"south_america", "small_south_america"};
    }

    public static World createWorldFromMapFile(String mapName,
        FreerailsProgressMonitor pm) {
        pm.setMessage("Setting up world.");
        pm.setValue(0);
        pm.setMax(7);

        int progess = 0;

        //Load the xml file specifying terrain types.
        URL tiles_xml_url = OldWorldImpl.class.getResource(
                "/jfreerails/data/terrain_tiles.xml");

        TileSetFactory tileFactory = new NewTileSetFactoryImpl();
        pm.setValue(++progess);

        //	new jfreerails.TileSetFactoryImpl(tiles_xml_url);
        WorldImpl w = new WorldImpl();
        pm.setValue(++progess);

        tileFactory.addTerrainTileTypesList(w);
        pm.setValue(++progess);

        WagonAndEngineTypesFactory wetf = new WagonAndEngineTypesFactory();
        pm.setValue(++progess);
        wetf.addTypesToWorld(w);
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
            InputCityNames r = new InputCityNames(w, cities_xml_url);
        } catch (SAXException e) {
        }

        //Randomly position the city tiles
        CityTilePositioner ctp = new CityTilePositioner(w);

        //Set the time..
        w.set(ITEM.CALENDAR, new GameCalendar(30, 1840));
        w.set(ITEM.TIME, new GameTime(0));

	//Create the economy
	Economy economy = new Economy();
	economy.setIncomeTaxRate(25);
	economy.setBaseInterestRate((float) 5.0);
	w.set(ITEM.ECONOMY, economy, Player.AUTHORITATIVE);

        return w;
    }
}
