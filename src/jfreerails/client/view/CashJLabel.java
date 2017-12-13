/*
 * Created on 01-Jun-2003
 * 
 */
package jfreerails.client.view;

import java.awt.Graphics;
import java.awt.event.ActionListener;

import javax.swing.JLabel;

import jfreerails.client.model.ModelRoot;
import jfreerails.client.renderer.ViewLists;
import jfreerails.world.accounts.BankAccount;
import jfreerails.world.common.Money;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;

/**
 * This JLabel shows the amount of cash available.
 * @author Luke
 * 
 */
public class CashJLabel extends JLabel {

	private ReadOnlyWorld w;
	private ModelRoot modelRoot;

	public CashJLabel(){
		this.setText("         ");
	}

	public void setup(ModelRoot mr) {
		this.w = mr.getWorld();
		modelRoot = mr;
	}
	
	public void paint(Graphics g) {
	    if(null != w && w.size(KEY.BANK_ACCOUNTS,
			modelRoot.getPlayerPrincipal()) > 0){
		BankAccount account = (BankAccount)w.get
		    (KEY.BANK_ACCOUNTS, 0, modelRoot.getPlayerPrincipal());
		Money m = account.getCurrentBalance();
		String s = m.toString();			
		this.setText(s);			
	    }
	    super.paint(g);
	}

}
