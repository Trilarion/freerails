/*
 * RunTypesParser.java
 *
 * Created on 27 April 2003, 18:40
 */

package jfreerails.server.parser;

import jfreerails.world.top.WorldImpl;

/**
 *
 * @author  Luke
 */
public class RunTypesParser {

	public static void main(String[] args) {
		try {
			java.net.URL url = RunTypesParser.class.getResource("/jfreerails/data/cargo_and_terrain.xml");
			
           CargoAndTerrainParser.parse(url, new CargoAndTerrainHandlerImpl(new WorldImpl()));
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	
	
}
