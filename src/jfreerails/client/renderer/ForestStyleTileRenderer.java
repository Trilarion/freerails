/*
 * Copyright (C) Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/*
* ForestStyleTileView.java
*
* Created on 07 July 2001, 14:36
*/
package jfreerails.client.renderer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import jfreerails.client.common.BinaryNumberFormatter;
import jfreerails.client.common.ImageManager;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.ReadOnlyWorld;


/**
*
* @author  Luke Lindsay
*/
final public class ForestStyleTileRenderer
    extends jfreerails.client.renderer.AbstractTileRenderer {
    private static final int[] X_LOOK_AT = {-1, 1};
    private static final int[] Y_LOOK_AT = {0, 0};

    public ForestStyleTileRenderer(ImageManager imageManager, int[] rgbValues,
        TerrainType tileModel) throws IOException {
        super(tileModel, rgbValues);
        this.setTileIcons(new BufferedImage[4]);

        for (int i = 0; i < this.getTileIcons().length; i++) {
            String fileName = generateRelativeFileName(i);
            this.getTileIcons()[i] = imageManager.getImage(fileName);
        }
    }

    public int selectTileIcon(int x, int y, ReadOnlyWorld w) {
        int iconNumber = 0;

        for (int i = 0; i < 2; i++) {
            iconNumber = iconNumber |
                checkTile(x + X_LOOK_AT[i], y + Y_LOOK_AT[i], w);
            iconNumber = iconNumber << 1;
        }

        iconNumber = iconNumber >> 1;

        return iconNumber;
    }

    public void dumpImages(ImageManager imageManager) {
        for (int i = 0; i < this.getTileIcons().length; i++) {
            String fileName = generateRelativeFileName(i);
            imageManager.setImage(fileName, this.getTileIcons()[i]);
        }
    }

    protected String generateFileNameNumber(int i) {
        return BinaryNumberFormatter.format(i, 2);
    }
}
