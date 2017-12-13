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
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;

/** This class generates Moves that pay the player for delivering the cargo.
 *
 * @author Luke Lindsay
 *
 */
public class ProcessCargoAtStationMoveGenerator {
    public static AddTransactionMove processCargo(ReadOnlyWorld w,
        CargoBundle cargoBundle, int stationID) {
        StationModel thisStation = (StationModel)w.get(KEY.STATIONS, stationID);
        Iterator batches = cargoBundle.cargoBatchIterator();
        int amountOfCargo = 0;
        double amount = 0;

        while (batches.hasNext()) {
            CargoBatch batch = (CargoBatch)batches.next();
	    int dx = (batch.getSourceX() - thisStation.x);
	    int dy = (batch.getSourceY() - thisStation.y);
            double dist = Math.sqrt(dx*dx + dy*dy);
            amount += cargoBundle.getAmount(batch) * Math.log(dist) * 100;
        }
	System.out.println("amount for cargo is " + amount);

        DeliverCargoReceipt receipt = new DeliverCargoReceipt(new Money(
                    (long)amount), cargoBundle);

	/* FIXME until stations or trains have owners we will credit the first
	 * players account */
	FreerailsPrincipal p = ((Player) w.get(KEY.PLAYERS, 0,
		    Player.AUTHORITATIVE)).getPrincipal();	

        return new AddTransactionMove(0, receipt, p);
    }
}
