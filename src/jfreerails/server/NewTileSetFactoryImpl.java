/*
 * Created on 28-Apr-2003
 * 
 */
package jfreerails.server;


import jfreerails.server.parser.CargoAndTerrainHandlerImpl;
import jfreerails.server.parser.CargoAndTerrainParser;
import jfreerails.server.parser.RunTypesParser;
import jfreerails.world.top.World;

/**
 * @author Luke
 * 
 */
public class NewTileSetFactoryImpl implements TileSetFactory {

	World world;
	

	public void addTerrainTileTypesList(World w) {
		try {

			world = w;
			java.net.URL url =
				RunTypesParser.class.getResource("/jfreerails/data/cargo_and_terrain.xml");
			
			CargoAndTerrainParser.parse(url, new CargoAndTerrainHandlerImpl(w));

		
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalStateException();
		}

	}

	
}
