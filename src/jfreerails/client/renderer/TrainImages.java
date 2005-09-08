/*
 * Created on 17-May-2003
 *
 */
package jfreerails.client.renderer;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

import jfreerails.client.common.ImageManager;
import jfreerails.util.FreerailsProgressMonitor;
import jfreerails.world.cargo.CargoType;
import jfreerails.world.common.Step;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.train.EngineType;

/**
 * This class stores the overhead and side on wagon and engine images.
 * 
 * @author Luke
 * 
 */
public class TrainImages {
	private final Image[] sideOnWagonImages;

	private final Image[][] overheadWagonImages;

	private final Image[] sideOnEngineImages;

	private final Image[][] overheadEngineImages;

	private final ImageManager imageManager;

	private final ReadOnlyWorld w;

	private final Image[] explosionImages;

    public TrainImages(ReadOnlyWorld w, ImageManager imageManager,
                       FreerailsProgressMonitor pm) throws IOException {
        this.w = w;
        this.imageManager = imageManager;

        final int numberOfWagonTypes = w.size(SKEY.CARGO_TYPES);
        final int numberOfEngineTypes = w.size(SKEY.ENGINE_TYPES);

        // Setup progress monitor..
     
        pm.nextStep(numberOfWagonTypes + numberOfEngineTypes);

        int progress = 0;
        pm.setValue(progress);

        sideOnWagonImages = new Image[numberOfWagonTypes];
        overheadWagonImages = new Image[numberOfWagonTypes][8];
        sideOnEngineImages = new Image[numberOfEngineTypes];
        overheadEngineImages = new Image[numberOfEngineTypes][8];
        // @SonnyZ
        explosionImages = new Image[15];

        for (int i = 0; i < numberOfWagonTypes; i++) {
            CargoType cargoType = (CargoType) w.get(SKEY.CARGO_TYPES, i);
            String sideOnFileName = generateSideOnFilename(cargoType.getName());
            sideOnWagonImages[i] = imageManager.getImage(sideOnFileName);

            for (int direction = 0; direction < 8; direction++) {
                String overheadOnFileName = generateOverheadFilename(cargoType
                        .getName(), direction);
                overheadWagonImages[i][direction] = imageManager
                        .getImage(overheadOnFileName);
            }

            pm.setValue(++progress);
        }

        for (int i = 0; i < numberOfEngineTypes; i++) {
            EngineType engineType = (EngineType) w.get(SKEY.ENGINE_TYPES, i);
            String sideOnFileName = generateSideOnFilename(engineType
                    .getEngineTypeName());
            sideOnEngineImages[i] = imageManager.getImage(sideOnFileName);

            for (int direction = 0; direction < 8; direction++) {
                String overheadOnFileName = generateOverheadFilename(engineType
                        .getEngineTypeName(), direction);
                overheadEngineImages[i][direction] = imageManager
                        .getImage(overheadOnFileName);
            }

            pm.setValue(++progress);
        }
        // @SonnyZ
        for (int i = 1; i <= 15; i++) {
            String explosionFileName = generateExplosionFileName(i);
            explosionImages[i - 1] = imageManager.getImage(explosionFileName);
        }
    }

	public Image getSideOnWagonImage(int cargoTypeNumber) {
		return sideOnWagonImages[cargoTypeNumber];
	}

	public Image getSideOnWagonImage(int cargoTypeNumber, int height) {
		CargoType cargoType = (CargoType) w.get(SKEY.CARGO_TYPES,
				cargoTypeNumber);
		String sideOnFileName = generateSideOnFilename(cargoType.getName());

		try {
			return imageManager.getScaledImage(sideOnFileName, height);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(sideOnFileName);
		}
	}

	public Image getOverheadWagonImage(int cargoTypeNumber, int direction) {
		return overheadWagonImages[cargoTypeNumber][direction];
	}

	public Image getSideOnEngineImage(int engineTypeNumber) {
		return sideOnEngineImages[engineTypeNumber];
	}

	public Image getSideOnEngineImage(int engineTypeNumber, int height) {
		EngineType engineType = (EngineType) w.get(SKEY.ENGINE_TYPES,
				engineTypeNumber);
		String sideOnFileName = generateSideOnFilename(engineType
				.getEngineTypeName());

		try {
			return imageManager.getScaledImage(sideOnFileName, height);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException(sideOnFileName);
		}
	}

	public Image getExplosionImage(int explosionMoment) {
		return explosionImages[explosionMoment];
	}

	public Image getOverheadEngineImage(int engineTypeNumber, int direction) {
		return overheadEngineImages[engineTypeNumber][direction];
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

	public static String generateExplosionFileName(int i) {
		return "explosion" + File.separator + "explode" + i + ".png";
	}
}