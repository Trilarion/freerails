/*
 * Created on 21-Jun-2003
 *
 */
package jfreerails.world.accounts;

import jfreerails.world.common.Money;
import jfreerails.world.top.World;

/**
 * @author Luke Lindsay
 *
 */
public class Bill implements Transaction {
	
	private final Money amount;
	
	private final String desc;
	
	public Bill(Money amount, String description){
		this.amount = amount;
		this.desc = description;
	}
	
	public Money getValue() {		
		return amount;
	}
	
	public String getDescription(World w) {		
		return desc;
	}
}
