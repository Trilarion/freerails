/*
 * Copyright (C) Robert Tuck
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

/**
 * Represents an initial deposit on opening an account.
 *
 * @author rtuck99@users.berlios.de
 */
package jfreerails.world.accounts;

import jfreerails.world.common.GameTime;

public class InitialDeposit extends Transaction {
    public InitialDeposit(GameTime t, long value) {
	super(t, value);
    }

    public final int getCategory() {
	return CATEGORY_OUTSIDE_INVESTMENT;
    }

    public final int getSubcategory() {
	return SUBCATEGORY_NO_SUBCATEGORY;
    }
}
