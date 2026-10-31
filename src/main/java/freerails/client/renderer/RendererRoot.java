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

package freerails.client.renderer;

import freerails.client.renderer.tile.TileRendererList;
import freerails.client.renderer.track.TrackPieceRenderer;
import freerails.model.world.UnmodifiableWorld;

import java.awt.*;
import java.io.IOException;

// TODO should this really extend TileRendererList?
/**
 * Provides access to the objects that render terrain, track, and trains.
 */
public interface RendererRoot extends TileRendererList {

    /**
     * Returns the track piece view.
     *
     * @param i the index of the item
     * @return the track piece view produced by this method
     */
    TrackPieceRenderer getTrackPieceView(int i);

    /**
     * Returns the wagon images.
     *
     * @param cargoTypeId the cargo type id parameter
     * @return the wagon images produced by this method
     */
    TrainImages getWagonImages(int cargoTypeId);

    /**
     * Returns the engine images.
     *
     * @param type the type parameter
     * @return the engine images produced by this method
     */
    TrainImages getEngineImages(int type);

    /**
     * Returns whether validate.
     * @param world the world
     * @return whether validate
     */
    @Override
    boolean validate(UnmodifiableWorld world);

    /**
     * Returns the image.
     *
     * @param relativeFilename the relative filename parameter
     * @return the image produced by this method
     * @throws IOException if the requested I/O operation cannot be completed.
     */
    Image getImage(String relativeFilename) throws IOException;

    /**
     * Returns the scaled image.
     *
     * @param relativeFilename the relative filename parameter
     * @param height the height parameter
     * @return the scaled image produced by this method
     * @throws IOException if the requested I/O operation cannot be completed.
     */
    Image getScaledImage(String relativeFilename, int height) throws IOException;
}