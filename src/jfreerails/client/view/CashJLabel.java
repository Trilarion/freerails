/*
 * Created on 01-Jun-2003
 * 
 */
package jfreerails.client.view;

import java.awt.Graphics;
import java.awt.event.ActionListener;

import javax.swing.JLabel;

import jfreerails.world.accounts.BankAccount;
import jfreerails.world.common.Money;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;

/**
 * This JLabel shows the amount of cash available.
 * @author Luke
 * 
 */
public class CashJLabel extends JLabel implements View {

	private ReadOnlyWorld w;
	private FreerailsPrincipal principal;

	public CashJLabel(){
		this.setText("         ");
	}

	public void setup(ModelRoot model, ActionListener submitButtonCallBack) {
		this.w = model.getWorld();
		principal = model.getPlayerPrincipal();
	}
	

	
	public void paint(Graphics g) {
		if(null != w){
			BankAccount account = (BankAccount)w.get(KEY.BANK_ACCOUNTS, 0, principal);
			Money m = account.getCurrentBalance();
			String s = m.toString();			
			this.setText(s);			
		}
		super.paint(g);
	}

}
