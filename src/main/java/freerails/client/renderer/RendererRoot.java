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
     * @param i
     * @return
     */
    TrackPieceRenderer getTrackPieceView(int i);

    /**
     * @param cargoTypeId
     * @return
     */
    TrainImages getWagonImages(int cargoTypeId);

    /**
     * @param type
     * @return
     */
    TrainImages getEngineImages(int type);

    @Override
    boolean validate(UnmodifiableWorld world);

    /**
     * @param relativeFilename
     * @return
     * @throws IOException
     */
    Image getImage(String relativeFilename) throws IOException;

    /**
     * @param relativeFilename
     * @param height
     * @return
     * @throws IOException
     */
    Image getScaledImage(String relativeFilename, int height) throws IOException;
}