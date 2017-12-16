/*
 * RunTypesParser.java
 *
 * Created on 27 April 2003, 18:40
 */
package freerails.server.parser;

import org.apache.log4j.Logger;

import freerails.world.top.WorldImpl;

/**
 * The main method on this class uses CargoAndTerrainParser to the parse cargo
 * and terrain types xml file - use it to test the parser and xml file work
 * together.
 * 
 * @author Luke
 */
public class RunTypesParser {
    private static final Logger logger = Logger.getLogger(RunTypesParser.class
            .getName());

    public static void main(String[] args) {
        try {
            java.net.URL url = RunTypesParser.class
                    .getResource("/freerails/data/cargo_and_terrain.xml");
            CargoAndTerrainParser.parse(url, new CargoAndTerrainHandlerImpl(
                    new WorldImpl()));
            logger.info("It worked");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}