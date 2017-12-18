package freerails.move;

import freerails.world.accounts.DeliverCargoReceipt;
import freerails.world.cargo.CargoBatch;
import freerails.world.common.ImList;
import freerails.world.common.Money;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.player.Player;

import java.util.ArrayList;

/**
 * This {@link CompositeMove} transfers cargo from a train to a station and
 * vice-versa.
 *
 * @author Luke Lindsay
 */
public class TransferCargoAtStationMove extends CompositeMove {
    public static final int CHANGE_ON_TRAIN_INDEX = 1;
    public static final int CHANGE_AT_STATION_INDEX = 0;
    private static final long serialVersionUID = 3257291318215456563L;
    private final boolean waitingForFullLoad;

    private TransferCargoAtStationMove(Move[] moves, boolean waiting) {
        super(moves);
        waitingForFullLoad = waiting;
    }

    public TransferCargoAtStationMove(ArrayList<Move> movesArrayList,
                                      boolean waiting) {
        super(movesArrayList);
        this.waitingForFullLoad = waiting;
    }

    public static TransferCargoAtStationMove generateMove(
            ChangeCargoBundleMove changeAtStation,
            ChangeCargoBundleMove changeOnTrain, CompositeMove payment,
            boolean waiting) {
        return new TransferCargoAtStationMove(new Move[]{changeAtStation,
                changeOnTrain, payment}, waiting);
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

    /**
     * The player who is getting paid for the delivery.
     */
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

    public boolean isWaitingForFullLoad() {
        return waitingForFullLoad;
    }
}