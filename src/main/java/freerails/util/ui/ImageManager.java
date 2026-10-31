/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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
package freerails.util.ui;

import java.awt.*;
import java.io.IOException;

/**
 * This interface defines methods for loading and producing
 * scaled images whose quality may be controlled.
 */
public interface ImageManager {

    /**
     * Creates a blank translucent image with the requested dimensions.
     *
     * @param height image height in pixels
     * @param width image width in pixels
     * @return blank image
     */
    Image newBlankImage(int height, int width);

    /**
     * Loads an image from the configured resource path.
     *
     * @param relativeFilename relative resource filename
     * @return loaded image
     * @throws IOException if the resource cannot be found or read
     */
    Image getImage(String relativeFilename) throws IOException;

    /**
     * Replaces the cached image for a relative resource filename.
     *
     * @param relativeFilename relative resource filename
     * @param i image to associate with the filename
     */
    void setImage(String relativeFilename, Image i);

    /**
     * Loads an image scaled to the requested height while preserving its aspect ratio.
     *
     * @param relativeFilename relative resource filename
     * @param height target image height in pixels
     * @return scaled image
     * @throws IOException if the source resource cannot be found or read
     */
    Image getScaledImage(String relativeFilename, int height) throws IOException;
}