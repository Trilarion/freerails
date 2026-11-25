/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

import freerails.model.economy.Money;
import freerails.model.economy.transaction.CargoDeliveryTransaction;
import freerails.model.operations.station.Station;
import freerails.model.operations.train.motion.MotionUtils;
import freerails.model.player.Player;
import freerails.model.simulation.Speed;
import freerails.model.world.UnmodifiableWorld;
import freerails.move.*;
import freerails.util.Utils;
import freerails.util.ui.SoundManager;

import java.util.*;
import java.util.function.ObjIntConsumer;

/**
 * Inspects incoming moves and generates a user message if appropriate. It is also used to trigger sounds.
 * <p>
 * It receives complete moves, after they have been applied to the client's world, so that all
 * deliveries made at one stop produce a single message.
 */
public class UserMessageGenerator implements MoveReceiver {

    /**
     * Revenue for each extra loop of the cash register sound.
     */
    private static final long REVENUE_PER_CASH_SOUND_LOOP = 4000;

    private final ModelRoot modelRoot;
    private final ObjIntConsumer<String> soundPlayer;

    /**
     * Creates a user message generator instance that plays sounds with the {@link SoundManager}.
     *
     * @param modelRoot the client model that provides current game state
     */
    public UserMessageGenerator(ModelRoot modelRoot) {
        this(modelRoot, SoundManager.getInstance()::playSound);
    }

    /**
     * Creates a user message generator instance with the supplied sound player.
     *
     * @param modelRoot the client model that provides current game state
     * @param soundPlayer plays the named sound with the given number of extra loops
     */
    UserMessageGenerator(ModelRoot modelRoot, ObjIntConsumer<String> soundPlayer) {
        this.modelRoot = Utils.verifyNotNull(modelRoot);
        this.soundPlayer = Utils.verifyNotNull(soundPlayer);
    }

    /**
     * Reports speed changes, cargo deliveries, and trains leaving stations contained in the move.
     *
     * @param move the move that was applied
     */
    @Override
    public void process(Move move) {
        List<Move> leaves = ContainerMove.leaves(move);

        for (Move leaf : leaves) {
            if (leaf instanceof ChangeGameSpeedMove) {
                logSpeed();
            }
        }
        reportDeliveries(leaves);
        playDepartureWhistles(leaves);
    }

    /**
     * Shows a message with the cargo the player's trains delivered and the revenue, and plays a
     * cash register sound to indicate that revenue is coming in. Deliveries made by one train at
     * one station are combined into one entry.
     *
     * @param leaves the non-container moves of the applied move
     */
    private void reportDeliveries(List<Move> leaves) {
        Player player = modelRoot.getPlayer();
        Map<DeliveryStop, List<CargoDeliveryTransaction>> deliveries = new LinkedHashMap<>();
        for (Move leaf : leaves) {
            // Moves are broadcast to all clients; other players' income is not reported.
            if (leaf instanceof AddTransactionMove transactionMove && transactionMove.getPlayer().equals(player)
                    && transactionMove.getTransaction() instanceof CargoDeliveryTransaction delivery) {
                DeliveryStop stop = new DeliveryStop(delivery.getTrainId(), delivery.getStationId());
                deliveries.computeIfAbsent(stop, key -> new ArrayList<>()).add(delivery);
            }
        }
        if (deliveries.isEmpty()) {
            return;
        }

        UnmodifiableWorld world = modelRoot.getWorld();
        List<String> messages = new ArrayList<>();
        Money revenue = Money.ZERO;
        for (Map.Entry<DeliveryStop, List<CargoDeliveryTransaction>> entry : deliveries.entrySet()) {
            messages.add(deliveryMessage(world, player, entry.getKey(), entry.getValue()));
            for (CargoDeliveryTransaction delivery : entry.getValue()) {
                revenue = Money.add(revenue, delivery.getAmount());
            }
        }

        // Keep the pause message alone on screen, as logSpeed does.
        if (world.getSpeed() != Speed.PAUSE) {
            modelRoot.getProperties().setProperty(ModelRootProperty.QUICK_MESSAGE, String.join("\n", messages));
        }
        // The greater the revenue, the more loops of the sample are played.
        int loops = (int) Math.min(Integer.MAX_VALUE, Math.max(0, revenue.amount() / REVENUE_PER_CASH_SOUND_LOOP));
        soundPlayer.accept(ClientConstants.SOUND_CASH, loops);
    }

