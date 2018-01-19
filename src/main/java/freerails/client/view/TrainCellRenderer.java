package freerails.client.view;

import freerails.client.renderer.RendererRoot;
import freerails.world.train.EngineType;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

final class TrainCellRenderer implements ListCellRenderer {

    private final RendererRoot rendererRoot;
    private final Map<String, JLabel> savesJLabels;

    public TrainCellRenderer(RendererRoot rendererRoot) {
        this.rendererRoot = rendererRoot;
        savesJLabels = new HashMap<>();
    }

    public Component getListCellRendererComponent(JList list, Object value,
            // value to display
                                                  int index, // cell index
                                                  boolean isSelected, // is the cell selected
                                                  boolean cellHasFocus) /* the list and the cell have the focus */ {

        EngineType engine = (EngineType) value;
        String text = "<html><body>" + (isSelected ? "<strong>" : "") + engine.getEngineTypeName() + "<br>" + engine.getMaxSpeed() + " m.p.h. " + engine.getPowerAtDrawbar() + " hp $" + engine.getPrice().toString() + (isSelected ? "</strong>" : "") + "</body></html>";

        JLabel label = savesJLabels.get(text);
        if (label == null) {
            label = new JLabel(text);
            label.setFont(new Font("Dialog", 0, 12));
            Image image = rendererRoot.getEngineImages(index).getSideOnImage();
            int height = image.getHeight(null);
            int width = image.getWidth(null);
            int scale = height / 50;
            Icon icon = new ImageIcon(image.getScaledInstance(width / scale, height / scale, Image.SCALE_FAST));
            label.setIcon(icon);
            savesJLabels.put(text, label);
        }
        return label;
    }
}
