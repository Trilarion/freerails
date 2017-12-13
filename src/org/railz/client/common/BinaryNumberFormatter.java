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
 * BinaryNumberFormatter.java
 *
 * Created on 30 April 2003, 20:23
 */
package org.railz.client.common;


/**
 *
 * @author  Luke
 */
public class BinaryNumberFormatter {
    public static String format(int i, int bits) {
        int maxValue = 1 << (bits);

        if (i < 0) {
            throw new IllegalArgumentException(
                "i must be greater than 0.  It was " + i);
        }

        if (i >= maxValue) {
            throw new IllegalArgumentException("i must be less than " +
                maxValue + ".  It was " + i);
        }

        String s = Integer.toString(i + maxValue, 2);
        String number = s.substring(1);

        return number;
    }

    public static String formatWithLowBitOnLeft(int i, int bits) {
        StringBuffer buff = new StringBuffer(format(i, bits));
        buff.reverse();

        return buff.toString();
    }
}
