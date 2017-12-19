/*
 * Created on 28-Apr-2003
 *
 */
package freerails.server;

import freerails.server.parser.CargoAndTerrainHandlerImpl;
import freerails.server.parser.CargoAndTerrainParser;
import freerails.server.parser.RunTypesParser;
import freerails.world.top.World;

/**
 * This class adds cargo and terrain types defined in an XML file to a World
 * object.
 *
 */
public class TileSetFactoryImpl implements TileSetFactory {

    /**
     *
     * @param w
     */
    public void addTerrainTileTypesList(World w) {
        try {
            java.net.URL url = RunTypesParser.class
                    .getResource("/freerails/data/cargo_and_terrain.xml");

            CargoAndTerrainParser.parse(url, new CargoAndTerrainHandlerImpl(w));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}