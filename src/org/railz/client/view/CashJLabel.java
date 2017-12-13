/*
 * Copyright (C) 2003 Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/*
 * Created on 01-Jun-2003
 * 
 */
package org.railz.client.view;

import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.JLabel;

import org.railz.client.model.ModelRoot;
import org.railz.client.renderer.ViewLists;
import org.railz.world.accounts.BankAccount;
import org.railz.world.top.KEY;
import org.railz.world.top.ReadOnlyWorld;

/**
 * This JLabel shows the amount of cash available.
 * @author Luke
 * 
 */
public class CashJLabel extends JLabel {

	private ReadOnlyWorld w;
	private ModelRoot modelRoot;
	private NumberFormat numberFormat = NumberFormat.getInstance();

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
		long m = account.getCurrentBalance();
		String s = numberFormat.format(m);			
		this.setText(s);			
	    }
	    super.paint(g);
	}
}
