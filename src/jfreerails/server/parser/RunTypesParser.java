/*
 * RunTypesParser.java
 *
 * Created on 27 April 2003, 18:40
 */

package jfreerails.server.parser;
/**
 *
 * @author  Luke
 */
public class RunTypesParser {

	public static void main(String[] args) {
		try {
			java.net.URL url = RunTypesParser.class.getResource("/jfreerails/data/cargo_and_terrain.xml");
			System.out.println("About to parse: "+url);
           CargoAndTerrainParser.parse(url, new CargoAndTerrainHandlerImpl());
			System.out.println("Done!");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	
	
}
