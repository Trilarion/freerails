/*
 * Created on 17-May-2003
 *
 */
package jfreerails.client.renderer;

import java.awt.Image;
import java.io.IOException;
import java.io.File;
import jfreerails.client.common.ImageManager;
import jfreerails.util.FreerailsProgressMonitor;
import jfreerails.world.cargo.CargoType;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.train.EngineType;
import jfreerails.world.common.OneTileMoveVector;


/**
 * This class stores the overhead and side on wagon and engine images.
 * @author Luke
 *
 */
public class TrainImages {
    private final Image[] sideOnWagonImages;
    private final Image[] sideOnEmptyWagonImages;
    private final Image[][] overheadWagonImages;
    private final Image[] sideOnEngineImages;
    private final Image[][] overheadEngineImages;
    private final ImageManager imageManager;
    private final ReadOnlyWorld w;

    public TrainImages(ReadOnlyWorld w, ImageManager imageManager,
        FreerailsProgressMonitor pm) throws IOException {
        this.w = w;
        this.imageManager = imageManager;

        final int numberOfWagonTypes = w.size(KEY.CARGO_TYPES);
        final int numberOfEngineTypes = w.size(KEY.ENGINE_TYPES);

        //Setup progress monitor..
        pm.setMessage("Loading train images.");
        pm.setMax(numberOfWagonTypes + numberOfEngineTypes);

        int progress = 0;
        pm.setValue(progress);

        sideOnWagonImages = new Image[numberOfWagonTypes];
	sideOnEmptyWagonImages = new Image[numberOfWagonTypes];
        overheadWagonImages = new Image[numberOfWagonTypes][8];
        sideOnEngineImages = new Image[numberOfEngineTypes];
        overheadEngineImages = new Image[numberOfEngineTypes][8];

        for (int i = 0; i < numberOfWagonTypes; i++) {
            CargoType cargoType = (CargoType)w.get(KEY.CARGO_TYPES, i);
            String sideOnFileName = generateSideOnFilename(cargoType.getName());
            sideOnWagonImages[i] = imageManager.getImage(sideOnFileName);
	    sideOnEmptyWagonImages[i] = imageManager.getImage
		(generateSideOnEmptyFileName(cargoType.getName()));

            for (int direction = 0; direction < 8; direction++) {
                String overheadOnFileName = generateOverheadFilename(cargoType.getName(),
                        direction);
                overheadWagonImages[i][direction] = imageManager.getImage(overheadOnFileName);
            }

            pm.setValue(++progress);
        }

        for (int i = 0; i < numberOfEngineTypes; i++) {
            EngineType engineType = (EngineType)w.get(KEY.ENGINE_TYPES, i);
            String sideOnFileName = generateSideOnFilename(engineType.getEngineTypeName());
            sideOnEngineImages[i] = imageManager.getImage(sideOnFileName);

            for (int direction = 0; direction < 8; direction++) {
                String overheadOnFileName = generateOverheadFilename(engineType.getEngineTypeName(),
                        direction);
                overheadEngineImages[i][direction] = imageManager.getImage(overheadOnFileName);
            }

            pm.setValue(++progress);
        }
    }

    public Image getSideOnWagonImage(int cargoTypeNumber) {
        return sideOnWagonImages[cargoTypeNumber];
    }

    public Image getSideOnWagonImage(int cargoTypeNumber, int height, int
	    percentFull) {
        CargoType cargoType = (CargoType)w.get(KEY.CARGO_TYPES, cargoTypeNumber);
        String sideOnFileName;
       if (percentFull >= 50) {
	   sideOnFileName = generateSideOnFilename(cargoType.getName());
       } else {
	   sideOnFileName = generateSideOnEmptyFileName(cargoType.getName());
       }

        try {
            return imageManager.getScaledImage(sideOnFileName, height);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(sideOnFileName);
        }
    }

    public Image getSideOnWagonImage(int cargoTypeNumber, int height) {
	return getSideOnWagonImage(cargoTypeNumber, height, 100);
    }

    public Image getOverheadWagonImage(int cargoTypeNumber, int direction) {
        return overheadWagonImages[cargoTypeNumber][direction];
    }

    public Image getSideOnEngineImage(int engineTypeNumber) {
        return sideOnEngineImages[engineTypeNumber];
    }

    public Image getSideOnEngineImage(int engineTypeNumber, int height) {
        EngineType engineType = (EngineType)w.get(KEY.ENGINE_TYPES,
                engineTypeNumber);
        String sideOnFileName = generateSideOnFilename(engineType.getEngineTypeName());

        try {
            return imageManager.getScaledImage(sideOnFileName, height);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(sideOnFileName);
        }
    }

    public Image getOverheadEngineImage(int engineTypeNumber, int direction) {
        return overheadEngineImages[engineTypeNumber][direction];
    }

    private static String generateOverheadFilename(String name, int i) {
        OneTileMoveVector[] vectors = OneTileMoveVector.getList();

        return "trains" + File.separator + "overhead" + File.separator + name +
        "_" + vectors[i].toAbrvString() + ".png";
    }

    private static String generateSideOnFilename(String name) {
        return "trains" + File.separator + "sideon" + File.separator + name +
        ".png";
    }

    private static String generateSideOnEmptyFileName(String name) {
	return "trains" + File.separator + "sideon" + File.separator + "empty"
	    + File.separator + name + ".png";
    }
}
