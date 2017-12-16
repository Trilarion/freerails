/*
 * Created on Dec 13, 2003
 */
package freerails.client.top;

import freerails.client.common.SoundManager;
import freerails.client.view.ActionRoot;
import freerails.config.ClientConfig;
import freerails.controller.ModelRoot;
import freerails.controller.ModelRoot.Property;
import freerails.move.*;
import freerails.network.MoveReceiver;
import freerails.world.accounts.DeliverCargoReceipt;
import freerails.world.accounts.Transaction;
import freerails.world.cargo.CargoBatch;
import freerails.world.cargo.CargoType;
import freerails.world.common.GameSpeed;
import freerails.world.common.ImList;
import freerails.world.station.StationModel;
import freerails.world.top.ITEM;
import freerails.world.top.KEY;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.SKEY;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * This class inspects incoming moves and generates a user message if
 * appropriate. It is also used to trigger sounds.
 *
 * @author Luke
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
        if (move instanceof CompositeMove) {
            ImList<Move> moves = ((CompositeMove) move).getMoves();

            for (int i = 0; i < moves.size(); i++) {
                processMove(moves.get(i));
            }
        }

        if (move instanceof WorldDiffMove) {
            WorldDiffMove wdm = (WorldDiffMove) move;
            if (wdm.getCause().equals(WorldDiffMove.Cause.TrainArrives)) {
                trainArrives(wdm);
            }

        } else if (move instanceof ChangeGameSpeedMove) {
            logSpeed();
        }
    }

    /**
     * Generates a message giving details of any cargo delivered and plays a
     * cash register sound to indicate that revenue is coming in.
     */
    private void trainArrives(WorldDiffMove wdm) {
        ArrayList<DeliverCargoReceipt> cargoDelivered = new ArrayList<DeliverCargoReceipt>();
        CompositeMove listChanges = wdm.getListChanges();
        for (int i = 0; i < listChanges.size(); i++) {
            Move m = listChanges.getMoves().get(i);
            if (m instanceof AddTransactionMove) {
                AddTransactionMove atm = (AddTransactionMove) m;
                if (!atm.getPrincipal().equals(modelRoot.getPrincipal())) {
                    // We don't want to know about other players' income!
                    return;
                }

                Transaction t = atm.getTransaction();
                if (t instanceof DeliverCargoReceipt) {
                    DeliverCargoReceipt receipt = (DeliverCargoReceipt) t;
                    cargoDelivered.add(receipt);
                }
            }
        }
        if (cargoDelivered.size() > 0) {
            ReadOnlyWorld world = modelRoot.getWorld();

            StringBuffer message = new StringBuffer();
            DeliverCargoReceipt first = cargoDelivered.get(0);
            int stationId = first.getStationId();
            int trainId = first.getTrainId();
            message.append("Train #");
            message.append(trainId + 1); // So that the first train
            // is #1, not #0.
            message.append(" arrives at ");
            StationModel station = (StationModel) world.get(modelRoot
                    .getPrincipal(), KEY.STATIONS, stationId);
            message.append(station.getStationName());
            message.append("\n");
            long revenue = 0;
            int[] cargoQuantities = new int[modelRoot.getWorld().size(
                    SKEY.CARGO_TYPES)];
            for (DeliverCargoReceipt receipt : cargoDelivered) {
                CargoBatch batch = receipt.getCb();
                revenue += receipt.deltaCash().getAmount();
                cargoQuantities[batch.getCargoType()] = receipt.getQuantity();
            }
            for (int i = 0; i < cargoQuantities.length; i++) {
                int j = cargoQuantities[i];
                if (j > 0) {
                    CargoType cargoType = (CargoType) world.get(
                            SKEY.CARGO_TYPES, i);
                    message.append(j);
                    message.append(" ");
                    message.append(cargoType.getDisplayName());
                    message.append("\n");
                }
            }
            message.append("Revenue $");
            message.append(formatter.format(revenue));
            modelRoot.setProperty(Property.QUICK_MESSAGE, message.toString());
            // Play the sound of cash coming in. The greater the
            // revenue,
            // the more loops of the sample we play.
            int loops = (int) revenue / 4000;

            try {
                soundManager.playSound(ClientConfig.SOUND_CASH, loops);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void logSpeed() {
        ReadOnlyWorld world = modelRoot.getWorld();
        GameSpeed speed = ((GameSpeed) world.get(ITEM.GAME_SPEED));
        int gameSpeed = speed.getSpeed();

        if (gameSpeed <= 0) {
            modelRoot
                    .setProperty(Property.PERMANENT_MESSAGE, "Game is paused.");

            /*
             * Also hide any other message. It looks silly if it says "Game is
             * paused." and "Game speed: fast" on screen at the same time!
             */
            modelRoot.setProperty(Property.QUICK_MESSAGE, "");
        } else {
            modelRoot.setProperty(Property.PERMANENT_MESSAGE, null);

            String gameSpeedDesc = actionRoot.getServerControls()
                    .getGameSpeedDesc(gameSpeed);
            modelRoot.setProperty(Property.QUICK_MESSAGE, "Game speed: "
                    + gameSpeedDesc);
        }
    }
}