
package jfreerails.move;

/**
 * @author Luke Lindsay
 *
 * This move transfers cargo from a train to a station and vice-versa.
 */
public class TransferCargoAtStationMove extends CompositeMove {

	private TransferCargoAtStationMove(Move[] moves) {
		super(moves);		
	}
	
	public static TransferCargoAtStationMove generateMove(ChangeCargoBundleMove changeAtStation, ChangeCargoBundleMove changeOnTrain){
		return new 	TransferCargoAtStationMove(new Move[]{changeAtStation, changeOnTrain});	
	}
	
	public static TransferCargoAtStationMove generateMove(ChangeCargoBundleMove changeAtStation, ChangeCargoBundleMove changeOnTrain, AddTransactionMove payment){
			return new 	TransferCargoAtStationMove(new Move[]{changeAtStation, changeOnTrain, payment});	
	}
}
