package jfreerails.move;


/**
 * This {@link CompositeMove} transfers cargo from a train to a station and vice-versa.
 *
 * @author Luke Lindsay
 *
 *
 */
public class TransferCargoAtStationMove extends CompositeMove {
    private TransferCargoAtStationMove(Move[] moves) {
        super(moves);
    }

    public static TransferCargoAtStationMove generateMove(
        ChangeCargoBundleMove changeAtStation,
        ChangeCargoBundleMove changeOnTrain) {
        return new TransferCargoAtStationMove(new Move[] {
                changeAtStation, changeOnTrain
            });
    }

    public static TransferCargoAtStationMove generateMove(
        ChangeCargoBundleMove changeAtStation,
        ChangeCargoBundleMove changeOnTrain, AddTransactionMove payment) {
        return new TransferCargoAtStationMove(new Move[] {
                changeAtStation, changeOnTrain, payment
            });
    }

    public ChangeCargoBundleMove getChangeAtStation() {
        return (ChangeCargoBundleMove)super.getMoves()[0];
    }

    public ChangeCargoBundleMove getChangeOnTrain() {
        return (ChangeCargoBundleMove)super.getMoves()[1];
    }

    public AddTransactionMove getPayment() {
        if (super.getMoves().length < 3) {
            return null;
        } else {
            return (AddTransactionMove)super.getMoves()[2];
        }
    }
}