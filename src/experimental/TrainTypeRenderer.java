package experimental;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;


/**
 *
 * @author Luke
 */
public class TrainTypeRenderer {
    private static int numberOfTypes = 0;
    private static final Set set = new HashSet();
    private final Color color;

    public TrainTypeRenderer(Color c) {
        numberOfTypes++;
        color = c;
        set.add(this);
    }

    public Color getColor() {
        return color;
    }
}