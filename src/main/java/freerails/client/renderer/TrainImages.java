/*
 * Created on 19-Apr-2006
 *
 */
package freerails.client.renderer;

import freerails.client.common.ImageManager;
import freerails.world.common.Step;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Stores side-on and over-head images of a particular wagon or engine type.
 *
 * @author Luke
 */
public class TrainImages {

    public final String sideOnFileName;
    private final Image sideOnImage;
    private final Image[] overheadImages = new Image[8];

    public TrainImages(ImageManager imageManager, String name)
            throws IOException {
        sideOnFileName = TrainImages.generateSideOnFilename(name);
        sideOnImage = imageManager.getImage(sideOnFileName);

        for (int direction = 0; direction < 8; direction++) {
            String overheadOnFileName = TrainImages.generateOverheadFilename(
                    name, direction);
            overheadImages[direction] = imageManager
                    .getImage(overheadOnFileName);
        }

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

    public Image getSideOnImage() {
        return sideOnImage;
    }

    public Image getOverheadImage(int direction) {
        return overheadImages[direction];
    }

}
