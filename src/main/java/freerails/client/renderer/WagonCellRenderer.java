/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.client.renderer;

import freerails.model.cargo.Cargo;

import javax.swing.*;
import java.awt.*;

public class WagonCellRenderer implements ListCellRenderer<Cargo> {

    private final Component[] labels;

    public WagonCellRenderer(ListModel listModel, RendererRoot rendererRoot) {

        labels = new Component[listModel.getSize()];
        for (int i = 0; i < listModel.getSize(); i++) {
            JLabel label = new JLabel();
            label.setFont(new Font("Dialog", 0, 12));
            Image image = rendererRoot.getWagonImages(i).getSideOnImage();
            int height = image.getHeight(null);
            int width = image.getWidth(null);
            int scale = height / 10;

            Icon icon = new ImageIcon(image.getScaledInstance(width / scale, height / scale, Image.SCALE_FAST));
            label.setIcon(icon);
            labels[i] = label;
        }
    }

    /**
     *
     * @param list
     * @param value value to display
     * @param index cell index
     * @param isSelected is the cell selected
     * @param cellHasFocus the list and the cell have the focus
     * @return
     */
    public Component getListCellRendererComponent(JList<? extends Cargo> list, Cargo value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus)  {
        if (index >= 0 && index < labels.length) {
            String text = "<html><body>" + (isSelected ? "<strong>" : "") + value.getName() + (isSelected ? "</strong>" : "&nbsp;&nbsp;&nbsp;&nbsp;"/*
             * padding to stop
             * word wrap due to
             * greater width of
             * strong font
             */) + "</body></html>";
            ((JLabel) labels[index]).setText(text);
            return labels[index];
        }
        return null;
    }
}
