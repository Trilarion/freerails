package freerails.client.renderer;

import freerails.model.train.Engine;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class TrainCellRenderer implements ListCellRenderer<Engine> {

    private final RendererRoot rendererRoot;
    private final Map<String, JLabel> savesJLabels;

    public TrainCellRenderer(RendererRoot rendererRoot) {
        this.rendererRoot = rendererRoot;
        savesJLabels = new HashMap<>();
    }

    public Component getListCellRendererComponent(JList<? extends Engine> list, Engine engine, // value to display
                                                  int index, // cell index
                                                  boolean isSelected, // is the cell selected
                                                  boolean cellHasFocus) /* the list and the cell have the focus */ {

        String text = "<html><body>" + (isSelected ? "<strong>" : "") + engine.getName() + "<br>" + engine.getMaximumSpeed() + " m.p.h. " + engine.getMaximumThrust() + " hp $" + engine.getPrice() + (isSelected ? "</strong>" : "") + "</body></html>";

        JLabel label = savesJLabels.get(text);
        if (label == null) {
            label = new JLabel(text);
            label.setFont(new Font("Dialog", 0, 12));
            Image image = rendererRoot.getEngineImages(engine.getId()).getSideOnImage();
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