    /**
     * Builds the message for the deliveries of one train at one station.
     *
     * @param world the world after the deliveries
     * @param player the player who made the deliveries
     * @param stop the train and station of the deliveries
     * @param deliveries the delivery transactions
     * @return the message, one line for the stop, one per cargo type, and one for the revenue
     */
    private static String deliveryMessage(UnmodifiableWorld world, Player player, DeliveryStop stop,
                                          List<CargoDeliveryTransaction> deliveries) {
        Money revenue = Money.ZERO;
        Map<Integer, Integer> quantities = new TreeMap<>();
        for (CargoDeliveryTransaction delivery : deliveries) {
            revenue = Money.add(revenue, delivery.getAmount());
            quantities.merge(delivery.getCargoBatch().cargoTypeId(), delivery.getQuantity(), Integer::sum);
        }

        Station station = world.getStation(player, stop.stationId());
        String stationName = station != null ? station.getStationName() : "station " + stop.stationId();
        StringBuilder message = new StringBuilder();
        message.append("Train ").append(stop.trainId()).append(" delivers at ").append(stationName).append('\n');
        for (Map.Entry<Integer, Integer> quantity : quantities.entrySet()) {
            if (quantity.getValue() > 0) {
                message.append(quantity.getValue()).append(' ')
                        .append(world.getCargo(quantity.getKey()).getName()).append('\n');
            }
        }
        message.append("Revenue ").append(MoneyFormatter.format(revenue));
        return message.toString();
    }

    /**
     * Plays a whistle for each of the player's trains that starts moving away from a station.
     *
     * @param leaves the non-container moves of the applied move
     */
    private void playDepartureWhistles(List<Move> leaves) {
        Player player = modelRoot.getPlayer();
        Set<Integer> movingTrains = new LinkedHashSet<>();
        for (Move leaf : leaves) {
            if (leaf instanceof AddTrainMotionMove motionMove && motionMove.getPlayer().equals(player)
                    && MotionUtils.isMovement(motionMove.getActivity())) {
                movingTrains.add(motionMove.getTrainId());
            }
        }

        UnmodifiableWorld world = modelRoot.getWorld();
        for (int trainId : movingTrains) {
            if (MotionUtils.latestMovementLeavesStation(world, player, trainId)) {
                soundPlayer.accept(ClientConstants.SOUND_WHISTLE, 0);
            }
        }
    }

    /**
     * Performs the log speed operation.
     */
    public void logSpeed() {
        UnmodifiableWorld world = modelRoot.getWorld();
        Speed speed = world.getSpeed();

        if (speed == Speed.PAUSE) {
            modelRoot.getProperties().setProperty(ModelRootProperty.PERMANENT_MESSAGE, "Game is paused.");

            /*
             * Also hide any other message. It looks silly if it says "Game is
             * paused." and "Game speed: fast" on screen at the same time!
             */
            modelRoot.getProperties().setProperty(ModelRootProperty.QUICK_MESSAGE, "");
        } else {
            modelRoot.getProperties().setProperty(ModelRootProperty.PERMANENT_MESSAGE, null);

            String gameSpeedDesc = switch (speed) {
                case SLOW -> "Slow";
                case MODERATE -> "Moderate";
                case FAST -> "Fast";
                case PAUSE -> throw new IllegalStateException("Paused speed was handled above.");
            };
            modelRoot.getProperties().setProperty(ModelRootProperty.QUICK_MESSAGE, "Game speed: " + gameSpeedDesc);
        }
    }

    /**
     * Identifies the train and station of a group of deliveries.
     *
     * @param trainId the train that delivered the cargo
     * @param stationId the station that received the cargo
     */
    private record DeliveryStop(int trainId, int stationId) {
    }
}
