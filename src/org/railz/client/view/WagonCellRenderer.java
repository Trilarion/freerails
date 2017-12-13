/*
 * Copyright (C) 2004 Robert Tuck
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
package org.railz.client.view;

import java.awt.*;
import javax.swing.*;

import org.railz.client.renderer.*;
import org.railz.world.cargo.*;
import org.railz.world.top.*;
import org.railz.world.train.*;
class WagonCellRenderer implements ListCellRenderer {
    private Component[] labels;

    TrainImages trainImages;

    public WagonCellRenderer(World2ListModelAdapter w2lma, TrainImages s) {
	trainImages = s;

	labels = new Component[w2lma.getSize()];
	for (int i = 0; i < w2lma.getSize(); i++) {
	    JLabel label = new JLabel();
	    label.setFont(new java.awt.Font("Dialog", 0, 12));
	    Image image = trainImages.getSideOnWagonImage(i);
	    int height = image.getHeight(null);
	    int width = image.getWidth(null);
	    int scale = height/10;

	    ImageIcon icon = new
		ImageIcon(image.getScaledInstance(width/scale,
			    height/scale, Image.SCALE_FAST));			
	    label.setIcon(icon);
	    labels[i] = label;
	}
    }

    public Component getListCellRendererComponent(JList list, Object value, /* value to display*/
	    int index, /* cell index*/
	    boolean isSelected, /* is the cell selected*/
	    boolean cellHasFocus) /* the list and the cell have the focus*/ {
	if (index >= 0 && index < labels.length) {
	    CargoType cargoType = (CargoType) value;
	    String text = "<html><body>" + (isSelected ? "<strong>" : "") + cargoType.getDisplayName() + (isSelected ? "</strong>" : "&nbsp;&nbsp;&nbsp;&nbsp;"/*padding to stop word wrap due to greater wodth of strong font*/) + "</body></html>";
	    ((JLabel) labels[index]).setText(text);
	    return labels[index];
	}
	return null;
    }
}

