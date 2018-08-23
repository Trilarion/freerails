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

import freerails.model.cargo.Cargo;
import freerails.model.finance.transaction.Transaction;
import freerails.move.*;
import freerails.move.receiver.MoveReceiver;
import freerails.util.Utils;
import freerails.model.finance.Money;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.cargo.CargoBatch;
import freerails.model.finance.transaction.CargoDeliveryTransaction;

import freerails.model.station.Station;
import freerails.util.ui.SoundManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Inspects incoming moves and generates a user message if appropriate. It is also used to trigger sounds.
 */
public class UserMessageGenerator implements MoveReceiver {

    private final DecimalFormat formatter = new DecimalFormat("#,###,###");
    private final SoundManager soundManager = SoundManager.getInstance();
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
    @Override
    public void process(Move move) {
        // TODO then it could also be a splitMoveReceiver in MoveChainFork
        if (move instanceof CompositeMove) {
            List<Move> moves = ((CompositeMove) move).getMoves();

            for (Move move1 : moves) {
                process(move1);
            }
        }

        // TODO there was a WorldDiffMove before which triggered trainArrives (must be re-implemented) somehow
        // trainArrives(move);

        if (move instanceof ChangeGameSpeedMove) {
            logSpeed();
        }
    }

    /**
     * Generates a message giving details of any cargo delivered and plays a
     * cash register sound to indicate that revenue is coming in.
     * @param moves
     */
    private void trainArrives(Move mov) {
        CompositeMove moves = (CompositeMove) mov;
        // TODO does this work anymore, was a WorldDiffMove before
        List<CargoDeliveryTransaction> cargoDelivered = new ArrayList<>();
        for (int i = 0; i < moves.size(); i++) {
            Move move = moves.getMoves().get(i);
            if (move instanceof AddTransactionMove) {
                AddTransactionMove atm = (AddTransactionMove) move;
                if (!atm.getPlayer().equals(modelRoot.getPlayer())) {
                    // We don't want to know about other players' income!
                    return;
                }

                Transaction transaction = atm.getTransaction();
                if (transaction instanceof CargoDeliveryTransaction) {
                    CargoDeliveryTransaction receipt = (CargoDeliveryTransaction) transaction;
                    cargoDelivered.add(receipt);
                }
            }
        }
        if (!cargoDelivered.isEmpty()) {
            UnmodifiableWorld world = modelRoot.getWorld();

            StringBuilder message = new StringBuilder();
            CargoDeliveryTransaction first = cargoDelivered.get(0);
            int stationId = first.getStationId();
            int trainId = first.getTrainId();
            message.append("Train #");
            message.append(trainId + 1); // So that the first train
            // is #1, not #0.
            message.append(" arrives at ");
            Station station = world.getStation(modelRoot.getPlayer(), stationId);
            message.append(station.getStationName());
            message.append('\n');
            Money revenue = Money.ZERO;
            Map<Cargo, Integer> cargoQuantities = new HashMap<>();
            for (CargoDeliveryTransaction receipt : cargoDelivered) {
                CargoBatch batch = receipt.getCargoBatch();
                revenue = Money.add(revenue, receipt.getAmount());
                cargoQuantities.put(world.getCargo(batch.getCargoTypeId()), receipt.getQuantity());
            }
            for (Map.Entry<Cargo, Integer> entry: cargoQuantities.entrySet()) {
                if (entry.getValue() > 0) {
                    message.append(entry.getValue());
                    message.append(' ');
                    message.append(entry.getKey().getName());
                    message.append('\n');
                }
            }
            message.append("Revenue $");
            message.append(formatter.format(revenue.amount));
            modelRoot.setProperty(ModelRootProperty.QUICK_MESSAGE, message.toString());
            // Play the sound of cash coming in. The greater the
            // revenue, the more loops of the sample we play.
            int loops = (int) revenue.amount / 4000;
            soundManager.playSound(ClientConstants.SOUND_CASH, loops);
        }
    }

    /**
     *
     */
    public void logSpeed() {
        UnmodifiableWorld world = modelRoot.getWorld();
        int gameSpeed = world.getSpeed().getTicksPerSecond();

        if (gameSpeed <= 0) {
            modelRoot.setProperty(ModelRootProperty.PERMANENT_MESSAGE, "Game is paused.");

            /*
             * Also hide any other message. It looks silly if it says "Game is
             * paused." and "Game speed: fast" on screen at the same time!
             */
            modelRoot.setProperty(ModelRootProperty.QUICK_MESSAGE, "");
        } else {
            modelRoot.setProperty(ModelRootProperty.PERMANENT_MESSAGE, null);

            String gameSpeedDesc = actionRoot.getServerControls().getGameSpeedDesc(gameSpeed);
            modelRoot.setProperty(ModelRootProperty.QUICK_MESSAGE, "Game speed: " + gameSpeedDesc);
        }
    }
}