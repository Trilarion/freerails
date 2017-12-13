/*
 * Created on 31-May-2003
 *
 */
package jfreerails.server;

import java.util.Iterator;
import jfreerails.controller.FreerailsServerSerializable;
import jfreerails.controller.MoveReceiver;
import jfreerails.move.ChangeCargoBundleMove;
import jfreerails.move.Move;
import jfreerails.world.cargo.CargoBatch;
import jfreerails.world.cargo.CargoBundle;
import jfreerails.world.cargo.CargoType;
import jfreerails.world.station.StationModel;
import jfreerails.world.station.SupplyAtStation;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.World;


/**
 * This class loops over the list of stations and adds cargo
 * depending on what the surrounding tiles supply.
 *
 * @author Luke
 *
 */
public class CargoAtStationsGenerator implements FreerailsServerSerializable {
    private final MoveReceiver moveReceiver;

    /**
     * arbitrary multiplier from supply-rate into "wagon capacity units"
     */
    private static final int UNITS_OF_CARGO_PER_WAGON = 40;

    public CargoAtStationsGenerator(MoveReceiver moveExecuter) {
        moveReceiver = moveExecuter;
    }

    public void update(World w) {
        NonNullElements nonNullStations = new NonNullElements(KEY.STATIONS, w);

        while (nonNullStations.next()) {
            StationModel station = (StationModel)nonNullStations.getElement();
            SupplyAtStation supply = station.getSupply();
            CargoBundle cargoBundle = (CargoBundle)w.get(KEY.CARGO_BUNDLES,
                    station.getCargoBundleNumber());
            CargoBundle before = cargoBundle.getCopy();
            CargoBundle after = cargoBundle.getCopy();
            int stationNumber = nonNullStations.getIndex();

            /* Let the cargo have a half life of one year, so half the existing cargo wastes away.*/
            Iterator it = after.cargoBatchIterator();

            while (it.hasNext()) {
                CargoBatch cb = (CargoBatch)it.next();
                int amount = after.getAmount(cb);

                if (amount > 0) {
                    after.setAmount(cb, amount / 2);
                }
            }

            for (int i = 0; i < w.size(KEY.CARGO_TYPES); i++) {
                int amountSupplied = supply.getSupply(i);
		CargoType cargoType = (CargoType) w.get(KEY.CARGO_TYPES, i);

                if (amountSupplied > 0) {
                    CargoBatch cb = new CargoBatch(i, station.x, station.y, 0,
                            stationNumber);
                    int amountAlready = after.getAmount(cb);
		    after.setAmount(cb, (amountSupplied *
				UNITS_OF_CARGO_PER_WAGON) + amountAlready);
                }
            }

            Move m = new ChangeCargoBundleMove(before, after,
                    station.getCargoBundleNumber());
            moveReceiver.processMove(m);
        }
    }
}
