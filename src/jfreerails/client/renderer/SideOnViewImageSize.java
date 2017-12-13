/*
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

package jfreerails.client.renderer;

import java.util.HashSet;
import java.util.Set;


public class SideOnViewImageSize {
    private static final Set set = new HashSet();
    public static final SideOnViewImageSize TINY = new SideOnViewImageSize(10);
    private final double scale;

    private SideOnViewImageSize(double s) {
        scale = s;
        set.add(this);
    }

    public int getWidth() {
        return (int)scale;
    }

    public int getHeight() {
        return (int)scale / 2;
    }
}