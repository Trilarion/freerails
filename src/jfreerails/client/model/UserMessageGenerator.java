package jfreerails.client.model;

import java.util.GregorianCalendar;
import java.text.DateFormat;
import java.text.DecimalFormat;

import jfreerails.controller.MoveReceiver;
import jfreerails.move.AddTransactionMove;
import jfreerails.move.Move;
import jfreerails.move.TransferCargoAtStationMove;
import jfreerails.world.accounts.DeliverCargoReceipt;
import jfreerails.world.cargo.CargoBundle;
import jfreerails.world.cargo.CargoType;
import jfreerails.world.common.GameCalendar;
import jfreerails.world.common.GameTime;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.train.TrainModel;


/**
 * This class inspects incoming moves and generates a user message if
 * appropriate.
 * It could also be used to trigger sounds.
 *  @author Luke
 *
 * Created on Dec 13, 2003
 * 
 */
class UserMessageGenerator implements MoveReceiver {
    ModelRoot mr;
    ReadOnlyWorld world;
    DecimalFormat formatter = new DecimalFormat("#,###,###");

    UserMessageGenerator(ModelRoot mr, ReadOnlyWorld world) {
        this.mr = mr;
        this.world = world;
    }

    public void processMove(Move move) {
        //Check whether it is a train arriving at a station.
        if (move instanceof TransferCargoAtStationMove) {
	    TransferCargoAtStationMove transferCargoAtStationMove =
		(TransferCargoAtStationMove)move;

	    AddTransactionMove addTransactionMove =
		transferCargoAtStationMove.getPayment();
	    DeliverCargoReceipt deliverCargoReceipt =
		(DeliverCargoReceipt)addTransactionMove.getTransaction();
            long revenue = deliverCargoReceipt.getValue();

	    // ignore other player's trains
	    if (! move.getPrincipal().equals(mr.getPlayerPrincipal()))
		return;

            if (0 < revenue) {
                int trainCargoBundle =
		    transferCargoAtStationMove.getChangeOnTrain()
                                                                 .getIndex();
                int stationCargoBundle =
		    transferCargoAtStationMove.getChangeAtStation()
                                                                   .getIndex();
                NonNullElements trains = new NonNullElements(KEY.TRAINS, world,
		       	mr.getPlayerPrincipal());
		NonNullElements players = new NonNullElements(KEY.PLAYERS,
			world);

                int trainNumber = -1;
                int statonNumber = -1;
                String stationName = "No station";

                while (trains.next()) {
                    TrainModel train = (TrainModel)trains.getElement();

                    if (train.getCargoBundleNumber() == trainCargoBundle) {
                        trainNumber = trains.getIndex() + 1;

                        break;
                    }
                }

		while (players.next()) {
		    FreerailsPrincipal p = (FreerailsPrincipal)
			((Player) players.getElement()).getPrincipal();
		    NonNullElements stations = new NonNullElements(KEY.STATIONS,
			    world, p);
		    while (stations.next()) {
			StationModel station = (StationModel)stations
			    .getElement();

			if (station.getCargoBundleNumber() ==
				stationCargoBundle) {
			    statonNumber = stations.getRowNumber();
			    stationName = station.getStationName();
			    break;
			}
		    }
                }

                CargoBundle cb = deliverCargoReceipt.getCargoDelivered();

                GameTime gt = (GameTime)world.get(ITEM.TIME);
                GameCalendar gc = (GameCalendar)world.get(ITEM.CALENDAR);
		GregorianCalendar cal = gc.getCalendar(gt);
		DateFormat df =
		    DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
			    DateFormat.MEDIUM);

                String message = df.format(cal.getTime()) + "  Train #" +
                    trainNumber + " arrives at " + stationName + "\n";

                for (int i = 0; i < world.size(KEY.CARGO_TYPES); i++) {
                    int amount = cb.getAmount(i);

                    if (amount > 0) {
                        CargoType ct = (CargoType)world.get(KEY.CARGO_TYPES, i);
                        message += amount + " " + ct.getDisplayName() + "\n";
                    }
                }

                message += "$" + formatter.format(revenue);
                mr.getUserMessageLogger().println(message);
            }
        }
    }
}
