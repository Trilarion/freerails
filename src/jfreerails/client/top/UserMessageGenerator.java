/*
 * Created on Dec 13, 2003
 */
package jfreerails.client.top;

import java.text.DecimalFormat;
import jfreerails.client.view.ModelRoot;
import jfreerails.controller.MoveReceiver;
import jfreerails.move.ChangeGameSpeedMove;
import jfreerails.move.Move;
import jfreerails.move.TransferCargoAtStationMove;
import jfreerails.world.cargo.CargoType;
import jfreerails.world.common.GameCalendar;
import jfreerails.world.common.GameSpeed;
import jfreerails.world.common.GameTime;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.train.TrainModel;


/**
 * This class inspects incoming moves and generates a user message if appropriate.
 * It could also be used to trigger sounds.
 *  @author Luke
 *
 */
public class UserMessageGenerator implements MoveReceiver {
    ModelRoot modelRoot;
    DecimalFormat formatter = new DecimalFormat("#,###,###");

    public UserMessageGenerator(ModelRoot mr) {
        if (null == mr) {
            throw new NullPointerException();
        }

        this.modelRoot = mr;
    }

    public void processMove(Move move) {
        //Check whether it is a train arriving at a station.
        if (move instanceof TransferCargoAtStationMove) {
            TransferCargoAtStationMove transferCargoAtStationMove = (TransferCargoAtStationMove)move;
            long revenue = transferCargoAtStationMove.getRevenue().getAmount();
            FreerailsPrincipal playerPrincipal = modelRoot.getPlayerPrincipal();
            boolean positiveRevenue = 0 < revenue;
            boolean isRightPlayer = transferCargoAtStationMove.getPrincipal()
                                                              .equals(playerPrincipal);

            if (positiveRevenue && isRightPlayer) {
                ReadOnlyWorld world = modelRoot.getWorld();
                int trainCargoBundle = transferCargoAtStationMove.getChangeOnTrain()
                                                                 .getIndex();
                int stationCargoBundle = transferCargoAtStationMove.getChangeAtStation()
                                                                   .getIndex();
                NonNullElements trains = new NonNullElements(KEY.TRAINS, world,
                        playerPrincipal);
                NonNullElements stations = new NonNullElements(KEY.STATIONS,
                        world, playerPrincipal);

                int trainNumber = -1;
                String stationName = "No station";

                while (trains.next()) {
                    TrainModel train = (TrainModel)trains.getElement();

                    if (train.getCargoBundleNumber() == trainCargoBundle) {
                        trainNumber = trains.getIndex() + 1;

                        break;
                    }
                }

                while (stations.next()) {
                    StationModel station = (StationModel)stations.getElement();

                    if (station.getCargoBundleNumber() == stationCargoBundle) {
                        stationName = station.getStationName();

                        break;
                    }
                }

                GameTime gt = (GameTime)world.get(ITEM.TIME);
                GameCalendar gc = (GameCalendar)world.get(ITEM.CALENDAR);
                String message = gc.getTimeOfDay(gt.getTime()) + "  Train #" +
                    trainNumber + " arrives at " + stationName + "\n";

                for (int i = 0; i < world.size(SKEY.CARGO_TYPES); i++) {
                    int amount = transferCargoAtStationMove.getQuantityOfCargo(i);

                    if (amount > 0) {
                        CargoType ct = (CargoType)world.get(SKEY.CARGO_TYPES, i);
                        message += amount + " " + ct.getDisplayName() + "\n";
                    }
                }

                message += "$" + formatter.format(revenue);
                modelRoot.getUserMessageLogger().println(message);
            }
        } else if (move instanceof ChangeGameSpeedMove) {
            ReadOnlyWorld world = modelRoot.getWorld();
            int gameSpeed = ((GameSpeed)world.get(ITEM.GAME_SPEED)).getSpeed();

            if (gameSpeed <= 0) {
                modelRoot.getUserMessageLogger().showMessage("Game is paused.");
            } else {
                modelRoot.getUserMessageLogger().hideMessage();

                String gameSpeedDesc = modelRoot.getServerControls()
                                                .getGameSpeedDesc(gameSpeed);
                modelRoot.getUserMessageLogger().println("Game speed: " +
                    gameSpeedDesc);
            }
        }
    }
}