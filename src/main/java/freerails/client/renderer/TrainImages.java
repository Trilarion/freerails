/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 *
 */
package freerails.client.renderer;

import freerails.util.ui.ImageManager;
import freerails.model.terrain.TileTransition;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Stores side-on and over-head images of a particular wagon or engine type.
 */
public class TrainImages {

    public final String sideOnFileName;
    private final Image sideOnImage;
    private final Image[] overheadImages = new Image[8];

    /**
     * @param imageManager
     * @param name
     * @throws IOException
     */
    public TrainImages(ImageManager imageManager, String name) throws IOException {
        sideOnFileName = "trains" + File.separator + "sideon" + File.separator + name + ".png";
        sideOnImage = imageManager.getImage(sideOnFileName);

        TileTransition[] vectors = TileTransition.getTransitions();
        for (int direction = 0; direction < 8; direction++) {
            String overheadOnFileName = "trains" + File.separator + "overhead" + File.separator + name + '_' + vectors[direction].toAbrvString() + ".png";
            overheadImages[direction] = imageManager.getImage(overheadOnFileName);
        }
    }

    /**
     * @return
     */
    public Image getSideOnImage() {
        return sideOnImage;
    }

    /**
     * @param direction
     * @return
     */
    public Image getOverheadImage(int direction) {
        return overheadImages[direction];
    }

}
