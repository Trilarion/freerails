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
 *
 * @author  lindsal8
 * 
 */
public class SideOnTrainTrainViewImages {
    
    private java.awt.GraphicsConfiguration defaultConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    
    public static final int HEIGHT_100_PIXELS=0;
    public static final int HEIGHT_50_PIXELS=1;
    public static final int HEIGHT_25_PIXELS=2;
    public static final int HEIGHT_10_PIXELS=3;
    
    Image[][] engines;
    Image[][] wagons;
    
    /** Creates new SideOnTrainTrainView */
    public SideOnTrainTrainViewImages(int numberOfWagonTypes, int numberOfEngineTypes) {
        engines = new Image[numberOfEngineTypes][4];
        wagons = new Image[numberOfWagonTypes][4];
    }
    
    public void setEngineImage(int typeNumber, Image i){
        engines[typeNumber][HEIGHT_100_PIXELS]=getScaledImage(i, 100);
        engines[typeNumber][HEIGHT_50_PIXELS]=getScaledImage(i, 50);
        engines[typeNumber][HEIGHT_25_PIXELS]=getScaledImage(i, 25);
        engines[typeNumber][HEIGHT_10_PIXELS]=getScaledImage(i, 10);
    }
    
     public void setWagonImage(int typeNumber, Image i){
        wagons[typeNumber][HEIGHT_100_PIXELS]=getScaledImage(i, 100);
        wagons[typeNumber][HEIGHT_50_PIXELS]=getScaledImage(i, 50);
        wagons[typeNumber][HEIGHT_25_PIXELS]=getScaledImage(i, 25);
        wagons[typeNumber][HEIGHT_10_PIXELS]=getScaledImage(i, 10);
    }
    
    private Image getScaledImage(Image i, int heightToScaleTo){        
        int width = i.getWidth(null)*heightToScaleTo/i.getHeight(null);        
        Image result = defaultConfiguration.createCompatibleImage(width, heightToScaleTo, Transparency.BITMASK);
        Graphics g = result.getGraphics();
        g.drawImage(i, 0,0,width, heightToScaleTo, null);
        return result;
    }
    
    public Image getEngineImage(int type, int height){
        return engines[type][height];
    }
    
    public Image getWagonImage(int type, int height){
        return wagons[type][height];
    }    
}
