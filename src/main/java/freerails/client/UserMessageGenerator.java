/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.client;

import freerails.client.common.SoundManager;
import freerails.client.view.ActionRoot;
import freerails.controller.ModelRoot;
import freerails.controller.ModelRoot.Property;
import freerails.move.*;
import freerails.server.MoveReceiver;
import freerails.util.Utils;
import freerails.world.ITEM;
import freerails.world.KEY;
import freerails.world.ReadOnlyWorld;
import freerails.world.SKEY;
import freerails.world.cargo.CargoBatch;
import freerails.world.cargo.CargoType;
import freerails.world.finances.CargoDeliveryMoneyTransaction;
import freerails.world.finances.Transaction;
import freerails.world.game.GameSpeed;
import freerails.world.station.Station;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Inspects incoming moves and generates a user message if
 * appropriate. It is also used to trigger sounds.
 */
public class UserMessageGenerator implements MoveReceiver {

    private final DecimalFormat formatter = new DecimalFormat("#,###,###");
    private final SoundManager soundManager = SoundManager.getSoundManager();
    private ModelRoot modelRoot;
    private ActionRoot actionRoot;

    /**
     * @param modelRoot
     * @param actionRoot
     */
    public UserMessageGenerator(ModelRoot modelRoot, ActionRoot actionRoot) {
        this.actionRoot = Utils.verifyNotNull(actionRoot);
        this.modelRoot = Utils.verifyNotNull(modelRoot);
    }

    /**
     * @param move
     */
    public void process(Move move) {
        if (move instanceof CompositeMove) {
            List<Move> moves = ((CompositeMove) move).getMoves();

            for (int i = 0; i < moves.size(); i++) {
                process(moves.get(i));
            }
        }

        if (move instanceof WorldDiffMove) {
            WorldDiffMove wdm = (WorldDiffMove) move;
            if (wdm.getCause() == WorldDiffMoveCause.TrainArrives) {
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
        List<CargoDeliveryMoneyTransaction> cargoDelivered = new ArrayList<>();
        CompositeMove listChanges = wdm.getListChanges();
        for (int i = 0; i < listChanges.size(); i++) {
            Move move = listChanges.getMoves().get(i);
            if (move instanceof AddTransactionMove) {
                AddTransactionMove atm = (AddTransactionMove) move;
                if (!atm.getPrincipal().equals(modelRoot.getPrincipal())) {
                    // We don't want to know about other players' income!
                    return;
                }

                Transaction transaction = atm.getTransaction();
                if (transaction instanceof CargoDeliveryMoneyTransaction) {
                    CargoDeliveryMoneyTransaction receipt = (CargoDeliveryMoneyTransaction) transaction;
                    cargoDelivered.add(receipt);
                }
            }
        }
        if (!cargoDelivered.isEmpty()) {
            ReadOnlyWorld world = modelRoot.getWorld();

            StringBuilder message = new StringBuilder();
            CargoDeliveryMoneyTransaction first = cargoDelivered.get(0);
            int stationId = first.getStationId();
            int trainId = first.getTrainId();
            message.append("Train #");
            message.append(trainId + 1); // So that the first train
            // is #1, not #0.
            message.append(" arrives at ");
            Station station = (Station) world.get(modelRoot.getPrincipal(), KEY.STATIONS, stationId);
            message.append(station.getStationName());
            message.append('\n');
            long revenue = 0;
            int[] cargoQuantities = new int[modelRoot.getWorld().size(SKEY.CARGO_TYPES)];
            for (CargoDeliveryMoneyTransaction receipt : cargoDelivered) {
                CargoBatch batch = receipt.getCargoBatch();
                revenue += receipt.value().getAmount();
                cargoQuantities[batch.getCargoType()] = receipt.getQuantity();
            }
            for (int i = 0; i < cargoQuantities.length; i++) {
                int j = cargoQuantities[i];
                if (j > 0) {
                    CargoType cargoType = (CargoType) world.get(SKEY.CARGO_TYPES, i);
                    message.append(j);
                    message.append(' ');
                    message.append(cargoType.getDisplayName());
                    message.append('\n');
                }
            }
            message.append("Revenue $");
            message.append(formatter.format(revenue));
            modelRoot.setProperty(Property.QUICK_MESSAGE, message.toString());
            // Play the sound of cash coming in. The greater the
            // revenue, the more loops of the sample we play.
            int loops = (int) revenue / 4000;
            soundManager.playSound(ClientConfig.SOUND_CASH, loops);
        }
    }

    /**
     *
     */
    public void logSpeed() {
        ReadOnlyWorld world = modelRoot.getWorld();
        GameSpeed speed = ((GameSpeed) world.get(ITEM.GAME_SPEED));
        int gameSpeed = speed.getSpeed();

        if (gameSpeed <= 0) {
            modelRoot.setProperty(Property.PERMANENT_MESSAGE, "Game is paused.");

            /*
             * Also hide any other message. It looks silly if it says "Game is
             * paused." and "Game speed: fast" on screen at the same time!
             */
            modelRoot.setProperty(Property.QUICK_MESSAGE, "");
        } else {
            modelRoot.setProperty(Property.PERMANENT_MESSAGE, null);

            String gameSpeedDesc = actionRoot.getServerControls().getGameSpeedDesc(gameSpeed);
            modelRoot.setProperty(Property.QUICK_MESSAGE, "Game speed: " + gameSpeedDesc);
        }
    }
}