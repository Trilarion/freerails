/*
 * Created on 17-May-2003
 * 
 */
package jfreerails.client.renderer;

import java.awt.Image;
import java.io.IOException;

import jfreerails.client.common.ImageManager;
import jfreerails.world.cargo.CargoType;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;
import jfreerails.world.train.EngineType;

/**
 * This class stores the overhead and side on wagon and engine images.
 * @author Luke
 * 
 */
public class TrainImages {
	
	public static final int HEIGHT_100_PIXELS=0;
	public static final int HEIGHT_50_PIXELS=0;
	public static final int HEIGHT_25_PIXELS=0;

	private final Image[] sideOnWagonImages;
	private final Image[][] overheadWagonImages;
	private final Image[] sideOnEngineImages;
	private final Image[][] overheadEngineImages;

	public TrainImages(World w, ImageManager imageManager) throws IOException {
		final int numberOfWagonTypes = w.size(KEY.CARGO_TYPES);
		final int numberOfEngineTypes = w.size(KEY.ENGINE_TYPES);
		sideOnWagonImages = new Image[numberOfWagonTypes];
		overheadWagonImages = new Image[numberOfWagonTypes][8];
		sideOnEngineImages = new Image[numberOfEngineTypes];
		overheadEngineImages = new Image[numberOfEngineTypes][8];

		for (int i = 0; i < numberOfWagonTypes; i++) {
			CargoType cargoType = (CargoType) w.get(KEY.CARGO_TYPES, i);
			String sideOnFileName = WagonRenderer.generateSideOnFilename(cargoType.getName());
			sideOnWagonImages[i] = imageManager.getImage(sideOnFileName);
			for (int direction = 0; direction < 8; direction++) {
				String overheadOnFileName =
					WagonRenderer.generateOverheadFilename(cargoType.getName(), direction);
				overheadWagonImages[i][direction] = imageManager.getImage(overheadOnFileName);
			}
		}

		for (int i = 0; i < numberOfEngineTypes; i++) {
			EngineType engineType = (EngineType) w.get(KEY.ENGINE_TYPES, i);
			String sideOnFileName = WagonRenderer.generateSideOnFilename(engineType.getEngineTypeName());
			sideOnEngineImages[i] = imageManager.getImage(sideOnFileName);
			for (int direction = 0; direction < 8; direction++) {
				String overheadOnFileName =
					WagonRenderer.generateOverheadFilename(engineType.getEngineTypeName(), direction);
				overheadEngineImages[i][direction] = imageManager.getImage(overheadOnFileName);
			}
		}
	}

	public Image getSideOnWagonImage(int cargoTypeNumber) {
		return sideOnWagonImages[cargoTypeNumber];
	}

	public Image getOverheadWagonImage(int cargoTypeNumber, int direction) {
		return overheadWagonImages[cargoTypeNumber][direction];
	}
	
	public Image getSideOnEngineImage(int engineTypeNumber) {
		return sideOnEngineImages[engineTypeNumber];
	}

	public Image getOverheadEngineImage(int engineTypeNumber, int direction) {
		return overheadEngineImages[engineTypeNumber][direction];
	}
}
