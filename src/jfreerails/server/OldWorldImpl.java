package jfreerails.server;

import java.net.URL;

import jfreerails.server.parser.*;
import jfreerails.world.accounts.BankAccount;
import jfreerails.world.accounts.Receipt;
import jfreerails.world.city.CityTilePositioner;
import jfreerails.world.city.InputCityNames;
import jfreerails.world.common.GameCalendar;
import jfreerails.world.common.GameTime;
import jfreerails.world.common.Money;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;

import org.xml.sax.SAXException;

public class OldWorldImpl {

	private World w;

	public OldWorldImpl(
		TileSetFactory tileFactory,
		TrackSetFactory trackSetFactory,
		World world) {
		if (null == tileFactory || null == trackSetFactory || null == world) {
			throw new java.lang.NullPointerException(
				"Null pointer passed to WorldImpl constructor");
		}
		this.w = world;
		tileFactory.addTerrainTileTypesList(w);
		trackSetFactory.addTrackRules(w);
	}

	public static World createWorldFromMapFile(String mapName) {

		//Load the xml file specifying terrain types.
		URL tiles_xml_url =
			OldWorldImpl.class.getResource(
				"/jfreerails/data/terrain_tiles.xml");

		TileSetFactory tileFactory = new NewTileSetFactoryImpl();
		//	new jfreerails.TileSetFactoryImpl(tiles_xml_url);
			
		WorldImpl w = new WorldImpl();	
		
		WagonAndEngineTypesFactory wetf = new WagonAndEngineTypesFactory();
		wetf.addTypesToWorld(w);

		tileFactory.addTerrainTileTypesList(w);		
				
		URL track_xml_url =
			OldWorldImpl.class.getResource("/jfreerails/data/track_tiles.xml");

		Track_TilesHandlerImpl trackSetFactory =
			new Track_TilesHandlerImpl(track_xml_url);
			
		trackSetFactory.addTrackRules(w);
						
		//Load the terrain map
		URL map_url = OldWorldImpl.class.getResource("/jfreerails/data/" + mapName + ".png");		
		MapFactory.setupMap(map_url, w);
		
		//Load the city names
	  	URL cities_xml_url = 
		  	//OldWorldImpl.class.getResource("/jfreerails/data/south_america_cities.xml");
		  	OldWorldImpl.class.getResource("/jfreerails/data/" + mapName + "_cities.xml");
	  	try {
		  	InputCityNames r = new InputCityNames(w,cities_xml_url);
	  	} catch (SAXException e) {}
		
		//Randomly position the city tiles
		CityTilePositioner ctp = new CityTilePositioner(w);
		
		//Set the time..
		w.set(ITEM.CALENDAR, new GameCalendar(1200, 1840));
		w.set(ITEM.TIME, new GameTime(0));
		
		//Set up bank account with initial balance of 1000,000.
		BankAccount bankAccount = new BankAccount();
		Receipt initialCredit = new Receipt(new Money(1000000));
		bankAccount.addTransaction(initialCredit);
		w.add(KEY.BANK_ACCOUNTS, bankAccount);		
		
		return w;
	}
}
