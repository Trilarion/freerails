/*
 * Created on 30-Jul-2003
 *
 */
package jfreerails.server;

import java.util.Iterator;

import jfreerails.move.AddTransactionMove;
import jfreerails.world.accounts.Receipt;
import jfreerails.world.cargo.CargoBatch;
import jfreerails.world.cargo.CargoBundle;
import jfreerails.world.common.Money;
import jfreerails.world.top.World;

/**
 * @author Luke Lindsay
 *
 */
public class ProcessCargoAtStationMoveGenerator {
	
	/** Generates a Move that pays the player for delivering the cargo.
	 */
	public static AddTransactionMove processCargo(World w, CargoBundle cargoBundle, int stationID){
		
		/* For now this is very simplistic: it gives 
		 * the player $1000 for each unit of cargo.
		 */
		
		Iterator batches = cargoBundle.cargoBatchIterator();
		int amountOfCargo = 0;
		while(batches.hasNext()){
			CargoBatch batch = (CargoBatch)batches.next();
			amountOfCargo += cargoBundle.getAmount(batch);
		}		
		Money amount = new Money(amountOfCargo * 1000); 
		Receipt receipt = new Receipt(amount);
		return new AddTransactionMove(0, receipt);
	}

}
