package jfreerails.move;

import java.util.ArrayList;

import jfreerails.world.accounts.DeliverCargoReceipt;
import jfreerails.world.cargo.CargoBatch;
import jfreerails.world.common.ImList;
import jfreerails.world.common.Money;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;

/**
 * This {@link CompositeMove} transfers cargo from a train to a station and
 * vice-versa.
 * 
 * @author Luke Lindsay
 * 
 * 
 */
public class TransferCargoAtStationMove extends CompositeMove {
    private static final long serialVersionUID = 3257291318215456563L;

    public static final int CHANGE_ON_TRAIN_INDEX = 1;

    public static final int CHANGE_AT_STATION_INDEX = 0;

    private final boolean waitingForFullLoad;

    private TransferCargoAtStationMove(Move[] moves, boolean waiting) {
        super(moves);
        waitingForFullLoad = waiting;
    }

    public static TransferCargoAtStationMove generateMove(
            ChangeCargoBundleMove changeAtStation,
            ChangeCargoBundleMove changeOnTrain, CompositeMove payment,
            boolean waiting) {
        return new TransferCargoAtStationMove(new Move[] { changeAtStation,
                changeOnTrain, payment }, waiting);
    }

    public ChangeCargoBundleMove getChangeAtStation() {
        return (ChangeCargoBundleMove) super.getMoves().get(
                CHANGE_AT_STATION_INDEX);
    }

    public ChangeCargoBundleMove getChangeOnTrain() {
        return (ChangeCargoBundleMove) super.getMoves().get(
                CHANGE_ON_TRAIN_INDEX);
    }

    public Money getRevenue() {
        ImList<Move> moves = super.getMoves();
        long amount = CHANGE_AT_STATION_INDEX;

        for (int i = CHANGE_AT_STATION_INDEX; i < moves.size(); i++) {
            if (moves.get(i) instanceof AddTransactionMove) {
                AddTransactionMove move = (AddTransactionMove) moves.get(i);
                DeliverCargoReceipt receipt = (DeliverCargoReceipt) move
                        .getTransaction();
                amount += receipt.deltaCash().getAmount();
            }
        }

        return new Money(amount);
    }

    public int getQuantityOfCargo(int cargoType) {
        ImList<Move> moves = super.getMoves();
        int quantity = CHANGE_AT_STATION_INDEX;

        for (int i = CHANGE_AT_STATION_INDEX; i < moves.size(); i++) {
            if (moves.get(i) instanceof AddTransactionMove) {
                AddTransactionMove move = (AddTransactionMove) moves.get(i);
                DeliverCargoReceipt receipt = (DeliverCargoReceipt) move
                        .getTransaction();
                CargoBatch cb = receipt.getCb();

                if (cb.getCargoType() == cargoType) {
                    quantity += receipt.getQuantity();
                }
            }
        }

        return quantity;
    }

    /** The player who is getting paid for the delivery. */
    public FreerailsPrincipal getPrincipal() {
        ImList<Move> moves = super.getMoves();

        for (int i = CHANGE_AT_STATION_INDEX; i < moves.size(); i++) {
            if (moves.get(i) instanceof AddTransactionMove) {
                AddTransactionMove move = (AddTransactionMove) moves.get(i);

                return move.getPrincipal();
            }
        }

        return Player.NOBODY;
    }

    public TransferCargoAtStationMove(ArrayList<Move> movesArrayList,
            boolean waiting) {
        super(movesArrayList);
        this.waitingForFullLoad = waiting;
    }

    public boolean isWaitingForFullLoad() {
        return waitingForFullLoad;
    }
}