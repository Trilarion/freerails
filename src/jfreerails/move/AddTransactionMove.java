/*
 * Created on 07-Jul-2003
 *
 */
package jfreerails.move;

import jfreerails.world.accounts.BankAccount;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;

/**
 * @author Luke Lindsay
 *
 */
public class AddTransactionMove implements Move {
	
	private final Transaction transaction;
	
	private final int account;
	
	public AddTransactionMove(int account, Transaction t){
		this.account = account;
		this.transaction = t;
	}
	
	public MoveStatus tryDoMove(World w) {
		if(w.boundsContain(KEY.BANK_ACCOUNTS, account)){
			return MoveStatus.MOVE_OK;
		}else{
			return MoveStatus.MOVE_FAILED;
		}		
	}

	public MoveStatus tryUndoMove(World w) {
		if(w.boundsContain(KEY.BANK_ACCOUNTS, account)){
			BankAccount bankAccount = (BankAccount)w.get(KEY.BANK_ACCOUNTS, this.account);
			int size = bankAccount.size();
			if(0==size){
				return MoveStatus.MOVE_FAILED;
			}
			Transaction lastTransaction = bankAccount.getTransaction(size - 1);
			if(lastTransaction.equals(this.transaction)){
				return MoveStatus.MOVE_OK;
			}else{
				return MoveStatus.MOVE_FAILED;
			}
		}else{
			return MoveStatus.MOVE_FAILED;
		}
	}


	public MoveStatus doMove(World w) {
		MoveStatus ms = tryDoMove(w);
		if(ms.ok){
			BankAccount bankAccount = (BankAccount)w.get(KEY.BANK_ACCOUNTS, this.account);
			bankAccount.addTransaction(this.transaction);
		}
		return ms;
	}

	public MoveStatus undoMove(World w) {
		MoveStatus ms = tryUndoMove(w);
		if(ms.ok){
			BankAccount bankAccount = (BankAccount)w.get(KEY.BANK_ACCOUNTS, this.account);
			bankAccount.removeLastTransaction();
		}
		return ms;
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof AddTransactionMove){
			AddTransactionMove test = (AddTransactionMove)obj;
			return test.account == this.account && test.transaction.equals(this.transaction);
		}else{
			return false;
		}
	}
}
