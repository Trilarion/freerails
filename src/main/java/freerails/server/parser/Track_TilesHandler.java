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

package freerails.server.parser;

import org.xml.sax.Attributes;

/**
 * Defines methods to handle parsing the track types XML.
 */

interface Track_TilesHandler {
    /**
     * A container element start event handling method.
     *
     * @param meta attributes
     */
    void start_CanOnlyBuildOnTheseTerrainTypes(final Attributes meta);

    /**
     * A container element end event handling method.
     */
    void end_CanOnlyBuildOnTheseTerrainTypes();

    /**
     * A container element start event handling method.
     *
     * @param meta attributes
     */
    void start_ListOfTrackPieceTemplates(final Attributes meta);

    /**
     * A container element end event handling method.
     */
    void end_ListOfTrackPieceTemplates();

    /**
     * A container element start event handling method.
     *
     * @param meta attributes
     */
    void start_CannotBuildOnTheseTerrainTypes(final Attributes meta);

    /**
     * A container element end event handling method.
     */
    void end_CannotBuildOnTheseTerrainTypes();

    /**
     * A container element start event handling method.
     *
     * @param meta attributes
     */
    void start_TrackType(final Attributes meta);

    /**
     * A container element end event handling method.
     */
    void end_TrackType();

    /**
     * An empty element event handling method.
     */
    void handle_TerrainType(final Attributes meta);

    /**
     * A container element end event handling method.
     */
    void end_Tiles();

    /**
     * A container element start event handling method.
     *
     * @param meta attributes
     */
    void start_TrackPieceTemplate(final Attributes meta);

    /**
     * A container element start event handling method.
     *
     * @param meta attributes
     */
    void start_TrackSet(final Attributes meta);

}