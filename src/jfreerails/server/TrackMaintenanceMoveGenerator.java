/*
 * Created on 11-Aug-2003
 *
 */
package jfreerails.server;

import jfreerails.controller.MoveExecuter;
import jfreerails.move.AddTransactionMove;
import jfreerails.move.Move;
import jfreerails.world.accounts.AddItemTransaction;
import jfreerails.world.accounts.BankAccount;
import jfreerails.world.accounts.Bill;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.Money;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;
import jfreerails.world.track.TrackRule;

/** This class iterates over the entries in the BankAccount
 * and counts the number of units of each track type, then 
 * calculates the cost of maintenance. 
 * 
 * @author Luke Lindsay
 *
 */
public class TrackMaintenanceMoveGenerator {
	
	public static AddTransactionMove generateMove(World w){
		int [] track  = calulateNumberOfEachTrackType(w);
		long amount = 0;
		for(int i = 0; i < track.length ; i ++){
			TrackRule trackRule = (TrackRule)w.get(KEY.TRACK_RULES, i);
			long maintenanceCost = trackRule.getMaintenanceCost().getAmount();
			amount += maintenanceCost * track[i];
		}
		Transaction t = new Bill(new Money(amount));
		return new AddTransactionMove(0, t);
	}
	
	public static int [] calulateNumberOfEachTrackType(World w){
		int [] unitsOfTrack = new int[w.size(KEY.TRACK_RULES)];
		BankAccount account = (BankAccount)w.get(KEY.BANK_ACCOUNTS, 0);
		for(int i = 0 ; i < account.size(); i++){
			Transaction t = account.getTransaction(i);
			if( t instanceof AddItemTransaction){
				AddItemTransaction addItemTransaction = (AddItemTransaction)t;
				if(AddItemTransaction.TRACK == addItemTransaction.getCategory()){
					unitsOfTrack[addItemTransaction.getType()] += addItemTransaction.getQuantity();
				} 
			}
		}
		return unitsOfTrack;
	}
	
	public static void update(World w){		
		Move m = generateMove(w);
		MoveExecuter.getMoveExecuter().processMove(m);		
	}

}
