/*
 * SideOnTrainTrainView.java
 *
 * Created on 25 December 2002, 18:42
 */
package jfreerails.client.renderer;

import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Transparency;


/**
 * Stores side on train images.
 * @author  Luke
 *
 */
public class SideOnTrainTrainViewImages {
    private final java.awt.GraphicsConfiguration defaultConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                                                                           .getDefaultScreenDevice()
                                                                                           .getDefaultConfiguration();
    private static final int HEIGHT_100_PIXELS = 0;
    private static final int HEIGHT_50_PIXELS = 1;
    private static final int HEIGHT_25_PIXELS = 2;
    private static final int HEIGHT_10_PIXELS = 3;
    private final Image[][] engines;
    private final Image[][] wagons;

    public SideOnTrainTrainViewImages(int numberOfWagonTypes,
        int numberOfEngineTypes) {
        engines = new Image[numberOfEngineTypes][4];
        wagons = new Image[numberOfWagonTypes][4];
    }

    public void setEngineImage(int typeNumber, Image i) {
        engines[typeNumber][HEIGHT_100_PIXELS] = getScaledImage(i, 100);
        engines[typeNumber][HEIGHT_50_PIXELS] = getScaledImage(i, 50);
        engines[typeNumber][HEIGHT_25_PIXELS] = getScaledImage(i, 25);
        engines[typeNumber][HEIGHT_10_PIXELS] = getScaledImage(i, 10);
    }

    public void setWagonImage(int typeNumber, Image i) {
        wagons[typeNumber][HEIGHT_100_PIXELS] = getScaledImage(i, 100);
        wagons[typeNumber][HEIGHT_50_PIXELS] = getScaledImage(i, 50);
        wagons[typeNumber][HEIGHT_25_PIXELS] = getScaledImage(i, 25);
        wagons[typeNumber][HEIGHT_10_PIXELS] = getScaledImage(i, 10);
    }

    private Image getScaledImage(Image i, int heightToScaleTo) {
        int width = i.getWidth(null) * heightToScaleTo / i.getHeight(null);
        Image result = defaultConfiguration.createCompatibleImage(width,
                heightToScaleTo, Transparency.BITMASK);
        Graphics g = result.getGraphics();
        g.drawImage(i, 0, 0, width, heightToScaleTo, null);

        return result;
    }
}