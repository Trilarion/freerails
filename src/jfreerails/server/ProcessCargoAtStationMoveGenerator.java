/*
 * Created on 30-Jul-2003
 *
 */
package jfreerails.server;

import java.util.Iterator;
import jfreerails.move.AddTransactionMove;
import jfreerails.world.accounts.DeliverCargoReceipt;
import jfreerails.world.cargo.CargoBatch;
import jfreerails.world.cargo.CargoBundle;
import jfreerails.world.common.Money;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;


/** This class generates Moves that pay the player for delivering the cargo.
 *
 * @author Luke Lindsay
 *
 */
public class ProcessCargoAtStationMoveGenerator {
    public static AddTransactionMove processCargo(ReadOnlyWorld w,
        CargoBundle cargoBundle, int stationID, FreerailsPrincipal p) {
        StationModel thisStation = (StationModel)w.get(KEY.STATIONS, stationID,
                p);
        Iterator batches = cargoBundle.cargoBatchIterator();
        double amount = 0;

        while (batches.hasNext()) {
            CargoBatch batch = (CargoBatch)batches.next();
            int distanceSquared = (batch.getSourceX() - thisStation.x) * (batch.getSourceX() -
                thisStation.x) * (batch.getSourceY() - thisStation.y) * (batch.getSourceY() -
                thisStation.y);
            double dist = Math.sqrt(distanceSquared);
            amount += cargoBundle.getAmount(batch) * Math.log(dist) * 100;
        }

        DeliverCargoReceipt receipt = new DeliverCargoReceipt(new Money(
                    (long)amount), cargoBundle);

        return new AddTransactionMove(p, receipt);
    }
}