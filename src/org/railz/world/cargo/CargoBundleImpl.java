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
 * Created on 24-May-2003
 *
 */
package org.railz.world.cargo;

import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Iterator;


/**This CargoBundle implementation uses a <code>java.util.HashMap</code> to
 * map quantities to cargo batches.
 *
 * @author Luke
 *
 */
public class CargoBundleImpl implements CargoBundle {
    private final HashMap hashMap;

    public String toString() {
        String s = "CargoBundle {\n";
        Iterator it = this.cargoBatchIterator();

        while (it.hasNext()) {
            CargoBatch cb = (CargoBatch)((Entry) it.next()).getKey();
            s += this.getAmount(cb) + " units of cargo type " +
            cb.getCargoType() + "\n";
        }

        s += "}";

        return s;
    }

    public CargoBundleImpl() {
        hashMap = new HashMap();
    }

    private CargoBundleImpl(HashMap hm) {
        hashMap = hm;
    }

    protected HashMap getHashMap() {
        return hashMap;
    }

    public int getAmount(int cargoType) {
        Iterator it = cargoBatchIterator();
        int amount = 0;

        while (it.hasNext()) {
            CargoBatch cb = (CargoBatch)((Entry) it.next()).getKey();

            if (cb.getCargoType() == cargoType) {
                amount += getAmount(cb);
            }
        }

        return amount;
    }

    public int getAmount(CargoBatch cb) {
        if (contains(cb)) {
            Integer i = (Integer)hashMap.get(cb);

            return i.intValue();
        } else {
            return 0;
        }
    }

    public void setAmount(CargoBatch cb, int amount) {
        if (0 == amount) {
            hashMap.remove(cb);
        } else {
            hashMap.put(cb, new Integer(amount));
        }
    }

    public boolean contains(CargoBatch cb) {
        return hashMap.containsKey(cb);
    }

    public Iterator cargoBatchIterator() {
        return hashMap.entrySet().iterator();
    }

    public boolean equals(Object o) {
        if (o instanceof CargoBundleImpl) {
            CargoBundleImpl test = (CargoBundleImpl)o;

            return hashMap.equals(test.getHashMap());
        } else {
            return false;
        }
    }

    public CargoBundle getCopy() {
        return new CargoBundleImpl((HashMap)this.hashMap.clone());
    }

    public void addCargo(CargoBatch cb, int amount) {
        int amountAlready = this.getAmount(cb);
        this.setAmount(cb, amount + amountAlready);
    }
}
