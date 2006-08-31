/*
 * Created on 19-Apr-2006
 * 
 */
package jfreerails.client.renderer;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

import jfreerails.client.common.ImageManager;
import jfreerails.world.common.Step;

/** Stores side-on and over-head images of a particular wagon or engine type.
 * @author Luke
 * 
 * */
public class TrainImages {
	
	private final Image sideOnImage;

	private final Image[] overheadImages = new Image[8];	

	public final String sideOnFileName;
	
	public Image getSideOnImage() {
		return sideOnImage;
	}		
	
	public Image getOverheadImage(int direction){
		return overheadImages[direction];
	}

	public static String generateOverheadFilename(String name, int i) {
		Step[] vectors = Step.getList();
	
		return "trains" + File.separator + "overhead" + File.separator + name
				+ "_" + vectors[i].toAbrvString() + ".png";
	}

	public static String generateSideOnFilename(String name) {
		return "trains" + File.separator + "sideon" + File.separator + name
				+ ".png";
	}

	public TrainImages(ImageManager imageManager, String name)throws IOException  {					
		sideOnFileName = TrainImages.generateSideOnFilename(name);
		sideOnImage = imageManager.getImage(sideOnFileName);

        for (int direction = 0; direction < 8; direction++) {
            String overheadOnFileName = TrainImages.generateOverheadFilename(name, direction);
            overheadImages[direction] = imageManager
                    .getImage(overheadOnFileName);
        }

	}

}
