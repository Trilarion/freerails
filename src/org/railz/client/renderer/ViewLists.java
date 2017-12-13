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

package org.railz.client.renderer;

import javax.swing.ImageIcon;

import org.railz.world.top.*;


public interface ViewLists {
    /** 48x48 pixels */
    public static final int LARGE_ICON = 0;

    TileRendererList getTileViewList();

    TileRendererList getBuildingViewList();

    TrackPieceRendererList getTrackPieceViewList();

    TrainImages getTrainImages();

    boolean validate(ReadOnlyWorld world);

    /**
     * The use of this is to be discouraged in favour of
     * getImageIcon(ObjectKey, int) for those icons relating to gameworld
     * objects. Future use of this method is reserved for use with
     * client-specific toolbar icons etc.
     * @return the ImageIcon corresponding to the specified name
     */
    ImageIcon getImageIcon(String iconName);

    /**
     * @param key an ObjectKey describing the object associated with the icon
     * @param type describes the type of icon to retrieve - LARGE_ICON etc.
     */
   ImageIcon getImageIcon(ObjectKey key, int type); 
}
