/*
 * Created by IntelliJ IDEA.
 * User: lindsal
 * Date: Jan 14, 2002
 * Time: 4:10:53 PM
 * To change template for new interface use 
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package jfreerails.world.cargo;

import jfreerails.type.CargoType;
import jfreerails.world.station.*;
import jfreerails.world.station.StationModel;

import java.awt.*;

public interface CargoBatch {
    Point getPointOfOrigin();

    StationModel getStationOfOrigin();

    CargoType getCargoType();

    CompositeCargoBundle getCargoBundle();

    /**
     * Does ...
     *
     * @return A boolean with ...
     */

    boolean hasTravelled();
}
