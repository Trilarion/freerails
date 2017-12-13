/*
 * Created on 28-Apr-2003
 *
 */
package jfreerails.server;

import jfreerails.server.common.*;
import jfreerails.server.parser.CargoAndTerrainHandlerImpl;
import jfreerails.server.parser.CargoAndTerrainParser;
import jfreerails.server.parser.RunTypesParser;
import jfreerails.world.top.World;


/**
 * This class adds cargo and terrain types defined in an XML file to a World object.
 *
 * @author Luke
 *
 */
public class NewTileSetFactoryImpl implements TileSetFactory {
    public void addTerrainTileTypesList(World w) {
        try {
            java.net.URL url = RunTypesParser.class.getResource(
                    "/jfreerails/data/cargo_and_terrain.xml");

            CargoAndTerrainParser.parse(url, new CargoAndTerrainHandlerImpl(w));
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }
    }
}