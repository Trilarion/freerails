/*
 * Created on 23-Mar-2003
 *
 */
package jfreerails.world.top;

import jfreerails.world.common.Money;
import jfreerails.world.train.EngineType;
import jfreerails.world.train.WagonType;


/**
 * This class adds hard coded wagon and engine types to the World.  Later the wagon
 * and engine types will be defined in an xml file, but this will do for now.
 *
 * @author Luke
 *
 */
public class WagonAndEngineTypesFactory {
    public void addTypesToWorld(World w) {
        //Wagon types
        WagonType[] wagonTypes = new WagonType[] {
                new WagonType("Mail", WagonType.MAIL),
                new WagonType("Passenger", WagonType.PASSENGER),
                new WagonType("Fast Freight", WagonType.FAST_FREIGHT),
                new WagonType("Slow Freight", WagonType.SLOW_FREIGHT),
                new WagonType("Bulk Freight", WagonType.BULK_FREIGHT),
            };

        for (int i = 0; i < wagonTypes.length; i++) {
            w.add(KEY.WAGON_TYPES, wagonTypes[i]);
        }

        //Engine types
        EngineType[] engineTypes = new EngineType[] {
                new EngineType("Grasshopper", 1000, new Money(10000), 10,
                    new Money(100)),
                new EngineType("Norris", 1000, new Money(10000), 15,
                    new Money(100)),
            };

        for (int i = 0; i < engineTypes.length; i++) {
            w.add(KEY.ENGINE_TYPES, engineTypes[i]);
        }
    }
}