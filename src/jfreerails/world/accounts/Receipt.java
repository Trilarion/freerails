/*
 * Created on 21-Jun-2003
 *
 */
package jfreerails.world.accounts;

import jfreerails.world.common.Money;

/**
 * @author Luke Lindsay
 *
 */
public class Receipt implements Transaction {

	private final Money amount;
	
	public Receipt(Money m){
		this.amount = m;
	}

	public Money getValue() {		
		return amount;
	}
	
	public boolean equals(Object o){
		if(o instanceof Receipt){			
			Receipt test = (Receipt)o;			
			return test.amount.equals(this.amount);
		}else{
			return false;
		}
	}
}
