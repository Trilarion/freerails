/*
 * Created on Dec 13, 2003
 */
package jfreerails.client.top;

import java.text.DecimalFormat;
import jfreerails.client.common.ModelRoot;
import static jfreerails.client.common.ModelRoot.Property;
import jfreerails.client.common.SoundManager;
import jfreerails.client.view.ActionRoot;
import jfreerails.move.ChangeGameSpeedMove;
import jfreerails.move.Move;
import jfreerails.move.TransferCargoAtStationMove;
import jfreerails.network.MoveReceiver;
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
 * This class inspects incoming moves and generates a user message if
 * appropriate. It could also be used to trigger sounds.
 *
 * @author Luke
 *
 */
public class UserMessageGenerator implements MoveReceiver {
    private ModelRoot modelRoot;
    private ActionRoot actionRoot;
    private final DecimalFormat formatter = new DecimalFormat("#,###,###");
    private SoundManager soundManager = SoundManager.getSoundManager();

    public UserMessageGenerator(ModelRoot mr, ActionRoot actionRoot) {
        if (null == mr || null == actionRoot) {
            throw new NullPointerException();
        }

        this.actionRoot = actionRoot;
        this.modelRoot = mr;
    }

    public void processMove(Move move) {
        //Check whether it is a train arriving at a station.
        if (move instanceof TransferCargoAtStationMove) {
            TransferCargoAtStationMove transferCargoAtStationMove = (TransferCargoAtStationMove)move;
            long revenue = transferCargoAtStationMove.getRevenue().getAmount();
            FreerailsPrincipal playerPrincipal = modelRoot.getPrincipal();
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

                    if (train.getCargoBundleID() == trainCargoBundle) {
                        trainNumber = trains.getNaturalNumber();

                        break;
                    }
                }

                while (stations.next()) {
                    StationModel station = (StationModel)stations.getElement();

                    if (station.getCargoBundleID() == stationCargoBundle) {
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

                //Play the sound of cash coming in. The greater the
                // revenue,
                //the more loops of the sample we play.
                int loops = (int)revenue / 4000;

                try {
                    soundManager.playSound("/jfreerails/client/sounds/cash.wav",
                        loops);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                modelRoot.setProperty(Property.QUICK_MESSAGE, message);
            } else {
                //If there is no revenue and we are not waiting for a full
                // load, whistle!
                if (!transferCargoAtStationMove.isWaitingForFullLoad()) {
                    try {
                        soundManager.playSound("/jfreerails/client/sounds/whistle.wav",
                            0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (move instanceof ChangeGameSpeedMove) {
            logSpeed();
        }
    }

    public void logSpeed() {
        ReadOnlyWorld world = modelRoot.getWorld();
        int gameSpeed = ((GameSpeed)world.get(ITEM.GAME_SPEED)).getSpeed();

        if (gameSpeed <= 0) {
            modelRoot.setProperty(Property.PERMANENT_MESSAGE, "Game is paused.");

            /*
             * Also hide any other message. It looks silly if it says "Game is
             * paused." and "Game speed: fast" on screen at the same time!
             */
            modelRoot.setProperty(Property.QUICK_MESSAGE, "");
        } else {
            modelRoot.setProperty(Property.PERMANENT_MESSAGE, null);

            String gameSpeedDesc = actionRoot.getServerControls()
                                             .getGameSpeedDesc(gameSpeed);
            modelRoot.setProperty(Property.QUICK_MESSAGE,
                "Game speed: " + gameSpeedDesc);
        }
    }
}