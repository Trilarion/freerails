/*
 * Created on 23-Mar-2003
 *
 */
package jfreerails.world.top;

import jfreerails.world.cargo.CargoType;
import jfreerails.world.common.Money;
import jfreerails.world.train.EngineType;
import jfreerails.world.train.WagonType;
import jfreerails.world.player.Player;


/**
 * This class adds hard coded wagon and engine types to the World.  Later the wagon
 * and engine types will be defined in an xml file, but this will do for now.
 *
 * @author Luke
 *
 */
public class WagonAndEngineTypesFactory {
    private static final int UNITS_OF_CARGO_PER_WAGON = 40;

    public void addTypesToWorld(World w) {
        //Wagon types
	/*
	 * Create a wagon type for each cargo type
	 * XXX correspondence between cargo type and WagonType table index will
	 * not be guaranteed in future XXX
	 */
	int s = w.size(KEY.CARGO_TYPES);
	WagonType[] wagonTypes = new WagonType[s];
	for (int i = 0; i < s; i++) {
	    CargoType ct = (CargoType) w.get(KEY.CARGO_TYPES, i);
	    wagonTypes[i] = new WagonType(ct.getName(), ct.getCategory(),
		    UNITS_OF_CARGO_PER_WAGON, i);
	}

        for (int i = 0; i < wagonTypes.length; i++) {
            w.add(KEY.WAGON_TYPES, wagonTypes[i], Player.AUTHORITATIVE);
        }

        //Engine types
        EngineType[] engineTypes = new EngineType[] {
                new EngineType("Grasshopper", 1000, new Money(10000), 10,
                    new Money(100)),
                new EngineType("Norris", 1000, new Money(10000), 15,
                    new Money(100)),
            };

        for (int i = 0; i < engineTypes.length; i++) {
            w.add(KEY.ENGINE_TYPES, engineTypes[i], Player.AUTHORITATIVE);
        }
    }
}
