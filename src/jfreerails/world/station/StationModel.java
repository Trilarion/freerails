/*
 * Created by IntelliJ IDEA.
 * User: lindsal
 * Date: Jan 14, 2002
 * Time: 4:14:24 PM
 * To change template for new interface use 
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package jfreerails.world.station;

import jfreerails.misc.*;
import jfreerails.misc.GameTime;
import jfreerails.type.StationType;
import jfreerails.world.terrain.*;
import jfreerails.world.terrain.City;

public interface StationModel {
    GameTime getBuiltDate();

    String getStationName();

    StationType getStationType();

    City getCity();
}
