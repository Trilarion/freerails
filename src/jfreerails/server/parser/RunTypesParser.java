/*
 * RunTypesParser.java
 *
 * Created on 27 April 2003, 18:40
 */
package jfreerails.server.parser;

import jfreerails.world.top.WorldImpl;


/**
 * The main method on this classes uses CargoAndTerrainParser to the parse cargo and terrain types xml file - use
 * it to test the parser and xml file work togther.
 * @author  Luke
 */
public class RunTypesParser {
    public static void main(String[] args) {
        try {
            java.net.URL url = RunTypesParser.class.getResource(
                    "/jfreerails/data/cargo_and_terrain.xml");
            CargoAndTerrainParser.parse(url,
                new CargoAndTerrainHandlerImpl(new WorldImpl()));
            System.out.println("It worked");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}