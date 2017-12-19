/*
 * Created on 30-Jul-2003
 *
 */
package freerails.controller;

import freerails.move.AddTransactionMove;
import freerails.move.Move;
import freerails.world.accounts.DeliverCargoReceipt;
import freerails.world.cargo.CargoBatch;
import freerails.world.cargo.CargoBundle;
import freerails.world.common.Money;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.StationModel;
import freerails.world.top.KEY;
import freerails.world.top.ReadOnlyWorld;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class generates Moves that pay the player for delivering the cargo.
 *
 */
public class ProcessCargoAtStationMoveGenerator {
    /**
     * Determines how much the player gets for delivering cargo. Changed from
     * 100 to 75 to fix bug 910132 (Too easy to make money!)
     */
    private final static int MAGIC_NUMBER = 75;

    /**
     *
     * @param w
     * @param bundle
     * @param stationID
     * @param p
     * @param trainId
     * @return
     */
    public static ArrayList<Move> processCargo(ReadOnlyWorld w,
                                               CargoBundle bundle, int stationID, FreerailsPrincipal p, int trainId) {
        StationModel thisStation = (StationModel) w.get(p, KEY.STATIONS,
                stationID);
        Iterator<CargoBatch> batches = bundle.cargoBatchIterator();

        ArrayList<Move> moves = new ArrayList<>();

        while (batches.hasNext()) {
            CargoBatch batch = batches.next();
            double distanceSquared = (batch.getSourceX() - thisStation.x)
                    * (batch.getSourceX() - thisStation.x)
                    + (batch.getSourceY() - thisStation.y)
                    * (batch.getSourceY() - thisStation.y);
            double dist = Math.sqrt(distanceSquared);
            int quantity = bundle.getAmount(batch);

            double amount = quantity * Math.log(dist) * MAGIC_NUMBER;
            Money money = new Money((long) amount);
            DeliverCargoReceipt receipt = new DeliverCargoReceipt(money,
                    quantity, stationID, batch, trainId);
            moves.add(new AddTransactionMove(p, receipt));
        }

        return moves;
    }
}